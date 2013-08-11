package com.codefixia.audio;

public interface Filter {
  public void setFilter(float f, float r);
  public float applyFilter(float in);
}