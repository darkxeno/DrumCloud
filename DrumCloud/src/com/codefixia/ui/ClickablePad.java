package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class ClickablePad extends Clickable{

	private String text="";
  
  public ClickablePad(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  public void draw(){
    DrumCloud.X.fill(DrumCloud.X.red(getFillColor()),DrumCloud.X.green(getFillColor()),DrumCloud.X.blue(getFillColor()),100);   
    DrumCloud.X.rect(getX(), getY(), getW(), getH());     
  }
  
  public void drawCommonContent(){
    DrumCloud.X.textSize(FontAdjuster.getSize(10));
    DrumCloud.X.textAlign(DrumCloud.CENTER);
    DrumCloud.X.fill(200);
    DrumCloud.X.text(getText(), getX()+(getW()*0.15f), getY()-(getH()*0.03f));   
  }

public String getText() {
	return text;
}

public void setText(String text) {
	this.text = text;
}

}
// Click and Drag an object
// Daniel Shiffman 