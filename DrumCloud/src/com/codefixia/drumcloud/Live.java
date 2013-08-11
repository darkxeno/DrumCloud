package com.codefixia.drumcloud;



import processing.core.PApplet;
import processing.core.PVector;


public class Live {

  private final DrumCloud drumCloud;

  Live(DrumCloud drumCloud) {
    this.drumCloud = drumCloud;
  }



  public void setup() {

  }



  public void draw() {
  
  }

  public void mousePressed(float x, float y) {
    mousePressed((int)x, (int)y);
  }  

  public void mousePressed() {
    mousePressed(drumCloud.mouseX, drumCloud.mouseY);
  }

  public void mousePressed(int mouseX, int mouseY) {

  }

  public void mouseReleased(float x, float y) {

    mouseReleased((int)x, (int)y);
  }  

  public void mouseReleased() {    
    mouseReleased(drumCloud.mouseX, drumCloud.mouseY);
  }


  public void mouseReleased(int mouseX, int mouseY) {

  }


  public void debug() {

  }



  public void mouseDragged() {
    //println("Drag on sequencer mx:"+mouseX+" my:"+mouseY);

  }

  public void mouseMoved() {
    PApplet.println("Moved on sequencer px:"+drumCloud.pmouseX+" py:"+drumCloud.pmouseY+" mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
  }

}

