package com.codefixia.audio;

/**
 * Implement this interface so the AudioThread can request samples from you
 */
public interface AudioGenerator {
  /** AudioThread calls this when it wants a sample */
  public short getSample();
  public boolean isPlaying();
}