/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.*;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPReply;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *########################################################
# This script is part of the srna-tools website.
# It attempts to download the file mature.fa and hairpin.fa
# from mirbase 
# and saves them as mature.fa and hairpin.fa
# then it generates the files for patman matching with overhang
# by adding "XX" on both sides of each sequence and saves
# these as mature_plusX.fa and hairpin_plusX.fa
########################################################
 * @author ezb11yfu
 */
public class Updater 
{
    private static final String mirbase_host = "mirbase.org";
    private static final String version_dir = "/pub/mirbase/";
    private static final String current_version_dir = version_dir + "CURRENT/";
    private static final String taxonomy_filename = "organisms.txt";
    private static final String taxonomy_alt_filename = "organisms.txt.gz";
    private static final String mature_filename = "mature.fa";
    private static final String hairpin_filename = "hairpin.fa";
    private static final String gz = ".gz";
    
    public static boolean download(boolean force_update) throws Exception
    {
        String version = getLatestOnlineVersion();
        
        if (!force_update && MirBase.localVersionExists(version))
        {
            return false;
        }
        
        download(version);
        unpack(version);
        
        return true;
    }
    
    public static boolean download(String version, boolean force_update) throws Exception
    {
        if (!onlineVersionExists(version))
        {
            throw new IOException("Requested online miRBase version does not exist!");
        }
        
        if (!force_update && MirBase.localVersionExists(version))
        {
            return false;
        }
        
        download(version);
        unpack(version);
        
        return true;
    }
    
    private static void download(String version) throws Exception
    {
        String download_path = MirBase.getMirBaseFolder(version).getPath() + DIR_SEPARATOR;
        
        File download_dir = new File(download_path);
        if (!download_dir.exists())
        {
            download_dir.mkdir();
        }
        
        File tax_file = new File(download_path + taxonomy_filename + gz);
        File mature_file = new File(download_path + mature_filename + gz);
        File hairpin_file = new File(download_path + hairpin_filename + gz);
                
        // Use FTP to get all relevant files and put into data dir, if existing 
        // files are found these should be backed up with the existing name.
        FTPClient client = new FTPClient();
        client.connect(mirbase_host);
        client.login("anonymous", "");
        client.changeDirectory(current_version_dir);//.changeWorkingDirectory(current_version_dir);
        
        try
        {
            client.download(taxonomy_filename + gz, tax_file);
        }
        catch (FTPException fe)
        {
            // Some problem occured downloading the taxonomy file... probably miRBase
            // renamed the file again... try alternative spelling.  If it fails
            // again then there's a bigger problem so we don't handle the exception
            // in that case.  Note that we keep the filename consistent locally:
            // "organism.txt"
            client.download(taxonomy_alt_filename + gz, tax_file);
        }
        
        
        client.download(mature_filename + gz, mature_file);
        client.download(hairpin_filename + gz, hairpin_file);
        client.disconnect(true);

    }
    
    public static boolean isUpToDate() throws Exception
    {
        return getLatestOnlineVersion().equals(MirBase.getLatestLocalVersion());
    }
    
    public static String getLatestOnlineVersion() throws Exception
    {
        //return "";
        FTPClient client = new FTPClient();
        client.connect(mirbase_host);
        client.login("anonymous", "");
        client.changeDirectory(current_version_dir);
        String cur_dir = client.currentDirectory();
        client.disconnect(true);
         
        return new File(cur_dir).getName();
    }
    
    public static boolean onlineVersionExists(String version) throws Exception
    {
        boolean exists = true;
        
        String ver_str = version_dir + version;
        
        FTPClient client = new FTPClient();
        client.connect(mirbase_host);
        client.login("anonymous", "");
        try
        {
            client.changeDirectory(ver_str);
        }
        catch(IllegalStateException | IOException | FTPIllegalReplyException | FTPException e)
        {
            exists = false;
        }        
                
        client.disconnect(true);
         
        return exists;
    }
    
    
    public static void unpack(String version) throws Exception
    {
        File mature_file = new File(MirBase.getMirBaseFolder(version).getPath() + DIR_SEPARATOR + mature_filename + gz);
        File hairpin_file = new File(MirBase.getMirBaseFolder(version).getPath() + DIR_SEPARATOR + hairpin_filename + gz);
        File organism_file = new File(MirBase.getMirBaseFolder(version).getPath() + DIR_SEPARATOR + taxonomy_filename + gz);
        File mature_file_ex = MirBase.getMatureFile(version, MirBase.Category.ALL, false);
        File hairpin_file_ex = MirBase.getHairpinFile(version, MirBase.Category.ALL, false);
        File organism_file_ex = MirBase.getOrganismFile(version);
        
        FileUtils.decompressFile(mature_file, mature_file_ex);
        FileUtils.decompressFile(hairpin_file, hairpin_file_ex);
        FileUtils.decompressFile(organism_file, organism_file_ex);
         
        // Create filtered versions of the files
        makeFilteredFiles(version);
        
        // Create DNA form versions of the files
        makeDNAFormFiles(version);
    }
    
