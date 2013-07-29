public class ToggleButton extends Clickable{
  
  String text="";
  String activatedText;
  
  boolean ON=false;
  boolean blinkWhenOn=false;
  float blinkIntervalMS=500;
  boolean blinkOn=false;
  boolean released=false;
  boolean dragging=false;
  
  public ToggleButton(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  public void drawState(){
    if(ON){
      drawActivated();
    }else if(overed){
      drawOvered();
    }else{
      drawNormal();
    }
  }
  
  public void drawActivated(){
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
  
  public void drawNormal(){
    stroke(strokeColor);
    fill(red(fillColor)-100,green(fillColor)-100,blue(fillColor)-100,200);  
    rect(x,y,w,h);
    drawCommonContent();
  }  
  
  public void drawCommonContent() {
    textSize(FontAdjuster.getSize(20));
    textAlign(CENTER);
    if(ON){
      fill(255);
      if(activatedText!=null)
        text(activatedText, x+(w*0.5f), y+(h*0.7f));
      else
        text(text, x+(w*0.5f), y+(h*0.7f));
    }
    else{
      fill(200);
      text(text, x+(w*0.5f), y+(h*0.7f));
    }    
  }
  
  public boolean isClicked(int mx, int my) {
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
  
  boolean isReleased(int mx, int my) {
    if (!dragging && isOver(mx, my)) {
      released = true;
      offsetX = x-mx;
      offsetY = y-my;
      ON=!ON;
    }
    else {
      released =false;
    }
    overed=false;    
    dragging=false;
    clicked =false;

    return released;
  }
  
  boolean isDragging(int mx, int my) {
    if (clicked && isOver(mx, my)) {
      overed=false;
      dragging = true;
    }

    return dragging;
  }   
    
  boolean cancelClick(int mx, int my) {
      dragging=false;
      clicked =false;
      overed=false;
      return true;
  }   

}
