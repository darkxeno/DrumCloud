

import processing.core.PApplet;


public class Automator {

  private final DrumMachine drumCloud;
  
  private PressZones kick;
  private PressZones bass;
  private PressZones snare;
  private PressZones hithat;
  private BeatKnob kickKnob;
  private BeatKnob bassKnob;
  private BeatKnob snareKnob;
  private BeatKnob hithatKnob;  
  private float zoneWidth,zoneHeight,zoneOriginX,zoneOriginY,zoneMarginX,zoneMarginY,knobOriginX,knobOriginY;

  Automator(DrumMachine drumCloud) {
    this.drumCloud = drumCloud;
  }



  public void setup() {
    zoneWidth=width*0.153;
    zoneHeight=height*0.52;
    zoneOriginX=width*0.131;
    zoneMarginX=buttonSize*0.29;
    zoneMarginY=buttonSize*0.275;
    
    knobOriginY=height*0.2;
    knobOriginX=zoneOriginX+zoneWidth*0.5;
    
    if (isAndroidDevice) {
      zoneOriginY=height*0.45;
    }
    else {
      zoneOriginY=height*0.445;
    }    
    kick=new PressZones(zoneOriginX,zoneOriginY,zoneWidth,zoneHeight);
    kick.fillColor=redColor;
    String[] kSubzones = { "K1", "K2", "K3", "K4" };
    kick.setSubZoneTexts(kSubzones);
    
    bass=new PressZones(zoneOriginX+(zoneWidth+zoneMarginX),zoneOriginY,zoneWidth,zoneHeight);
    bass.fillColor=orangeColor;
    String[] bSubzones  = { "B1", "B2", "B3", "B4" };
    bass.setSubZoneTexts(bSubzones);
     
    snare=new PressZones(zoneOriginX+2*(zoneWidth+zoneMarginX),zoneOriginY,zoneWidth,zoneHeight);
    snare.fillColor=blueColor;
    String[] sSubzones  = { "S1", "S2", "S3", "S4" };
    snare.setSubZoneTexts(sSubzones);    
    
    hithat=new PressZones(zoneOriginX+3*(zoneWidth+zoneMarginX),zoneOriginY,zoneWidth,zoneHeight);
    hithat.fillColor=greenColor;
    String[] hSubzones  = { "H1", "H2", "H3", "H4" };
    hithat.setSubZoneTexts(hSubzones);

    kickKnob=new BeatKnob(knobOriginX,knobOriginY,zoneWidth,zoneWidth);
    kickKnob.setFillColor(redColor);
    bassKnob=new BeatKnob(knobOriginX+1*(zoneWidth+zoneMarginX),knobOriginY,zoneWidth,zoneWidth);
    bassKnob.setFillColor(orangeColor);
    snareKnob=new BeatKnob(knobOriginX+2*(zoneWidth+zoneMarginX),knobOriginY,zoneWidth,zoneWidth);
    snareKnob.setFillColor(blueColor);
    hithatKnob=new BeatKnob(knobOriginX+3*(zoneWidth+zoneMarginX),knobOriginY,zoneWidth,zoneWidth);
    hithatKnob.setFillColor(greenColor);    
  }



  public void draw() {
    kick.drawState();
    bass.drawState();
    snare.drawState();
    hithat.drawState();
    kickKnob.draw();
    bassKnob.draw();
    snareKnob.draw();
    hithatKnob.draw();    
  }

  public void mousePressed(float x, float y) {
    mousePressed((int)x, (int)y);
  }  

  public void mousePressed() {
    mousePressed(drumCloud.mouseX, drumCloud.mouseY);
  }

