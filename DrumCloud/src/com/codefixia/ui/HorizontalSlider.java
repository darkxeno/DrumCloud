package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class HorizontalSlider extends Draggable{

  float divisions=10;
  float divWidth=0.0f;
  int outStroke=3;
  private String text="";
  float minValue=0.0f,maxValue=1.0f,defaultValue=0.5f;
  
  public HorizontalSlider(float x, float y, float w, float h) {
    super(x,y,w,h);
    //expandedFactor=1.2;
  }
  
  public void limitX(float min,float max){
    limitedX=true;
    minX=min;
    maxX=max;
    calculateClickZone();
    divWidth=(maxXZone-minXZone)/divisions;
    DrumCloud.println("totalW:"+(maxXZone-minXZone)+" divWidth:"+divWidth);    
  }
 
  public void valuesX(float min,float max,float def){
    minValue=min;
    maxValue=max;
    defaultValue=def;
    setX(DrumCloud.map(def,minValue,maxValue,minX,maxX));   
  }
 
  public float normalizedValue(){
    return DrumCloud.map(getX(), minX, maxX, 0, 1);  
  }
  
  public float value(){
    return DrumCloud.map(getX(), minX, maxX, minValue, maxValue);  
  }  
  
  public void draw() {
    
    DrumCloud.X.strokeWeight(outStroke);
    DrumCloud.X.stroke(100,100,100,255);
    DrumCloud.X.fill(DrumCloud.map(normalizedValue(),0,1,50,127),255);
    DrumCloud.X.rect(minXZone,minYZone,maxX-minX+getW(),maxYZone-minYZone);
    DrumCloud.X.strokeWeight(2);
    DrumCloud.X.stroke(255,255,255,255);
    for(int i=1;i<divisions;i++){
      float xPos=minXZone+i*divWidth;
      if(i%2==0){
        DrumCloud.X.line(xPos,minYZone+outStroke,xPos,minYZone+getH()*0.4f);
        DrumCloud.X.line(xPos,maxYZone-getH()*0.4f,xPos,maxYZone-outStroke);     
      }
      else{
        DrumCloud.X.line(xPos,minYZone+outStroke,xPos,minYZone+getH()*0.3f);
        DrumCloud.X.line(xPos,maxYZone-getH()*0.3f,xPos,maxYZone-outStroke);     
      }
    }  
    DrumCloud.X.strokeWeight(outStroke);  
    DrumCloud.X.stroke(200);
    if (isDragging()) DrumCloud.X.fill (50,180);
    else if (rollover) DrumCloud.X.fill(140,180);
    else DrumCloud.X.fill(100,180);
    DrumCloud.X.rect(getX(),getY()+outStroke,getW(),getH()-2*outStroke);
    DrumCloud.X.fill(200);
    //textSize(20);
    //text(round(normalizedValue()*100)+"%",x+w*.5,maxYZone+h*.7);
    DrumCloud.X.textSize(FontAdjuster.getSize(20));
    float posY;
    if(DrumCloud.isAndroidDevice())
      posY=getY()+getH()*.7f;
    else
      posY=getY()+getH()*.75f;    
    DrumCloud.X.text(DrumCloud.round(value()),getX()+getW()*.5f,posY);
    DrumCloud.X.textSize(FontAdjuster.getSize(12));
    DrumCloud.X.textLeading(FontAdjuster.getSize(12));
    DrumCloud.X.text(getText(),getX()+getW()*.5f,maxYZone+getH()*.5f);    
  }

public String getText() {
	return text;
}

public void setText(String text) {
	this.text = text;
} 

}