package com.codefixia.drumcloud;

import java.util.LinkedList;

import processing.core.PApplet;

import com.codefixia.drumcloud.DrumCloud.ToggleButton;

public class Sequencer {

  /**
	 * 
	 */
	private final DrumCloud drumCloud;

	/**
	 * @param drumCloud
	 */
	Sequencer(DrumCloud drumCloud) {
		this.drumCloud = drumCloud;
	}

ToggleButton[][] tracks;

  float buttonWidth=0;
  float buttonHeight=0;

  float barOffset=0;

  float xOffset=0, yOffset=0;

  float totalHeight;
  float totalWidth;
  float visibleHeight;
  float visibleWidth;
  
  int lastClickX=5000;
  int lastClickY=5000;
  int displacementX=0;
  int displacementY=0;

  float verticalScrollBarOffset=0;
  float horizontalScrollBarOffset=0;
  float scrollBarYSize;
  float scrollBarXSize;
  float scrollBarWidth;
  float playBarWidth;
  
  LinkedList<Integer> mouseMovesX=new LinkedList<Integer>();
  LinkedList<Integer> mouseMovesY=new LinkedList<Integer>();
  float maxAccel=40;
  float velocityX=0;
  float velocityY=0;
  float friction=1;
  boolean released=false;

  private float zoom=1.0f;

