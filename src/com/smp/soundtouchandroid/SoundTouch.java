
package com.smp.soundtouchandroid;

public class SoundTouch
{
	static
    {
        System.loadLibrary("soundtouch");
    }
	
	static final int FINAL_BUFFER = 4096;
	
	private int channels, samplingRate, bytesPerSample;
	private float tempo;
	private int pitchSemi;
	
	public SoundTouch(int channels, int samplingRate, int bytesPerSample, float tempo, int pitchSemi)
	{
		this.channels = channels;
		this.samplingRate = samplingRate;
		this.bytesPerSample = bytesPerSample;
		this.tempo = tempo;
		this.pitchSemi = pitchSemi;
		
		//todo: id's
		setup(0, channels, samplingRate, bytesPerSample, tempo, pitchSemi);
	}
	
	private native final void setup(int id, int channels, int samplingRate, int bytesPerSample, float tempo, int pitchSemi);
    private native final void putBytes(byte[] input, int length);
    private native final int getBytes(byte[] output, int toGet);
    private native final void finish(int bufSize);
    
    public void putBytes(byte[] input)
    {
    	putBytes(input, input.length);
    }
    
    public int getBytes(byte[] output)
    {
    	return getBytes(output, output.length);
    }
    
    //call finish after the last bytes have been written
    public void finish()
    {
    	finish(FINAL_BUFFER);
    }
    
	
}
