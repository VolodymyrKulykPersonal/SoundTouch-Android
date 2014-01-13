package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.BUFFER_SIZE_TRACK;

import java.io.FileNotFoundException;
import java.io.IOException;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

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
		this.id = id;

		if (Build.VERSION.SDK_INT >= 16)
		{
			this.file = new MediaCodecMp3Decoder(file);
		}
		else
		{
			this.file = new JLayerMp3Decoder(file);
		}

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
		track.play();

		try
		{
			Log.d("MP3", "TRY");
			playAudio();
		}
		catch (com.smp.soundtouchandroid.DecoderException e)
		{
			e.printStackTrace();
		}
		finally
		{
			Log.d("MP3", "FINALLY");
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

	private void playAudio() throws com.smp.soundtouchandroid.DecoderException
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
		input = file.decodeChunk();
		soundTouch.putBytes(input);

		bytesReceived = soundTouch.getBytes(input);

		track.write(input, 0, bytesReceived);

		return input;
	}

	// TODO code duplication...refactor?
	private int processChunkForInt() throws com.smp.soundtouchandroid.DecoderException
	{
		byte[] input;
		int bytesReceived = 0;
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
		input = file.decodeChunk();
		soundTouch.putBytes(input);

		bytesReceived = soundTouch.getBytes(input);

		track.write(input, 0, bytesReceived);

		return bytesReceived;
	}
}
