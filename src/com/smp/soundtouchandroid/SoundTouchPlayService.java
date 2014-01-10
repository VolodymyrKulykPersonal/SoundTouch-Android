package com.smp.soundtouchandroid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;



import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

public class SoundTouchPlayService extends IntentService
{
	public SoundTouchPlayService()
	{
		super("SoundTouchService");

	}
	

	private static AudioTrack track;
	private final static int BUFFER_SIZE_TRACK = 16384;
	
	
	static 
	{
		track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE_TRACK, AudioTrack.MODE_STREAM);
	}
	
	private SoundTouch soundTouch;
	private final int BUFFER_SIZE_PUT = 4096;
	private final int BUFFER_SIZE_GET = 4096;
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		soundTouch = new SoundTouch(2, 44100, 2, 1.0f, 2);

		byte[] bytes = null;

		try
		{
			int ms = Calendar.getInstance().get(Calendar.MILLISECOND);
			Log.i("TIME", String.valueOf(ms));
			// bytes = wav.readWavPcm();
			String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
			String fileName = "music.mp3";
			String fullPath = baseDir + "/" + fileName;
			bytes = Mp3File.decode(fullPath, 0, 5000);
			ms = Calendar.getInstance().get(Calendar.MILLISECOND);
			Log.i("TIME", String.valueOf(ms));

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);

		final ByteBuffer playBuffer = ByteBuffer.wrap(bytes);
		final byte[] put = new byte[BUFFER_SIZE_PUT];

		while (playBuffer.hasRemaining())
		{
			if (playBuffer.remaining() < BUFFER_SIZE_PUT)
			{
				final byte[] lastPut = new byte[playBuffer.remaining()];
				playBuffer.get(lastPut);
				soundTouch.putBytes(lastPut);
				soundTouch.finish();
			}
			else
			{
				playBuffer.get(put);
				soundTouch.putBytes(put);
			}
		}
		byte[] get = new byte[BUFFER_SIZE_GET];

		track.play();

		int bytesReceived = -1;
		while (bytesReceived != 0)
		{
			bytesReceived = soundTouch.getBytes(get);

			track.write(get, 0, bytesReceived);
			// Log.d("SoundJ", "in main");
		}
		
		//amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);

		track.pause();
		track.flush();
		
		//track.release();
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
