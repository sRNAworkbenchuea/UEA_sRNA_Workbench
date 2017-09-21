package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Pair_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;

/**
 *
 * @author Matthew
 */
public class SamplePairManager {
    
    FilenameServiceImpl fileServ = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
    List<Sample_Pair_Entity> samplePairs = new ArrayList<>();
    Collection<String> filenames = new HashSet<>();
    
    public void addSamplePair(String referenceSampleId, String observedSampleId) throws NotInDatabaseException
    {
        FilePair pair = new FilePair(referenceSampleId, observedSampleId);
        Sample_Pair_Entity samplePair = fileServ.getSamplePair(pair);
        if(samplePair == null)
        {
            throw new NotInDatabaseException("One of the samples IDs" + referenceSampleId + ", " + observedSampleId + " is not in the database");
        }
        this.samplePairs.add(samplePair);
    }
    
    public boolean isEmpty()
    {
        return samplePairs.isEmpty();
    }
    
    public List<Sample_Pair_Entity> getSamplePairs()
    {
        
        return this.samplePairs;
    }
    
    public Collection<Filename_Entity> getFilenames()
    {
        List<Filename_Entity> files = new ArrayList<>();
        if(this.filenames.isEmpty())
        {
            for(Sample_Pair_Entity pair : samplePairs)
            {
                files.addAll(pair.getReference().getFilenames());
                files.addAll(pair.getObserved().getFilenames());
            }
        }
        return files;
    }
    
}
