package com.smp.soundtouchandroid;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;


import android.content.Context;

public class WavReader
{
	private WavInfo info;
	private int resId;
	private Context context;
	
	private WavReader(int resId, Context context)
	{
		this.resId = resId;
		this.context = context.getApplicationContext();
		
		InputStream stream = context.getResources()
				.openRawResource(resId);
		
		try
		{
			info = WavInfo.readHeader(stream);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public WavInfo getWaveInfo()
	{
		return info;
	}
	
	public static WavReader getInstance(int resId, Context context)
	{
		return new WavReader(resId, context);
	}
	
	public byte[] readWavPcm() throws IOException
	{
		BufferedInputStream stream = new BufferedInputStream(context.getResources()
				.openRawResource(resId));
		byte[] data = new byte[info.getDataSize()];
		stream.read(data, 0, data.length);
		return data;
	}
}
