public class Sequencer {

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
  
  LinkedList<Integer> mouseMovesX=new LinkedList<Integer>();
  LinkedList<Integer> mouseMovesY=new LinkedList<Integer>();
  float maxAccel=40;
  float velocityX=0;
  float velocityY=0;
  float friction=1;
  boolean released=false;

  public void setup() {
    buttonWidth=(float)(width/totalSamples*1.5);
    buttonHeight=buttonWidth;//height/(totalGrids+1)*2;
    tracks=new ToggleButton[totalSamples][totalGrids+1];
    totalHeight=(totalGrids+1)*buttonHeight;
    totalWidth=(totalSamples)*buttonWidth;
    visibleHeight=height;
    visibleWidth=width;
    scrollBarYSize=(visibleHeight/totalHeight)*visibleHeight;
    scrollBarXSize=(visibleWidth/totalWidth)*visibleWidth;
    scrollBarWidth=width*0.025f;

    for (int i=0;i<samplesPerBeat.length;i++) {
      for (int j=0;j<samplesPerBeat[i].length+1;j++) {
        tracks[i][j]=new ToggleButton(width-((i+1)*buttonWidth), j*buttonHeight, buttonWidth, buttonHeight);
        if (j==0) {
          tracks[i][j].text=(i%4)+1+"";
        }
        if (((j-1)/8)%2==0) {
          switch((int)i/4) {
          case 0:
            tracks[i][j].fillColor=redColor;
            break;
          case 1:
            tracks[i][j].fillColor=orangeColor;
            break;
          case 2:
            tracks[i][j].fillColor=blueColor;
            break;
          case 3:
            tracks[i][j].fillColor=greenColor;
            break;
          }
        }
        else {
          switch((int)i/4) {
          case 0:
            tracks[i][j].fillColor=color(red(redColor)*0.9f,green(redColor)*0.9f,blue(redColor)*0.9f);
            break;
          case 1:
            tracks[i][j].fillColor=color(red(orangeColor)*0.9f,green(orangeColor)*0.9f,blue(orangeColor)*0.9f);
            break;
          case 2:
            tracks[i][j].fillColor=color(red(blueColor)*0.9f,green(blueColor)*0.9f,blue(blueColor)*0.9f);
            break;
          case 3:
            tracks[i][j].fillColor=color(red(greenColor)*0.9f,green(greenColor)*0.9f,blue(greenColor)*0.9f);
            break;
          }          
        }
      }
    }
  }

  public void drawScrollBars() {
    fill(127, 127);
    noStroke();
    //println("YBar posY:"+yOffset+" restY"+(totalHeight-visibleHeight)+" sizeH:"+(visibleHeight/totalHeight)*visibleHeight);
    float scrollYPos=map(yOffset, 0, totalHeight-visibleHeight, 0, visibleHeight-scrollBarYSize);
    rect(width*0.005f, scrollYPos, scrollBarWidth, scrollBarYSize);

    //println("XBar posX:"+xOffset+" restX"+(totalWidth-visibleWidth)+" sizeH:"+(totalWidth/visibleWidth)*visibleWidth);
    float scrollXPos=map(xOffset, 0, totalWidth-visibleWidth, 0, visibleWidth-scrollBarXSize);  
    rect(width-scrollBarXSize-scrollXPos, height-(width*0.03f), scrollBarXSize, scrollBarWidth);
  }

  public void drawPlayBar() {
    fill(yellowColor);
    barOffset=map((millis()-totalPaused)%tempoMS, 0.0f, tempoMS, buttonHeight, totalHeight-buttonHeight);
    //println("barOffset:"+barOffset+" val:"+(millis()-totalPaused)%tempoMS);
    rect(visibleWidth-totalWidth, barOffset, totalWidth, buttonHeight);
  }
  
