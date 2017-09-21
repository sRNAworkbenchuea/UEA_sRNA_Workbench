/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Matthew
 */
public class IteratingStopWatch extends Timer{
    
    Map<String, Long> lapTimes = new LinkedHashMap<>();
    long lastLap;

    @Override
    public void start() {
        _stopped = false;
        lapTimes.clear();
        
        _startTime = System.nanoTime();
        //lapTimes.put("_start", _startTime);
        lastLap = _startTime;
    }

    @Override
    public void stop() {
        if ( _stopped )
            return;

        _stopTime = System.nanoTime();
        _stopped = true;
    }
    
    public void printTimes(PrintStream os, boolean outputNanos)
    {
        printHeader(os, outputNanos);
        for(Entry<String, Long> time : lapTimes.entrySet())
        {
            os.println(time.getKey() + ": " + formatElapsedTime(time.getValue(), outputNanos));
        }
        os.println( "-- End of stopwatch time(s) --" );
    }

    @Override
    public void lap(String msg) {
        long lap = System.nanoTime();
        long lapTime = System.nanoTime() - lastLap;
        Long currentTotal = lapTimes.get(msg);
        if(currentTotal == null)
        {
            lapTimes.put(msg, lapTime);
        }
        else
        {
            lapTimes.put(msg, lapTime+currentTotal);
        }
        lastLap = lap;
    }
    
    public static void main(String[] args) {
        IteratingStopWatch sw = new IteratingStopWatch();

        try {
            sw.start();
            for(int i = 0;i<10;i++){
                

                Thread.sleep(43);

                sw.lap("one");

                System.out.println(sw.getFormattedElapsedTime());

                Thread.sleep(243);
            }

            sw.lap("two");

            Thread.sleep(4000);

            sw.stop();
        } catch (InterruptedException ex) {
        }

        sw.printTimes();
    }
    
}
