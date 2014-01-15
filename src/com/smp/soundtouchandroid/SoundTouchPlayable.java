package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.*;

import java.io.IOException;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

public class SoundTouchPlayable implements Runnable
{
	private Object pauseLock;
	private SoundTouch soundTouch;
	private AudioTrack track;
	private Mp3Decoder file;

	private volatile boolean paused, finished;
	private int id;

	public SoundTouchPlayable(String file, int id, int channels, int samplingRate,
			int bytesPerSample, float tempo, int pitchSemi)

			throws IOException
	{
		if (Build.VERSION.SDK_INT >= 16)
		{
			this.file = new MediaCodecMp3Decoder(file);
		}
		else
		{
			this.file = new JLayerMp3Decoder(file);
		}
		setup(id, channels, samplingRate, bytesPerSample, tempo, pitchSemi);
	}

	private void setup(int id, int channels, int samplingRate,
			int bytesPerSample, float tempo, int pitchSemi)
	{
		this.id = id;

		pauseLock = new Object();
		paused = true;
		finished = false;

		int channelFormat = -1;
		if (channels == 1)
			channelFormat = AudioFormat.CHANNEL_OUT_MONO;
		if (channels == 2)
			channelFormat = AudioFormat.CHANNEL_OUT_STEREO;

		soundTouch = new SoundTouch(id, channels, samplingRate, bytesPerSample, tempo, pitchSemi);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate, channelFormat,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE_TRACK, AudioTrack.MODE_STREAM);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		//track.play();

		try
		{
			playFile();
		}
		catch (com.smp.soundtouchandroid.DecoderException e)
		{
			e.printStackTrace();
		}
		finally
		{
			soundTouch.clearBuffer(id);
			track.stop();
			track.flush();
			track.release();
			file.close();
		}
	}

	public void play()
	{
		synchronized (pauseLock)
		{
			track.play();
			paused = false;
			pauseLock.notifyAll();
		}
	}

	public void pause()
	{
		synchronized (pauseLock)
		{
			track.pause();
			paused = true;
		}
	}

	public void stop()
	{
		if (paused)
		{
			synchronized (pauseLock)
			{
				paused = false;
				pauseLock.notifyAll();
			}
		}
		finished = true;

	}

	private void playFile() throws com.smp.soundtouchandroid.DecoderException
	{
		byte[] input = null;
		int bytesReceived = 0;

		do
		{
			if (finished)
				break;
			input = processChunk();
		}
		while (input.length > 0);

		soundTouch.finish();

		do
		{
			if (finished)
				break;
			bytesReceived = processChunkForInt();
		}
		while (bytesReceived > 0);
	}

	private byte[] processChunk() throws com.smp.soundtouchandroid.DecoderException
	{
		
		byte[] input;
		int bytesReceived = 0;

		input = file.decodeChunk();
		soundTouch.putBytes(input);

		bytesReceived = soundTouch.getBytes(input);

		track.write(input, 0, bytesReceived);

		synchronized (pauseLock)
		{
			while (paused)
			{
				try
				{
					pauseLock.wait();
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		return input;
	}

	// TODO code duplication...refactor?
	private int processChunkForInt() throws com.smp.soundtouchandroid.DecoderException
	{
		byte[] input;
		int bytesReceived = 0;

		input = file.decodeChunk();
		soundTouch.putBytes(input);

		bytesReceived = soundTouch.getBytes(input);

		track.write(input, 0, bytesReceived);

		synchronized (pauseLock)
		{
			while (paused)
			{
				try
				{
					pauseLock.wait();
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		return bytesReceived;
	}
}