  public void proccessDrawAccel(){
    if(velocityY!=0 && released){
      yOffset+=velocityY;      
      yOffset=constrain(yOffset,0,totalHeight-visibleHeight);
      if(velocityY>friction*2)
        velocityY-=friction;
      else if(velocityY<-friction*2)
        velocityY+=friction;
      else velocityY=0;
    }
    if(velocityX!=0 && released){
      xOffset-=velocityX;      
      xOffset=constrain(xOffset,0,totalWidth-visibleWidth);
      if(velocityX>friction*2)
        velocityX-=friction;
      else if(velocityX<-friction*2)
        velocityX+=friction;
      else velocityX=0;
    }    
  }

  public void draw() {
  proccessDrawAccel();    
    pushMatrix();
    translate(xOffset, -yOffset);
    for (int i=0;i<samplesPerBeat.length;i++) {
      for (int j=0;j<samplesPerBeat[i].length+1;j++) {
        tracks[i][j].drawState();
      }
    }
    drawPlayBar();  
    popMatrix();
    drawScrollBars();
  }  

  public void mousePressed() {
  lastClickX=mouseX;
  lastClickY=mouseY;
  released=false;
    println("Press on sequencer mx:"+mouseX+" my:"+mouseY);
    /*for (int i=0;i<samplesPerBeat.length;i++) {
      for (int j=1;j<samplesPerBeat[i].length+1;j++) {
        tracks[i][j].isClicked(mouseX-(int)xOffset, mouseY+(int)yOffset);
      }
    }*/
  }

  public void mouseReleased() {
    println("Release on sequencer mx:"+mouseX+" my:"+mouseY);
    for (int i=0;i<samplesPerBeat.length;i++) {
      for (int j=1;j<samplesPerBeat[i].length+1;j++) {
        if(displacementX<buttonWidth*0.5 && displacementY<buttonHeight*0.5){//dist(mouseX,mouseY,lastClickX,lastClickY)<buttonWidth){
          if (tracks[i][j].isReleased(mouseX-(int)xOffset, mouseY+(int)yOffset)) {
            int soundGroup=(i/4)+((i%4)*4);
            //println("From:"+i+" to:"+soundGroup);
            samplesPerBeat[soundGroup][j-1]=!samplesPerBeat[soundGroup][j-1];

          }
        }else{
          tracks[i][j].cancelClick(mouseX-(int)xOffset, mouseY+(int)yOffset);
        }
      }
    }
    displacementX=0;
    displacementY=0;
    released=true;
  }
  
  public void proccessDragAccel(int pmouseX,int pmouseY,int mouseX,int mouseY){
    
      int distX=pmouseX-mouseX;
      int distY=pmouseY-mouseY;    
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
      displacementX+=abs(distX);
      displacementY+=abs(distY);
      //println("DisX:"+displacementX+" DisY:"+displacementY);  
      velocityY=map(constrain(avgY,-25,25),-25,25,-maxAccel,maxAccel);
      velocityX=map(constrain(avgX,-25,25),-25,25,-maxAccel,maxAccel);
      xOffset+=mouseX-pmouseX;
      xOffset=constrain(xOffset, 0, totalWidth-visibleWidth);
      yOffset+=pmouseY-mouseY;
      yOffset=constrain(yOffset, 0, totalHeight-visibleHeight);    
    
  }

  public void mouseDragged() {
    //println("Drag on sequencer mx:"+mouseX+" my:"+mouseY);

    proccessDragAccel(pmouseX,pmouseY,mouseX,mouseY);
    
    for (int i=0;i<samplesPerBeat.length;i++) {
      for (int j=1;j<samplesPerBeat[i].length+1;j++) {
        tracks[i][j].isDragging(mouseX-(int)xOffset, mouseY+(int)yOffset);
      }
    }
    released=false;
  }

  public void mouseMoved() {
    //println("Moved on sequencer px:"+pmouseX+" py:"+pmouseY+" mx:"+mouseX+" my:"+mouseY);
  }
}
