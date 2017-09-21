/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.duplex.DuplexFeatures.MirnaClass;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;

/**
 *
 * @author Dan
 */
public class DuplexFileManager 
{
    // Input params
    private File duplex_zip;
    private File output_dir;
    
    // Output files
    private File arff_file;
    private File features_file;
    private File duplex_file;
    
    public DuplexFileManager(File duplex_zip, File output_dir)
    {
        this.duplex_zip = duplex_zip;
        this.output_dir = output_dir;
    }
    
    public File getArffFile()       {return this.arff_file;}
    public File getFeaturesFile()   {return this.features_file;}
    public File getDuplexFile()     {return this.duplex_file;}
        
    public void extract() throws IOException
    {
        // Get duplex and features file
        List<File> duplex_package = FileUtils.decompressFiles(duplex_zip, output_dir); 
        for(File f : duplex_package)
        {
            if (f.getName().endsWith("_duplex-features.lsvm"))
            {
                features_file = f;
            }
            else if (f.getName().endsWith("_duplexes.dup"))
            {
                duplex_file = f;
            }
            else if (f.getName().endsWith("_duplex-features.arff"))
            {
                arff_file = f;
            }
            else
            {
                throw new IOException("Duplex File Manager Exception: Unknown file found in duplex zip file.");
            }
        }    
    }
    

    public void save(List<Duplex> duplexes, List<DuplexFeatures> features) throws IOException
    {
        duplex_file = new File(output_dir.getPath() + DIR_SEPARATOR + duplex_zip.getName() + "_duplexes.dup");
        arff_file = new File(output_dir.getPath() + DIR_SEPARATOR + duplex_zip.getName() + "_duplex-features.arff");
        features_file = new File(output_dir.getPath() + DIR_SEPARATOR + duplex_zip.getName() + "_duplex-features.lsvm");
        File[] files = new File[]{duplex_file, arff_file, features_file};

        // Generate the output files.
        writeToFile(duplexes, duplex_file, true);
        writeToArffFile(features, arff_file);
        writeToLibSVMFile(features, features_file);

        // Compress all files into the specified output file.                
        FileUtils.compressFiles(files, duplex_zip);
    }
    
    public static List<Duplex> loadDuplexesFromFile(File infile) throws IOException
    {
        List<Duplex> dups = new ArrayList<Duplex>();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
        
        String line = null;
        while((line = br.readLine()) != null)
        {
            dups.add(Duplex.parse(line));
        }
        
        return dups;
    }
    
    
    private static double getDoubleVal(String part)
    {
        return Double.parseDouble(part.substring(part.indexOf(":")+1));
    }
    
    private static int getIntVal(String part)
    {
        return Integer.parseInt(part.substring(part.indexOf(":")+1));
    }
    
