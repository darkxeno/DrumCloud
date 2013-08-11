package com.codefixia.drumcloud;



import com.codefixia.ui.BeatKnob;
import com.codefixia.ui.PressZones;
import com.codefixia.ui.ToggleButtonsBar;
import com.codefixia.utils.AutoMode;
import com.codefixia.utils.MainMode;

import processing.core.PApplet;


public class Automator {

  private final DrumCloud drumCloud;
  
  private PressZones kick;
  private PressZones bass;
  private PressZones snare;
  private PressZones hithat;
  private BeatKnob kickKnob;
  private BeatKnob bassKnob;
  private BeatKnob snareKnob;
  private BeatKnob hithatKnob;
  private AutoMode mode=AutoMode.VOLUME;
  private int[] offsetsSample={ -1, 0, 1, 2, 3 };
  private int[][] offsetsX;
  private int[][] offsetsY;
  private ToggleButtonsBar modeSelector;
  private int width,height;
  private float zoneWidth,zoneHeight,zoneOriginX,zoneOriginY,zoneMarginX,zoneMarginY,knobOriginX,knobOriginY;

  Automator(DrumCloud drumCloud) {
    this.drumCloud = drumCloud;
    offsetsX=new int[4][4];
    offsetsY=new int[4][4];
  }



  public void setup() {
	width=drumCloud.width;
	height=drumCloud.height;	  
    zoneWidth=width*0.153f;
    zoneHeight=height*0.485f;
    zoneOriginX=width*0.131f;
    zoneMarginX=width*0.04437f;
    zoneMarginY=width*0.428f;
    
    knobOriginY=height*0.2f;
    knobOriginX=zoneOriginX+zoneWidth*0.5f;
    
    modeSelector=new ToggleButtonsBar(DrumCloud.X.barOriginX, DrumCloud.X.padsContainerOriginY-height*0.05f, DrumCloud.X.barWidth, height*0.05f);
    String[] buttons={
    		DrumCloud.X.getString(R.string.VOLUME),
    		DrumCloud.X.getString(R.string.SAMPLE),
    		DrumCloud.X.getString(R.string.LOOP),
    		DrumCloud.X.getString(R.string.PITCH)
    };
    modeSelector.setButtons(buttons);
    
    if (DrumCloud.isAndroidDevice) {
      zoneOriginY=height*0.425f;
    }
    else {
      zoneOriginY=height*0.445f;
    }    
    kick=new PressZones(zoneOriginX,zoneOriginY,zoneWidth,zoneHeight);
    kick.setFillColor(DrumCloud.X.redColor);
    
    bass=new PressZones(zoneOriginX+(zoneWidth+zoneMarginX),zoneOriginY,zoneWidth,zoneHeight);
    bass.setFillColor(DrumCloud.X.orangeColor);
     
    snare=new PressZones(zoneOriginX+2*(zoneWidth+zoneMarginX),zoneOriginY,zoneWidth,zoneHeight);
    snare.setFillColor(DrumCloud.X.blueColor);   
    
    hithat=new PressZones(zoneOriginX+3*(zoneWidth+zoneMarginX),zoneOriginY,zoneWidth,zoneHeight);
    hithat.setFillColor(DrumCloud.X.greenColor);
    
    setSubZonesByMode(mode.value());

    kickKnob=new BeatKnob(knobOriginX,knobOriginY,zoneWidth,zoneWidth);
    kickKnob.setFillColor(DrumCloud.X.redColor);
    bassKnob=new BeatKnob(knobOriginX+1*(zoneWidth+zoneMarginX),knobOriginY,zoneWidth,zoneWidth);
    bassKnob.setFillColor(DrumCloud.X.orangeColor);
    snareKnob=new BeatKnob(knobOriginX+2*(zoneWidth+zoneMarginX),knobOriginY,zoneWidth,zoneWidth);
    snareKnob.setFillColor(DrumCloud.X.blueColor);
    hithatKnob=new BeatKnob(knobOriginX+3*(zoneWidth+zoneMarginX),knobOriginY,zoneWidth,zoneWidth);
    hithatKnob.setFillColor(DrumCloud.X.greenColor);    
  }



