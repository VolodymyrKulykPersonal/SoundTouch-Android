package com.smp.soundtouchandroid;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;



import android.util.Log;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public class Mp3File
{
	public static byte[] decode(String path, int startMs, int maxMs)
			throws IOException, DecoderException
	{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);

		float totalMs = 0;
		boolean seeking = true;

		File file = new File(path);
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file), 8 * 1024);
		try
		{
			Bitstream bitstream = new Bitstream(inputStream);
			Decoder decoder = new Decoder();

			boolean done = false;
			while (!done)
			{
				Header frameHeader = bitstream.readFrame();
				if (frameHeader == null)
				{
					done = true;
				}
				else
				{
					totalMs += frameHeader.ms_per_frame();

					if (totalMs >= startMs)
					{
						seeking = false;
					}

					if (!seeking)
					{
						SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

						if (output.getSampleFrequency() != 44100
								|| output.getChannelCount() != 2)
						{
							throw new DecoderException("mono or non-44100 MP3 not supported");
						}

						short[] pcm = output.getBuffer();
						for (short s : pcm)
						{
							outStream.write(s & 0xff);
							outStream.write((s >> 8) & 0xff);
						}
					}

					if (totalMs >= (startMs + maxMs))
					{
						done = true;
					}
				}
				//should be in finally
				bitstream.closeFrame();
			}

			
		}
		catch (BitstreamException e)
		{
			throw new IOException("Bitstream error: " + e);
		}
		catch (DecoderException e)
		{
			Log.w("DECODE", "Decoder error", e);
			throw new DecoderException("Decoder Exception");
		}
		catch (javazoom.jl.decoder.DecoderException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{

			}
		}
		return outStream.toByteArray();
	}
}
