
package com.smp.soundtouchandroid;
import static com.smp.soundtouchandroid.Constants.*;
public class SoundTouch
{
	static
    {
        System.loadLibrary("soundtouch");
    }
	private static SoundTouch instance;
	
	public static synchronized SoundTouch getInstance() 
	{
		if (instance == null)
		{
			instance = new SoundTouch();
		}
		
		return instance;
	}
	
	private int channels, samplingRate, bytesPerSample;
	private float tempo;
	private int pitchSemi;
	
	public SoundTouch setup(int channels, int samplingRate, int bytesPerSample, float tempo, int pitchSemi)
	{
		instance.channels = channels;
		instance.samplingRate = samplingRate;
		instance.bytesPerSample = bytesPerSample;
		instance.tempo = tempo;
		instance.pitchSemi = pitchSemi;
		
		//todo: id's
		setup(0, channels, samplingRate, bytesPerSample, tempo, pitchSemi);
		
		return instance;
	}
	
	private SoundTouch()
	{
		// TODO Auto-generated constructor stub
	}

	private native final void setup(int id, int channels, int samplingRate, int bytesPerSample, float tempo, int pitchSemi);
    private native final void putBytes(byte[] input, int length);
    private native final int getBytes(byte[] output, int toGet);
    private native final void finish(int bufSize);
    
    public void putBytes(byte[] input)
    {
    	putBytes(input, input.length);
    }
    
    public int getBytes(byte[] output)
    {
    	return getBytes(output, output.length);
    }
    
    //call finish after the last bytes have been written
    public void finish()
    {
    	finish(BUFFER_SIZE_PUT);
    }
}
