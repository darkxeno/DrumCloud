package com.codefixia.ui;

import processing.core.PApplet;
import processing.core.PConstants;
import android.R.color;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class PressZones extends Clickable {

	  private int touchId=-1;
	  private int lastZoneSelected=-1;

	  float animationTime=0.25f;

	  private String subZoneTexts[];
	  private boolean showOtherZones=true; 
	  private boolean dragReEnterEnabled=true;  
	  private int startGrid=0;
	  float subZoneHeight;

	  String text="";
	  private int showTime;

	  public PressZones(float tempX, float tempY, float tempW, float tempH) {
	    super(tempX, tempY, tempW, tempH);
	    setOffsetY(0);
	  }

	  public int getStartGrid() {
		  return startGrid;
	  }

	  public int getLastZoneSelected() {
		  PApplet.println("lastZoneSelected:"+lastZoneSelected);
		  return lastZoneSelected;
	  }

	  public void setLastZoneSelected(int lastZoneSelected) {
		  this.lastZoneSelected = lastZoneSelected;
	  }

	  public float normalizedValue(){
		  if(clicked){
			  return PApplet.map(getOffsetY(),0,h,1,0);
		  }
		  else return 0.0f;
	  }

	  public void draw() {
	    DrumCloud.X.fill(DrumCloud.X.red(fillColor), DrumCloud.X.green(fillColor), DrumCloud.X.blue(fillColor), 100);   
	    DrumCloud.X.rect(x, y, w, h);
	  }

	  public void drawCommonContent() {
	    //println("x:"+x+" y:"+y+" w:"+w+" h:"+h);
		DrumCloud.X.textSize(FontAdjuster.getSize(20));
	    DrumCloud.X.textAlign(PConstants.CENTER);
	    DrumCloud.X.fill(200);
	    DrumCloud.X.text(text, x+(w*0.5f), y+(h*0.7f));
	    if(showOtherZones)
	      drawSubZones();    
	  }
	  
	  public void drawClicked(){
	    drawNormal();
	    DrumCloud.X.ellipseMode(PConstants.CENTER);
	    DrumCloud.X.stroke(200);
	    DrumCloud.X.noFill();
	    DrumCloud.X.ellipse(x+getOffsetX(), y+getOffsetY(), DrumCloud.X.width*0.1f, DrumCloud.X.width*0.1f);    
	  }
	  
	  public void drawNormal(){
		  DrumCloud.X.stroke(getStrokeColor());
		  DrumCloud.X.fill(DrumCloud.X.red(getFillColor()),DrumCloud.X.green(getFillColor()),DrumCloud.X.blue(getFillColor()),200);  
		  DrumCloud.X.rect(getX(),getY(),getW(),getH());
		  drawCommonContent();
		  DrumCloud.X.stroke(255,127);
		  DrumCloud.X.fill(255,187);
		  //DrumCloud.X.line(x+getOffsetX()-DrumCloud.X.width*0.02f, y+getOffsetY(), x+getOffsetX()+DrumCloud.X.width*0.02f,y+getOffsetY());
		  //DrumCloud.X.line(x+getOffsetX(), y+getOffsetY()-DrumCloud.X.width*0.02f, x+getOffsetX(),y+getOffsetY()+DrumCloud.X.width*0.02f);
		  DrumCloud.X.ellipse(x+getOffsetX(), y+getOffsetY(), DrumCloud.X.width*0.05f, DrumCloud.X.width*0.05f);
	  }	  
	  
	  public void drawOvered(){
	    drawNormal();    
	  }  

	  public void setSubZoneTexts(String[] texts) {
	    if (texts.length>0) {
	      subZoneTexts=texts;
	      subZoneHeight=h/(float)texts.length;
	    }
	  }
	  
	  void setSubZoneColors(color[] colors) {
	    if (subZoneTexts.length>0) {
	      for (int i=0;i<subZoneTexts.length;i++) {
	        
	      }
	    }
	  }  

	  void drawSubZones(){
		  DrumCloud.X.fill(150);
		  DrumCloud.X.stroke(150);
		  DrumCloud.X.textSize(FontAdjuster.getSize(30));		  
		  DrumCloud.X.textAlign(PConstants.CENTER);
		  
	      for (int i=0;i<subZoneTexts.length;i++) {
	    	  DrumCloud.X.text(subZoneTexts[i],x+w*0.5f,y+(i*subZoneHeight+(subZoneHeight*0.6f)));
	    	  if(i!=0)
	    		  DrumCloud.X.line(x,y+i*subZoneHeight,x+w,y+i*subZoneHeight);
	      }
	  }
	  
	  public int isZoneSelected(int id,int mx, int my) {
		if(touchId!=id){
			return -1;
		}
		else{ 
			if(clicked){
				//println("isOver(mx, my):"+isOver(mx, my));
				if (false && !isOver(mx, my)){
					clicked=false;
					PApplet.println("Moved out of zone");
					return 0;
				}else{
					//PApplet.println("Updating presszone position");
					setOffsetX(PApplet.constrain(mx-x, 0, w));
					setOffsetY(PApplet.constrain(my-y, 0, h));
					lastZoneSelected=(int) (offsetY/((h+0.001f)/(float)subZoneTexts.length));
					return lastZoneSelected;
				}
			}else{
				if(touchId==id && dragReEnterEnabled && isOver(mx, my)){
					clicked=true;
				}
			}	
			lastZoneSelected=-1;
			return lastZoneSelected;
		}
	  }

	  public boolean isClickStarted(int id,int mx, int my,int gridPos) {		  
		if (isOver(mx, my)) {
			  if(!clicked){
				  clicked = true;
				  setOffsetX(PApplet.constrain(mx-x, 0, w));
				  setOffsetY(PApplet.constrain(my-y, 0, h));
				  touchId=id;
				  startGrid=gridPos;
				  showTime=DrumCloud.X.millis();
				  return true;
			  }
			  return false;
	    }
	    else {
	      clicked =false;
	    }	    

	    return clicked;
	  } 
	  
	  public boolean isClickStopped(int id){
		if(touchId!=id)
			return false;
		else{
			touchId=-1;
			if(clicked){
				clicked=false;
				return true;
			}else{
				clicked=false;
				return false;    
			}
		}
	  }

}