    public static List<DuplexFeatures> loadFeaturesFromFile(File infile) throws IOException
    {
        List<DuplexFeatures> features = new ArrayList<DuplexFeatures>();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
        
        String line = null;
        while((line = br.readLine()) != null)
        {
            String[] parts = line.split(" ");
            
            double class_val = (int)Double.parseDouble(parts[0]);
            MirnaClass is_mirna = class_val == 1 ? MirnaClass.IS_MIRNA : class_val == -1 ? MirnaClass.NOT_MIRNA : MirnaClass.UNKNOWN;
                        
            LengthFeatures lf = new LengthFeatures(getIntVal(parts[1]), getIntVal(parts[2]), getIntVal(parts[3]));
            NucleotideCompositionFeatures ncf = new NucleotideCompositionFeatures(
                    translateNTBack(getDoubleVal(parts[4])), translateNTBack(getDoubleVal(parts[5])),
                    getDoubleVal(parts[6]), getDoubleVal(parts[7]), getDoubleVal(parts[8]), getDoubleVal(parts[9]),
                    getDoubleVal(parts[10]), getDoubleVal(parts[11]), getDoubleVal(parts[12]), getDoubleVal(parts[13])
                    );
            AlignmentFeatures af = new AlignmentFeatures(
                    getDoubleVal(parts[14]), getIntVal(parts[15]), getIntVal(parts[16]),
                    getDoubleVal(parts[17]), getIntVal(parts[18]), getIntVal(parts[19]), getIntVal(parts[20])
                    );
            
            DuplexFeatures df = new DuplexFeatures(lf, ncf, af, is_mirna);
            
            features.add(df);
        }
        
        return features;
    }
    
    
    public static List<DuplexFeatures> loadArffFeaturesFromFile(File infile) throws IOException
    {
        List<DuplexFeatures> features = new ArrayList<DuplexFeatures>();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
        
        String line = null;
        while((line = br.readLine()) != null)
        {
            line = line.trim();
            
            if (line.isEmpty() || line.startsWith("@") || line.startsWith("%"))
                continue;
            
            String[] parts = line.split(",");
            
            MirnaClass is_mirna = parts[20].equals("yes") ? MirnaClass.IS_MIRNA : parts[20].equals("no") ? MirnaClass.NOT_MIRNA : MirnaClass.UNKNOWN;
                        
            LengthFeatures lf = new LengthFeatures(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            NucleotideCompositionFeatures ncf = new NucleotideCompositionFeatures(
                    translateNTBack(Double.parseDouble(parts[3])), translateNTBack(Double.parseDouble(parts[4])),
                    Double.parseDouble(parts[5]), Double.parseDouble(parts[6]), Double.parseDouble(parts[7]), Double.parseDouble(parts[8]),
                    Double.parseDouble(parts[9]), Double.parseDouble(parts[10]), Double.parseDouble(parts[11]), Double.parseDouble(parts[12])
                    );
            AlignmentFeatures af = new AlignmentFeatures(
                    Double.parseDouble(parts[13]), Integer.parseInt(parts[14]), Integer.parseInt(parts[15]),
                    Double.parseDouble(parts[16]), Integer.parseInt(parts[17]), Integer.parseInt(parts[18]), Integer.parseInt(parts[19])
                    );
            
            DuplexFeatures df = new DuplexFeatures(lf, ncf, af, is_mirna);
            
            features.add(df);
        }
        
        return features;
    }
    
    private static void writeToFile(List<Duplex> duplexes, File outfile, boolean dna_form) throws IOException
    {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outfile)));

        for(Duplex d : duplexes)
        {
            pw.println(d.toString(dna_form));
        }

        pw.flush();
        pw.close();
    }
    
    public static void writeToArffFile(List<DuplexFeatures> features, File out_file) throws IOException
    {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(out_file)));

        pw.print("@relation miRNA_duplex_features\n\n");
        
        pw.print("% Sequence length attributes\n");
        pw.print("@attribute len_mirna integer\n");
        pw.print("@attribute len_mirna_star integer\n");
        pw.print("@attribute len_diff integer\n");
        
        pw.print("\n% Nucleotide composition attributes\n");
        pw.print("@attribute mirna_base1 {A, U, G, C}\n");
        pw.print("@attribute mirna_star_base1 {A, U, G, C}\n");
        pw.print("@attribute mirna_percent_a real\n");
        pw.print("@attribute mirna_percent_u real\n");
        pw.print("@attribute mirna_percent_g real\n");
        pw.print("@attribute mirna_percent_c real\n");
        pw.print("@attribute mirna_star_percent_a real\n");
        pw.print("@attribute mirna_star_percent_u real\n");
        pw.print("@attribute mirna_star_percent_g real\n");
        pw.print("@attribute mirna_star_percent_c real\n");
        
        pw.print("\n% Alignment attributes\n");
        pw.print("@attribute mfe real\n");
        pw.print("@attribute len_5p_overhang integer\n");
        pw.print("@attribute len_3p_overhang integer\n");
        pw.print("@attribute complementarity_score real\n");
        pw.print("@attribute nb_mismatches integer\n");
        pw.print("@attribute nb_g_u_pairs integer\n");
        pw.print("@attribute nb_bulges integer\n");
        
        
        pw.print("\n% Supervised learning annotation (required for training and testing, optional for general use): yes - is a miRNA; no - isn't a miRNA\n");
        pw.print("\n@attribute is_mirna {yes, no}\n");
        
        pw.print("\n@data\n");
        
        for (DuplexFeatures df : features)
        {
            LengthFeatures lf = df.getLengthFeatures();
            NucleotideCompositionFeatures ncf = df.getNucleotideCompositionFeatures();
            AlignmentFeatures af = df.getAlignmentFeatures();
            
            pw.print(lf.len_mirna + ",");
            pw.print(lf.len_mirna_star + ",");
            pw.print(lf.len_diff + ",");
            pw.print(ncf.mirna_base1 + ",");
            pw.print(ncf.mirna_star_base1 + ",");
            pw.print(ncf.mirna_percent_a + ",");
            pw.print(ncf.mirna_percent_u + ",");
            pw.print(ncf.mirna_percent_g + ",");
            pw.print(ncf.mirna_percent_c + ",");
            pw.print(ncf.mirna_star_percent_a + ",");
            pw.print(ncf.mirna_star_percent_u + ",");
            pw.print(ncf.mirna_star_percent_g + ",");
            pw.print(ncf.mirna_star_percent_c + ",");
            pw.print(af.mfe + ",");
            pw.print(af.len_5p_overhang + ",");
            pw.print(af.len_3p_overhang + ",");
            pw.print(af.complementarity_score + ",");
            pw.print(af.nb_mismatches + ",");
            pw.print(af.nb_g_u_pairs + ",");
            pw.print(af.nb_bulges + ",");
            pw.print(df.isMirna() == DuplexFeatures.MirnaClass.IS_MIRNA ? "yes" : "no");
            pw.print("\n");
        }
        
        pw.flush();
        pw.close();
    }
    
    
    private static double translateNT(char nt)
    {
        switch(nt)
        {
        case 'A':
            return 0.0;
        case 'U':
            return 1.0;
        case 'G':
            return 2.0;
        case 'C':
            return 3.0;
        default:
            throw new IllegalArgumentException("Unknown nucleotide detected.");
        }
    }
    
    private static char translateNTBack(double val)
    {
        int v = (int)val;
        switch(v)
        {
        case 0:
            return 'A';
        case 1:
            return 'U';
        case 2:
            return 'G';
        case 3:
            return 'C';
        default:
            throw new IllegalArgumentException("Unknown nucleotide detected.");
        }
    }
    
    
    
    public static void writeToLibSVMFile(List<DuplexFeatures> features, File out_file) throws IOException
    {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(out_file)));

        // No preamble... straight into the candidates
        for (DuplexFeatures df : features)
        {
            LengthFeatures lf = df.getLengthFeatures();
            NucleotideCompositionFeatures ncf = df.getNucleotideCompositionFeatures();
            AlignmentFeatures af = df.getAlignmentFeatures();
            
            pw.print(df.isMirna().getVal());
            //pw.print("0"); // class (0 - unknown)
            pw.print(" 1:" + lf.len_mirna);
            pw.print(" 2:" + lf.len_mirna_star);
            pw.print(" 3:" + lf.len_diff);
            pw.print(" 4:" + translateNT(ncf.mirna_base1));
            pw.print(" 5:" + translateNT(ncf.mirna_star_base1));
            pw.print(" 6:" + ncf.mirna_percent_a);
            pw.print(" 7:" + ncf.mirna_percent_u);
            pw.print(" 8:" + ncf.mirna_percent_g);
            pw.print(" 9:" + ncf.mirna_percent_c);
            pw.print(" 10:" + ncf.mirna_star_percent_a);
            pw.print(" 11:" + ncf.mirna_star_percent_u);
            pw.print(" 12:" + ncf.mirna_star_percent_g);
            pw.print(" 13:" + ncf.mirna_star_percent_c);
            pw.print(" 14:" + af.mfe);
            pw.print(" 15:" + af.len_5p_overhang);
            pw.print(" 16:" + af.len_3p_overhang);
            pw.print(" 17:" + af.complementarity_score);
            pw.print(" 18:" + af.nb_mismatches);
            pw.print(" 19:" + af.nb_g_u_pairs);
            pw.print(" 20:" + af.nb_bulges);
            pw.print("\n");
        }
        
        pw.flush();
        pw.close();
    }
}
