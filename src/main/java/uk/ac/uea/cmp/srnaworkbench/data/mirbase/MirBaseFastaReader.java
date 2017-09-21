/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ezb11yfu
 */
public class MirBaseFastaReader 
{
    private int count;

    private BufferedReader br;
    private InputStreamReader isr;
    private FileInputStream fi = null;

    public MirBaseFastaReader(File infile) throws IOException
    {
        this.fi = new FileInputStream(infile);
        this.isr = new InputStreamReader(fi);
        this.br = new BufferedReader(isr);

        count = 0;
    }

    public int getCount()       {return this.count;}

    public List<MirBaseHeader> process() throws IOException
    {
        ArrayList<MirBaseHeader> data = new ArrayList<MirBaseHeader>();

        String line = "";
        boolean done = false;
        while (!done)
        {
            if (line.startsWith(">"))
            {
                MirBaseHeader mbh = MirBaseHeader.parse(line.substring(1)); 

                // Assume the next line is going to be there!
                StringBuilder sb = new StringBuilder();
                while((line = br.readLine()) != null)
                {
                    if (line.startsWith(">"))
                    {
                        break;
                    }
                    sb.append(line);
                }

                mbh.setSeq(sb.toString());
                
                data.add(mbh);

                count++;
            }
            else
            {
                line = br.readLine();
            }

            if (line == null)
            {
                done = true;
            }
        }

        br.close();
        isr.close();
        fi.close();

        return data;
    }   
    

}
