package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Reference_Sequence_Set_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.DuplicateReferenceException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.PoorIntegrityException;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * An object for managing annotations for a reference set.
 * This is an object backed by a ServiceLayer and database.
 * @author Matthew
 */
public class ReferenceSequenceManager {
    
    /* Default reference and type definitions */
    public static final String GENOME_REFERENCE_NAME = "genome";
    public static final String INTERGENIC_TYPE_NAME = "intergenic";
    public static final String NO_REFERENCE_NAME = "none";
    public static final String NO_TYPE_NAME = "none";


    AnnotationService service = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
     
    // The entity that this object will be organising
    String referenceSetName;
    
    public ReferenceSequenceManager(String referenceSetName)
    {
        // create a new Reference_Sequence_Set
        // Check if this reference set is already in the database
        // if not, add it with a new priority
        this.referenceSetName = referenceSetName;
        
        // Try to add the reference. We will be using a currently in-database if this doesn't work
        try {
            service.addReference(referenceSetName);
        } catch (DuplicateReferenceException ex) {
            // This is not an issue, we are now just editing a current reference, which is hopefully wha the end user wanted.
            LOGGER.log(Level.WARNING, "Reference set {0} already in database. Attaching to the already persisted reference", this.referenceSetName);
        }
    }
    
   public void addType(String type)
   {
        try {
            this.service.addType(referenceSetName, type);
        } catch (ChildBeforeParentException ex) {
            Logger.getLogger(ReferenceSequenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Worrying ChildBeforeParentException. Bailing.");
        }
   }
    

    public void addKeyword(String type, String keyword){
        try {
            //        Annotation_Type_Entity type_e = this.typeMap.get(type);
//        this.keyword2type.put(keyword, type_e);
            this.service.addTypeKeyword(keyword, referenceSetName, type);
        } catch (ChildBeforeParentException ex) {
            Logger.getLogger(ReferenceSequenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Worrying case of ChildBeforeParent exception");
        }
    }
    
    /**
     * Add a GFFRecord to this reference sequence
     * @param gff 
     */
    public void addGFFRecord(GFFRecord gff)
    {
        this.service.addGFFRecord(gff, this.referenceSetName);
    }
    
    /**
     * Setting this means that there is only one type associated with this
     * reference. All other keywords/types will be ignored
     *
     * @param type
     */
    public void setPrimaryType(String type) {
        try {
            service.setPrimaryType(type, referenceSetName);
        } catch (AnnotationNotInDatabaseException ex) {
            Logger.getLogger(ReferenceSequenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("No reference in database found when there should be one");
        }
    }
    
    public String getPrimaryType()
    {
        return this.service.getPrimaryType(this.referenceSetName);
    }
    
    /**
     * Returns all types available for this reference sequence
     * @return 
     */
    public AnnotationSet getAllTypes() 
    {
        try
        {
            return this.service.getTypesForReference(this.referenceSetName);
        } catch (AnnotationNotInDatabaseException ex)
        {
            throw new RuntimeException("Annotation type not found in database where it should be");
        }
    }
    
    
    /**
     * Finds the best type for the given annotation identifier
     * @param identifier
     * @return 
     */
    public String findType(String identifier)
    {
        return service.findType(referenceSetName, identifier);
    }
    
//    public String getType(String keyphrase) throws NotInDatabaseException
//    {
//        try {
//            //        for (String key : this.keyword2type.keySet()) {
////            if (keyphrase.contains(key)) {
////                return this.keyword2type.get(key);
////            }
////        }
////        return null;
//            String type = service.getType(this.referenceSetName, keyphrase);
//            return type;
//        } catch (PoorIntegrityException ex) {
//            Logger.getLogger(Annotation_Types.class.getName()).log(Level.SEVERE, null, ex);
//            throw new RuntimeException("keywords are in the database but no types are! Bailing.");
//        }
//    }
    
    public static void main(String args[]) throws Exception
    {
        ReferenceSequenceManager annotationObj = new ReferenceSequenceManager("Rfam");
        annotationObj.addType("rRNA");
        annotationObj.addType("snoRNA");
        annotationObj.addType("ncRNA");

        annotationObj.addKeyword("rRNA", "5S");
        annotationObj.addKeyword("rRNA", "rRNA");
        annotationObj.addKeyword("snoRNA", "sno");
        
        annotationObj.service.printAnnotations();
        
        
        System.out.println(annotationObj.findType("432r4f;5sunit;moreheader"));
        System.out.println(annotationObj.findType("432r4f sno50 moreheader"));
        System.out.println(annotationObj.findType("432r4f perc4 moreheader"));

        annotationObj.setPrimaryType("ncRNA");
        System.out.println("Primary type set to " + annotationObj.getPrimaryType());
        System.out.println(annotationObj.findType("432r4f;5sunit;moreheader"));
        System.out.println(annotationObj.findType("432r4f sno50 moreheader"));
        System.out.println(annotationObj.findType("432r4f perc4 moreheader"));
        
        

    }
}