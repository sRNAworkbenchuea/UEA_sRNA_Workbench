package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;

/**
 * A list of AnnotationSets
 * A convenience object that provides a way of holding a list of AnnotationsSets,
 * adding a single type as an AnnotationSet, adding an AnnotationSet, returning all
 * AnnotationSets and returning a flat list all Annotation types associated
 * with all sets.
 * @author Matthew
 */
public class AnnotationSetList {
    
    List<AnnotationSet> annotationSets = new ArrayList<>();
    Set<String> allTypes = new HashSet<>();
    
    public AnnotationSetList(List<AnnotationSet> annotationSets)
    {
        this.annotationSets = annotationSets;
        for(AnnotationSet aset : annotationSets)
        {
            for(String type : aset.getTypes())
            {
                this.allTypes.add(type);
            }
        }
    }
    
    public AnnotationSetList(){
        
    }
    
    /**
     * 
     * @return a default AnnotationSetList containing the following sets:
     * All, Mapped, Unmapped, intergenic
     */
    public static AnnotationSetList createAnnotationSetList() throws AnnotationNotInDatabaseException
    {
        AnnotationSetList annots = new AnnotationSetList();
        annots.addAnnotationSet(AnnotationSet.getAllSet());    // ... all sequences
        annots.addAnnotationSet(AnnotationSet.getMappedSet()); // ... mapped sequences
        annots.addType(ReferenceSequenceManager.NO_TYPE_NAME); // ... unmapped sequences
        annots.addType(ReferenceSequenceManager.INTERGENIC_TYPE_NAME);
        return annots;
    }
    
    public void addAnnotationSet(AnnotationSet annotationSet)
    {
        this.annotationSets.add(annotationSet);
        for(String type : annotationSet.getTypes())
        {
            this.allTypes.add(type);
        }
    }
    
    /**
     * Adds a single type to this list by converting it into an
     * AnnotationSet with the name of the type, holding only this type.
     * @param typeName
     * @throws AnnotationNotInDatabaseException 
     */
    public void addType(String typeName) throws AnnotationNotInDatabaseException
    {
        AnnotationSet singleTypeSet = new AnnotationSet(typeName);
        singleTypeSet.addAnnotationType(typeName);
        this.annotationSets.add(singleTypeSet);
    }
    
    public Set<String> getAllTypes()
    {
        return this.allTypes;
    }
    
    /**
     * 
     * @return The highest priority AnnotationSet that contains this type, or null
     * if the type is not in any set.
     * The priority of a set is determined by the order in which
     * sets were added to this object. The last set to be added has the highest
     * priority.
     */
    public AnnotationSet getSetForType(String type)
    {
        for(AnnotationSet thisSet : Lists.reverse(this.getAllSets()))
        {
            if(thisSet.getTypes().contains(type))
            {
                return thisSet;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param type
     * @return All Sets that contain type, ordered by priority
     */
    public List<AnnotationSet> getAllSetsForType(String type)
    {
        List<AnnotationSet> sets = new ArrayList<>();
//        for(AnnotationSet thisSet : Lists.reverse(this.getAllSets()))
        for(AnnotationSet thisSet : this.getAllSets())
        {
            if(thisSet.getTypes().contains(type))
            {
                sets.add(thisSet);
            }
        }
        return sets;
    }
    
    /**
     * Collapse all types in this set list to one AnnotationSet
     * @param name
     * @return 
     */
    public AnnotationSet toAnnotationSet(String name)
    {
        AnnotationSet as = new AnnotationSet(name);
        for(String type : this.allTypes)
        {
            try {
                as.addAnnotationType(type);
            } catch (AnnotationNotInDatabaseException ex) {
                throw new RuntimeException("Annotation type not found in the database where it should be");
            }
        }
        return as;
    }
   
    public List<AnnotationSet> getAllSets()
    {
        return this.annotationSets;
    }
    
}
