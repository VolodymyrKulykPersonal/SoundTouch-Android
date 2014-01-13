package com.smp.soundtouchandroid;


public interface Mp3Decoder
{
	public byte[] decodeChunk() throws DecoderException;
	public void close();
}
