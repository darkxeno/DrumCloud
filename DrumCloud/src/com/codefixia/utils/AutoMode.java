package com.codefixia.utils;

public enum AutoMode 
{
VOLUME(0),SAMPLE(1),LOOP(2),PITCH(3);

  private int value;

  AutoMode(int value){
    this.value=value;
  }

  public int value(){
    return value;
  }
  
  public static AutoMode fromInt(int value){
     switch(value){
       case 0:
         return VOLUME;
       case 1:
         return SAMPLE;
       case 2:
         return LOOP;
       case 3:
         return PITCH;
       default:
         return VOLUME;
     }
  }
};
