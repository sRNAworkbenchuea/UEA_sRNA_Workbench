/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author a019349
 */
public class FiRePatLog {
    
    private String log_file_name;
    private boolean file_usable;
    private PrintWriter writer;
    
    // class to record log file of events, warnings, and system error messages during a FiRePat analysis
    FiRePatLog()
    {
        log_file_name = "FiRePat.log";
        file_usable = false;
        writer = null;
    }
    
    
    public void initialize(String n, boolean use_log_file)
    {
        if(use_log_file)
        {
            initialize(n);
        }
        else
        {
            file_usable = false;
            System.out.println("FiRePat: log file not being used");
        }
    }
    private void initialize(String n)
    {
        log_file_name = n;
        try
        {
            writer = new PrintWriter(log_file_name, "UTF-8");
            file_usable = true;
            println("FiRePat: initializing log file '"+log_file_name+"'");
        }
        catch(Exception e)
        {
            System.out.println(e);
            System.out.println("FiRePat: unable to initialize log file '"+log_file_name+"' messages will only be printed to console");
        }        
    }   
    public void println(String s)
    {
        println(s, true, true);
    }
    public void println(String s, boolean to_log, boolean to_console)
    {
        if(to_console) System.out.println(s);
        if(to_log)
        {
            if(file_usable)
            {
                writer.println(s);
                writer.flush();
            }
        }
    }
    public void print(String s)
    {
        print(s, true, true);
    }
    public void print(String s, boolean to_log, boolean to_console)
    {
        if(to_console) System.out.print(s);
        if(to_log)
        {
            if(file_usable)
            {
                writer.println((s));
                writer.flush();
            }
        }
    }
    public void close()
    {
        println("FiRePat: closing log file '"+log_file_name+"'");
        file_usable = false;
        try
        {
            writer.close();
        }
        catch (Exception ex) 
        {
            System.out.println("FiRePat: cannot close log file '"+log_file_name+"'");
        }
    }
    
}
