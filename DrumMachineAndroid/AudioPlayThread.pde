class AudioPlayThread extends Thread {

  private DrumMachineAndroid drumMachine;  
  
  boolean running;           // Is the thread running?  Yes or no?
  int wait;                  // How many milliseconds should we wait in between executions?
  String id;                 // Thread name
  long millis;
  long count;                // counter
 
  long getCount() {
    return count;
  }
 
  long getMillis() {
    return millis;
  }
 
  // Constructor, create the thread
  // It is not running by default
  AudioPlayThread (DrumMachineAndroid d,int w, String s) {
    drumMachine=d;
    wait = w;
    running = false;
    id = s;
    count = 0;
  }

// Overriding "start()"
  void start () {    
    
    // Set running equal to true
    running = true;
    // Print messages
    println("Starting thread (will execute every " + wait + " milliseconds.)"); 
    // Do whatever start does in Thread, don't forget this!
    super.start();
  }
 
 
   // We must implement run, this gets triggered by start()
  void run () {
    while (running) {
      millis=millis();
      try{
      drumMachine.proccessTempoVars();
      }catch(Exception ex){
        println("Exception on proccessTempoVars:"+ex.toString());
      }
      count++;
      // Ok, let's wait for however long we should wait
      if(wait>0){
        try {
          sleep((long)(wait));
        } catch (Exception e) {
        }
      }
    }
    System.out.println(id + " thread is done!");  // The thread is done when we get to the end of run()
  }
  
  // Our method that quits the thread
  void quit() {
    System.out.println("Quitting."); 
    running = false;  // Setting running to false ends the loop in run()
    // IUn case the thread is waiting. . .
    interrupt();
  }  

}
