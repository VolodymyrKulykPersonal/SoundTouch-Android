package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.BUFFER_SIZE_TRACK;

import java.io.FileNotFoundException;
import java.io.IOException;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class SoundTouchPlayable implements Runnable
{
	private Object pauseLock;
	private SoundTouch soundTouch;
	private AudioTrack track;
	private Mp3File file;

	private boolean paused, finished;

	public SoundTouchPlayable(String file, int channels, int samplingRate,
			int bytesPerSample, float tempo, int pitchSemi)

			throws FileNotFoundException
	{
		pauseLock = new Object();
		paused = true;
		finished = false;

		int channelFormat = -1;
		if (channels == 1)
			channelFormat = AudioFormat.CHANNEL_OUT_MONO;
		if (channels == 2)
			channelFormat = AudioFormat.CHANNEL_OUT_STEREO;

		soundTouch = SoundTouch.getInstance().setup(channels, samplingRate, bytesPerSample, tempo, pitchSemi);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate, channelFormat,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE_TRACK, AudioTrack.MODE_STREAM);

		this.file = new Mp3File(file);
	}

	@Override
	public void run()
	{
		track.play();

		while (!finished)
		{
			playAudio();

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
		}

		track.pause();
		track.release();
		file.close();

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
		finished = true;
	}

	private void playAudio()
	{
		byte[] input;
		try
		{
			int bytesReceived = 0;
			do
			{
				input = file.decodeChunk();
				soundTouch.putBytes(input);

				bytesReceived = soundTouch.getBytes(input);

				track.write(input, 0, bytesReceived);

			}
			while (input.length > 0);

			soundTouch.finish();

			do
			{
				input = file.decodeChunk();
				soundTouch.putBytes(input);

				bytesReceived = soundTouch.getBytes(input);

				track.write(input, 0, bytesReceived);

			}
			while (bytesReceived > 0);

		}
		catch (BitstreamException e)
		{
			finished = true;
			e.printStackTrace();
		}
		catch (DecoderException e)
		{
			finished = true;
			e.printStackTrace();
		}
		catch (IOException e)
		{
			finished = true;
			e.printStackTrace();
		}
	}
}
