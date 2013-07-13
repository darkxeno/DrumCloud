public class ToggleButton extends Clickable{
  
  String text="";
  String activatedText;
  
  boolean ON=false;
  boolean blinkWhenOn=false;
  float blinkIntervalMS=500;
  boolean blinkOn=false;
  
  public ToggleButton(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  void drawState(){
    if(ON){
      drawActivated();
    }else if(overed){
      drawOvered();
    }else{
      drawNormal();
    }
  }
  
  void drawActivated(){
    if(blinkWhenOn){
      if(millis()%(blinkIntervalMS*2)<blinkIntervalMS){
        blinkOn=true;  
        stroke(255);
      }
      else{
        blinkOn=false;
        stroke(strokeColor);
      }     
    }    
    fill(red(fillColor),green(fillColor),blue(fillColor),255);
    rect(x,y,w,h);
    drawCommonContent();  
  }
  
  void drawNormal(){
    stroke(strokeColor);
    fill(red(fillColor)-100,green(fillColor)-100,blue(fillColor)-100,200);  
    rect(x,y,w,h);
    drawCommonContent();
  }  
  
  void drawCommonContent() {
    textSize(FontAdjuster.getSize(20));
    textAlign(CENTER);
    if(ON){
      fill(255);
      if(activatedText!=null)
        text(activatedText, x+(w*0.5), y+(h*0.7));
      else
        text(text, x+(w*0.5), y+(h*0.7));
    }
    else{
      fill(200);
      text(text, x+(w*0.5), y+(h*0.7));
    }    
  }
  
  boolean isClicked(int mx, int my) {
    if (isOver(mx, my)) {
      clicked = true;
      offsetX = x-mx;
      offsetY = y-my;
      ON=!ON;
    }
    else {
      clicked =false;
    }

    return clicked;
  }   

}