  public void setup() {
    buttonWidth=(float)(drumCloud.width/drumCloud.totalSamples*2);
    buttonHeight=buttonWidth;//height/(totalGrids+1)*2;
    tracks=new ToggleButton[drumCloud.totalSamples][drumCloud.totalGrids+1];
    totalHeight=(drumCloud.totalGrids+1)*buttonHeight;
    totalWidth=(drumCloud.totalSamples)*buttonWidth;
    playBarWidth=(drumCloud.totalSamples)*buttonWidth;
    visibleHeight=drumCloud.height;
    visibleWidth=drumCloud.width;
    scrollBarYSize=(visibleHeight/totalHeight)*visibleHeight;
    scrollBarXSize=(visibleWidth/totalWidth)*visibleWidth;
    scrollBarWidth=drumCloud.width*0.02f;

    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=0;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j]=drumCloud.new ToggleButton(drumCloud.width-((i+1)*buttonWidth), j*buttonHeight, buttonWidth, buttonHeight);
        if (j==0) {
          tracks[i][j].text=(i%4)+1+"";
        }
        if (((j-1)/8)%2==0) {
          switch((int)i/4) {
          case 0:
            tracks[i][j].fillColor=drumCloud.redColor;
            break;
          case 1:
            tracks[i][j].fillColor=drumCloud.orangeColor;
            break;
          case 2:
            tracks[i][j].fillColor=drumCloud.blueColor;
            break;
          case 3:
            tracks[i][j].fillColor=drumCloud.greenColor;
            break;
          }
        }
        else {
          switch((int)i/4) {
          case 0:
            tracks[i][j].fillColor=drumCloud.color(drumCloud.red(drumCloud.redColor)*0.9f,drumCloud.green(drumCloud.redColor)*0.9f,drumCloud.blue(drumCloud.redColor)*0.9f);
            break;
          case 1:
            tracks[i][j].fillColor=drumCloud.color(drumCloud.red(drumCloud.orangeColor)*0.9f,drumCloud.green(drumCloud.orangeColor)*0.9f,drumCloud.blue(drumCloud.orangeColor)*0.9f);
            break;
          case 2:
            tracks[i][j].fillColor=drumCloud.color(drumCloud.red(drumCloud.blueColor)*0.9f,drumCloud.green(drumCloud.blueColor)*0.9f,drumCloud.blue(drumCloud.blueColor)*0.9f);
            break;
          case 3:
            tracks[i][j].fillColor=drumCloud.color(drumCloud.red(drumCloud.greenColor)*0.9f,drumCloud.green(drumCloud.greenColor)*0.9f,drumCloud.blue(drumCloud.greenColor)*0.9f);
            break;
          }          
        }
      }
    }
  }

  public void drawScrollBars() {
    drumCloud.fill(127, 127);
    drumCloud.noStroke();
    //println("YBar posY:"+yOffset+" restY"+(totalHeight-visibleHeight)+" sizeH:"+(visibleHeight/totalHeight)*visibleHeight);
    float scrollYPos=DrumCloud.map(yOffset, 0, totalHeight-visibleHeight, 0, visibleHeight-scrollBarYSize);
    drumCloud.rect(drumCloud.width*0.005f, scrollYPos, scrollBarWidth, scrollBarYSize);

    //println("XBar posX:"+xOffset+" restX"+(totalWidth-visibleWidth)+" sizeH:"+(totalWidth/visibleWidth)*visibleWidth);
    float scrollXPos=DrumCloud.map(xOffset, 0,-(totalWidth-visibleWidth), 0, visibleWidth-scrollBarXSize);  
    drumCloud.rect(drumCloud.width-scrollBarXSize-scrollXPos, drumCloud.height-(drumCloud.width*0.03f), scrollBarXSize, scrollBarWidth);
  }

  public void drawPlayBar() {
    drumCloud.fill(drumCloud.yellowColor);
    barOffset=DrumCloud.map((drumCloud.millis()-drumCloud.totalPaused)%drumCloud.tempoMS, 0.0f, drumCloud.tempoMS, buttonHeight, totalHeight-buttonHeight);
    //println("barOffset:"+barOffset+" val:"+(millis()-totalPaused)%tempoMS);
    drumCloud.rect(visibleWidth-playBarWidth, barOffset, totalWidth, buttonHeight);
  }
  
  public void updateTracksState(){
	  for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
		  for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
			  tracks[i][j].ON=drumCloud.samplesPerBeat[i][j-1];
		  }
	  }	  
  }  

  public void draw() {
	proccessDrawAccel();	  
    drumCloud.pushMatrix();
    drumCloud.translate(-xOffset, -yOffset);    
    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=0;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j].drawState();
      }
    }
    drawPlayBar();  
    drumCloud.popMatrix();
    drawScrollBars();
  }
  
  public void mousePressed(float x, float y) {
	  mousePressed((int)x,(int)y);
  }  
  
  public void mousePressed() {
	  mousePressed(drumCloud.mouseX,drumCloud.mouseY);
  }

  public void mousePressed(int mouseX,int mouseY) {
	lastClickX=mouseX;
	lastClickY=mouseY;
	released=false;
    DrumCloud.println("Press on sequencer mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
  }
  
  public void mouseReleased(float x, float y) {
	  displacementX=0;
	  displacementY=0;
	  mouseReleased((int)x,(int)y);
  }  
  
  public void mouseReleased() {	  
	  mouseReleased(drumCloud.mouseX,drumCloud.mouseY);
  }

  public void mouseReleased(int mouseX,int mouseY) {
    DrumCloud.println("Release on sequencer mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
    	for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
    		if(displacementX<buttonWidth*0.5 && displacementY<buttonHeight*0.5){//dist(mouseX,mouseY,lastClickX,lastClickY)<buttonWidth){
    			if (tracks[i][j].isReleased(mouseX+(int)xOffset,mouseY+(int)yOffset)) {
    				int soundGroup=(i/4)+((i%4)*4);
    				//println("From:"+i+" to:"+soundGroup);
    				drumCloud.samplesPerBeat[soundGroup][j-1]=!drumCloud.samplesPerBeat[soundGroup][j-1];

    			}
    		}else{
    			tracks[i][j].cancelClick(drumCloud.mouseX+(int)xOffset, +(int)yOffset);
    		}
    	}
    }
    displacementX=0;
    displacementY=0;
    released=true;
  }
  
  public void proccessDrawAccel(){
	  if(velocityY!=0 && released){
		  yOffset+=velocityY;		  
		  yOffset=DrumCloud.constrain(yOffset,0,totalHeight-visibleHeight);
		  if(velocityY>friction*2)
			  velocityY-=friction;
		  else if(velocityY<-friction*2)
			  velocityY+=friction;
		  else velocityY=0;
	  }
	  if(velocityX!=0 && released){
		  xOffset+=velocityX;		  
		  xOffset=DrumCloud.constrain(xOffset,-(totalWidth-visibleWidth),0);
		  if(velocityX>friction*2)
			  velocityX-=friction;
		  else if(velocityX<-friction*2)
			  velocityX+=friction;
		  else velocityX=0;
	  }	  
  }
  
  public void processDragAccel(int pmouseX,int pmouseY,int mouseX,int mouseY){
	  
	 	  
	    int distX=pmouseX-mouseX;
	    int distY=pmouseY-mouseY;
	    
	    xOffset+=distX;
	    xOffset=DrumCloud.constrain(xOffset, -(totalWidth-visibleWidth),0);
	    yOffset+=distY;
	    yOffset=DrumCloud.constrain(yOffset, 0, totalHeight-visibleHeight);	
	    
	    displacementX+=DrumCloud.abs(distX);
	    displacementY+=DrumCloud.abs(distY);
	    //println("DisX:"+displacementX+" DisY:"+displacementY);  
	    
	    
	    if(mouseMovesY.size()>=3){
	    	mouseMovesY.remove();
	    	mouseMovesX.remove();
	    }
		mouseMovesY.add(distY);
		mouseMovesX.add(distX);
		int size=mouseMovesY.size();
		float avgY = 0,avgX = 0;
		for(int i=0;i<size;i++){
			avgY+=(float)mouseMovesY.get(i);
			avgX+=(float)mouseMovesX.get(i);
		}
		avgY/=size;
		avgX/=size;
	    velocityY=DrumCloud.map(DrumCloud.constrain(avgY,-25,25),-25,25,-maxAccel,maxAccel);
	    velocityX=DrumCloud.map(DrumCloud.constrain(avgX,-25,25),-25,25,-maxAccel,maxAccel);  
  }

  public void mouseDragged() {
    //println("Drag on sequencer mx:"+mouseX+" my:"+mouseY);

	processDragAccel(drumCloud.pmouseX,drumCloud.pmouseY,drumCloud.mouseX,drumCloud.mouseY);
    
    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j].isDragging(drumCloud.mouseX+(int)xOffset, drumCloud.mouseY+(int)yOffset);
      }
    }
    released=false;
  }

  public void mouseMoved() {
    DrumCloud.println("Moved on sequencer px:"+drumCloud.pmouseX+" py:"+drumCloud.pmouseY+" mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
  }


public void changeZoom(int centerX,int centerY,float pinchAmount) {
	if( (pinchAmount>0 && this.zoom<1)||(pinchAmount<0 && this.zoom>0.5f) ){
		this.zoom=DrumCloud.constrain(this.zoom+pinchAmount * 0.003f, 0.5f, 1);
		//visibleHeight=drumCloud.height*this.zoom;
		//visibleWidth=drumCloud.width*this.zoom;
	    buttonWidth=(float)(drumCloud.width/drumCloud.totalSamples*2*this.zoom);
	    buttonHeight=buttonWidth;
		float newTotalHeight=(drumCloud.totalGrids+1)*buttonHeight;
		totalHeight=newTotalHeight;
		totalWidth=(drumCloud.totalSamples)*buttonWidth;
		if(totalHeight<visibleHeight)totalHeight=visibleHeight;
		if(totalWidth<visibleWidth)totalWidth=visibleWidth;
	    scrollBarYSize=(visibleHeight/totalHeight)*visibleHeight;
	    scrollBarXSize=(visibleWidth/totalWidth)*visibleWidth;		
		xOffset=PApplet.map(centerX,0,drumCloud.width,0,totalWidth);
		xOffset=DrumCloud.constrain(xOffset,-(totalWidth-visibleWidth),0);
		yOffset=PApplet.map(centerY,0,drumCloud.height,0,totalHeight);
		yOffset=DrumCloud.constrain(yOffset,0,totalHeight-visibleHeight);
		playBarWidth=(drumCloud.totalSamples)*buttonWidth;
		//PApplet.println("yOffset:"+yOffset+" visibleHeight:"+visibleHeight+" totalHeight:"+totalHeight);
		for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
			for (int j=0;j<drumCloud.samplesPerBeat[i].length+1;j++) {
				tracks[i][j].x=drumCloud.width-((i+1)*buttonWidth);
				tracks[i][j].y=buttonHeight*j;
				tracks[i][j].w=buttonWidth;
				tracks[i][j].h=buttonHeight;
			}
		}
	}
}



}