package com.codefixia.utils;

import com.codefixia.drumcloud.DrumCloud;

public class FontAdjuster {

  private static int width=480;

  public static int getSize(int defaultSize){
    
    if(getWidth()==480){
      return defaultSize;
    }else{
      float ratio=getWidth()/480.0f;
      defaultSize=(int)DrumCloud.round(defaultSize*ratio);
      return defaultSize;
    }
  
  }

public static int getWidth() {
	return width;
}

public static void setWidth(int width) {
	FontAdjuster.width = width;
}

}