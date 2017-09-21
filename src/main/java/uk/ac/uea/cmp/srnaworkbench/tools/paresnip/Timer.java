
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

/**
 * A class that does the timings for us.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public class Timer {

    private long startTimeMilliseconds;
    private long millisInSecond = 1000;

    /**
     * Starts the timer running.
     */
    public void startTimer(){
        startTimeMilliseconds = System.currentTimeMillis();
    }//end method.


    /**
     * Get an estimate of the elapsed time in H:M:S:MS since the startTimer() method
     * was called, in the form of a string.
     * @return H:M:S:MS
     */
    public String getTimeElapsed(){

        String h = "";
        String m = "";
        String s = "";
        String ms = "";
        long currentTime = System.currentTimeMillis();

        long elapsedMS = currentTime - startTimeMilliseconds;

        int hours = (int) ((elapsedMS/millisInSecond)/60)/60;
        long hoursR = hours*60*60*1000;
        h+=hours;

        int minutes = (int)((elapsedMS-hoursR)/1000)/60;
        long minutesR = minutes*60*1000;
        m+=minutes;

        int seconds = (int) (elapsedMS-hoursR-minutesR)/1000;
        long secondsR = seconds*1000;
        s+=seconds;

        int millis = (int) (elapsedMS-hoursR-minutesR-secondsR);
        ms+=millis;

        return "h: "+h+" m: "+m+" s: "+s+" ms: "+ms;
    }


}//end method.
