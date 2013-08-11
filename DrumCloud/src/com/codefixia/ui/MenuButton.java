package com.codefixia.ui;

import processing.core.PApplet;
import processing.core.PConstants;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class MenuButton extends Clickable{
	  
	  String text="...";
	  String activeText;
	  
	  boolean ON=false;
	  boolean blinkWhenOn=false;
	  float blinkIntervalMS=500;
	  boolean blinkOn=false;
	  boolean released=false;
	  boolean triangleShape=true;	  

	  public boolean isTriangleShape() {
		  return triangleShape;
	  }

	  public void setTriangleShape(boolean triangleShape) {
		  this.triangleShape = triangleShape;
	  }

	  public MenuButton(float tempX, float tempY, float tempW, float tempH) {
		  super(tempX,tempY,tempW,tempH);
	  }
	  
	  public void drawState(){
	    if(clicked){
	      drawOvered();
	    }else{
	      drawNormal();
	    }
	  }
	  
	  public void drawOvered(){
		DrumCloud.X.stroke(strokeColor);
		DrumCloud.X.fill(DrumCloud.X.red(fillColor),DrumCloud.X.green(fillColor),DrumCloud.X.blue(fillColor),200);
		if(triangleShape)
			DrumCloud.X.triangle(x, y, x+w, y, x+w, y+h);
		else
			DrumCloud.X.rect(x, y, w, h);
	    drawCommonContent();  
	  }
	  
	  public void drawNormal(){
		DrumCloud.X.stroke(strokeColor);
		DrumCloud.X.fill(DrumCloud.X.red(fillColor)-100,DrumCloud.X.green(fillColor)-100,DrumCloud.X.blue(fillColor)-100,200);
		if(triangleShape)
			DrumCloud.X.triangle(x, y, x+w, y, x+w, y+h);
		else
			DrumCloud.X.rect(x, y, w, h);		
	    drawCommonContent();
	  }  
	  
	  public void drawCommonContent() {
		DrumCloud.X.textSize(FontAdjuster.getSize(20));
		DrumCloud.X.textAlign(PConstants.CENTER);
		DrumCloud.X.pushMatrix();
	    //translate(0,0);		
		if(triangleShape){
			DrumCloud.X.translate(x+(w*0.7f), y+(h*0.3f));
			DrumCloud.X.rotate(PApplet.radians(45));
		}
		else{
			DrumCloud.X.translate(x+(w*0.5f), y+(h*0.5f));
			DrumCloud.X.rotate(PApplet.radians(90));
		}
	    if(ON){
	    	DrumCloud.X.fill(255);
	      if(activeText!=null)
	    	  DrumCloud.X.text(activeText,0,0);
	      else
	    	  DrumCloud.X.text(text,0,0);
	    }
	    else{
	    	DrumCloud.X.fill(200);
	    	DrumCloud.X.text(text,0,0);
	    }    
	    DrumCloud.X.popMatrix();
	  }
	  
	  public boolean isClicked(int mx, int my) {
	    if (isOver(mx, my)) {
	      clicked = true;
	      setOffsetX(x-mx);
	      setOffsetY(y-my);
	      ON=!ON;
	    }
	    else {
	      clicked =false;
	    }

	    return clicked;
	  }
	  
	  boolean isReleased(int mx, int my) {
	    if (isOver(mx, my)) {
	      released = true;
	      setOffsetX(x-mx);
	      setOffsetY(y-my);
	      ON=!ON;
	    }
	    else {
	      released =false;
	    }
	    overed=false;    
	    clicked =false;

	    return released;
	  }
	    
	    
	  boolean cancelClick(int mx, int my) {
	      clicked =false;
	      overed=false;
	      return true;
	  }   

	}
