/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

/**
 *
 * @author ezb11yfu
 */
public class RNACoFoldOutput 
{
    private String dot_brackets;
    private double mfe;
    
    public RNACoFoldOutput()
    {
        this("", 0.0);
    }
    
    public RNACoFoldOutput(String dot_brackets, double mfe)
    {
        this.dot_brackets = dot_brackets;
        this.mfe = mfe;
    }
    
    public RNACoFoldOutput(String line)
    {
        String l = line.trim();
        int mfe_start = l.lastIndexOf("(");
        
        if (mfe_start < 0)
        {
            this.dot_brackets = "";
            this.mfe = 0.0;
        }
        else
        {
            this.dot_brackets = l.substring(0, mfe_start - 1).trim();
            this.mfe = Double.parseDouble(l.substring(mfe_start+1, l.length()-1));
        }
    }
    
    public String getDotBrackets()      {return this.dot_brackets;}
    public double getMFE()              {return this.mfe;}
    
}
