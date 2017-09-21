package uk.ac.uea.cmp.srnaworkbench.data.geo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Navigation of GEO is based on recommendations in ftp.ncbi.nlm.nih.gov/geo/README.txt
 * @author Matthew
 */
public class SeriesDownloader {
    private static final String SEP = "/";
    private static final String GEO_ROOT = "ftp.ncbi.nlm.nih.gov";
    private static final String SRA_ROOT = "ftp-trace.ncbi.nlm.nih.gov";
    private static final String SERIES_DIR = "series";
    private static final String MINIML_DIR = "miniml";
    private static final String MINIML_FILE_SUFFIX = "_family";
    private static final String MINIML_FILE_FORMAT = ".xml.tgz";
    
    private final SeriesAccession accession;
    private Document miniml = null;
    
    public SeriesDownloader(SeriesAccession accession){
        this.accession = accession;
    }
    
    private File downloadXML() throws IOException 
    {
        FTPClient client = new FTPClient();
        
        client.connect(GEO_ROOT);
        client.login("anonymous", "");
        client.changeWorkingDirectory("geo");
        String xmlDir = SERIES_DIR + SEP + accession.getSubDirectory() + SEP + accession.toString() + SEP + MINIML_DIR;
        client.changeWorkingDirectory(xmlDir);
        
        client.enterLocalPassiveMode();
        client.setFileType(FTP.BINARY_FILE_TYPE);
        
        String remoteFile = this.accession + MINIML_FILE_SUFFIX + MINIML_FILE_FORMAT;
        FTPFile file = client.mlistFile(remoteFile);
        long bytesToDl = file.getSize();
        
        File outputFile = new File(remoteFile);
        OutputStream out = new FileOutputStream(outputFile);
        boolean completed = client.retrieveFile(remoteFile, out);
        out.flush();
        out.close();
        client.disconnect();
        return outputFile;
    }
    
    private File extractTgz(File tgzFile) throws IOException{
        if(!tgzFile.toString().endsWith(".tgz"))
            throw new IOException("tar-gz archived file has wrong format (not .tgz): " + tgzFile.toString());
        File extractedDir = new File(this.accession.toString()); // the folder for to dump all from archive (should just be one file)
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(tgzFile, extractedDir);
        return extractedDir;
    }
    
    private void readMiniml(File minimlFile) throws ParserConfigurationException, SAXException, IOException{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(minimlFile);

	//optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        this.miniml = doc;
    }
    
    private File downloadSRAexperiment(String remoteLocation, File outputDirectory) throws IOException{
        FTPClient client = new FTPClient();
        File outputFile = null;
        client.connect(SRA_ROOT);
        client.login("anonymous", "");
        client.changeWorkingDirectory(remoteLocation);
        client.enterLocalPassiveMode();
        client.setFileType(FTP.BINARY_FILE_TYPE);
        client.setBufferSize(32000000);
        for(FTPFile runDir : client.listDirectories()){
            client.changeWorkingDirectory(runDir.getName());
            FTPFile firstFile = client.listFiles()[0];
            
            CopyStreamAdapter progressListener = new CopyStreamAdapter() {
                int currentP = 0;
                @Override
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                    int percent = (int) (100 * totalBytesTransferred / firstFile.getSize());
                    if (percent != currentP) {
                        System.out.println(percent);
                        currentP = percent;
                    }
                }

            };
            client.setCopyStreamListener(progressListener);
            
            System.out.println("Downloading " + firstFile);
            outputFile = new File(outputDirectory.getName() + SEP + firstFile.getName());
            OutputStream out = new FileOutputStream(outputFile);
            boolean completed = client.retrieveFile(firstFile.getName(), out);
            out.close();
            if(!completed)
                System.out.println("Failed!");
            client.changeToParentDirectory();
        }
        client.disconnect();
        return(outputFile);
    }

    private void downloadAllExperiments(File outputDir) throws InvalidAccessionNumberException, IOException{
        NodeList samples = miniml.getElementsByTagName("Sample");
        for (int i = 0; i < samples.getLength(); i++) {
            Node sample = samples.item(i);
            GeoSample thisSample = new GeoSample(sample);
            System.out.println("Downloading experiments for " + thisSample);
            File outputFile = this.downloadSRAexperiment(thisSample.getSraLink(), outputDir);
        }
    }
    
    private void dumpFastq(File file){
        AppUtils.INSTANCE.getBinaryExecutor().execFastqDump(file.toString());
    }
    
    private void listSampleSRAfiles(){
        NodeList samples = miniml.getElementsByTagName("Sample");
        for(int i=0; i<samples.getLength(); i++){
            Node sample = samples.item(i);
            System.out.print(sample.getAttributes().getNamedItem("iid").getNodeValue() + ":");
            Element sampleE = (Element) sample;
            System.out.print(sampleE.getElementsByTagName("Title").item(0).getTextContent() + ":");
            Node suppNode = sampleE.getElementsByTagName("Supplementary-Data").item(0);
            if(suppNode.getAttributes().getNamedItem("type").getNodeValue().equals("SRA Experiment")){
                System.out.println(((Element) suppNode).getTextContent());
            }
        }
    }
    
    public static void main(String[] args) throws Exception{
        
//        SeriesAccession sa = new SeriesAccession("GSE57936");
//        SeriesAccession sa = new SeriesAccession("GSE62800"); // ath dataset with 6 treatments / 3 replicates
         SeriesAccession sa = new SeriesAccession("GSE45049"); // bad piRNA mouse dataset

        SeriesDownloader seriesFtp = new SeriesDownloader(sa);
        File downloaded = seriesFtp.downloadXML();
        File extracted = seriesFtp.extractTgz(downloaded);
        seriesFtp.readMiniml(new File(extracted.toString() + SEP + seriesFtp.accession + MINIML_FILE_SUFFIX + ".xml"));
        seriesFtp.listSampleSRAfiles();
        seriesFtp.downloadAllExperiments(new File("GSE45049"));
        
    }
    
    public static void main0(String[] args) throws Exception{
        String testFile = "F:/gitrepos/Workbench/GSE62800/SRR1634280.sra";
        AppUtils.INSTANCE.getBinaryExecutor().execFastqDump(testFile);
        
    }
    
}
