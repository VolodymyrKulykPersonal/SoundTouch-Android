SoundTouch-Android
==================
<pre>
Copyright [2013] [Steve Myers]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>


An Android wrapper for the The SoundTouch C++ Audio Processing Library by Olli Parviainen 2001-2012,
distrubted under LGPL license.

It is pre-alpha, use at your own risk. The SoundTouch class is getting there, the rest is currently rubbish.

Currently only supports Androids with an FPU and 16 bit audio samples.

Example usage:

<pre style='color:#000000;background:#ffffff;'><html><body style='color:#000000; background:#ffffff; '><pre>
There are currently 16 track id's you can use (0-15), each one has a separate SoundTouch processor.

<span style='color:#696969; '>//Set your audio processing requirements: track id, channels, samplingRate, bytesPerSample, </span>
											tempoChange (1.0 is normal speed), pitchChange (in semi-tones)

SoundTouch soundTouch = new SoundTouch(0, 2, 44100, 2, 1.0f, 2)<span style='color:#808030; '>;</span>

<span style='color:#696969; '>//byte[] sizes are recommended to be 8192 bytes.</span>

<span style='color:#696969; '>//put a byte[] of PCM audio in the sound processor:</span>
soundTouch.putBytes(input)<span style='color:#808030; '>;</span>

<span style='color:#696969; '>//get a byte[] of processed audio and write to output:</span>
soundTouch.getBytes(output)<span style='color:#808030; '>;</span>

<span style='color:#696969; '>//after you write the last byte[], call finish().</span>

soundTouch.finish()<span style='color:#808030; '>;</span>

<span style='color:#696969; '>//now get the remaining bytes from the sound processor.</span>
int bytesReceived = 0<span style='color:#808030; '>;</span>
do
<span style='color:#800080; '>{</span>
    bytesReceived <span style='color:#808030; '>=</span> soundTouch<span style='color:#808030; '>.</span>getBytes<span style='color:#808030; '>(</span>output<span style='color:#808030; '>)</span><span style='color:#800080; '>;</span>
    <span style='color:#696969; '>//do stuff with output.</span>
<span style='color:#800080; '>}</span> while (bytesReceived != 0)

<span style='color:#696969; '>//if you stop playing, call clear on the track id to clear the pipeline for later use.</span>

soundTouch.clearBuffer(id)

Take a look at the (incomplete) SoundTouchPlayable to see how to use SoundTouch-Android library
to stream to an AudioTrack.
</pre>

