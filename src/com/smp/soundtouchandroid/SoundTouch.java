
package com.smp.soundtouchandroid;
import static com.smp.soundtouchandroid.Constants.*;
public class SoundTouch
{
	
	static
    {
        System.loadLibrary("soundtouch");
    }
	
	private int channels, samplingRate, bytesPerSample;
	private float tempo;
	private int pitchSemi;
	private int track;
	
	public SoundTouch(int track, int channels, int samplingRate, int bytesPerSample, float tempo, int pitchSemi)
	{
		
		this.channels = channels;
		this.samplingRate = samplingRate;
		this.bytesPerSample = bytesPerSample;
		this.tempo = tempo;
		this.pitchSemi = pitchSemi;
		this.track = track;
		
		setup(track, channels, samplingRate, bytesPerSample, tempo, pitchSemi);
	}
	
	private static synchronized native final void setup(int track, int channels, int samplingRate, int bytesPerSample, float tempo, int pitchSemi);
    private static synchronized native final void putBytes(int track, byte[] input, int length);
    private static synchronized native final int getBytes(int track, byte[] output, int toGet);
    private static synchronized native final void finish(int track, int bufSize);
    private static synchronized native final void clearBytes(int track);
    
    public void clearBuffer(int track)
    {
    	clearBytes(track);
    }
    public void putBytes(byte[] input)
    {
    	putBytes(track, input, input.length);
    }
    
    public int getBytes(byte[] output)
    {
    	return getBytes(track, output, output.length);
    }
    
    //call finish after the last bytes have been written
    public void finish()
    {
    	finish(track, BUFFER_SIZE_PUT);
    }
}
