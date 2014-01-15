package com.smp.soundtouchandroid;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SoundTouchPlayer
{
	private static SoundTouchPlayer instance;
	
	private ExecutorService player;
	
	public static synchronized SoundTouchPlayer getInstance()
	{
		if (instance == null) 
			instance = new SoundTouchPlayer();
		return instance;
	}
	
	private SoundTouchPlayer()
	{
		player = Executors.newSingleThreadExecutor();
	}
	
	public void submit(Runnable runnable)
	{
		player.submit(runnable);
	}
	
	public <T> Future<T> submit(Callable<T> task)
	{
		return player.submit(task);
	}

}
