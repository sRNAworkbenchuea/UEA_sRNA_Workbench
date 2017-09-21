
package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.DuplicateReferenceException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.AlignedSequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.UniqueSequencesServiceImpl;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WFModuleFailedException;
/**
 *
 * @author Matthew B and S
 */
public class FastaAnnotationWorkflow extends WorkflowModule{
    
    private Path fastaFile;
    private String referenceSetName;
    private FastaAnnotationParams params;
    private ReferenceSequenceManager types;
    
    private AnnotationService service;
    
    
    /**
     * 
     * @param id
     * @param fastaFile the fasta file that sequences in the database will be aligned to
     * @param referenceSetName the name of this reference set of sequence e.g. "Rfam" if its the Rfam database
     * @param params optional extra params, such as settings for converting the fasta headers to appropriate seqids
     */
    public FastaAnnotationWorkflow(String id, Path fastaFile, String referenceSetName, FastaAnnotationParams params)
    {
        super(id, "FastA Annotation");
        this.fastaFile = fastaFile;
        this.referenceSetName = referenceSetName;
        this.params = params;
        this.types = new ReferenceSequenceManager(this.referenceSetName);
        this.service = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
    }
    
    public void addType(String type)
    {
        this.types.addType(type);
    }
    
    public void addTypeKeyword(String keyword, String type)
    {
        this.types.addKeyword(type, keyword);
    }
    
     public void setPrimaryType(String type) {
         
         this.types.setPrimaryType(type);  
    }
    
    /**
     * Map the combined read file used by the Database to align against our
     * selected fasta file.
     *
     * @return the generated patman file
     */
    private File mapReads() {
        Path combinedReadFile = DatabaseWorkflowModule.getCombinedFastaFile();
        String alignedFile = Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + fastaFile.getFileName() + "_annotated.patman";
        File patman_out = new File(alignedFile);

        PatmanParams patmanParams = new PatmanParams();
        patmanParams.setPreProcess(false);
        patmanParams.setPostProcess(false);
        patmanParams.setMakeNR(false);
        Thread myThread = new Thread(new PatmanRunner(combinedReadFile.toFile(), fastaFile.toFile(),
                patman_out, Tools.getNextDirectory(), patmanParams));

        myThread.start();
        try {
            myThread.join();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return patman_out;
    }
    
    /**
     * Read alignments from a patman file into the database
     */
    private void processAlignments(File patmanFile) throws FileNotFoundException, IOException
    {
        AlignedSequenceServiceImpl alignedService = (AlignedSequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("AlignedSequenceService");
        Annotation_Type_Entity defaultType;
        
        BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( patmanFile ) ) );
        
        String line;
        while( (line = br.readLine()) != null)
        {
//            System.out.println("Line: " + line);
            PatmanEntry pe = PatmanEntry.parse(line);
            if (pe != null)
            {
                String header = pe.getLongReadHeader();

                String type = types.findType(header);

                System.out.println("type: " + type);
                // determine the shortened seqid
                String seqid = header;
                String delimiter = params.getDelimiter();
                int idColumn = params.getColumnNumber();
                if (delimiter != null)
                {
                    String[] headerCols = header.split(delimiter);
                    seqid = headerCols[idColumn];
                }

                System.out.println(seqid + " " + pe.getSequence());
                try
                {
                    //            String seq = pe.getSequence();

//            Aligned_Sequences_Entity aseq = new Aligned_Sequences_Entity(this.referenceSetName, seqid, seq,
//                pe.getStart(), pe.getEnd(), pe.getSequenceStrand().getCode(), pe.getMismatches());
//            aseq.setAnnotationType(type);
//            System.out.println("\tAdding alignment " + aseq + " from seqid " + seqid);
                    this.service.addPatmanAlignment(referenceSetName, seqid, pe, type);
                }
                catch (ChildBeforeParentException ex)
                {
                    Logger.getLogger(FastaAnnotationWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
        }

    }

    @Override
    protected void process() throws WFModuleFailedException {
        File patmanFile = this.mapReads();
        
        System.out.println("Processing patman alignments");
        try {
            this.processAlignments(patmanFile);
        } catch (IOException ex) {
            Logger.getLogger(FastaAnnotationWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Updating unique sequences");
        UniqueSequencesServiceImpl uniqueService = (UniqueSequencesServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("UniqueSequencesService");
        uniqueService.updateConsensusAnnotationTypes(this.referenceSetName);
    }
    
}
