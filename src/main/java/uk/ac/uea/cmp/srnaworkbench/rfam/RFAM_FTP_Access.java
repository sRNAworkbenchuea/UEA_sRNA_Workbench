/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.rfam;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class RFAM_FTP_Access
{

    private static final String RFAMDownloads = Tools.DATA_DIR.getAbsolutePath() +  DIR_SEPARATOR + "RFAMFILES";
    public static void main(String[] args)
    {
        accessRFAMFTPServer();
    }

    public static void accessRFAMFTPServer()
    {
        FTPClient client = new FTPClient();

        boolean error = false;
        try
        {
            int reply;
            client.connect("ftp.ebi.ac.uk");
            client.login("anonymous", "");

            System.out.println("Connected to " + client + ".");
            System.out.print(client.getReplyString());
            
            

      // After connection attempt, you should check the reply code to verify
            // success.
            reply = client.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                client.disconnect();
                LOGGER.log(Level.WARNING, "FTP server refused connection.");
                return;
            }
            client.changeWorkingDirectory("/pub/databases/Rfam/CURRENT/fasta_files");
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            //Retrieve relevant accessions
            ArrayList<String> accessions = RFAM_DatabaseAccess.getAccessions();
            
            
            
            //list all files
            FTPFile[] ftpFiles = client.listFiles();
            for (FTPFile ftpFile : ftpFiles)
            {
                // Check if FTPFile is a regular file
                if (ftpFile.getType() == FTPFile.FILE_TYPE)
                {
//                    System.out.println("FTPFile: " + ftpFile.getName() +
//                            "; " + FileUtils.byteCountToDisplaySize(
//                            ftpFile.getSize()));
//                    
                    String name = ftpFile.getName();
                    int index=Collections.binarySearch(accessions, ftpFile.getName());     

                    if(index >= 0)
                    {
                        String remoteFile1 = ftpFile.getName();
                        if(!Files.exists(Paths.get(RFAMDownloads )))
                        {
                            Files.createDirectory(Paths.get(RFAMDownloads));
                        }
                        File downloadFile1 = new File(RFAMDownloads + DIR_SEPARATOR + remoteFile1);
                        boolean success;
                        try (OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1))) {
                            success = client.retrieveFile(remoteFile1, outputStream1);
                        }

                        if (success)
                        {
                            extractToLocal(downloadFile1.getAbsolutePath());

                            //LOGGER.log(Level.INFO, "File {0} has been downloaded successfully.", ftpFile.getName());
                        }

                    }

                   
                }
            }
            
            
            

            client.logout();
            
            mergeRFAMFiles();
            
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Workbench Information");
                        alert.setHeaderText("Operation Completed");
                        alert.setContentText("Updating the RFAM database is complete");
                        alert.showAndWait();

        }
        catch (IOException e)
        {
            error = true;
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        finally
        {
            if (client.isConnected())
            {
                try
                {
                    client.disconnect();
                }
                catch (IOException ioe)
                {
                    // do nothing
                }
            }
        }
    }

    private static void extractToLocal(String path)
    {

        //final String INPUT_GZIP_FILE = "/home/mkyong/file1.gz";
        final String OUTPUT_FILE = FilenameUtils.removeExtension(path);

        byte[] buffer = new byte[1024];

        try
        {

            GZIPInputStream gzis
                    = new GZIPInputStream(new FileInputStream(path));

            FileOutputStream out
                    = new FileOutputStream(OUTPUT_FILE);

            int len;
            while ((len = gzis.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

        }
        catch (IOException ex)
        {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
    }
    
    public static File[] RFAMFilefinder( String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() { 
                 @Override
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith(".fa"); }
        } );

    }
    private static void mergeRFAMFiles()
    {
        Path outFile = Tools.TRRNA_FILE.toPath();
        File[] RFAMFiles = RFAMFilefinder(RFAMDownloads);
        try (FileChannel out = FileChannel.open(outFile, CREATE, WRITE))
        {
            for (int ix = 0, n = RFAMFiles.length - 1; ix < n; ix++)
            {
                Path inFile = RFAMFiles[ix].toPath();
                System.out.println(inFile + "...");
                try (FileChannel in = FileChannel.open(inFile, READ))
                {
                    for (long p = 0, l = in.size(); p < l;)
                    {
                        p += in.transferTo(p, l - p, out);
                    }
                }
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(RFAM_FTP_Access.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

   
