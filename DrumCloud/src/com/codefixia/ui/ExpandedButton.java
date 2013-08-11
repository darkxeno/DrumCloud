package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class ExpandedButton extends Clickable{
  
  /**
	 * 
	 */
	private final DrumCloud drumCloud;
String text="";
  
  public ExpandedButton(DrumCloud drumCloud, float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
	this.drumCloud = drumCloud;
  }
  
  public void draw(){
    this.drumCloud.fill(this.drumCloud.red(getFillColor()),this.drumCloud.green(getFillColor()),this.drumCloud.blue(getFillColor()),100);   
    this.drumCloud.rect(getX(), getY(), getW(), getH());     
  }
  
  public void drawCommonContent() {
    this.drumCloud.textSize(FontAdjuster.getSize(20));
    this.drumCloud.textAlign(DrumCloud.CENTER);
    this.drumCloud.fill(200);
    this.drumCloud.text(text, getX()+(getW()*0.5f), getY()+(getH()*0.7f));    
  }

}