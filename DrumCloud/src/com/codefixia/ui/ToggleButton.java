package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class ToggleButton extends Clickable{
  
  private String text="";
  private String activeText;
  
  private boolean enabled=true;
  private boolean ON=false;
  private boolean blinkWhenOn=false;
  float blinkIntervalMS=500;
  private boolean blinkOn=false;
  boolean released=false;
  boolean dragging=false;
  private int activeColor=-1;

  public ToggleButton(float tempX, float tempY, float tempW, float tempH) {
	  super(tempX,tempY,tempW,tempH);
  }

  public int getActiveColor() {
	  return activeColor;
  }

  public void setActiveColor(int activeColor) {
	  this.activeColor = activeColor;
  }

  public boolean isEnabled() {
	  return enabled;
  }

  public void setEnabled(boolean enabled) {
	  this.enabled = enabled;
  }

  public void drawState(){
	  if(isON()){
		  drawActivated();
	  }else if(overed){
		  drawOvered();
	  }else{
		  drawNormal();
	  }
  }

  public void drawActivated(){
	  if(isBlinkWhenOn()){
      if(DrumCloud.X.millis()%(blinkIntervalMS*2)<blinkIntervalMS){
        setBlinkOn(true);  
        DrumCloud.X.stroke(255);
      }
      else{
        setBlinkOn(false);
        DrumCloud.X.stroke(getStrokeColor());
      }     
    }    
    if(activeColor==-1)
    	DrumCloud.X.fill(DrumCloud.X.red(getFillColor()),DrumCloud.X.green(getFillColor()),DrumCloud.X.blue(getFillColor()),255);
    else
    	DrumCloud.X.fill(activeColor);
    DrumCloud.X.rect(getX(),getY(),getW(),getH());
    drawCommonContent();  
  }
  
  public void drawNormal(){
    DrumCloud.X.stroke(getStrokeColor());
    if(!enabled)
    	DrumCloud.X.fill(150,200);
    else if(activeColor==-1)
    	DrumCloud.X.fill(DrumCloud.X.red(getFillColor())-100,DrumCloud.X.green(getFillColor())-100,DrumCloud.X.blue(getFillColor())-100,200);
    else
    	DrumCloud.X.fill(getFillColor());
    DrumCloud.X.rect(getX(),getY(),getW(),getH());
    drawCommonContent();
  }  
  
  public void drawCommonContent() {
    DrumCloud.X.textSize(FontAdjuster.getSize(20));
    DrumCloud.X.textAlign(DrumCloud.CENTER);
    if(isON()){
      DrumCloud.X.fill(255);
      if(getActiveText()!=null)
        DrumCloud.X.text(getActiveText(), getX()+(getW()*0.5f), getY()+(getH()*0.7f));
      else
        DrumCloud.X.text(getText(), getX()+(getW()*0.5f), getY()+(getH()*0.7f));
    }
    else{
      if(!enabled)	
    	  DrumCloud.X.fill(50);
      else
    	  DrumCloud.X.fill(200);
      DrumCloud.X.text(getText(), getX()+(getW()*0.5f), getY()+(getH()*0.7f));
    }    
  }
  
  public boolean isClicked(int mx, int my) {
	if(!enabled)return false;
	
    if (isOver(mx, my)) {
      clicked = true;
      setOffsetX(getX()-mx);
      setOffsetY(getY()-my);
      setON(!isON());
    }
    else {
      clicked =false;
    }

    return clicked;
  }
  
  public boolean isReleased(int mx, int my) {
	  if(!enabled)return false;
	  
	  if (!dragging && isOver(mx, my)) {
		  released = true;
		  setOffsetX(getX()-mx);
		  setOffsetY(getY()-my);
		  setON(!isON());
	  }
	  else {
		  released =false;
	  }
	  overed=false;	  
	  dragging=false;
	  clicked =false;

	  return released;
  }
  
  public boolean isDragging(int mx, int my) {
	  if(!enabled)return false;
	  
	  if (clicked && isOver(mx, my)) {
		  overed=false;
		  dragging = true;
	  }

	  return dragging;
  }   
	  
  public boolean cancelClick() {
	  if(!enabled)return false;
	  dragging=false;
	  clicked =false;
	  overed=false;
	  return true;
  }

  public boolean isON() {
	  return ON;
  }

  public void setON(boolean oN) {
	  ON = oN;
  }

  public String getText() {
	  return text;
  }

  public void setText(String text) {
	  this.text = text;
  }

  public String getActiveText() {
	  return activeText;
  }

  public void setActiveText(String activeText) {
	  this.activeText = activeText;
  }

  public boolean isBlinkWhenOn() {
	  return blinkWhenOn;
  }

  public void setBlinkWhenOn(boolean blinkWhenOn) {
	  this.blinkWhenOn = blinkWhenOn;
  }

  public boolean isBlinkOn() {
	  return blinkOn;
  }

  public void setBlinkOn(boolean blinkOn) {
	  this.blinkOn = blinkOn;
  }   

}