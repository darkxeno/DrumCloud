public class BeatKnob extends Knob{


  int[] beatValues={1,2,4,8,16,32};
  
  public BeatKnob(float x, float y, float w, float h) {
    super(x,y,w,h);
    fakeValue=maxY-(maxY-minY)*0.5; 
  }

  public int beatValue(){
    float incValue=1.001/beatValues.length;
    int pos=floor(normalizedValue()/incValue);
    //println("Beat knob at:"+pos+" incValue:"+incValue);
    return beatValues[pos];
  }
  
  
  void draw() {
    stroke(150,255);
    fill(150,255);
    strokeWeight(outStroke);
    arc(x,y,w,h,radians(75),radians(105),PIE); 
    stroke(200);
    if (dragging) fill (50,180);
    else if (overed) fill(140,180);
    else fill(100,180);
    arc(x,y,w,h,radians(115),radians(425),PIE);
    angleValue=map(normalizedValue(),0,1,0,310);
    fill(fillColor);
    //stroke(100,255);
    arc(x,y,w,h,radians(65-angleValue),radians(65),PIE);
    float posY;
    if(DrumMachine.isAndroidDevice)
      posY=y+h*.55;
    else
      posY=y+h*.63;
    if(innerRadius>0){
      fill(backColor);
      arc(x,y,w*innerRadius,h*innerRadius,radians(115),radians(425),OPEN);
    }    
    fill(200);
    textSize(FontAdjuster.getSize(20));
    textSize(FontAdjuster.getSize(15));
    textAlign(CENTER);    
    if(textOnCenter){
      fill(100);
      textSize(FontAdjuster.getSize(18));
      text(round(beatValue()),x,y+h*0.11);      
      fill(200);      
    }else{
      text(round(beatValue()),x,y-h*0.29);   
    }
    textSize(FontAdjuster.getSize(12));     
    textAlign(LEFT);
    text(round(minValue),x+w*.2,posY);
    textAlign(RIGHT);
    text(round(maxValue),x-w*.2,posY);
    text(text,x+w*.5,y+h*.5);    
  }   
}