  public void draw() {
	modeSelector.draw();  
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
  
  public void applyModeChanges(PressZones pressZone, int sampleType){
      switch (mode) {
		case VOLUME:
			DrumCloud.X.controlVolume(pressZone.normalizedValue(), sampleType);	
			break;
		case SAMPLE:
			if(pressZone.getLastZoneSelected()!=-1)
			switch (sampleType) {
			case 1:
				drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.KICK]);				
				offsetsSample[DrumCloud.X.KICK]=DrumCloud.X.KICK-1+pressZone.getLastZoneSelected()*4;
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.KICK], kick.getStartGrid(), kickKnob.beatValue());
				break;
			case 2:
				drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.BASS]);
				offsetsSample[DrumCloud.X.BASS]=DrumCloud.X.BASS-1+pressZone.getLastZoneSelected()*4;
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.BASS], bass.getStartGrid(), bassKnob.beatValue());
				break;
			case 3:
				drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.SNARE]);
				offsetsSample[DrumCloud.X.SNARE]=DrumCloud.X.SNARE-1+pressZone.getLastZoneSelected()*4;
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.SNARE], snare.getStartGrid(), snareKnob.beatValue());
				break;
			case 4:
				drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.HITHAT]);
				offsetsSample[DrumCloud.X.HITHAT]=DrumCloud.X.HITHAT-1+pressZone.getLastZoneSelected()*4;
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.HITHAT], hithat.getStartGrid(), hithatKnob.beatValue());
				break;				
			}
			
			break;
		case LOOP:
			if(pressZone.getLastZoneSelected()!=-1)
			switch (sampleType) {
			case 1:
				kickKnob.setBeatValuePosition(5-pressZone.getLastZoneSelected());
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.KICK], kick.getStartGrid(), kickKnob.beatValue());
				break;
			case 2:
				bassKnob.setBeatValuePosition(5-pressZone.getLastZoneSelected());
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.BASS], bass.getStartGrid(), bassKnob.beatValue());
				break;
			case 3:
				snareKnob.setBeatValuePosition(5-pressZone.getLastZoneSelected());
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.SNARE], snare.getStartGrid(), snareKnob.beatValue());
				break;
			case 4:
				hithatKnob.setBeatValuePosition(5-pressZone.getLastZoneSelected());
				drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.HITHAT], hithat.getStartGrid(), hithatKnob.beatValue());
				break;				
			}	
			break;
		case PITCH:
			PApplet.println("Changing pitch:"+pressZone.normalizedValue()*2.0f+" of:"+sampleType);
			DrumCloud.X.controlPitch(pressZone.normalizedValue()*2.0f, sampleType);	
			break;			
		}
  }

  public void mousePressed(int id, int mouseX, int mouseY, float pressure) {
	  PApplet.println("Press on automator mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);	  
      if(kick.isClickStarted(id,mouseX,mouseY,drumCloud.getCurrentGrid())){
        drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.KICK]);
        drumCloud.addSoundTypeRepeated(offsetsSample[DrumCloud.X.KICK],kick.normalizedValue(),kickKnob.beatValue());
        applyModeChanges(kick,DrumCloud.X.KICK);        
      }
      if(bass.isClickStarted(id,mouseX,mouseY,drumCloud.getCurrentGrid())){
        drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.BASS]);
        drumCloud.addSoundTypeRepeated(offsetsSample[DrumCloud.X.BASS],bass.normalizedValue(),bassKnob.beatValue());
        applyModeChanges(bass,DrumCloud.X.BASS);
      }
      if(snare.isClickStarted(id,mouseX,mouseY,drumCloud.getCurrentGrid())){
        drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.SNARE]);
        drumCloud.addSoundTypeRepeated(offsetsSample[DrumCloud.X.SNARE],snare.normalizedValue(),snareKnob.beatValue());
        applyModeChanges(snare,DrumCloud.X.SNARE);
      }
      if(hithat.isClickStarted(id,mouseX,mouseY,drumCloud.getCurrentGrid())){
        drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.HITHAT]);
        drumCloud.addSoundTypeRepeated(offsetsSample[DrumCloud.X.HITHAT],hithat.normalizedValue(),hithatKnob.beatValue());
        applyModeChanges(hithat,DrumCloud.X.HITHAT);
      } 
 
      
     if(kickKnob.startDragging(mouseX,mouseY)){
    	 PApplet.println("Start dragging on click kick knob");
     }
     if(bassKnob.startDragging(mouseX,mouseY)){
       
     }
     if(snareKnob.startDragging(mouseX,mouseY)){
            
     }
     if(hithatKnob.startDragging(mouseX,mouseY)){
             
     }
     
     int selected=modeSelector.isButtonClicked(mouseX, mouseY);
     if(selected!=-1){
    	 setSubZonesByMode(selected);
    	 saveOffsetsByMode(mode.value());
    	 mode=AutoMode.fromInt(selected);
    	 loadOffsetsByMode(mode.value());
     }
     
  }
  
  public void setSubZonesByMode(int mode){
 	 switch (mode) {
 	 	case 0:
 		 String[] vSubzones = { "100%", "75%", "50%", "25%" };
 		 kick.setSubZoneTexts(vSubzones);			
 		 bass.setSubZoneTexts(vSubzones);    
 		 snare.setSubZoneTexts(vSubzones);     
 		 hithat.setSubZoneTexts(vSubzones);		    
 		 break; 	 
		case 1:
		    String[] kSubzones = { "K1", "K2", "K3", "K4" };
		    kick.setSubZoneTexts(kSubzones);			
		    String[] bSubzones  = { "B1", "B2", "B3", "B4" };
		    bass.setSubZoneTexts(bSubzones);    
		    String[] sSubzones  = { "S1", "S2", "S3", "S4" };
		    snare.setSubZoneTexts(sSubzones);     
		    String[] hSubzones  = { "H1", "H2", "H3", "H4" };
		    hithat.setSubZoneTexts(hSubzones);		    
			break;
		case 2:
		    String[] subzones = { "16", "8", "4", "2" };
		    kick.setSubZoneTexts(subzones);			
		    bass.setSubZoneTexts(subzones);    
		    snare.setSubZoneTexts(subzones);     
		    hithat.setSubZoneTexts(subzones);		    
			break;
		case 3:
		    String[] pSubzones = { "+2", "+1", "-1", "-2" };
		    kick.setSubZoneTexts(pSubzones);			
		    bass.setSubZoneTexts(pSubzones);    
		    snare.setSubZoneTexts(pSubzones);     
		    hithat.setSubZoneTexts(pSubzones);		    
			break;		    
		}
	  
  }
  
  public void saveOffsetsByMode(int mode){
	  for (int zone = 0; zone < 4; zone++) {
		  switch (zone) {
		  case 0:
			  offsetsX[mode][zone]=(int)kick.getOffsetX();
			  offsetsY[mode][zone]=(int)kick.getOffsetY();
			  break;
		  case 1:
			  offsetsX[mode][zone]=(int)bass.getOffsetX();
			  offsetsY[mode][zone]=(int)bass.getOffsetY();			
			  break;
		  case 2:
			  offsetsX[mode][zone]=(int)snare.getOffsetX();
			  offsetsY[mode][zone]=(int)snare.getOffsetY();						
			  break;
		  case 3:
			  offsetsX[mode][zone]=(int)hithat.getOffsetX();
			  offsetsY[mode][zone]=(int)hithat.getOffsetY();			
			  break;		
		  }	  
	  }
  }

  public void loadOffsetsByMode(int mode){
	  for (int zone = 0; zone < 4; zone++) {	  
		  switch (zone) {
		  case 0:
			  kick.setOffsetX(offsetsX[mode][zone]);
			  kick.setOffsetY(offsetsY[mode][zone]);
			  break;
		  case 1:
			  bass.setOffsetX(offsetsX[mode][zone]);
			  bass.setOffsetY(offsetsY[mode][zone]);			
			  break;
		  case 2:
			  snare.setOffsetX(offsetsX[mode][zone]);
			  snare.setOffsetY(offsetsY[mode][zone]);						
			  break;
		  case 3:
			  hithat.setOffsetX(offsetsX[mode][zone]);
			  hithat.setOffsetY(offsetsY[mode][zone]);			
			  break;		
		  }
	  }
  }  

  public void mouseReleased(float x, float y) {
    mouseReleased((int)x, (int)y);
  }  

  public void mouseReleased() {    
    mouseReleased(drumCloud.mouseX, drumCloud.mouseY);
  }


  public void mouseReleased(int id, int mouseX, int mouseY, float pressure) {
	 PApplet.println("Release on automator mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
     if(kick.isClickStopped(id)){
       drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.KICK]);
     }
     if(bass.isClickStopped(id)){
       drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.BASS]);
     }
     if(snare.isClickStopped(id)){
       drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.SNARE]);
     }
     if(hithat.isClickStopped(id)){
       drumCloud.removeAllSoundsOfType(offsetsSample[DrumCloud.X.HITHAT]);
     }
     
     if(kickKnob.stopDragging()){
    	 if(kick.isClicked()){
    		 drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.KICK], kick.getStartGrid(), kickKnob.beatValue());    		 
    	 }
     }
     if(bassKnob.stopDragging()){
    	 if(bass.isClicked()){
    		 drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.BASS], bass.getStartGrid(), bassKnob.beatValue());    		 
    	 }    	 
     }
     if(snareKnob.stopDragging()){
    	 if(snare.isClicked()){
    		 drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.SNARE], snare.getStartGrid(), snareKnob.beatValue());    		 
    	 }    	 
     }
     if(hithatKnob.stopDragging()){
    	 if(hithat.isClicked()){
    		 drumCloud.updateSoundTypeRepeated(offsetsSample[DrumCloud.X.HITHAT], hithat.getStartGrid(), hithatKnob.beatValue());
    	 }
     }    	      
  }


  public void debug() {

  }



  public void mouseDragged() {
     mouseDragged(-1,drumCloud.mouseX, drumCloud.mouseY,1.0f);
  }
  
  public void mouseDragged(int id, int mouseX, int mouseY, float pressure) {
     //PApplet.println("Drag on automator px:"+drumCloud.pmouseX+" py:"+drumCloud.pmouseY+" mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
	  
     if(kick.isZoneSelected(id,mouseX,mouseY)!=-1){
    	 applyModeChanges(kick,DrumCloud.X.KICK);
     }
     if(bass.isZoneSelected(id,mouseX,mouseY)!=-1){
    	 applyModeChanges(bass,DrumCloud.X.BASS);
     }
     if(snare.isZoneSelected(id,mouseX,mouseY)!=-1){
    	 applyModeChanges(snare,DrumCloud.X.SNARE);
     }
     if(hithat.isZoneSelected(id,mouseX,mouseY)!=-1){
    	 applyModeChanges(hithat,DrumCloud.X.HITHAT);
     }
     //PApplet.println("Setting kick vol:"+kick.normalizedValue()+" y:"+kick.getY()+" my:"+mouseY);
     kickKnob.dragVertically(mouseY);
     bassKnob.dragVertically(mouseY);
     snareKnob.dragVertically(mouseY);
     hithatKnob.dragVertically(mouseY);
  }  

  public void mouseMoved() {
    mouseMoved(drumCloud.mouseX, drumCloud.mouseY);     
  }
  
  public void mouseMoved(int mouseX, int mouseY) {
	  //PApplet.println("Moved on automator px:"+drumCloud.pmouseX+" py:"+drumCloud.pmouseY+" mx:"+drumCloud.mouseX+" my:"+drumCloud.mouseY);
	  kickKnob.isOvered(drumCloud.mouseX,drumCloud.mouseY);
	  bassKnob.isOvered(drumCloud.mouseX,drumCloud.mouseY);
	  snareKnob.isOvered(drumCloud.mouseX,drumCloud.mouseY);
	  snareKnob.isOvered(drumCloud.mouseX,drumCloud.mouseY);
	  hithatKnob.isOvered(drumCloud.mouseX,drumCloud.mouseY); 	  
  }  
  
  public void updateState() {
    /*for (int i=0;i<drumCloud.samplesPerBeat.length;i++) {
      for (int j=1;j<drumCloud.samplesPerBeat[i].length+1;j++) {
        tracks[i][j].ON=drumCloud.samplesPerBeat[i][j-1];
      }
    }*/
  }  

}
