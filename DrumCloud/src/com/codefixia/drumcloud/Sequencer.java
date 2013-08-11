package com.codefixia.drumcloud;


import java.util.LinkedList;

import com.codefixia.ui.ToggleButton;

import processing.core.PApplet;
import processing.core.PVector;


public class Sequencer {

  private final DrumCloud drumCloud;

  Sequencer(DrumCloud drumCloud) {
    this.drumCloud = drumCloud;
  }

  ToggleButton[][] tracks;

  float buttonWidth=0;
  float buttonHeight=0;

  float barOffset=0,lastBarOffset=0;

  float xOffset=0, yOffset=0;

  float totalHeight;
  float maxTotalHeight;
  float totalWidth;
  float maxTotalWidth;
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

  int miniMapOriginX;
  int miniMapOriginY;
  float miniMapScale=0.1f;

  LinkedList<Integer> mouseMovesX=new LinkedList<Integer>();
  LinkedList<Integer> mouseMovesY=new LinkedList<Integer>();
  float maxAccel=40;
  float velocityX=0;
  float velocityY=0;
  float friction=1;
  boolean released=false;

  private float zoom=1.0f;

  public void setup() {
    buttonWidth=(float)(drumCloud.width/drumCloud.totalSamples);
    buttonHeight=buttonWidth;
    tracks=new ToggleButton[drumCloud.totalSamples][drumCloud.totalGrids+1];
    totalHeight=(drumCloud.totalGrids+1)*buttonHeight+drumCloud.height*0.05f;
    maxTotalHeight=totalHeight;
    totalWidth=(drumCloud.totalSamples)*buttonWidth;
    maxTotalWidth=totalWidth;
    playBarWidth=(drumCloud.totalSamples)*buttonWidth;
    visibleHeight=drumCloud.height;
    visibleWidth=drumCloud.width;
    scrollBarYSize=(visibleHeight/totalHeight)*visibleHeight;
    scrollBarXSize=(visibleWidth/totalWidth)*visibleWidth;
    scrollBarWidth=drumCloud.width*0.02f;
    miniMapOriginX=(int) (drumCloud.width*0.05f);
    miniMapOriginY=(int) (drumCloud.height*0.778f);  

    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=0;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j]=new ToggleButton(drumCloud.width-((i+1)*buttonWidth), j*buttonHeight, buttonWidth, buttonHeight);
        if (j==0) {
          tracks[i][j].setText((i%4)+1+"");
        }
        if (((j-1)/8)%2==0) {
          switch((int)i/4) {
          case 0:
            tracks[i][j].setFillColor(drumCloud.redColor);
            break;
          case 1:
            tracks[i][j].setFillColor(drumCloud.orangeColor);
            break;
          case 2:
            tracks[i][j].setFillColor(drumCloud.blueColor);
            break;
          case 3:
            tracks[i][j].setFillColor(drumCloud.greenColor);
            break;
          }
        }
        else {
          switch((int)i/4) {
          case 0:
            tracks[i][j].setFillColor(drumCloud.color(drumCloud.red(drumCloud.redColor)*0.9f, drumCloud.green(drumCloud.redColor)*0.9f, drumCloud.blue(drumCloud.redColor)*0.9f));
            break;
          case 1:
            tracks[i][j].setFillColor(drumCloud.color(drumCloud.red(drumCloud.orangeColor)*0.9f, drumCloud.green(drumCloud.orangeColor)*0.9f, drumCloud.blue(drumCloud.orangeColor)*0.9f));
            break;
          case 2:
            tracks[i][j].setFillColor(drumCloud.color(drumCloud.red(drumCloud.blueColor)*0.9f, drumCloud.green(drumCloud.blueColor)*0.9f, drumCloud.blue(drumCloud.blueColor)*0.9f));
            break;
          case 3:
            tracks[i][j].setFillColor(drumCloud.color(drumCloud.red(drumCloud.greenColor)*0.9f, drumCloud.green(drumCloud.greenColor)*0.9f, drumCloud.blue(drumCloud.greenColor)*0.9f));
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
    float scrollYPos=PApplet.map(yOffset, 0, (totalHeight-visibleHeight)/this.zoom, 0, visibleHeight-scrollBarYSize);
    drumCloud.rect(drumCloud.width*0.005f, scrollYPos, scrollBarWidth, scrollBarYSize);

    //println("XBar posX:"+xOffset+" restX"+(totalWidth-visibleWidth)+" sizeH:"+(totalWidth/visibleWidth)*visibleWidth);
    float scrollXPos=PApplet.map(xOffset, 0, (totalWidth-visibleWidth)/this.zoom, 0, visibleWidth-scrollBarXSize);  
    drumCloud.rect(scrollXPos, drumCloud.height-(drumCloud.width*0.025f), scrollBarXSize, scrollBarWidth);
  }

  public void drawPlayBar() {
	if (drumCloud.pausedMS<0) {  
		drumCloud.fill(drumCloud.yellowColor,180.0f);
    	barOffset=PApplet.map((drumCloud.millis()-drumCloud.totalPaused)%drumCloud.tempoMS, 0.0f, drumCloud.tempoMS, buttonHeight, maxTotalHeight);
    	lastBarOffset=barOffset;
    	//println("barOffset:"+barOffset+" val:"+(millis()-totalPaused)%tempoMS);
    	drumCloud.rect(visibleWidth-maxTotalWidth, barOffset, maxTotalWidth, buttonHeight);
	}else{
		drumCloud.fill(150,180.0f);
		drumCloud.rect(visibleWidth-maxTotalWidth, lastBarOffset, maxTotalWidth, buttonHeight);
	}   
  }

  public void updateState() {
    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j].setON(drumCloud.samplesPerBeat[i][j-1]);
      }
    }
  }  

  public void draw() {
    processDrawAccel();	  
    drumCloud.pushMatrix();
    drumCloud.scale(this.zoom, this.zoom);    
    drumCloud.translate(-xOffset, -yOffset);
    //drumCloud.translate(xScaleFix, 0);
    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=0;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j].drawState();
      }
    }    
    drawPlayBar();  
    drumCloud.popMatrix();
    drawScrollBars();
    drawMiniMap();
    //debug();
  }

  public void mousePressed(float x, float y) {
    mousePressed((int)x, (int)y);
  }  
  
  public void mousePressed(int mouseX, int mouseY) {
    lastClickX=mouseX;
    lastClickY=mouseY;
    released=false;
    PApplet.println("Press on sequencer mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
  }

  public void mouseReleased(float x, float y) {
    displacementX=0;
    displacementY=0;
    mouseReleased((int)x, (int)y);
  }  

  public PVector mouseToScreen(int x, int y) {
    return new PVector((x/this.zoom)+xOffset, (y/this.zoom)+yOffset);
  }

  public void mouseReleased(int mouseX, int mouseY) {
    PApplet.println("Release on sequencer mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
    if(mouseY>drumCloud.height*0.95f){
    	return;
    }
    PVector screenCoords=mouseToScreen(mouseX, mouseY);
    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        if (displacementX<buttonWidth*0.5 && displacementY<buttonHeight*0.5) {//dist(mouseX,mouseY,lastClickX,lastClickY)<buttonWidth){
          if (tracks[i][j].isReleased((int)screenCoords.x, (int)screenCoords.y)) {
            int soundGroup=(i/4)+((i%4)*4);
            //println("From:"+i+" to:"+soundGroup);
            drumCloud.samplesPerBeat[soundGroup][j-1]=!drumCloud.samplesPerBeat[soundGroup][j-1];
          }
        }
        else {
          tracks[i][j].cancelClick();
        }
      }
    }
    displacementX=0;
    displacementY=0;
    released=true;
  }


  public void debug() {
    drumCloud.text("OFFSET("+xOffset+","+yOffset+")", drumCloud.width*0.2f, drumCloud.height*0.1f);
    drumCloud.text("TOTAL("+totalWidth+","+totalHeight+")", drumCloud.width*0.25f, drumCloud.height*0.2f);
    drumCloud.text("VISIBLE("+visibleWidth+","+visibleHeight+")", drumCloud.width*0.25f, drumCloud.height*0.3f);
  }

  public void drawMiniMap() {
	drumCloud.rect(miniMapOriginX+xOffset*0.1f, miniMapOriginY+yOffset*0.1f, visibleWidth/this.zoom*0.1f, visibleHeight/this.zoom*0.1f);
	drumCloud.rect(miniMapOriginX, miniMapOriginY, maxTotalWidth*0.1f, maxTotalHeight*0.1f);  
    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        drumCloud.noFill();
        drumCloud.stroke(150);
        if (tracks[i][j].isON())
          drumCloud.rect(miniMapOriginX+tracks[i][j].getX()*miniMapScale, 
          miniMapOriginY+tracks[i][j].getY()*miniMapScale, 
          tracks[i][j].getW()*miniMapScale, tracks[i][j].getH()*miniMapScale);
      }
    }
  }  


  public void processDrawAccel() {
    if (velocityY!=0 && released) {
      yOffset+=velocityY;		  
      limitOffsetY();
      if (velocityY>friction*2)
        velocityY-=friction;
      else if (velocityY<-friction*2)
        velocityY+=friction;
      else velocityY=0;
    }
    if (velocityX!=0 && released) {
      xOffset+=velocityX;
      limitOffsetX();		  		  
      if (velocityX>friction*2)
        velocityX-=friction;
      else if (velocityX<-friction*2)
        velocityX+=friction;
      else velocityX=0;
    }
  }  
  
  public void limitOffsetX(){
      xOffset=PApplet.constrain(xOffset, 0, (totalWidth-visibleWidth)/this.zoom);  
  }
  
  public void limitOffsetY(){
      yOffset=PApplet.constrain(yOffset, 0, (totalHeight-visibleHeight)/this.zoom);  
  }  

  public void processDragAccel(int pmouseX, int pmouseY, int mouseX, int mouseY) {

    int distX=pmouseX-mouseX;
    int distY=pmouseY-mouseY;

    xOffset+=distX;
    limitOffsetX();
    yOffset+=distY;
    limitOffsetY();	

    displacementX+=PApplet.abs(distX);
    displacementY+=PApplet.abs(distY);
    //println("DisX:"+displacementX+" DisY:"+displacementY);  


    if (mouseMovesY.size()>=3) {
      mouseMovesY.remove();
      mouseMovesX.remove();
    }
    mouseMovesY.add(distY);
    mouseMovesX.add(distX);
    int size=mouseMovesY.size();
    float avgY = 0, avgX = 0;
    for (int i=0;i<size;i++) {
      avgY+=(float)mouseMovesY.get(i);
      avgX+=(float)mouseMovesX.get(i);
    }
    avgY/=size;
    avgX/=size;
    velocityY=PApplet.map(PApplet.constrain(avgY, -25, 25), -25, 25, -maxAccel, maxAccel);
    velocityX=PApplet.map(PApplet.constrain(avgX, -25, 25), -25, 25, -maxAccel, maxAccel);
  }

  public void mouseDragged(int pmouseX,int pmouseY,int mouseX,int mouseY) {
    //println("Drag on sequencer mx:"+mouseX+" my:"+mouseY);

    processDragAccel(pmouseX, pmouseY, mouseX, mouseY);

    for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j].isDragging(drumCloud.mouseX+(int)xOffset, drumCloud.mouseY+(int)yOffset);
      }
    }
    released=false;
  }

  public void mouseMoved() {
    PApplet.println("Moved on sequencer px:"+drumCloud.pmouseX+" py:"+drumCloud.pmouseY+" mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
  }


  public void changeZoom(int centerX, int centerY, float pinchAmount) {
    if ( (pinchAmount>0 && this.zoom<2.0)||(pinchAmount<0 && this.zoom>1.0f) ) {
      this.zoom=PApplet.constrain(this.zoom+pinchAmount * 0.003f, 1, 2.0f);
      float newButtonWidth=(float)(drumCloud.width/drumCloud.totalSamples*this.zoom);
      totalHeight=(drumCloud.totalGrids+1)*newButtonWidth+drumCloud.height*0.05f;
      playBarWidth=totalHeight;
      totalWidth=(drumCloud.totalSamples)*newButtonWidth;
      scrollBarYSize=(visibleHeight/totalHeight)*visibleHeight;
      scrollBarXSize=(visibleWidth/totalWidth)*visibleWidth;      
      limitOffsetX();
      limitOffsetY();
    }
  }
}
