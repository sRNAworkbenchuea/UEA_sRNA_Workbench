package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.ViewerGUI;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WFModuleFailedException;
/**
 * A module to annotate database sequences with different sources of annotation
 * e.g. GFF genes, RFAM fasta file, miRBase miRNA hairpins
 * @author Matthew Beckers
 */
public class AnnotationWorkflowModule extends WorkflowModule{

    public AnnotationWorkflowModule()
    {
        super("Annotation", "Annotation");
    }
    
    @Override
    protected void process() throws WFModuleFailedException {
        // Get parameters
        // call respective readers for input files
        // When all file readers complete, execute annotation methods
    }

}
