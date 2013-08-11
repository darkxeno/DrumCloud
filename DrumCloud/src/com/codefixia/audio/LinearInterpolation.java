package com.codefixia.audio;

/**
 * Construct new data points within the range of a discrete set of known data points by linear equation
 *
 * @author Jacquet Wong
 */
public class LinearInterpolation {

  public LinearInterpolation() {
  }

  /**
   * Do interpolation on the samples according to the original and destinated sample rates
   *
   * @param oldSampleRate sample rate of the original samples
   * @param newSampleRate sample rate of the interpolated samples
   * @param samples       original samples
   * @return interpolated samples
   */
  public short[] interpolate(int oldSampleRate, int newSampleRate, short[] samples) {

    if (oldSampleRate==newSampleRate) {
      return samples;
    }

    int newLength=(int)Math.round(((float)samples.length/oldSampleRate*newSampleRate));
    float lengthMultiplier=(float)newLength/samples.length;
    short[] interpolatedSamples = new short[newLength];

    // interpolate the value by the linear equation y=mx+c        
    for (int i = 0; i < newLength; i++) {

      // get the nearest positions for the interpolated point
      float currentPosition = i / lengthMultiplier;
      int nearestLeftPosition = (int)currentPosition;
      int nearestRightPosition = nearestLeftPosition + 1;
      if (nearestRightPosition>=samples.length) {
        nearestRightPosition=samples.length-1;
      }

      float slope=samples[nearestRightPosition]-samples[nearestLeftPosition];     // delta x is 1
      float positionFromLeft = currentPosition - nearestLeftPosition;

      interpolatedSamples[i] = (short)(slope*positionFromLeft+samples[nearestLeftPosition]);      // y=mx+c
    }

    return interpolatedSamples;
  }
}