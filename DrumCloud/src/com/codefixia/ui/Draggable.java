package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;

class Draggable {

  private boolean dragging = false; // Is the object being dragged?
  boolean rollover = false; // Is the mouse over the ellipse?
  
  private float x;          // Location and size
private float y;
private float w;
private float h;
  float offsetX, offsetY; // Mouseclick offset
  float minX,minY,maxX,maxY;
  boolean limitedX=false,limitedY=false;
  float expandedFactor=1.0f;
  boolean slideOnClick=false;
  float minXZone,minYZone,maxXZone,maxYZone;

  Draggable(float tempX, float tempY, float tempW, float tempH) {
	setX(tempX);
    setY(tempY);
    setW(tempW);
    setH(tempH);
    offsetX = 0;
    offsetY = 0;
    calculateClickZone();
  }
  
  public void limitY(float min,float max){
    limitedY=true;
    minY=min;
    maxY=max;
    calculateClickZone();
  }
  
  public void limitX(float min,float max){
    limitedX=true;
    minX=min;
    maxX=max;
    calculateClickZone();    
  }  

  public void calculateClickZone(){
    float xExp=(expandedFactor-1.0f)*getW();
    float yExp=(expandedFactor-1.0f)*getH();
    
    if(limitedX){
      minXZone=minX-(xExp*0.5f);
      maxXZone=maxX+(xExp*0.5f)+getW();
    }else{
      minXZone=getX()-(xExp*0.5f);
      maxXZone=getX()+getW()+(xExp*0.5f);    
    }
    if(limitedY){
      minYZone=minY-(yExp*0.5f);
      maxYZone=getH()+maxY+(yExp*0.5f);    
    }else{
      minYZone=getY()-(yExp*0.5f);
      maxYZone=getH()+getY()+(yExp*0.5f);
    }  
  }

  // Method to display
  public void debugDisplay() {
    DrumCloud.X.stroke(255);
    //if(expandedFactor!=1.0){
      DrumCloud.X.fill(255,0,0);
      DrumCloud.X.rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);    
    //}
    
    if (isDragging()) DrumCloud.X.fill (50);
    else if (rollover) DrumCloud.X.fill(100);
    else DrumCloud.X.fill(175,200);
    DrumCloud.X.rect(getX(),getY(),getW(),getH());
  }
  
  public void draw() {
    DrumCloud.X.strokeWeight(3);
    DrumCloud.X.fill(0,0,0,0.5f);
    DrumCloud.X.rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);    
    DrumCloud.X.stroke(200);
    if (isDragging()) DrumCloud.X.fill (50);
    else if (rollover) DrumCloud.X.fill(100);
    else DrumCloud.X.fill(175,200);
    DrumCloud.X.rect(getX(),getY(),getW(),getH());
  }  

  // Is a point inside the rectangle (for click)?
  public void clicked(int mx, int my) {
    calculateClickZone();
    //println("zone (X:"+minXZone+" to "+maxXZone+" Y:"+minYZone+" to "+maxYZone+",) mouse:("+mx+","+my+")");
    if (mx > minXZone && mx < maxXZone && my > minYZone && my < maxYZone) {
    //if (mx > x && mx < x + w && my > y && my < y + h) {
      setDragging(true);
      // If so, keep track of relative location of click to corner of rectangle
      offsetX = getX()-mx;
      offsetY = getY()-my;
      if(slideOnClick){
        if(limitedY)setY(my-getH()*0.5f);
        if(limitedX)setX(mx-getW()*0.5f);
        intoLimits();
        //println("moved to x:"+x+" y:"+y);
      }    
    }
  }
  
  public void intoLimits(){
    if(limitedY){
      if(getY()<minY)
        setY(minY);
      else if(getY()>maxY)
         setY(maxY);   
    }
    if(limitedX){
      if(getY()<minX)
        setX(minX);
      else if(getY()>maxX)
         setX(maxX);   
    }    
  }
  
  // Is a point inside the rectangle (for rollover)
  public void rollover(int mx, int my) {
    if (mx > minXZone && mx < maxXZone && my > minYZone && my < maxYZone) {
    //if (mx > x && mx < x + w && my > y && my < y + h) {
      rollover = true;
    } else {
      rollover = false;
    }
  }

  // Stop dragging
  public void stopDragging() {
    setDragging(false);
  }
  
  // Drag the rectangle
  public void drag(int mx, int my) {
    if (isDragging()) {
      if(!limitedX || (mx + offsetX>=minX && mx + offsetX<=maxX))
        setX(mx + offsetX);
      if(!limitedY || (my + offsetY>=minY && my + offsetY<=maxY))
        setY(my + offsetY);
    }
  }
  
  public void dragVertically(int my) {
    if (isDragging()) {
      if(!limitedY)
        setY(my + offsetY);
      else if(my + offsetY<minY)
        setY(minY);
      else if(my + offsetY>maxY)
        setY(maxY);
      else setY(my + offsetY);
    }
  }
  
  public void dragHorizontally(int mx) {
    if (isDragging()) {
      if(!limitedX)
        setX(mx + offsetX);
      else if(mx + offsetX<minX)
        setX(minX);
      else if(mx + offsetX>maxX)
        setX(maxX);
      else setX(mx + offsetX);
    }
  }

public float getY() {
	return y;
}

public void setY(float y) {
	this.y = y;
}

public float getW() {
	return w;
}

public void setW(float w) {
	this.w = w;
}

public float getX() {
	return x;
}

public void setX(float x) {
	this.x = x;
}

public boolean isDragging() {
	return dragging;
}

public void setDragging(boolean dragging) {
	this.dragging = dragging;
}

public float getH() {
	return h;
}

public void setH(float h) {
	this.h = h;
}  

}