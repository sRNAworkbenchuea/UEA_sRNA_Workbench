/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.io;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.ScrollableResults;

/**
 * This class is supposed to take the pain out of writing a json out sequentially
 * by ordering a list of results and writing them out with JsonGenerator.
 * 
 * To use, implement this class and override writeBottomLevel and getDiff as well
 * as implementing your specific JsonDTO. See javadoc for each of these.
 * 
 * The constructor then accepts two lists. The first is the String identifiers for objects at each hierachy
 * i.e. a Normalisation object or Sample object. The second is a list of String identifiers for the arrays holding each of the objects
 * that were placed at the same index in the previous array. i.e. "Normalisations" would be the holding array
 * for "Normalisation"
 * @author matt
 */
public abstract class JsonWriter <JDTO extends JsonDTO> {
    
    /**
     * A static JsonFactory to use to create all JsonGenerators
     */
    public static final JsonFactory jsonFactory = new JsonFactory();
        
    Levels levels;
    
    /**
     * The constructor then accepts two lists. The first is the String identifiers for objects at each hierachy
     * i.e. a Normalisation object or Sample object. The second is a list of String identifiers for the arrays holding each of the objects
     * that were placed at the same index in the previous array. i.e. "Normalisations" would be the holding array
     * for "Normalisation"
     * @param hierachicalCategories
     * @param holdingArrays
     * @param factory
     * @param outputFile
     * @param result
     * @throws IOException 
     */
    public JsonWriter (List<String> hierachicalCategories, List<String> holdingArrays) throws IOException
    {
        //this.categories = new CategoryList(hierachicalCategories);
        levels = new Levels(hierachicalCategories, holdingArrays);
    }
    /**
     * Write bottom level of Json using the DTO for this JsonWriter
     * 
     * This should be overidden by using the JsonGenerator to write out and object containing
     * data in the bottom level of the Json structure. For example this can be two key-value pairs
     * detailing M and A values.
     * @param obj the DTO object to get your data for this table row from
     * @param jg Use this to write out the desired data that exists in the bottom level
     */
    public abstract void writeBottomLevel(JDTO obj, JsonGenerator jg) throws IOException;
    
    /**
     * Override this with if-else code to find the hierachical difference between
     * two of your DTOs. This check each of the value types in order from the top
     * of the Json structure to the bottom and return an identifier for the first one that is different
     * in the two objects. The identifier MUST match one of the Object identifiers given in the constructor
     * @param dto
     * @param other
     * @return 
     */
    public abstract String getDiff(JDTO dto, JDTO other);
            
    /**
     * Writes the given ResultSet to to a json file.
     * @param outputFile A Path defining the output file to write this JSON to.
     * @param result The returned ScrollableResults compatible with this JsonWriter
     * @throws IOException 
     */
    public void writeJson(Path outputFile, ScrollableResults result) throws IOException 
    {
        JsonGenerator jg = JsonWriter.jsonFactory.createGenerator(outputFile.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        try
        {
            start(jg);
            JDTO lastdto = null;
            if(result.next())
            {
                lastdto = (JDTO) result.get(0);
                System.out.println(lastdto.toString());
                //writeObject(lastdto, categories.next());
                levels.writeLevels(lastdto, jg);
                this.writeBottomLevel(lastdto, jg);
            }
            while(result.next())
            {
                JDTO thisdto = (JDTO) result.get(0);
                System.out.println(thisdto.toString());

                String change = this.getDiff(thisdto, lastdto);
//                addObject(change, thisdto);
                levels.findLevel(change, jg);
                levels.writeLevels(thisdto, jg);
                this.writeBottomLevel(thisdto, jg);
                lastdto = thisdto;
            }
            finish(jg);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            jg.close();
        }
    }
    
    public void start(JsonGenerator jg) throws IOException
    {
        jg.writeStartObject();
    }
    
    public void finish(JsonGenerator jg) throws IOException
    {
        levels.findLevel("TOP_LEVEL", jg);
        //jg.writeEndObject();
    }

    private static class JsonWriterException extends Exception {

        public JsonWriterException(IOException e) {
            super(e);
        }
    }
    
    class Levels 
    {
        List<Level> levels;
        int p = 0;
        Levels(List<String> objects, List<String> arrays)
        {
            levels = new ArrayList<>();
            for(int i = 0; i < (arrays.size()); i++)
            {
                if(i < arrays.size())
                    levels.add(new ArrayLevel(arrays.get(i)));
                if(i < objects.size())
                    levels.add(new ObjectLevel(objects.get(i)));
            }
        }
        
        void findLevel(String name, JsonGenerator jg) throws IOException
        {
            Level current = levels.get(p);
            while(p > 0 ){
                current.writeEnd(jg);
                if(current.getName().equals(name))
                    return;
                p--;
                current = levels.get(p);             
            }
            
        }
        
        void writeLevels(JDTO dto, JsonGenerator jg) throws IOException
        {
            while(p < levels.size())
            {
                Level current = levels.get(p);
                current.writeStart(jg, dto);
                p++;                
            }
            p--;
        }
        

    }
    
    abstract class Level
    {
        String name;
        
        Level (String name)
        {
            this.name = name;
        }
        
        String getName()
        {
            return name;
        }
        
        abstract void writeStart(JsonGenerator jg, JDTO dto) throws IOException;
        abstract void writeEnd(JsonGenerator jg) throws IOException;

        
        
    }
    
    class ArrayLevel extends Level
    {
        ArrayLevel(String name)
        {
            super(name);
        }
        
        @Override
        void writeStart(JsonGenerator jg, JDTO dto) throws IOException
        {
            jg.writeArrayFieldStart(name);
        }
        
        @Override
        void writeEnd(JsonGenerator jg) throws IOException {
            jg.writeEndArray();
        }
        
    }
    
    class ObjectLevel extends Level
    {
        ObjectLevel(String name) {
            super(name);
        }
        
        @Override
        void writeStart(JsonGenerator jg, JDTO dto) throws IOException 
        {
            jg.writeStartObject();
            jg.writeObjectField(name, dto.getElement(name));
        }
        
        @Override
        void writeEnd(JsonGenerator jg) throws IOException
        {
            jg.writeEndObject();
        }
    }

}
