class HorizontalSlider extends Draggable{

  float divisions=10;
  float divWidth=0.0;
  int outStroke=3;
  String text="";
  float minValue=0.0,maxValue=1.0,defaultValue=0.5;
  
  HorizontalSlider(float x, float y, float w, float h) {
    super(x,y,w,h);
    //expandedFactor=1.2;
  }
  
  void limitX(float min,float max){
    limitedX=true;
    minX=min;
    maxX=max;
    calculateClickZone();
    divWidth=(maxXZone-minXZone)/divisions;
    println("totalW:"+(maxXZone-minXZone)+" divWidth:"+divWidth);    
  }
 
  void valuesX(float min,float max,float def){
    minValue=min;
    maxValue=max;
    defaultValue=def;
    x=map(def,minValue,maxValue,minX,maxX);   
  }
 
  float normalizedValue(){
    return map(x, minX, maxX, 0, 1);  
  }
  
  float value(){
    return map(x, minX, maxX, minValue, maxValue);  
  }  
  
  void draw() {
    
    strokeWeight(outStroke);
    stroke(100,100,100,255);
    fill(map(normalizedValue(),0,1,50,127),255);
    rect(minXZone,minYZone,maxX-minX+w,maxYZone-minYZone);
    strokeWeight(2);
    stroke(255,255,255,255);
    for(int i=1;i<divisions;i++){
      float xPos=minXZone+i*divWidth;
      if(i%2==0){
        line(xPos,minYZone+outStroke,xPos,minYZone+h*0.4);
        line(xPos,maxYZone-h*0.4,xPos,maxYZone-outStroke);     
      }
      else{
        line(xPos,minYZone+outStroke,xPos,minYZone+h*0.3);
        line(xPos,maxYZone-h*0.3,xPos,maxYZone-outStroke);     
      }
    }  
    strokeWeight(outStroke);  
    stroke(200);
    if (dragging) fill (50,180);
    else if (rollover) fill(140,180);
    else fill(100,180);
    rect(x,y+outStroke,w,h-2*outStroke);
    fill(200);
    //textSize(20);
    //text(round(normalizedValue()*100)+"%",x+w*.5,maxYZone+h*.7);
    textSize(FontAdjuster.getSize(20));
    float posY;
    if(DrumMachine.isAndroidDevice)
      posY=y+h*.7;
    else
      posY=y+h*.75;    
    text(round(value()),x+w*.5,posY);
    textSize(FontAdjuster.getSize(12));
    textLeading(FontAdjuster.getSize(12));
    text(text,x+w*.5,maxYZone+h*.5);    
  } 

}
