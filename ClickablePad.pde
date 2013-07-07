public class ClickablePad extends Clickable{
  
  String text="";
  
  public ClickablePad(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  void draw(){
    fill(red(fillColor),green(fillColor),blue(fillColor),100);   
    rect(x, y, w, h);     
  }
  
  void drawCommonContent(){
    textSize(FontAdjuster.getSize(10));
    textAlign(CENTER);
    fill(200);
    text(text, x+(w*0.15), y-(h*0.03));   
  }

}
