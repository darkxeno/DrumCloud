package com.codefixia.ads;

public enum AdsMode 
{
FLURRY(0),EXTERNAL(1),LEADBOLT_OTHER_1(2),LEADBOLT_OTHER_2(3),LEADBOLT_OTHER_3(4);

  private int value;

  AdsMode(int value){
    this.value=value;
  }

  public int value(){
    return value;
  }
  
  public static AdsMode fromInt(int value){
     switch(value){
       case 0:
         return FLURRY;
       case 1:
         return EXTERNAL;
       case 2:
         return LEADBOLT_OTHER_1;
       case 3:
         return LEADBOLT_OTHER_2;
       default:
         return FLURRY;
     }
  }
};
