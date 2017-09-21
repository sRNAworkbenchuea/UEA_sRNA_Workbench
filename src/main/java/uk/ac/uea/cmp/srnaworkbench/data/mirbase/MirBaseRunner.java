/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author ezb11yfu
 */
public class MirBaseRunner implements Runnable
{
    private String command;
    private String[] args;
    private String filter;
    
    private Map<String, MirBaseEntry> mirnas;
    
    private MirBase mirbase;
    
    private static String[] commands = new String[]{
            "org_count",
            "length_distribution",
            "nucleotide_distribution",
            "build_mirna_groups",
            "filter_mature",
            "filter_hairpin",
            "test"
    };
    
    public static String usage()
    {
        return ("Usage: --tool mirbase --mature_file <path> --hairpin_file <path> --org_file <path> --command {" + commands.toString() + "} [--filter <group>]\n");
    }
    
    public MirBaseRunner(String command, String[] args, String filter) throws Exception
    {
        this(MirBase.getLatestLocalVersion(), command, args, filter);
    }
    
    public MirBaseRunner(String version, String command, String[] args, String filter) throws Exception
    {
        this.command = command.toLowerCase();
        this.filter = filter == null ? null : filter.toLowerCase();
        
        if (!validCommand(command))
        {
            throw new IllegalArgumentException("Invalid command specified. Valid commands are: " + commands.toString());
        }
        
        this.args = args;
        
        this.mirbase = new MirBase(version);
    }
    
    public void setCommand(String command) throws IllegalArgumentException
    {
        command = command.toLowerCase();
        if (!validCommand(command))
            throw new IllegalArgumentException("Invalid command specified. Valid commands are: " + commands.toString());
        this.command = command.toLowerCase();
    }
    
    public void setFilter(String filter)
    {
        this.filter = filter;
    }
    
    private boolean validCommand(String command)
    {
        String clc = command.toLowerCase();
        
        for(String cmd : commands)
        {
            if (clc.equalsIgnoreCase(cmd))
                return true;
        }
        
        return false;
    }
    
    public Map<String, MirBaseEntry> getGrouping()
    {
        return this.mirnas;
    }
    
    @Override
    public void run()
    {
        try
        {
            // Execute command
            if (this.command.equals("org_count"))
            {
                boolean hairpin = false;
                for(int i = 0; i < this.args.length; i++)
                {
                    String arg = this.args[i];
                    
                    if (arg.equals("--hairpin"))
                        hairpin = true;
                }
                
                List<MirBaseHeader> seqs = hairpin ? this.mirbase.getFilteredHairpinSeqs(filter) : this.mirbase.getFilteredMatureSeqs(filter);
                
                OrganismDistribution ld = new OrganismDistribution(seqs);
                
                String filter_string = filter == null ? "" : (filter + " ");
                System.out.println(filter_string + "organism distribution:");

                System.out.println(ld.toString());
            }
            else if (this.command.equals("length_distribution"))
            {
                boolean hairpin = false;
                for(int i = 0; i < this.args.length; i++)
                {
                    String arg = this.args[i];
                    
                    if (arg.equals("--hairpin"))
                        hairpin = true;
                }
                
                List<MirBaseHeader> seqs = hairpin ? this.mirbase.getFilteredHairpinSeqs(filter) : this.mirbase.getFilteredMatureSeqs(filter);
                
                LengthDistribution ld = new LengthDistribution(seqs);
                
                String filter_string = filter == null ? "" : (filter + " ");        
                System.out.println(filter_string + "length distribution:");

                System.out.println(ld.toString());
            }
            else if (this.command.equals("nucleotide_distribution"))
            {
                boolean hairpin = false;
                for(int i = 0; i < this.args.length; i++)
                {
                    String arg = this.args[i];
                    
                    if (arg.equals("--hairpin"))
                        hairpin = true;
                }
                
                List<MirBaseHeader> seqs = hairpin ? this.mirbase.getFilteredHairpinSeqs(filter) : this.mirbase.getFilteredMatureSeqs(filter);
                
                NucleotideDistribution ntd = new NucleotideDistribution(seqs);
                
                String filter_string = filter == null ? "" : (filter + " ");        
                System.out.println(filter_string + "nucleotide distribution:");

                System.out.println(ntd.toString());
            }
            else if (this.command.equals("filter_mature"))
            {
                File out_file = null;
                
                for(int i = 0; i < this.args.length; i++)
                {
                    String arg = this.args[i];
                    
                    if (arg.equals("--out_file"))
                        out_file = new File(args[++i]);
                    else
                        throw new IllegalArgumentException("MiRBase Error: Illegal param detected: " + arg);
                }
                
                mirbase.createFilteredMatureFile(out_file, this.filter);
            }
            else if (this.command.equals("filter_hairpin"))
            {
                File out_file = null;
                
                for(int i = 0; i < this.args.length; i++)
                {
                    String arg = this.args[i];
                    
                    if (arg.equals("--out_file"))
                        out_file = new File(args[++i]);
                    else
                        throw new IllegalArgumentException("MiRBase Error: Illegal param detected: " + arg);
                }
                
                mirbase.createFilteredHairpinFile(out_file, this.filter);
            }
            else if (this.command.equals("build_mirna_groups"))
            {
                boolean group_species = false;
                boolean group_variants = false;
                
                for(String arg : this.args)
                {
                    if (arg.equals("--group_species"))
                        group_species = true;
                    else if (arg.equals("--group_variants"))
                        group_variants = true;
                    else
                        throw new IllegalArgumentException("MiRBase Error: Illegal param detected: " + arg);
                }
                
                this.mirnas = mirbase.buildMirnas(group_species, group_variants, this.filter);
            }
        }
        catch (Exception ioe)
        {
          LOGGER.log( Level.SEVERE, "MirBase Error: Unknown exception caught: {0}\nStackTrace: {1}\n", 
            new Object[]{ ioe.getMessage(), Tools.getStackTrace( ioe ) }); 
        }
    }
    
    
}
