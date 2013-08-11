package com.codefixia.audio;



public class FXChain implements Synth {
  private float currentAmp;
  private float dAmp;
  private float targetAmp;
  private boolean goingUp;
  private Filter filter;

  private float[] dLine;   

  private float sampleRate;

  public FXChain(float sampleRate_) {
    sampleRate = sampleRate_;
    currentAmp = 1;
    dAmp = 0;
    // filter = new MickFilter(sampleRate);
    filter = new RLPF(sampleRate);

    //filter.setFilter(0.1, 0.1);
  }

  public void ramp(float val, float timeMs) {
    // calc the dAmp;
    // - change per ms
    targetAmp = val;
    dAmp = (targetAmp - currentAmp) / (timeMs / 1000 * sampleRate);
    if (targetAmp > currentAmp) {
      goingUp = true;
    }
    else {
      goingUp = false;
    }
  }


  public void setDelayTime(float delayMs) {
  }

  public void setDelayFeedback(float fb) {
  }

  public void volume(float volume) {
  }


  public short getSample(short input) {
    float in;
    in = (float) input / 32768;// -1 to 1

    in =  filter.applyFilter(in);
    if (goingUp && currentAmp < targetAmp) {
      currentAmp += dAmp;
    }
    else if (!goingUp && currentAmp > targetAmp) {
      currentAmp += dAmp;
    }  

    if (currentAmp > 1) {
      currentAmp = 1;
    }
    if (currentAmp < 0) {
      currentAmp = 0;
    }  
    in *= currentAmp;  
    return (short) (in * 32768);
  }

  public void setFilter(float f, float r) {
    filter.setFilter(f, r);
  }
}