package com.codefixia.audio;


public class FastFourierTransform {

  /**
   * Get the frequency intensities
   *
   * @param amplitudes    amplitudes of the signal
   * @return      intensities of each frequency unit: mag[frequency_unit]=intensity
   */
  public double[] getMagnitudes(double[] amplitudes) {

    int sampleSize = amplitudes.length;

    // call the fft and transform the complex numbers
    resampleFFT fft = new resampleFFT(sampleSize/2, -1);
    fft.transform(amplitudes);
    // end call the fft and transform the complex numbers

      double[] complexNumbers=amplitudes;

    // even indexes (0,2,4,6,...) are real parts
    // odd indexes (1,3,5,7,...) are img parts
    int indexSize=sampleSize/2;

    // FFT produces a transformed pair of arrays where the first half of the values represent positive frequency components and the second half represents negative frequency components.
    // we omit the negative ones
    int positiveSize=indexSize/2;

    double[] mag = new double[positiveSize];
    for (int i = 0; i < indexSize; i+=2) {
      mag[i/2] = Math.sqrt(complexNumbers[i] * complexNumbers[i]+ complexNumbers[i+1] * complexNumbers[i+1]);
    }

    return mag;
  }
}