  public void mousePressed(int mouseX, int mouseY) {
      if(kick.isClicked(mouseX,mouseY)){
        drumCloud.removeAllSoundsOfType(0);
        drumCloud.addSoundTypeRepeated(0,kickKnob.beatValue());
        controlVolume(kick.normalizedValue(), KICK);
        println("Added kick by:"+kickKnob.beatValue());
      }
      if(bass.isClicked(mouseX,mouseY)){
        drumCloud.removeAllSoundsOfType(1);
        drumCloud.addSoundTypeRepeated(1,bassKnob.beatValue());
        controlVolume(bass.normalizedValue(), BASS);
      }
      if(snare.isClicked(mouseX,mouseY)){
        drumCloud.removeAllSoundsOfType(2);
        drumCloud.addSoundTypeRepeated(2,snareKnob.beatValue()); 
        controlVolume(snare.normalizedValue(), SNARE);
      }
      if(hithat.isClicked(mouseX,mouseY)){
        drumCloud.removeAllSoundsOfType(3);
        drumCloud.addSoundTypeRepeated(3,hithatKnob.beatValue());
        controlVolume(hithat.normalizedValue(), HITHAT);  
      } 
 
      
     if(kickKnob.startDragging(mouseX,mouseY)){
       
     }
     if(bassKnob.startDragging(mouseX,mouseY)){
       
     }
     if(snareKnob.startDragging(mouseX,mouseY)){
            
     }
     if(hithatKnob.startDragging(mouseX,mouseY)){
             
     }
  }

  public void mouseReleased(float x, float y) {
    mouseReleased((int)x, (int)y);
  }  

  public void mouseReleased() {    
    mouseReleased(drumCloud.mouseX, drumCloud.mouseY);
  }


  public void mouseReleased(int mouseX, int mouseY) {
     if(kick.isClickStopped()){
       drumCloud.removeAllSoundsOfType(0);
     }
     if(bass.isClickStopped()){
       drumCloud.removeAllSoundsOfType(1);
     }
     if(snare.isClickStopped()){
       drumCloud.removeAllSoundsOfType(2);
     }
     if(hithat.isClickStopped()){
       drumCloud.removeAllSoundsOfType(3);
     }
     
     if(kickKnob.stopDragging()){
       
     }
     bassKnob.stopDragging();
     snareKnob.stopDragging();
     hithatKnob.stopDragging();     
  }


  public void debug() {

  }



  public void mouseDragged() {
     mouseDragged(drumCloud.mouseX, drumCloud.mouseY);
  }
  
  public void mouseDragged(int mouseX, int mouseY) {
     //PApplet.println("Drag on automator px:"+drumCloud.pmouseX+" py:"+drumCloud.pmouseY+" mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
     kick.isMoveOut(mouseX,mouseY);
     bass.isMoveOut(mouseX,mouseY);
     snare.isMoveOut(mouseX,mouseY);
     hithat.isMoveOut(mouseX,mouseY);
     println("Setting kick vol:"+kick.normalizedValue()+" y:"+kick.y+" my:"+mouseY);
     controlVolume(kick.normalizedValue(), KICK);
     controlVolume(bass.normalizedValue(), BASS);
     controlVolume(snare.normalizedValue(), SNARE);
     controlVolume(hithat.normalizedValue(), HITHAT);
     kickKnob.dragVertically(mouseY);
     bassKnob.dragVertically(mouseY);
     snareKnob.dragVertically(mouseY);
     hithatKnob.dragVertically(mouseY);
  }  

  public void mouseMoved() {
    mouseMoved(drumCloud.mouseX, drumCloud.mouseY);  
     kickKnob.isOvered(mouseX,mouseY);
     bassKnob.isOvered(mouseX,mouseY);
     snareKnob.isOvered(mouseX,mouseY);
     hithatKnob.isOvered(mouseX,mouseY);    
  }
  
  public void mouseMoved(int mouseX, int mouseY) {
     //PApplet.println("Moved on automator px:"+drumCloud.pmouseX+" py:"+drumCloud.pmouseY+" mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);    
  }  
  
  public void updateState() {
    /*for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j].ON=drumCloud.samplesPerBeat[i][j-1];
      }
    }*/
  }  

}

