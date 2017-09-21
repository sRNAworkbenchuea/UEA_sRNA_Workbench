/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.WF;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;

/**
 *
 * @author w0445959
 */
//@Transactional
//@Service("ReportSummariser")
public class ReportSummariser
{
    private static final Charset charset = Charset.forName("UTF-8");
    public ReportSummariser()
    {
        
    }

    public void generatePrintedReport(Path location) throws IOException
    {
        String genomeName = DatabaseWorkflowModule.getInstance().getGenomePath().getFileName().toString();
        String header = "Files,"
                + "Raw Count Non Redundant,"
                + "Raw Count Redundant,"
                + "Raw Count Complexity," +
                
                genomeName + " MAP Non Redundant," +
                genomeName + " MAP Redundant," + 
                genomeName + " MAP Complexity";
        
        //FilenameServiceImpl alignedService = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        SequenceServiceImpl sequenceService = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");

        
        try(BufferedWriter fout = Files.newBufferedWriter(location , charset, StandardOpenOption.CREATE))
        {
            fout.write(header + LINE_SEPARATOR);
            Map<String, List<Path>> samples = DatabaseWorkflowModule.getInstance().getSamples();
            
            HashMap<String,Integer[]> UnmappedTotals = sequenceService.getSequenceTotals();
            
            HashMap<String,Integer[]> mappedTotals = sequenceService.getMappedTotals();
            
            for(Entry<String, List<Path>> entry : new TreeMap<>(samples).entrySet())
            {
                for(Path p : entry.getValue())
                {
                    String filename = p.getFileName().toString();
                    fout.write(filename);
                    fout.write(",");
                    Integer[] get = UnmappedTotals.get(filename);
                    fout.write(get[0].toString());
                    fout.write(",");
                    fout.write(UnmappedTotals.get(filename)[1].toString());
                    fout.write(",");
                    Float comp = (UnmappedTotals.get(filename)[0]).floatValue()/(UnmappedTotals.get(filename)[1]).floatValue();
                    fout.write(comp.toString());
                    fout.write(",");
                    fout.write(mappedTotals.get(filename)[0].toString());
                    fout.write(",");
                    fout.write(mappedTotals.get(filename)[1].toString());
                    fout.write(",");
                    comp = (mappedTotals.get(filename)[1]).floatValue()/(mappedTotals.get(filename)[0]).floatValue();
                    fout.write(comp.toString());
                    fout.write(LINE_SEPARATOR);
                    
                    
                }
            }
//            
        }
    }

    
}
