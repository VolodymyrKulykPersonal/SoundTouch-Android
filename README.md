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


An Android wrapper for the the SoundTouch C++ Audio Processing Library by Olli Parviainen 2001-2012,
distrubted under LGPL license.

It is alpha, use at your own risk.

Currently only supports Androids with an FPU and 16 bit audio samples.

Example usage:

```java
//There are currently 16 track id's you can use (0-15), each one has a separate SoundTouch processor.

//Set your audio processing requirements: track id, channels, samplingRate, bytesPerSample, 
//                                      tempoChange (1.0 is normal speed), pitchChange (in semi-tones)

SoundTouch soundTouch = new SoundTouch(0, 2, 44100, 2, 1.0f, 2);

//byte[] sizes are recommended to be 8192 bytes.

//put a byte[] of PCM audio in the sound processor:
soundTouch.putBytes(input);

//write output to a byte[]:
int bytesReceived = soundTouch.getBytes(output);

//after you write the last byte[], call finish().
soundTouch.finish();

//now get the remaining bytes from the sound processor.
int bytesReceived = 0;
do
{
    bytesReceived = soundTouch.getBytes(output);
    //do stuff with output.
} while (bytesReceived != 0)

//if you stop playing, call clear on the track id to clear the pipeline for later use.
soundTouch.clearBuffer(id)
```

Take a look at the (incomplete) SoundTouchPlayable to see how to use SoundTouch-Android library
to stream to an AudioTrack.


