class VerticalSlider extends Draggable{

  float divisions=10;
  float divHeight=0.0;
  int outStroke=3;
  String text="";
  
  VerticalSlider(float x, float y, float w, float h) {
    super(x,y,w,h);
    expandedFactor=1.2;
  }
  
  void limitY(float min,float max){
    limitedY=true;
    minY=min;
    maxY=max;
    calculateClickZone();
    divHeight=(maxYZone-minYZone)/divisions;
    println("totalH:"+(maxYZone-minYZone)+" divHeight:"+divHeight);    
  } 
 
  float normalizedValue(){
    return map(y, minY, maxY, 1.0, 0.0);  
  }
  
  void draw() {
    
    strokeWeight(outStroke);
    stroke(100,100,100,255);
    fill(map(normalizedValue(),0,1,50,127),255);
    rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);
    strokeWeight(2);
    //stroke(150);
    stroke(255,255);
    for(int i=1;i<divisions;i++){
      float yPos=minYZone+i*divHeight;
      if(i%2==0){
        line(minXZone+outStroke,yPos,minXZone+w*.5,yPos);
        line(maxXZone-w*.5,yPos,maxXZone-outStroke,yPos);      
      }
      else{
        line(minXZone+outStroke,yPos,minXZone+w*.4,yPos);
        line(maxXZone-w*.4,yPos,maxXZone-outStroke,yPos);      
      }
    }  
    strokeWeight(outStroke);  
    stroke(200);
    if (dragging) fill (50,180);
    else if (rollover) fill(140,180);
    else fill(100,180);
    rect(x,y,w,h);
    fill(200);
    //textSize(20);
    //text(round(normalizedValue()*100)+"%",x+w*.5,maxYZone+h*.7);
    textSize(FontAdjuster.getSize(20));
    float posY;
    if(DrumMachine.isAndroidDevice)
      posY=y+h*.73;
    else
      posY=y+h*.785;
    text(round(normalizedValue()*100),x+w*.5,posY);
    textSize(FontAdjuster.getSize(12));
    textLeading(FontAdjuster.getSize(12));
    text(text,x+w*.5,maxYZone+h*.5);    
  } 

}
