public static class FontAdjuster {

  static int width=480;

  public static int getSize(int defaultSize){
    
    if(width==480){
      return defaultSize;
    }else{
      float ratio=width/480.0;
      defaultSize=(int)round(defaultSize*ratio);
      return defaultSize;
    }
  
  }

}
