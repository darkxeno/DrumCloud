package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;

public class Clickable {
 
  boolean clicked = false; // Is the object being clicked?
  boolean overed = false;
  
  int strokeWeight=3;
 
protected float x;          // Location and size
protected float y;
protected float w;
protected float h;
protected float offsetX=0; // Mouseclick offset
protected float offsetY=0;
  protected int fillColor=DrumCloud.X.color(127,128);
  protected int strokeColor=DrumCloud.X.color(100,128);
  
  

  public boolean isClicked() {
	return clicked;
  }

public Clickable(float tempX, float tempY, float tempW, float tempH) {
	setX(tempX);
    setY(tempY);
    setW(tempW);
    setH(tempH);
    setOffsetX(0);
    setOffsetY(0);
  }

  public void drawState(){
    if(clicked){
      drawClicked();
    }else if(overed){
      drawOvered();
    }else{
      drawNormal();
    }
  }
  
  public void draw(){
    this.drawNormal();
  }
  
  public void draw(int fillColor){
    this.setFillColor(fillColor);
    this.drawNormal();
  }
  
  public void draw(int fillColor,int strokeColor){
    this.setFillColor(fillColor);
    this.setStrokeColor(strokeColor);
    this.drawNormal();
  }
  
  public void drawNormal(){
	DrumCloud.X.stroke(getStrokeColor());
	DrumCloud.X.fill(DrumCloud.X.red(getFillColor()),DrumCloud.X.green(getFillColor()),DrumCloud.X.blue(getFillColor()),200);  
	DrumCloud.X.rect(getX(),getY(),getW(),getH());
    drawCommonContent();
  }

  public void drawCommonContent(){
  }   
  
  public void drawOvered(){
	DrumCloud.X.stroke(150);
	DrumCloud.X.fill(DrumCloud.X.red(getFillColor())*0.7f,DrumCloud.X.green(getFillColor())*0.7f,DrumCloud.X.blue(getFillColor())*0.7f,DrumCloud.X.alpha(getFillColor()));
	DrumCloud.X.rect(getX(),getY(),getW(),getH());
    drawCommonContent();    
  }
  
  public void drawClicked(){
    //stroke(red(strokeColor)*0.8,green(strokeColor)*0.8,blue(strokeColor)*0.8,alpha(strokeColor)*0.8);
	  DrumCloud.X.stroke(200);
    DrumCloud.X.fill(DrumCloud.X.red(getFillColor())*0.5f,DrumCloud.X.green(getFillColor())*0.5f,DrumCloud.X.blue(getFillColor())*0.5f,DrumCloud.X.alpha(getFillColor()));
    DrumCloud.X.rect(getX(),getY(),getW(),getH());
    drawCommonContent();    
  }


  // Is a point inside the rectangle (for click)?
  public boolean isClicked(int mx, int my) {
    if (isOver(mx,my)) {
      clicked = true;
      // If so, keep track of relative location of click to corner of rectangle
      setOffsetX(getX()-mx);
      setOffsetY(getY()-my);
    }else{
      clicked =false;
    }
    
    return clicked;
  }
  
  public boolean isOver(int mx, int my) {
    if (mx > getX() && mx < getX() + getW() && my > getY() && my < getY() + getH()) {
      overed=true;
    }else{
      overed=false;
    }
    return overed;
  }

  public void stopClick() {
    clicked=false;
  }

public int getFillColor() {
	return fillColor;
}

public void setFillColor(int fillColor) {
	this.fillColor = fillColor;
}

public float getX() {
	return x;
}

public void setX(float x) {
	this.x = x;
}

public float getH() {
	return h;
}

public void setH(float h) {
	this.h = h;
}

public float getW() {
	return w;
}

public void setW(float w) {
	this.w = w;
}

public float getY() {
	return y;
}

public void setY(float y) {
	this.y = y;
}

public int getStrokeColor() {
	return strokeColor;
}

public void setStrokeColor(int strokeColor) {
	this.strokeColor = strokeColor;
}

public float getOffsetX() {
	return offsetX;
}

public void setOffsetX(float offsetX) {
	this.offsetX = offsetX;
}

public float getOffsetY() {
	return offsetY;
}

public void setOffsetY(float offsetY) {
	this.offsetY = offsetY;
}  
 

}