package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;

/**
 * Holds a set of annotation types that are validated against the database when
 * added. 
 * 
 * This is supposed to model a collection of related types that can be collected
 * under another name, e.g. "tRNA", "rRNA", "snoRNA" could be called "ncRNA"
 * @author Matthew Beckers
 */
public class AnnotationSet {
    
    private final String name;
    
    Set<String> annotations = new HashSet<>();
    AnnotationService service = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
    
    /**
     * Create a new annotation set with the specified name.
     * @param name 
     */
    public AnnotationSet(String name)
    {
        this.name = name;
    }
    
    public static AnnotationSet getAllSet()
    {
        AnnotationSet all = new AnnotationSet("All");
        all.addAllTypes();
        return all;
    }
    
    public static AnnotationSet getMappedSet()
    {
        AnnotationSet mapped = new AnnotationSet("Mapped");
        mapped.addMappedTypes();
        return mapped;
    }
    
    public void addAllTypes()
    {
        List<String> types = service.getAllTypes();
        for(String type : types)
        {
            try
            {
                this.addAnnotationType(type);
            } catch (AnnotationNotInDatabaseException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public void addMappedTypes()
    {
        List<String> types = service.getAllTypes();
        for (String type : types)
        {
            if(!type.equals(ReferenceSequenceManager.NO_TYPE_NAME))
            {
                try
                {
                    this.addAnnotationType(type);
                } catch (AnnotationNotInDatabaseException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void addAnnotationType(String typeName) throws AnnotationNotInDatabaseException
    {
        List<Annotation_Type_Entity> matchingTypes = this.service.getTypesByName(typeName);
        if(matchingTypes.isEmpty())
            throw new AnnotationNotInDatabaseException("Annotation type " + typeName + " is not in the database ");
        
        this.annotations.add(matchingTypes.get(0).getId().getType());
    }
    
    public Set<String> getTypes()
    {
        return this.annotations;
    }
    
}
