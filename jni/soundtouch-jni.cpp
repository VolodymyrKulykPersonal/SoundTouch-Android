#include <jni.h>
#include <android/log.h>
#include <queue>
#include <string>
#include <stdlib.h>
#include <stdio.h>
#include <vector>

//#include <stdio.h>
//#include <dlfcn.h>

#include "soundtouch/include/SoundTouch.h"
//#include "TimeShiftEffect.h"

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)
//#define //LOGV(...)

#define DLL_PUBLIC __attribute__ ((visibility ("default")))

using namespace soundtouch;
using namespace std;

struct SoundTouchExt
{
	SoundTouch* sTouch;
	queue<signed char>* fBufferOut;
	int channels;
	int sampleRate;
	float tempoChange;
	int pitchSemi;
	int bytesPerSample;

	SoundTouchExt()
	{
		sTouch = new SoundTouch();
		fBufferOut = new queue<signed char>();
	}

	SoundTouchExt(const SoundTouchExt& other)
	{
		sTouch = new SoundTouch();
		fBufferOut = new queue<signed char>();
		this->channels = other.channels;
		this->sampleRate = other.sampleRate;
		this->tempoChange = other.tempoChange;
		this->pitchSemi = other.pitchSemi;
		this->bytesPerSample = other.bytesPerSample;
	}
};

const int MAX_TRACKS = 16;

vector<SoundTouchExt> sProcessors(MAX_TRACKS);

static void* getConvBuffer(int);
static int write(const float*, queue<signed char>*, int, int);
static void setup(SoundTouchExt&, int, int, int, float, float);
static void convertInput16(jbyte*, float*, int);
static inline int saturate(float, float, float);
static void* getConvBuffer(int);
static int process(SoundTouchExt&, SAMPLETYPE*, queue<signed char>*, int, bool);
static int putQueueInChar(jbyte*, queue<signed char>*, int);


#ifdef __cplusplus

extern "C" DLL_PUBLIC void Java_com_smp_soundtouchandroid_SoundTouch_clearBytes(JNIEnv *env,
	jobject thiz, jint track)
{
	SoundTouchExt& soundTouch = sProcessors.at(track);

	const int BUFF_SIZE = 8192;

	queue<signed char>* fBufferOut = sProcessors.at(track).fBufferOut;

	SAMPLETYPE* fBufferIn = new SAMPLETYPE[BUFF_SIZE];
	soundTouch.sTouch->clear();

	delete[] fBufferIn;
	fBufferIn = NULL;

	while (!fBufferOut->empty())
	{
		fBufferOut->pop();
	}
}

extern "C" DLL_PUBLIC void Java_com_smp_soundtouchandroid_SoundTouch_setup(JNIEnv *env,
	jobject thiz, jint track, jint channels, jint samplingRate, jint bytesPerSample, jfloat tempo, jint pitchSemi)
{
	SoundTouchExt& soundTouch = sProcessors.at(track);
	setup(soundTouch, channels, samplingRate, bytesPerSample, tempo, pitchSemi);
}

extern "C" DLL_PUBLIC void Java_com_smp_soundtouchandroid_SoundTouch_finish(JNIEnv *env,
	jobject thiz, jint track,
	int length)
{
	SoundTouchExt& soundTouch = sProcessors.at(track);

	const int bytesPerSample = soundTouch.bytesPerSample;
	const int BUFF_SIZE = length / bytesPerSample;

	queue<signed char>* fBufferOut = sProcessors.at(track).fBufferOut;

	SAMPLETYPE* fBufferIn = new SAMPLETYPE[BUFF_SIZE];
	process(soundTouch, fBufferIn, fBufferOut, BUFF_SIZE, true); //audio is finishing

	delete[] fBufferIn;
	fBufferIn = NULL;
}
extern "C" DLL_PUBLIC void Java_com_smp_soundtouchandroid_SoundTouch_putBytes(JNIEnv *env,
	jobject thiz,
	jint track,
	jbyteArray input,
	jint length)
{

	SoundTouchExt& soundTouch = sProcessors.at(track);
	
	const int bytesPerSample = soundTouch.bytesPerSample;
	const int BUFF_SIZE = length / bytesPerSample;

	queue<signed char>* fBufferOut = sProcessors.at(track).fBufferOut;

	jboolean isCopy;
	jbyte* ar = env->GetByteArrayElements(input, &isCopy);


	SAMPLETYPE* fBufferIn = new SAMPLETYPE[BUFF_SIZE];

	//converts the chars to floats (16-bit only).
	convertInput16(ar, fBufferIn, BUFF_SIZE);

	process(soundTouch, fBufferIn, fBufferOut, BUFF_SIZE, false); //audio is ongoing.

	env->ReleaseByteArrayElements(input, ar, JNI_ABORT);
	
	delete[] fBufferIn;
	fBufferIn = NULL;
}

