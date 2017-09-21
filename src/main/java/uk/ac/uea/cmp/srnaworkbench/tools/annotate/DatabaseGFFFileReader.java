package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.GFFDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.ReferenceSequenceSetDAO;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Reference_Sequence_Set_Entity;
import uk.ac.uea.cmp.srnaworkbench.io.GFFFileReader;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import uk.ac.uea.cmp.srnaworkbench.utils.StringPool;

/**
 * Reads in a GFF file into the Database
 * @author matt
 */
public class DatabaseGFFFileReader extends GFFFileReader {
    
    // the reference we will be adding to
    ReferenceSequenceManager genomeReference = new ReferenceSequenceManager(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
    
    public DatabaseGFFFileReader(File gffFile)
    {
        super(gffFile);
    }
    
    @Override
    public boolean processFile()
    {
        
        BufferedReader br = FileUtils.createBufferedReader(_gffFile);

        if (br == null) {
            return false;
        }
        
        
        // TODO: remove the need for this exception
        if(this._typePriorityMap.isEmpty())
        {
            throw new UnsupportedOperationException("Database GFF file reader currently "
                    + "does not support reading in a GFF with no user specified target types");
        }
        
        // Store targeted types
        // Temporary hashmap to store and retrieve the persisted entities
        // this is used later for quick non-db access to the type entities
        // when adding them to the GFF_Entity
        Map<String, Annotation_Type_Entity> type_entity_map = new HashMap<>();
        
        // iterating over each available type and creating new type entities
//        Iterator<String> typeIt = this._typePriorityMap.keySet().iterator();
//        while(typeIt.hasNext())
//        {
//            String type = typeIt.next();
//            this.genomeReference.addType(type);
//            //Annotation_Type_Entity typeE = new Annotation_Type_Entity(genomeRef, type, this._typePriorityMap.get(type));
//            //type_entity_map.put(type, typeE);
//        }

        StringPool pool = new StringPool();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#"))//ignore comment lines
                {
                    GFFRecord gffrec = processLine(line, pool);

                    if (gffrec != null)
                    {
                        genomeReference.addGFFRecord(gffrec);
                    }
                }
            }
        } catch (IOException e) {
            WorkbenchLogger.LOGGER.log(Level.SEVERE, null, e);
            return false;
        } finally {
            IOUtils.closeQuietly(br);
            br = null;
            pool.clear();
        }

        return true;
    }
    

}
