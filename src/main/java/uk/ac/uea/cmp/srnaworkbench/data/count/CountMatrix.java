/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.count;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Simple wrapper for a double map representing a count matrix - files in columns,
 * sequence as rows
 * @author kikum
 */
public class CountMatrix {
    private final Map<String, Map<String, Double>> matrix = new TreeMap<>();
    private final DecimalFormat df; // stored precision of all values. Higher precision means it is more likely
    // for the same matrix from different sources to be non-equal due to negligible rounding errors.
    private static final int DEFAULT_PRECISION= 6;
    
    public CountMatrix(int precision)
    {
        if(precision < 0)
            throw new IllegalArgumentException("Precision of a CountMatrix must be >= 0, not " + precision);
        String formatString = "0.";
        for(int i=0; i<precision; i++)
            formatString+="0";
        df = new DecimalFormat(formatString);
        df.setRoundingMode(RoundingMode.HALF_DOWN);
    }
    public CountMatrix()
    {
        this(DEFAULT_PRECISION);
    }
    public void add(String sample, String sequence, Double value)
    {
        // only add if the value is not 0.
        if(value != 0){
            Double roundedValue = Double.parseDouble(df.format(value));
            if(!matrix.containsKey(sample)){
                Map<String, Double> seqMap = new TreeMap<>();
                seqMap.put(sequence, roundedValue);
                matrix.put(sample, seqMap);
            }
            else
            {
                matrix.get(sample).put(sequence, roundedValue);
            }
        }
    }
    
    public Map<String, Map<String, Double>> getMatrix()
    {
        return matrix;
    }
    
    public static CountMatrix fromCsv(File csvfile) throws FileNotFoundException
    {
        CountMatrix matrix = new CountMatrix();
        Scanner filescan = new Scanner(csvfile);
        List<String> files = new ArrayList<>();
        Scanner linescan = new Scanner(filescan.nextLine());
        linescan.useDelimiter(",");
        String readHeader = linescan.next();
        while (linescan.hasNext()) {
            
            String file = linescan.next();
            files.add(file);
        }
        while (filescan.hasNextLine()) {
            linescan = new Scanner(filescan.nextLine());
            linescan.useDelimiter(",");
            String seq = linescan.next();
            Iterator<String> fileIt = files.iterator();
            while (linescan.hasNext()) {
                matrix.add(fileIt.next(), seq, Double.parseDouble(linescan.next()));
            }
        }
        
        return matrix;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountMatrix other = (CountMatrix) obj;
        if (!Objects.equals(this.matrix, other.matrix)) {
            return false;
        }
        return true;
    }
    
    public void writeMatrix(File csvFile) throws IOException
    {
        FileWriter writer = new FileWriter(csvFile);
        
        
        StringJoiner header = new StringJoiner(",");
        header.add("read");
        Set<String> samples = this.matrix.keySet();
        Set<String> sequences = new TreeSet<>();
        
        for(String sample: samples){
            header.add(sample);
            for(String seq : this.matrix.get(sample).keySet())
                sequences.add(seq);
        }
        writer.write(header.toString() + "\n");
        
        Map<String, StringJoiner> seqRows = new LinkedHashMap<>();
        for(String sample: this.matrix.keySet())
        {
            Map<String, Double> counts = this.matrix.get(sample);
            for(String seq: sequences)
            {
                double value = 0.0;
                if(counts.containsKey(seq))
                    value = counts.get(seq);
                
                if(seqRows.containsKey(seq))
                {
                    seqRows.get(seq).add(Double.toString(value));
                }
                else
                {
                    StringJoiner joiner = new StringJoiner(",");
                    joiner.add(Double.toString(value));
                    seqRows.put(seq, joiner);
                }
            }
        }
        
        for(Entry<String, StringJoiner> row: seqRows.entrySet())
        {
            writer.write(row.getKey() + "," + row.getValue().toString() + "\n");
        }
        
        writer.flush();
        writer.close();
        
    }
    
    public static void main(String[] args)
    {
        try {
            CountMatrix matrix = CountMatrix.fromCsv(new File("D:/gitrepos/src/test/data/norm/counts.csv"));
            System.out.println(matrix);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CountMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
