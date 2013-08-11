package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

import processing.core.PApplet;
import processing.core.PConstants;

public class BeatKnob extends Knob{


	  int[] beatValues={1,2,4,8,16,32};
	  
	  public BeatKnob(float x, float y, float w, float h) {
	    super(x,y,w,h);
	    fakeValue=maxY-(maxY-minY)*0.6f;
	    minValue=beatValues[0];
	    maxValue=beatValues[beatValues.length-1];
	  }

	  public int beatValue(){
	    float incValue=1.001f/beatValues.length;
	    int pos=PApplet.floor(normalizedValue()/incValue);
	    //println("Beat knob at:"+pos+" incValue:"+incValue);
	    return beatValues[pos];
	  }
	  
	  public void setBeatValuePosition(int position){
		if(position>0 && position<beatValues.length){
			fakeValue=maxY-((maxY-minY)/beatValues.length)*position;
		}  
	  }
	  
	  public void draw() {
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
	    float posY=y+h*.63f;
	    if(innerRadius>0){
	      DrumCloud.X.fill(backColor);
	      //DrumCloud.X.arc(x,y,w*innerRadius,h*innerRadius,PApplet.radians(115),PApplet.radians(425),PConstants.OPEN);
	      DrumCloud.X.ellipse(x,y,w*innerRadius,h*innerRadius);
	    }    
	    DrumCloud.X.fill(200);
	    DrumCloud.X.textSize(FontAdjuster.getSize(20));
	    DrumCloud.X.textSize(FontAdjuster.getSize(15));
	    DrumCloud.X.textAlign(PConstants.CENTER);    
	    if(textOnCenter){
	      DrumCloud.X.fill(100);
	      DrumCloud.X.textSize(FontAdjuster.getSize(18));
	      DrumCloud.X.text(PApplet.round(beatValue()),x,y+h*0.11f);      
	      DrumCloud.X.fill(200);      
	    }else{
	    	DrumCloud.X.text(PApplet.round(beatValue()),x,y-h*0.29f);   
	    }
	    DrumCloud.X.textSize(FontAdjuster.getSize(12));     
	    DrumCloud.X.textAlign(PConstants.LEFT);
	    DrumCloud.X.text(PApplet.round(minValue),x+w*.2f,posY);
	    DrumCloud.X.textAlign(PConstants.RIGHT);
	    DrumCloud.X.text(PApplet.round(maxValue),x-w*.2f,posY);
	    DrumCloud.X.text(text,x+w*.5f,y+h*.5f);    
	  }   
	}
