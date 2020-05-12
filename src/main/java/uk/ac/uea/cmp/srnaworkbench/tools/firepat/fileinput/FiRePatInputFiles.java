/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.ac.uea.cmp.srnaworkbench.io.stream.Utils;

/**
 *
 * @author rew13hpu
 */
public class FiRePatInputFiles implements Serializable {

    private Map<String, File> sRNA_samples = new LinkedHashMap<>();
    private File firstInputFile;
    private File secondInputFile;
    private File outputDirectory;
    private int nreps;
    private Integer nsamples;
    private String sampleNames;
    private boolean autoSampleNames = true;

    private FiRePatInputFiles() {
        sRNA_samples = new LinkedHashMap<>();
        autoSampleNames = true;
    }

    private static FiRePatInputFiles input;

    public static FiRePatInputFiles getInstance() {
        if (input == null) {
            input = new FiRePatInputFiles();
        }

        return input;
    }

    
   
    public void setFirstInputFile(File f)
    {
        firstInputFile = f;
    }
    public void setSecondInputFile(File f)
    {
        secondInputFile = f;
    }
    public void setnsamples(Integer i)
    {
        nsamples = i;
    }
    public void setSampleNames(String s)
    {
        sampleNames = s;
    }
    public void setAutoSampleNames(boolean b)
    {
        autoSampleNames = b;
    }
    public String isStepOneComplete(String first_dataset_name, String second_dataset_name, String first_input_file_path, String second_input_file_path) 
    {
        boolean b = (!(firstInputFile==null) && !(secondInputFile==null) && !(nsamples==null));
        if (!b)
        {
            String alertContent = "Please select both input files and enter number of samples!";
            return alertContent;
        }
        if(autoSampleNames==false)
        {
            if(!checkNumSamplesAndNames())
            {
                String alertContent = "The number of manually entered sample names must match the number of samples! Or use sequential sample names.";
                return alertContent;
            }
        }
        if(first_input_file_path.equals(second_input_file_path))
        {
            String alertContent = "The first and second input files are identical! They must be different!";
            return alertContent;
        }
        if(first_dataset_name.equals(second_dataset_name))
        {
            String alertContent = "The names of the first and second datasets are identical! They must be different!";
            return alertContent;
        }        
        return "";
    }
    private boolean checkNumSamplesAndNames()
        {
            if(nsamples==null) return false;
            if(sampleNames=="") return false;
            String[] allnames = sampleNames.split(",");
            if(allnames.length!=nsamples) return false;
            return true;
        }
    public String isStepThreeComplete(int upCol, int downCol) 
    {
        if(outputDirectory==null)
        {
            String alertContent = "Please select directory for output!";
            return alertContent;
        }
        if(upCol==downCol)
        {
            String alertContent = "Colour indicating increased expression levels must be different from the colour indicating decreased levels!";
            return alertContent;
        }
        return("");
    }
    public void setOutputDirectory(File outputDirectory) 
    {
        if(!outputDirectory.exists())
        {
            outputDirectory.mkdir();
        }
        
        if (outputDirectory.isDirectory()) {
            this.outputDirectory = outputDirectory;
        }
        else
        {
            System.out.println("The output location must be a valid directory!");
            System.exit(0);
        }
        
    }
    public File getOutputDirectory()
    {
        return outputDirectory;
    }
}
