class Knob{

  int outStroke=3;
  String text="";
  boolean dragging = false; // Is the object being dragged?
  boolean overed = false; // Is the mouse over the ellipse?
  float x,y,w,h;          // Location and size
  float clickX, clickY,clickValue; // Mouseclick offset
  float minX,minY,maxX,maxY;
  boolean limitedX=false,limitedY=false;
  float expandedFactor=1.0;  
  float minValue=0,maxValue=100,angleValue=0;
  float fakeValue=0;
  color fillColor=redColor;
  color backColor=color(150);
  float innerRadius=0.5;
  boolean textOnCenter=true;
  
  Knob(float x, float y, float w, float h) {
    this.x=x;
    this.y=y;
    this.w=w;
    this.h=h;
    expandedFactor=1.2;
    ellipseMode(CENTER);
    limitY(y-h,y+h);
  }
  
  void setFillColor(color c){
    fillColor=c;
  }
  
  void limitY(float min,float max){
    limitedY=true;
    minY=min;
    maxY=max;
    fakeValue=max;
  } 
  
  void limitX(float min,float max){
    limitedX=true;
    minX=min;
    maxX=max;
    fakeValue=max; 
  }   
 
  float normalizedValue(){
    return map(fakeValue, minY, maxY, 1.0, 0.0);  
  }
  
  float value(){
    return map(fakeValue, minY, maxY, maxValue, minValue);  
  }  
  
  boolean isOvered(int mX,int mY){
    if(dist(x,y,mX,mY)<min(w,h)*expandedFactor*0.5){
      overed=true;
    }else{
      overed=false;
    }
    
    return overed;
  }
  
  boolean startDragging(int mX,int mY){
    if(isOvered(mX,mY)){
      if(!dragging){
        dragging=true;
        clickX=mX;
        clickY=mY;
        clickValue=fakeValue;
        return true;
      }
      return false;
    }else{
      dragging=false;
      return false;
    }
  }
  
  void intoLimits(){
    if(limitedY){
      if(fakeValue<minY)
        fakeValue=minY;
      else if(fakeValue>maxY)
         fakeValue=maxY;   
    }
    if(limitedX){
      if(fakeValue<minX)
        fakeValue=minX;
      else if(fakeValue>maxX)
         fakeValue=maxX;   
    }    
  } 
 
  boolean stopDragging() {
    if(dragging){
      dragging = false;      
      return true;
    }else{
      dragging = false;
      return false;
    }
  }
  
  void dragVertically(int my) {
    float offset=my-clickY;
    if (dragging) {
      if(!limitedY)
        fakeValue = clickValue+offset;
      else if(clickValue + offset<minY)
        fakeValue = minY;
      else if(clickValue + offset>maxY)
        fakeValue = maxY;
      else fakeValue = clickValue + offset;
    }
  }
  
  void dragHorizontally(int mx) {
    float offset=mx-clickX;
    if (dragging) {
      if(!limitedX)
        fakeValue = clickValue + offset;
      else if(clickValue + offset<minX)
        fakeValue = minX;
      else if(clickValue + offset>maxX)
        fakeValue = maxX;
      else fakeValue = clickValue + offset;
    }
  }   
  
  void draw() {
    stroke(150,255);
    fill(150,255);
    strokeWeight(outStroke);
    arc(x,y,w,h,radians(75),radians(105),PIE); 
    stroke(200);
    if (dragging) fill (50,180);
    else if (overed) fill(140,180);
    else fill(100,180);
    arc(x,y,w,h,radians(115),radians(425),PIE);
    angleValue=map(normalizedValue(),0,1,0,310);
    fill(fillColor);
    //stroke(100,255);
    arc(x,y,w,h,radians(65-angleValue),radians(65),PIE);
    float posY;
    if(DrumMachine.isAndroidDevice)
      posY=y+h*.55;
    else
      posY=y+h*.63;
    if(innerRadius>0){
      fill(backColor);
      arc(x,y,w*innerRadius,h*innerRadius,radians(115),radians(425),OPEN);
    }    
    fill(200);
    textSize(FontAdjuster.getSize(20));
    textSize(FontAdjuster.getSize(15));
    textAlign(CENTER);    
    if(textOnCenter){
      fill(100);
      textSize(FontAdjuster.getSize(18));
      text(round(value()),x,y+h*0.11);      
      fill(200);      
    }else{
      text(round(value()),x,y-h*0.29);   
    }
    textSize(FontAdjuster.getSize(12));     
    textAlign(LEFT);
    text(round(minValue),x+w*.2,posY);
    textAlign(RIGHT);
    text(round(maxValue),x-w*.2,posY);
    text(text,x+w*.5,y+h*.5);    
  } 

}
