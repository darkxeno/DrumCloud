package com.codefixia.utils;

public enum MainMode 
{
LIVE(0),SEQUENCER(1),AUTOMATOR(2),EFFECTS(3);

  private int value;

  MainMode(int value){
    this.value=value;
  }

  public int value(){
    return value;
  }
  
  public static MainMode fromInt(int value){
     switch(value){
       case 0:
         return LIVE;
       case 1:
         return SEQUENCER;
       case 2:
         return AUTOMATOR;
       case 3:
         return EFFECTS;
       default:
         return LIVE;
     }
  }
};
