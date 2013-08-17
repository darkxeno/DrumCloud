package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class VerticalSlider extends Draggable{

  float divisions=10;
  float divHeight=0.0f;
  int outStroke=3;
  private String text="";
  
  public VerticalSlider(float x, float y, float w, float h) {
    super(x,y,w,h);
    expandedFactor=1.2f;
  }
  
  public void limitY(float min,float max){
    limitedY=true;
    minY=min;
    maxY=max;
    calculateClickZone();
    divHeight=(maxYZone-minYZone)/divisions;
    //DrumCloud.println("totalH:"+(maxYZone-minYZone)+" divHeight:"+divHeight);    
  } 
 
  public float normalizedValue(){
    return DrumCloud.map(getY(), minY, maxY, 1.0f, 0.0f);  
  }
  
  public void draw() {
    
    DrumCloud.X.strokeWeight(outStroke);
    DrumCloud.X.stroke(100,100,100,255);
    DrumCloud.X.fill(DrumCloud.map(normalizedValue(),0,1,50,127),255);
    DrumCloud.X.rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);
    DrumCloud.X.strokeWeight(2);
    DrumCloud.X.stroke(255,255,255,255);
    for(int i=1;i<divisions;i++){
      float yPos=minYZone+i*divHeight;
      if(i%2==0){
        DrumCloud.X.line(minXZone+outStroke,yPos,minXZone+getW()*.5f,yPos);
        DrumCloud.X.line(maxXZone-getW()*.5f,yPos,maxXZone-outStroke,yPos);      
      }
      else{
        DrumCloud.X.line(minXZone+outStroke,yPos,minXZone+getW()*.4f,yPos);
        DrumCloud.X.line(maxXZone-getW()*.4f,yPos,maxXZone-outStroke,yPos);      
      }
    }  
    DrumCloud.X.strokeWeight(outStroke);  
    DrumCloud.X.stroke(200);
    if (isDragging()) DrumCloud.X.fill (50,180);
    else if (rollover) DrumCloud.X.fill(140,180);
    else DrumCloud.X.fill(100,180);
    DrumCloud.X.rect(getX(),getY(),getW(),getH());
    DrumCloud.X.fill(200);
    //textSize(20);
    //text(round(normalizedValue()*100)+"%",x+w*.5,maxYZone+h*.7);
    DrumCloud.X.textSize(FontAdjuster.getSize(20));
    float posY;
    if(DrumCloud.isAndroidDevice())
      posY=getY()+getH()*.73f;
    else
      posY=getY()+getH()*.785f;
    DrumCloud.X.text(DrumCloud.round(normalizedValue()*100),getX()+getW()*.5f,posY);
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
//The MIT License (MIT)