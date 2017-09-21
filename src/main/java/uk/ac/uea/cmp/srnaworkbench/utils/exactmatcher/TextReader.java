package uk.ac.uea.cmp.srnaworkbench.utils.exactmatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * TEXT READER
 * This class is for reading in text files specifically in csv format.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public class TextReader {

    private ArrayList<String> in;
    private String[] options;
    private String[] readCounts;
    private BufferedReader readerA;
    private int specificCount;

    /**
     * Creates a general purpose instance of TextReader.
     **/
    public TextReader(){
        readCounts = new String[5];
        for(int i = 0; i < 5; i++){
            readCounts[i] = "\"\"";
        }//end for.
    }//end constructor.

    /**
     * GET READ COUNTS
     * Table of read counts for each file provided.
     * @return String[]
     **/
    public String[] getReadCounts(){
        return readCounts;
    }//end method.

    /**
     * GET OPTION
     * Returns options used accross the provided tables.
     * @param i
     * @return String - option details.
     */
    public String getOption(int i){
        return options[i];
    }//end method.

    /**
     * ADD CSV FILE
     * This function reads the csv file provided and adds it to a temporary
     * datatable ready for processing.
     * @param fileAddress - address of the file.
     * @param id - short id provided associated with the file.
     * @return ArrayList - the file places into array structure.
     * @throws IOException
     */
    public ArrayList<ArrayList<String>> addCSVFile(String fileAddress, String id) throws IOException{

        ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
        BufferedReader reader = new BufferedReader(new FileReader(fileAddress));
        options = new String[14];
        //Read the complete record (line) in the file given.
        String record = reader.readLine();
        int indexCounter = 0;
        //For each record (line) in the csv file.
        for(int i = 0; record != null; i++){
            //Make a list of the options used(no error checking here).
            if(i < 13){options[i] = record;}  
            if(i > 6 && i < 11){
              if(i == 7){
                  String s = record.substring(3);
                  readCounts[indexCounter] = readCounts[indexCounter].concat(",\"ID: " + id + "\"," + s );
              }//end if.
              else{
                readCounts[indexCounter] = readCounts[indexCounter].concat("," + record);
              }//end if.
              indexCounter++;
            }//end if.
            ArrayList<String> fields = new ArrayList<String>();
            StringBuffer field = new StringBuffer();
            fields.add("ID " + i + ": ");
            //For each character in the record.
            for(int j = 0; j < record.length(); j++){
                //Get the character we are looking at.
                char c = record.charAt(j);
                //If we reach the start or end of a field.
                if(c == ','){
                    fields.add(field.toString());
                    field = new StringBuffer();
                }//end else.
                else{
                    field.append(record.charAt(j));
                }//end if.
            }//end for.
            fields.add(field.toString());
            //Finnished seperating the fields from the record, add them to table.
            table.add(fields);
            //Read the next record.
            record = reader.readLine();
        }//end while.
        //All done, so close the reader.
        reader.close();
        //Return the table we just made.
        return table;
    }//end method.

    /**
     * This function simply reads a text and places each line of the
     * file into an ArrayList to be returned.
     * @param file
     * @return ArrayList<String> (Each element is a line of the file read in).
     */
    public ArrayList<String> readTextFile(File file){
        //Grab some resizable memory to store the lines in the file on disk.
        in = new ArrayList<String>();
        //Grab some memory to hold the buffer.
        BufferedReader reader = null;
        //Try to...
        try {
            //Create a buffered reader.
            reader = new BufferedReader(new FileReader(file));
            //Read the first line in.
            String lineIn = reader.readLine();
            //While there are still lines to be delt with.
            while(lineIn != null){
                //Add the line to the array list.
                in.add(lineIn);
                //Read the next line in the file.
                lineIn = reader.readLine();
            }//end while.
            //Close the stream.
            reader.close();
        }//end try. //Catch Exceptions for lack of file and IO errors.
        catch (FileNotFoundException ex) {
            System.err.println("FILE NOT FOUND EXCEPTION: " + file.getName());
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }        catch (IOException ex) {
            System.err.println("IOEXCEPTION: An error occured during IO opperations.");
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch.
        //Return the file in the form of an array list.
        return in;
    }//end method.

    /**
     * Reads a FASTA formatted file into a data structure ignoring any sequences greater
     * than the specified length.
     * @param file The FASTA formatted file to be read in.
     * @param maxSequenceLengthFilter The maximum length of a sequence.
     * @return An array list where each line is a line in the file.
     */
    public ArrayList<String> readFastaFileSpecificSequenceLengthFilter(File file, int maxSequenceLengthFilter){
        int count = 0;
        //Grab some resizable memory to store the lines in the file on disk.
        in = new ArrayList<String>();
        //Grab some memory to hold the buffer.
        BufferedReader reader = null;
        //Try to...
        try {
            //Create a buffered reader.
            reader = new BufferedReader(new FileReader(file));
            //Read the first line in, i.e. the sequence ID.
            String lineInID = reader.readLine();
            //Read in the second line i.e. the sequence.
            String lineInSeq = reader.readLine();
            //While there are still lines to be delt with.
            while(lineInSeq != null){
                //If the sequence is the same length as that specified by the paramater.
                if(lineInSeq.length() == maxSequenceLengthFilter){
                    //Add the lines to the array list.
                    in.add(lineInID);
                    in.add(lineInSeq);
                }//end if.
                //Read the next two lines in the file.
                lineInID = reader.readLine();
                lineInSeq = reader.readLine();
                count++;
            }//end while.
            //Close the stream.
            reader.close();
        }//end try. //Catch Exceptions for lack of file and IO errors.
        catch (FileNotFoundException ex) {
            System.err.println("FILE NOT FOUND EXCEPTION: " + file.getName());
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }        catch (IOException ex) {
            System.err.println("IOEXCEPTION: An error occured during IO opperations.");
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch.
        //Set the specific count.
        this.specificCount = count;
        //Return the file in the form of an array list.
        return in;
    }//end method.

    /**
     * This method returns the specific count and must only be used after a call to the
     * method readFastaFileSpecificSequenceLengthFilter(File file, int maxSequenceLengthFilter).
     * @return The total number of reads within an srnaome.
     */
    public int getSpecificCount(){
        return this.specificCount;
    }//end method.

     /**
     * READ TEXT FILE
     * This function simply reads a text and places each line of the
     * file into an ArrayList to be returned.
     * @param file
     * @return ArrayList<String> (Each element is a line of the file read in).
     */
    public ArrayList<String> readTextFile(String file){
        in = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            System.err.println("FILE NOT FOUND EXCEPTION: " + file);
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        String lineIn = null;
        try {
            lineIn = reader.readLine();
            //System.out.println("ReadLine: " + lineIn);
        } catch (IOException ex) {
            System.err.println("IOEXCEPTION: Could not read first line in file " + file);
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(lineIn != null){
                //System.out.println("AddingLine: "+lineIn);
                in.add(lineIn);
            try {
                lineIn = reader.readLine();
                //System.out.println("ReadLine: " + lineIn);
            } catch (IOException ex) {
                System.err.println("IOEXCEPTION: couild not read line in file - line No. " + (1+in.size()));
                Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//end while.
        try {
            //end while.
            reader.close();
        } catch (IOException ex) {
            System.err.println("IOEXCEPTION: Could not close file reader.");
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return in;
    }//end method.

    /**
     * Opens a stream to the given file which we shall call file A.
     * @param fileAddress The location and name of the file.
     */
    public void openFileA(String fileAddress){
        try {
            readerA = new BufferedReader(new FileReader(fileAddress));
        } catch (FileNotFoundException ex) {
            System.err.println("FILE NOT FOUND: " + fileAddress);
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//end method.

    /**
     * GET LINE FILE A
     * This function reads the next line from the file opend by the function
     * OPEN FILE A.
     * @return String (The next line in the file as a complete string).
     * @throws IOException
     */
    public String getLineFileA() throws IOException{
        return readerA.readLine();
    }//end method.

    /**
     * CLOSE FILE A:
     * This function closes the stream which was made by the function openFileA().
     * It is important to close any stream which has been opened.
     * @throws IOException
     */
    public void closeFileA() throws IOException{
        readerA.close();
    }//end method.
    
    /**
     * READ FILE INTO MAP:
     * This function reads a FASTA file into a hash map such that each id in the
     * map is the identifier of a sequence and the value is the sequence itself.
     * @param f The file to be read in.
     * @return Key = header, Value = sequence.
     */
    public HashMap<String, String> readFastaFileIntoMap(File f){
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String key = "";
            String sequence = "";
            String line = reader.readLine();
            while(line != null){
                if(line.startsWith(">")){
                    key = line.substring(1);
                }
                else{sequence += line;}
                line = reader.readLine();
                if(line == null || line.startsWith(">") ){map.put(key,sequence); key = ""; sequence = "";}
            }//end while.
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }//end method.

    /**
     * Reads and returns the first two lines of a text file.
     * @param file The file to be read.
     * @return The first two lines of the file.
     */
    public static String[] peek(File file){
        String[] lines = new String[2];  
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            lines[0] = reader.readLine();
            lines[1] = reader.readLine();
            reader.close();
        } catch (IOException ex) {
            System.err.println("IOEXCEPTION: from function peek.");
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lines;
    }//end method.

    /**
     * Reads and returns the specified number of lines in a text file.
     * @param file The file to be read.
     * @param count The number of lines to be read from the file.
     * @return The lines which have been read.
     */
    public static ArrayList<String> quickLook(File file, int count){
        ArrayList<String> lines = new ArrayList<String>(count);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for(int i = 0; i < count; i++){
                lines.add(reader.readLine());
            }
            reader.close();
        } catch (IOException ex) {
            System.err.println("IOEXCEPTION: from function quickLook.");
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lines;
    }//end method.

    /**
     * This function reads the given file into a byte[] array of size File.length() and
     * returns the byte array.
     * NOTE: The maximum length of the file and resulting byte array is the maximum number
     * held by an int depending upon the system architecture e.g. a 32 bit system, the maximum
     * file length can be 2^31.
     * @param file = the file containing the binary data.
     * @return byte[] of length param.length().
     */
    public byte[] readBinaryFile(File file){
        //Get some memory the same size as the file in bytes.
        byte[] mismatchPos = new byte[(int)file.length()];
        try {
            //Open an input stream to read the template.
            FileInputStream input = new FileInputStream(file);
            input.read(mismatchPos);
            input.close();
        } catch (IOException ex) {
            System.err.println("IO EXCEPTION: Either the file could not be found or the file could not be read.");
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch.
        return mismatchPos;
    }//end method.

    /**
     * Read a file in FASTA format from disk and into main memory. The subsequent
     * array list contains paired entries in the form of identifier then sequence.
     * For example index 0 will be an identifier and its related sequence will be
     * at index 1. The next sequence identifier will be at index 2 and the related
     * sequence at index position 3, and so on and so forth.
     * @param file A String giving the file name/location.
     * @param frame A top level container.
     * @return An ArrayList containing sequences and their identifiers. The first
     * entry will be an identifier. The second entry will be its related sequence.
     */
    public static ArrayList<String> readFASTA(File file, JFrame frame){
        Progressor p  = new Progressor(frame, 0, (int) file.length(), "Reading from disk");
        p.start();
        ArrayList<String> temp = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String id = "";
            String sequence = "";
            String line = reader.readLine();
            while(line != null){
                if(line.startsWith(">")){ id = line; }
                else{ sequence += line; }
                line = reader.readLine();
                if(line == null || line.startsWith(">") ){
                    p.incrementProgress(sequence.length()+id.length());
                    temp.add(id); temp.add(sequence); id = ""; sequence = "";
                }
            }//end while.
            reader.close();
            p.doneThanks();
        } catch (IOException ex) {
            Logger.getLogger(TextReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return temp;
    }//end method.

}//end class.