    private static void makeFilteredFiles(String version) throws Exception
    {
        MirBase mb = new MirBase(version);
        
        mb.createFilteredHairpinFile(MirBase.getHairpinFile(version, MirBase.Category.ANIMAL, false), MirBase.Category.ANIMAL.getFilterString());
        mb.createFilteredHairpinFile(MirBase.getHairpinFile(version, MirBase.Category.PLANT, false), MirBase.Category.PLANT.getFilterString());
        mb.createFilteredHairpinFile(MirBase.getHairpinFile(version, MirBase.Category.VIRUS, false), MirBase.Category.VIRUS.getFilterString());
        
        mb.createFilteredMatureFile(MirBase.getMatureFile(version, MirBase.Category.ANIMAL, false), MirBase.Category.ANIMAL.getFilterString());
        mb.createFilteredMatureFile(MirBase.getMatureFile(version, MirBase.Category.PLANT, false), MirBase.Category.PLANT.getFilterString());
        mb.createFilteredMatureFile(MirBase.getMatureFile(version, MirBase.Category.VIRUS, false), MirBase.Category.VIRUS.getFilterString());
    }
    
    private static void makeDNAFormFiles(String version) throws Exception
    {
        File hall = MirBase.getHairpinFile(version, MirBase.Category.ALL, false);
        File ha = MirBase.getHairpinFile(version, MirBase.Category.ANIMAL, false);
        File hp = MirBase.getHairpinFile(version, MirBase.Category.PLANT, false);
        File hv = MirBase.getHairpinFile(version, MirBase.Category.VIRUS, false);
        
        File mall = MirBase.getMatureFile(version, MirBase.Category.ALL, false);
        File ma = MirBase.getMatureFile(version, MirBase.Category.ANIMAL, false);
        File mp = MirBase.getMatureFile(version, MirBase.Category.PLANT, false);
        File mv = MirBase.getMatureFile(version, MirBase.Category.VIRUS, false);
        
        convertToDNAForm(hall, MirBase.getHairpinFile(version, MirBase.Category.ALL, true));
        convertToDNAForm(ha, MirBase.getHairpinFile(version, MirBase.Category.ANIMAL, true));
        convertToDNAForm(hp, MirBase.getHairpinFile(version, MirBase.Category.PLANT, true));
        convertToDNAForm(hv, MirBase.getHairpinFile(version, MirBase.Category.VIRUS, true));
        
        convertToDNAForm(mall, MirBase.getMatureFile(version, MirBase.Category.ALL, true));
        convertToDNAForm(ma, MirBase.getMatureFile(version, MirBase.Category.ANIMAL, true));
        convertToDNAForm(mp, MirBase.getMatureFile(version, MirBase.Category.PLANT, true));
        convertToDNAForm(mv, MirBase.getMatureFile(version, MirBase.Category.VIRUS, true));
    }
    
    private static void convertToDNAForm(File in, File out) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out))));
        
        String line = null;
        while((line = reader.readLine()) != null)
        {
            if (!line.startsWith(">") && !line.startsWith(";"))
            {
                pw.print(line.replaceAll("U", "T"));
                pw.print("\n");
            }
            else
            {
                pw.print(line);
                pw.print("\n");
            }
        }
        
        pw.flush();
        pw.close();
        reader.close();
    }
    
    
    public static void main(String[] args)
    {
        try
        {
            download(true);
        }
        catch(Exception e)
        {
            System.err.println(e.getMessage());
           e.printStackTrace();
        }
    }
}
