package com.codefixia.ui;

import processing.core.PApplet;
import processing.core.PConstants;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

class Knob{

	  int outStroke=3;
	  String text="";
	  boolean dragging = false; // Is the object being dragged?
	  boolean overed = false; // Is the mouse over the ellipse?
	  float x,y,w,h;          // Location and size
	  float clickX, clickY,clickValue; // Mouseclick offset
	  float minX,minY,maxX,maxY;
	  boolean limitedX=false,limitedY=false;
	  float expandedFactor=1.0f;  
	  float minValue=0,maxValue=100,angleValue=0;
	  float fakeValue=0;
	  int fillColor=DrumCloud.X.redColor;
	  int backColor=DrumCloud.X.color(150);
	  float innerRadius=0.5f;
	  boolean textOnCenter=true;
	  
	  Knob(float x, float y, float w, float h) {
	    this.x=x;
	    this.y=y;
	    this.w=w;
	    this.h=h;
	    expandedFactor=1.2f;
	    DrumCloud.X.ellipseMode(PConstants.CENTER);
	    limitY(y-h,y+h);
	  }
	  
	  public void setFillColor(int c){
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
	    return PApplet.map(fakeValue, minY, maxY, 1.0f, 0.0f);  
	  }
	  
	  float value(){
	    return PApplet.map(fakeValue, minY, maxY, maxValue, minValue);  
	  }  
	  
	  public boolean isOvered(int mX,int mY){
	    if(PApplet.dist(x,y,mX,mY)<PApplet.min(w,h)*expandedFactor*0.5){
	      overed=true;
	    }else{
	      overed=false;
	    }
	    
	    return overed;
	  }
	  
	  public boolean startDragging(int mX,int mY){
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
	 
	  public boolean stopDragging() {  
	    if(dragging){
	      dragging = false;      
	      return true;
	    }else{
	      dragging = false;
	      return false;
	    }
	  }
	  
	  public void dragVertically(int my) {
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
		DrumCloud.X.stroke(150,255);
		DrumCloud.X.fill(150,255);
		DrumCloud.X.strokeWeight(outStroke);
		DrumCloud.X.arc(x,y,w,h,PApplet.radians(75),PApplet.radians(105),PConstants.PIE); 
	    DrumCloud.X.stroke(200);
	    if (dragging) DrumCloud.X.fill (50,180);
	    else if (overed) DrumCloud.X.fill(140,180);
	    else DrumCloud.X.fill(100,180);
	    DrumCloud.X.arc(x,y,w,h,PApplet.radians(115),PApplet.radians(425),PConstants.PIE);
	    angleValue=PApplet.map(normalizedValue(),0,1,0,310);
	    DrumCloud.X.fill(fillColor);
	    //stroke(100,255);
	    DrumCloud.X.arc(x,y,w,h,PApplet.radians(65-angleValue),PApplet.radians(65),PConstants.PIE);
	    float posY;
	    if(DrumCloud.isAndroidDevice)
	      posY=y+h*.55f;
	    else
	      posY=y+h*.63f;
	    if(innerRadius>0){
	      DrumCloud.X.fill(backColor);
	      DrumCloud.X.arc(x,y,w*innerRadius,h*innerRadius,PApplet.radians(115),PApplet.radians(425),PConstants.OPEN);
	    }    
	    DrumCloud.X.fill(200);
	    DrumCloud.X.textSize(FontAdjuster.getSize(20));
	    DrumCloud.X.textSize(FontAdjuster.getSize(15));
	    DrumCloud.X.textAlign(PConstants.CENTER);    
	    if(textOnCenter){
	      DrumCloud.X.fill(100);
	      DrumCloud.X.textSize(FontAdjuster.getSize(18));
	      DrumCloud.X.text(PApplet.round(value()),x,y+h*0.11f);      
	      DrumCloud.X.fill(200);      
	    }else{
	    	DrumCloud.X.text(PApplet.round(value()),x,y-h*0.29f);   
	    }
	    DrumCloud.X.textSize(FontAdjuster.getSize(12));     
	    DrumCloud.X.textAlign(PConstants.LEFT);
	    DrumCloud.X.text(PApplet.round(minValue),x+w*.2f,posY);
	    DrumCloud.X.textAlign(PConstants.RIGHT);
	    DrumCloud.X.text(PApplet.round(maxValue),x-w*.2f,posY);
	    DrumCloud.X.text(text,x+w*.5f,y+h*.5f);    
	  } 

	}
