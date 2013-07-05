public class ClickablePad extends Clickable{
  
  String text="P";
  
  public ClickablePad(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  void draw(){
    fill(red(fillColor),green(fillColor),blue(fillColor),100);   
    rect(x, y, w, h);     
  }
  
  void drawCommonContent(){
    textSize(42);
    textAlign(CENTER);
    fill(50);
    text(text, x+(w*0.5), y+(h*0.7));   
  }

}
