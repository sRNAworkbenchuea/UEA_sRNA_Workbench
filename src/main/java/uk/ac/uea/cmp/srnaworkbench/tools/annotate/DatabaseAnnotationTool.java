/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import java.nio.file.Path;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;

/**
 *
 * @author Matthew
 */
public class DatabaseAnnotationTool extends RunnableTool {
    List<Path> gffFiles;
    List<String> annotations;
    List<String> otherAnnotations;

    public DatabaseAnnotationTool(List<Path> gffFiles, List<String> annotations, List<String> otherAnnotations) {
        super("DatabaseAnnotation");
        this.gffFiles = gffFiles;
        this.annotations = annotations;
        this.otherAnnotations = otherAnnotations;
    }
    
    @Override
    protected void process() throws Exception {
        for(Path file : gffFiles)
        {
            System.out.println("gff file: " + file.getFileName());
            DatabaseGFFFileReader reader = new DatabaseGFFFileReader(file.toFile());
            for(String annotation : annotations)
            {
                System.out.println("annotation: " + annotation);
                reader.addSequenceTypeFilter(annotation);
            }
            for(String annotation : otherAnnotations)
            {
                 System.out.println("other annotations: " + annotation);
                reader.addSequenceTypeFilter(annotation);
            }
            reader.run();
        }
        GFFAnnotationServiceLayer service = (GFFAnnotationServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("GFFAnnotationService");
        AnnotationService aserv = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
        service.annotateAlignments();
        //aserv.printGFFtable();
    }
    
}
