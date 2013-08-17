package com.codefixia.ui;

import processing.core.PApplet;
import processing.core.PConstants;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class AnimatedBeatKnob extends BeatKnob{
	
	private DrumCloud drumCloud;
	private int startGrid=0;
	private final int maxGrids=32;
	private final float gridAngle=360/maxGrids;
	private float angleOffset=0;
	private float colorIntensity=0;
	private int stateColor=0;
	private float angleWidth=0;
	private boolean showTextIndicators=false;
	private boolean animating=false;

	public AnimatedBeatKnob(float x, float y, float w, float h,DrumCloud dCloud) {
		super(x, y, w, h);DrumCloud.X.millis();
		this.drumCloud=dCloud;
	}	

	public boolean isAnimating() {
		return animating;
	}

	public void setAnimating(boolean animating) {
		this.animating = animating;
	}


	public void setStartGrid(int startGrid) {
		this.startGrid = startGrid;
	}

	public int getStartGrid() {
		return startGrid;
	}

	public void draw() {
		if(animating){
			angleOffset=PApplet.map((drumCloud.getPlayedMS())%drumCloud.getTempoMS(), 0.0f, drumCloud.getTempoMS(), 450,90);			
			angleOffset+=startGrid*gridAngle;			
		}
		angleWidth=(360.0f/(float)maxGrids)*(beatValue()/2.0f);
		DrumCloud.X.stroke(150,255);
		DrumCloud.X.fill(150,255);
		DrumCloud.X.strokeWeight(outStroke);
		DrumCloud.X.ellipse(x,y,w,h); 
		DrumCloud.X.stroke(200);
		int times=maxGrids/beatValue();
		//PApplet.println("startGrid:"+startGrid+" angleWidth:"+angleWidth);
		for(int i=0;i<times;i++){
			float currentOffset=angleOffset+(i*angleWidth*2);
			colorIntensity=PApplet.map(currentOffset,90,450,256,0); 
			DrumCloud.X.fill(fillColor,colorIntensity);
			DrumCloud.X.arc(x,y,w,h,PApplet.radians(currentOffset),PApplet.radians(currentOffset+angleWidth),PConstants.PIE);
		}		
		angleValue=PApplet.map(normalizedValue(),0,1,0,360);
		if (dragging) stateColor=DrumCloud.X.color(50,180);
		else if (overed) stateColor=DrumCloud.X.color(140,180);
		else stateColor=DrumCloud.X.color(100,180);		
		DrumCloud.X.fill(stateColor);
		DrumCloud.X.ellipse(x,y,w*0.7f,h*0.7f);
		DrumCloud.X.fill(fillColor);
		DrumCloud.X.line(x, y+h*0.35f, x, y+h*0.5f);
		//stroke(100,255);
		DrumCloud.X.arc(x,y,w*0.7f,h*0.7f,PApplet.radians(360-angleValue),PApplet.radians(360),PConstants.PIE);
		if(innerRadius>0){
			DrumCloud.X.fill(backColor);
			DrumCloud.X.ellipse(x,y,w*innerRadius,h*innerRadius);
		}
		drawCenterText();
		if(showTextIndicators){
			drawTextIndicators();
		}
		DrumCloud.X.stroke(fillColor);
		DrumCloud.X.strokeWeight(3);
		DrumCloud.X.line(x, y+h*0.35f, x, y+h*0.5f);
		DrumCloud.X.noFill();
		DrumCloud.X.arc(x,y,w*0.69f,h*0.69f,PApplet.radians(80),PApplet.radians(100));
		DrumCloud.X.arc(x,y,w*1.08f,h*1.08f,PApplet.radians(80),PApplet.radians(100));
	}  
	public void drawCenterText(){
		DrumCloud.X.fill(50);
		DrumCloud.X.textSize(FontAdjuster.getSize(20));
		DrumCloud.X.textSize(FontAdjuster.getSize(15));
		DrumCloud.X.textAlign(PConstants.CENTER);
		if(textOnCenter){
			DrumCloud.X.textSize(FontAdjuster.getSize(18));
			DrumCloud.X.text(PApplet.round(beatValue()),x,y+h*0.11f);            
		}else{
			DrumCloud.X.text(PApplet.round(beatValue()),x,y-h*0.29f);   
		}		
	}
	
	public void drawTextIndicators(){
		float posY=h*.1f;		    
		DrumCloud.X.textSize(FontAdjuster.getSize(12));     
		DrumCloud.X.textAlign(PConstants.LEFT);
		DrumCloud.X.text(PApplet.round(minValue),x+w*.56f,y-posY);
		DrumCloud.X.textAlign(PConstants.LEFT);
		DrumCloud.X.text(PApplet.round(maxValue),x+w*.56f,y+posY);
		DrumCloud.X.text(text,x+w*.5f,y+h*.5f);  		
	}
}	
