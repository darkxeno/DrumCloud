package com.codefixia.audio;

import com.codefixia.drumcloud.DrumCloud;

/**
 * This class can play wavetables and includes an fx chain
 */
public class WavetableSynth extends AudioPlayer {


  private short[] sine;
  private short[] saw;
  private short[] wavetable;
  private float sampleRate;

  public WavetableSynth(int size, float sampleRate) {
    super(sampleRate);
    sine = new short[size];
    for (float i = 0; i < sine.length; i++) {
      float phase;
      phase = DrumCloud.TWO_PI / size * i;
      sine[(int)i] = (short) (DrumCloud.sin(phase) * 32768);
    }
    saw = new short[size];
    for (float i = 0; i<saw.length; i++) {
      saw[(int)i] = (short) (i / (float)saw.length *32768);
    }

    this.sampleRate = sampleRate;
    setAudioData(sine);
    setLooping(true);
  }

  public void setFrequency(float freq) {
    if (freq > 0) {
      //println("freq freq "+freq);
      setDReadHead((float)getAudioData().length / sampleRate * freq);
    }
  }

  public void loadWaveForm(float[] wavetable_) {
    if (wavetable == null || wavetable_.length != wavetable.length) {
      // only reallocate if there is a change in length
      wavetable = new short[wavetable_.length];
    }
    for (int i=0;i<wavetable.length;i++) {
      wavetable[i] = (short) (wavetable_[i] * 32768);
    }
    setAudioData(wavetable);
  }
}