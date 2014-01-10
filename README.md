SoundTouch-Android
==================
by Steve Myers

An Android wrapper for the The SoundTouch Library Audio Processing Library by Olli Parviainen 2001-2012

It is pre-alpha, use at your own risk. The SoundTouch class is getting there, the rest is currently rubbish.

Currently only supports Androids with an FPU and 16 bit audio samples.

Example usage:

<div style="background: #ffffff; overflow:auto;width:auto;border:solid gray;border-width:.1em .1em .1em .8em;padding:.2em .6em;"><pre style="margin: 0; line-height: 125%"><span style="color: #888888">//Currently a singleton</span>
SoundTouch soundTouch <span style="color: #333333">=</span> SoundTouch<span style="color: #333333">.</span><span style="color: #0000CC">getInstance</span><span style="color: #333333">();</span>

<span style="color: #888888">//Set your audio processing requirements: channels, samplingRate, bytesPerSample, tempoChange (1.0 is normal speed), pitchChange (in semi-tones)</span>
soundTouch<span style="color: #333333">.</span><span style="color: #0000CC">setup</span><span style="color: #333333">(</span><span style="color: #0000DD; font-weight: bold">2</span><span style="color: #333333">,</span> <span style="color: #0000DD; font-weight: bold">44100</span><span style="color: #333333">,</span> <span style="color: #0000DD; font-weight: bold">2</span><span style="color: #333333">,</span> <span style="color: #6600EE; font-weight: bold">1.0f</span><span style="color: #333333">,</span> <span style="color: #0000DD; font-weight: bold">2</span><span style="color: #333333">);</span>

<span style="color: #888888">//byte[] sizes are recommended to be 4096 bytes.</span>

<span style="color: #888888">//put a byte[] of PCM audio in the sound processor:</span>
soundTouch<span style="color: #333333">.</span><span style="color: #0000CC">putBytes</span><span style="color: #333333">(</span>input<span style="color: #333333">);</span>

<span style="color: #888888">//get a byte[] of processed audio and write to output:</span>
soundTouch<span style="color: #333333">.</span><span style="color: #0000CC">getBytes</span><span style="color: #333333">(</span>output<span style="color: #333333">);</span>

<span style="color: #888888">//after you write the last byte[], call finish().</span>

soundTouch<span style="color: #333333">.</span><span style="color: #0000CC">finish</span><span style="color: #333333">();</span>

<span style="color: #888888">//now get the remaining bytes from the sound processor.</span>
<span style="color: #333399; font-weight: bold">int</span> bytesReceived <span style="color: #333333">=</span> <span style="color: #333333">-</span><span style="color: #0000DD; font-weight: bold">1</span><span style="color: #333333">;</span>
<span style="color: #008800; font-weight: bold">while</span> <span style="color: #333333">(</span>bytesReceived <span style="color: #333333">!=</span> <span style="color: #0000DD; font-weight: bold">0</span><span style="color: #333333">)</span>
<span style="color: #333333">{</span>
	bytesReceived <span style="color: #333333">=</span> soundTouch<span style="color: #333333">.</span><span style="color: #0000CC">getBytes</span><span style="color: #333333">(</span>output<span style="color: #333333">);</span>
	<span style="color: #888888">//do stuff with output.</span>
<span style="color: #333333">}</span>
</pre></div>


