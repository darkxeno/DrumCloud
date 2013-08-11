package com.codefixia.audio;

import com.codefixia.drumcloud.DrumCloud;

public class AudioPlayThread extends Thread {
  
  private boolean running;           // Is the thread running?  Yes or no?
  private int wait;                  // How many milliseconds should we wait in between executions?
  String id;                 // Thread name
  long millis;
  long count;                // counter
 
  public long getCount() {
    return count;
  }
 
  public long getMillis() {
    return millis;
  }
 
  // Constructor, create the thread
  // It is not running by default
  public AudioPlayThread (int w, String s) {
    setWait(w);
    setRunning(false);
    id = s;
    count = 0;
  }

// Overriding "start()"
  public void start () {    
    
    // Set running equal to true
    setRunning(true);
    // Print messages
    DrumCloud.println("Starting thread (will execute every " + getWait() + " milliseconds.)"); 
    // Do whatever start does in Thread, don't forget this!
    super.start();
  }
 
 
   // We must implement run, this gets triggered by start()
  public void run () {
    while (isRunning()) {
      millis=DrumCloud.X.millis();
      try{
    	DrumCloud.X.processTempoVars();
      }catch(Exception ex){
        DrumCloud.println("Exception on proccessTempoVars:"+ex.toString());
      }
      count++;
      // Ok, let's wait for however long we should wait
      if(getWait()>0){
        try {
          sleep((long)(getWait()));
        } catch (Exception e) {
        }
      }
    }
    System.out.println(id + " thread is done!");  // The thread is done when we get to the end of run()
  }
  
  // Our method that quits the thread
  public void quit() {
    System.out.println("Quitting."); 
    setRunning(false);  // Setting running to false ends the loop in run()
    // IUn case the thread is waiting. . .
    interrupt();
  }

public boolean isRunning() {
	return running;
}

public void setRunning(boolean running) {
	this.running = running;
}

public int getWait() {
	return wait;
}

public void setWait(int wait) {
	this.wait = wait;
}  

}