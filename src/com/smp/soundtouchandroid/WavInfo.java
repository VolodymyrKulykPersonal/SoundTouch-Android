package com.smp.soundtouchandroid;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


import android.util.Log;

public class WavInfo
{
	private static final String RIFF_HEADER = "RIFF";
	private static final String WAVE_HEADER = "WAVE";
	private static final String FMT_HEADER = "fmt ";
	private static final String DATA_HEADER = "data";
	private static final int HEADER_SIZE = 128;
	private static final String CHARSET = "ASCII";

	public static WavInfo readHeader(InputStream wavStream)
			throws IOException, DecoderException
	{

		ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

		buffer.rewind();
		buffer.position(buffer.position() + 20);
		int format = buffer.getShort();
		checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means
																		// Linear
																		// PCM
		int channels = buffer.getShort();
		checkFormat(channels == 1 || channels == 2, "Unsupported channels: " + channels);
		int rate = buffer.getInt();
		checkFormat(rate <= 48000 && rate >= 11025, "Unsupported rate: " + rate);
		buffer.position(buffer.position() + 6);
		int bits = buffer.getShort();
		checkFormat(bits == 16, "Unsupported bits: " + bits);
		
		buffer.position(buffer.position() + 6);
		
		
		int dataSize = buffer.getInt();
		checkFormat(dataSize > 0, "wrong datasize: " + dataSize);

		return new WavInfo(rate, channels, dataSize);
	}
	
	public WavInfo(int rate, int channels, int dataSize)
	{
		this.rate = rate;
		this.channels = channels;
		this.dataSize = dataSize;
	}
	private int rate, channels, dataSize;
	
	static void checkFormat(boolean valid, String errorMsg) throws DecoderException
	{
		if(!valid)
			throw new DecoderException(errorMsg);
	}

	public int getDataSize()
	{
		return dataSize;
	}
	public int getChannels()
	{
		return channels;
	}
	public int getRate()
	{
		return rate;
	}
}
