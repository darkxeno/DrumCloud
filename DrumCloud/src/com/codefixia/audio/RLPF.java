package com.codefixia.audio;

import com.codefixia.drumcloud.DrumCloud;

/** https://github.com/supercollider/supercollider/blob/master/server/plugins/FilterUGens.cpp */

public class RLPF implements Filter {
  float a0, b1, b2, y1, y2;
  float freq;
  float reson;
  float sampleRate;
  boolean changed;

  public RLPF(float sampleRate_) {
    this.sampleRate = sampleRate_;
    reset();
    this.setFilter(sampleRate / 4, 0);
  }
  private void reset() {
    a0 = 0.f;
    b1 = 0.f;
    b2 = 0.f;
    y1 = 0.f;
    y2 = 0.f;
    //setFilter(sampleRate/2, 0);
  }
  /** f is in the range 0-sampleRate/2 */
  public void setFilter(float f, float r) {
    // constrain 
    // limit to 0-1 
    f = DrumCloud.constrain(f, 0, sampleRate/2);
    r = DrumCloud.constrain(r, 0, 1);
    // remap to appropriate ranges
    f = DrumCloud.map(f, 0, sampleRate/4, 30, sampleRate / 4);
    r = DrumCloud.map(r, 0, 1, 0.005f, 2);

    DrumCloud.println("rlpf: f "+f+" r "+r);

    this.freq = f * DrumCloud.TWO_PI / sampleRate;
    this.reson = r;
    changed = true;
  }

  public float applyFilter(float in) {
    float y0;
    if (changed) {
      float D = DrumCloud.tan(freq * reson * 0.5f);
      float C = ((1.f-D)/(1.f+D));
      float cosf = DrumCloud.cos(freq);
      b1 = (1.f + C) * cosf;
      b2 = -C;
      a0 = (1.f + C - b1) * .25f;
      changed = false;
    }
    y0 = a0 * in + b1 * y1 + b2 * y2;
    y2 = y1;
    y1 = y0;
    if (Float.isNaN(y0)) {
      reset();
    }
    return y0;
  }
}