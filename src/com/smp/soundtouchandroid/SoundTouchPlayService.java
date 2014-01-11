package com.smp.soundtouchandroid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;
import static com.smp.soundtouchandroid.Constants.*;

public class SoundTouchPlayService extends IntentService
{
	public SoundTouchPlayService()
	{
		super("SoundTouchService");

	}

	private static AudioTrack track;

	static
	{

	}

	private SoundTouch soundTouch;

	@Override
	protected void onHandleIntent(Intent intent)
	{
		track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE_TRACK, AudioTrack.MODE_STREAM);
		soundTouch = SoundTouch.getInstance().setup(2, 44100, 2, 2.0f, -2);

		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		String fileName = "music.mp3";
		String fullPath = baseDir + "/" + fileName;

		Mp3File fileOut = null;
		try
		{
			fileOut = new Mp3File(fullPath);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		

		track.pause();
		track.flush();

	}

	@Override
	public void onCreate()
	{

		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		super.onDestroy();
	}

}