extern "C" DLL_PUBLIC jint Java_com_smp_soundtouchandroid_SoundTouch_getBytes(JNIEnv *env,
	jobject thiz, jint track, jbyteArray get, jint toGet)
{
	queue<signed char>* fBufferOut = sProcessors.at(track).fBufferOut;

	jbyte* res = new jbyte[toGet];
	
	jint bytesWritten = putQueueInChar(res, fBufferOut, toGet);
	
	jboolean isCopy;
	jbyte* ar = (jbyte*)env->GetPrimitiveArrayCritical(get, &isCopy);

	memcpy(ar, res, toGet);

	env->ReleasePrimitiveArrayCritical(get, ar, JNI_ABORT);

	delete[] res;
	res = NULL;

	return bytesWritten;
}

static int putQueueInChar(jbyte* res, queue<signed char>* fBufferOut, int toPut)
{
	int count = 0;
	for (int i = 0; i < toPut; i++)
	{
		if (fBufferOut->size() > 0)
		{
			res[i] = fBufferOut->front();
			fBufferOut->pop();
			++count;
		}
		else
		{
			break;
		}
	}
	return count;
}

static int process(SoundTouchExt& soundTouch, SAMPLETYPE* fBufferIn, queue<signed char>* fBufferOut,
	const int BUFF_SIZE,
	bool finishing)
{
	const int channels = soundTouch.channels;
	const int buffSizeSamples = BUFF_SIZE / channels;
	const int bytesPerSample = soundTouch.bytesPerSample;

	SoundTouch* sTouch = soundTouch.sTouch;

	int nSamples = BUFF_SIZE / channels;
	
	int processed = 0;

	if (finishing)
	{
		sTouch->flush();
	}
	else
	{
		sTouch->putSamples(fBufferIn, nSamples);
	}

	do
	{
		nSamples = sTouch->receiveSamples(fBufferIn, buffSizeSamples);
		processed += write(fBufferIn, fBufferOut, nSamples * channels, bytesPerSample);
	} while (nSamples != 0);

	return processed;
}

static void* getConvBuffer(int sizeBytes)
{
	int convBuffSize = (sizeBytes + 15) & -8;
	// round up to following 8-byte bounday
	char *convBuff = new char[convBuffSize];
	return convBuff;
}

static int write(const float *bufferIn, queue<signed char>* bufferOut, int numElems, int bytesPerSample)
{
	int numBytes;

	int oldSize = bufferOut->size();

	if (numElems == 0) return 0;

	numBytes = numElems * bytesPerSample;
	short *temp = (short*)getConvBuffer(numBytes);

	switch (bytesPerSample)
	{
		
	case 2: //16 bit encoding
	{
				short *temp2 = (short *)temp;
				for (int i = 0; i < numElems; i++)
				{
					short value = (short)saturate(bufferIn[i] * 32768.0f, -32768.0f, 32767.0f); //magic to me
					temp2[i] = value; //works for little endian only.
				}
				break;
	}
		
	default:
		break;
	}
	for (int i = 0; i < numBytes / 2; ++i)
	{
		bufferOut->push(temp[i] & 0xff);
		bufferOut->push((temp[i] >> 8) & 0xff);
	}
	delete[] temp;
	temp = NULL;
	return bufferOut->size() - oldSize;
}

static void setup(SoundTouchExt& soundTouch, int channels, int sampleRate, int bytesPerSample, float tempoChange, float pitchSemi)
{
	SoundTouch* sTouch = soundTouch.sTouch;

	soundTouch.channels = channels;
	soundTouch.sampleRate = sampleRate;
	soundTouch.bytesPerSample = bytesPerSample;
	soundTouch.tempoChange = tempoChange;
	soundTouch.pitchSemi = pitchSemi;

	sTouch->setSampleRate(sampleRate);
	sTouch->setChannels(channels);

	sTouch->setTempo(tempoChange);
	sTouch->setPitchSemiTones(pitchSemi);
	sTouch->setRateChange(0);

	sTouch->setSetting(SETTING_USE_QUICKSEEK, false);
	sTouch->setSetting(SETTING_USE_AA_FILTER, true);

	//todo if speech
	if (false)
	{
		// use settings for speech processing
		sTouch->setSetting(SETTING_SEQUENCE_MS, 40);
		sTouch->setSetting(SETTING_SEEKWINDOW_MS, 15);
		sTouch->setSetting(SETTING_OVERLAP_MS, 8);
		//fprintf(stderr, "Tune processing parameters for speech processing.\n");
	}
}

static void convertInput16(jbyte* input, float* output, int lengthOut)
{
	short *temp2 = (short*)input;
	double conv = 1.0 / 32768.0; //it is magic to me.
	for (int i = 0; i < lengthOut; i++)
	{
		short value = temp2[i];
		output[i] = (float)(value * conv);
	}

}
static inline int saturate(float fvalue, float minval, float maxval)
{
	if (fvalue > maxval)
	{
		fvalue = maxval;
	}
	else if (fvalue < minval)
	{
		fvalue = minval;
	}
	return (int)fvalue;
}
#endif
