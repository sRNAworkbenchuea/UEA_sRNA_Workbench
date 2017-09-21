package uk.ac.uea.cmp.srnaworkbench.tools.locifilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Interaction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

public class LociFilterModule extends WorkflowModule {

    // inputs
    private final DataContainerList<HQLQuerySimple> in_srnaomeQuery;
    private final DataContainerList<HQLQuerySimple> in_targetQuery;
    // private final DataContainerList<GenomeManager> in_genome;
    private final DataContainerList<HQLQuerySimple> out_query;
    private LociFilterController lociController;
    private boolean complete;
    private boolean enable;

    public LociFilterModule(String id, String title, Rectangle2D visualBounds) {
        super(id, title);
        this.complete = false;
        this.enable = true;
        // inputs
        this.in_srnaomeQuery = new DataContainerList<>("srnaomeQuery", CompatibilityKey.sRNA_QUERY, 1, -1);
        this.in_targetQuery = new DataContainerList<>("targetQuery", CompatibilityKey.INTERACTION_QUERY, 1, -1);
        //this.in_genome = new DataContainerList<>("genome", CompatibilityKey.GENOME, 1, -1);
        // outputs
        this.out_query = new DataContainerList<>("output", CompatibilityKey.sRNA_QUERY, 1, 1);

        setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + "LociFilterScene.fxml");
        lociController = new LociFilterController(this, visualBounds);
        this.controller = lociController;

        try {
            this.out_query.add(new DataContainer<>(CompatibilityKey.sRNA_QUERY, new HQLQuerySimple(Sequence_Entity.class)));//DetachedCriteria.forClass(Interaction_Entity.class)));

            addInputDataContainerList(this.in_srnaomeQuery);
            addInputDataContainerList(this.in_targetQuery);
            addOutputDataContainerList(this.out_query);
            //   addInputDataContainerList(this.in_genome);
        } catch (InitialisationException | DuplicateIDException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (MaximumCapacityException | CompatibilityException ex) {
            Logger.getLogger(LociFilterModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isComplete() {
        return this.complete;
    }

    @Override
    public void process() throws HQLQuery.HQLFormatException, FileNotFoundException, Exception {

        lociController.write2Log("INFORMATION: Locifilter module started.");
        if (!enable) {
            HQLQuerySimple q = this.out_query.getContainer(0).getData();
            q.addWhere(String.format("A.id IN (%s)", this.in_srnaomeQuery.getContainer(0).getData().eval()));
            // this.out_query.getContainer(0).getData()this.in_srnaomeQuery.getContainer(0).getData();
            this.complete = true;
            this.lociController.updateUI();
            return;
        }
        lociController.write2Log("INFORMATION: Getting alignments.");
        // copy the in sRNA sequence query
        HQLQuerySimple sSimpleQuery = this.in_srnaomeQuery.getContainer(0).getData();
        HQLQueryComplex sQuery = new HQLQueryComplex(sSimpleQuery);
        sQuery.addSelect(sSimpleQuery.getFromAlias() + ".id", "id");

        // copy the in interaction  query
        HQLQuerySimple iQuerySimple = new HQLQuerySimple(this.in_targetQuery.getContainer(0).getData());
        HQLQueryComplex iQuery = new HQLQueryComplex(iQuerySimple);
        iQuery.addSelect(iQuerySimple.getFromAlias() + ".id", "id");

        SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");

        Map<String, LinkedList<Pair<Integer, Integer>>> all_loci = new HashMap<>();

        HQLQueryComplex targetSequencesAlignmentQuery = new HQLQueryComplex();
        targetSequencesAlignmentQuery.addSelect("A.rna_sequence", "sequence");
        targetSequencesAlignmentQuery.addSelect("A.id.chrom", "chr");
        targetSequencesAlignmentQuery.addSelect("A.id.start", "start");
        targetSequencesAlignmentQuery.addSelect("A.id.end", "end");
        targetSequencesAlignmentQuery.addFrom(Aligned_Sequences_Entity.class, "A");
        targetSequencesAlignmentQuery.addFrom(Interaction_Entity.class, "I1");
        targetSequencesAlignmentQuery.addFrom(Interaction_Entity.class, "I2");
        targetSequencesAlignmentQuery.addWhere("I1.sRNA = A.rna_sequence");
        targetSequencesAlignmentQuery.addWhere("I2.id IN (" + iQuery.eval() + ")");
        targetSequencesAlignmentQuery.addWhere("I1.sRNA = I2.sRNA");
        targetSequencesAlignmentQuery.addOrder("A.id.chrom", HQLQuery.HQL_DIR.ASC);
        targetSequencesAlignmentQuery.addOrder("A.id.start", HQLQuery.HQL_DIR.ASC);
        targetSequencesAlignmentQuery.addOrder("A.id.end", HQLQuery.HQL_DIR.ASC);

        PrintWriter writer = new PrintWriter(new File("testalignment.txt"));
        lociController.write2Log("INFORMATION: Computing Loci.");
        int extend = 400;

        List<Map<String, Object>> records = sequence_service.executeGenericSQL(targetSequencesAlignmentQuery.eval());
        for (Map<String, Object> record : records) {
            for (String s : record.keySet()) {
                //       writer.print(s + ":" + record.get(s));
            }
            //   writer.println("");
            String chr = record.get("chr").toString();
            int start = Integer.parseInt(record.get("start").toString());
            int end = Integer.parseInt(record.get("end").toString());
            // do not process alignments where start or end is negative (too many mappings)
            if (start >= 0 && end >= 0) {
                start -= extend;
                end += extend;
                if (!all_loci.containsKey(chr)) {
                    LinkedList<Pair<Integer, Integer>> loci = new LinkedList<>();
                    loci.add(new Pair<>(start, end));
                    all_loci.put(chr, loci);
                } else {
                    int currentLocusStart = all_loci.get(chr).getLast().getKey();
                    int currentLocusEnd = all_loci.get(chr).getLast().getValue();
                    // if the start of this mapping is before the end of the current locus
                    if (start < currentLocusEnd && end > currentLocusEnd) {
                        all_loci.get(chr).set(all_loci.get(chr).size() - 1, new Pair<>(currentLocusStart, end));
                    } else if (start > currentLocusEnd) {
                        all_loci.get(chr).add(new Pair<>(start, end));
                    }
                }
            }
        }
        lociController.write2Log("INFORMATION: Mapping sRNAs to computed loci.");
        HQLQueryComplex lociSequencesAlignmentQuery = new HQLQueryComplex();
        lociSequencesAlignmentQuery.addSelect("S.id", "id");
        lociSequencesAlignmentQuery.addFrom(Aligned_Sequences_Entity.class, "A");
        lociSequencesAlignmentQuery.addFrom(Sequence_Entity.class, "S");
        String whereCondition = "";
        for (String chr : all_loci.keySet()) {
            for (Pair<Integer, Integer> loci : all_loci.get(chr)) {
                String locusCondition = "A.id.chrom = '" + chr + "' AND A.id.start >= " + loci.getKey() + " AND A.id.end <= " + loci.getValue();
                if (!whereCondition.isEmpty()) {
                    whereCondition += String.format(" OR %s", locusCondition);
                } else {
                    whereCondition = locusCondition;
                }
            }
        }
        lociController.write2Log("INFORMATION: Saving results to database.");
        records = sequence_service.executeGenericSQL(sQuery.eval());
        for (Map<String, Object> record : records) {
            for (String s : record.keySet()) {
                writer.print(s + ":" + record.get(s));
            }
            writer.println("");
        }
        writer.close();

        lociSequencesAlignmentQuery.addWhere(whereCondition);
        lociSequencesAlignmentQuery.addWhere("S.RNA_Sequence = A.rna_sequence");
        //lociSequencesAlignmentQuery.addWhere(String.format("S.id IN (%s)", sQuery));

        HQLQuerySimple query = this.out_query.getContainer(0).getData();
        query.addWhere(String.format("A.id IN (%s)", sQuery.eval()));
        query.lock();

        //System.out.println("INCOMING SQL: " + this.in_srnaomeQuery.getContainer(0).getData().toString());
        //printQuery(sequence_service.executeGenericSQL(query.eval()), query.eval(), new File("srnaomeQ.txt"));
        //printQuery(sequence_service.executeGenericSQL(this.in_srnaomeQuery.getContainer(0).getData().toString()), this.in_srnaomeQuery.getContainer(0).getData().toString(), new File("inQ.txt"));
        // System.out.println("srnaomeeval: " + query.eval());
//       ;
        // System.out.println("q: " + q.eval());
        // this.out_query.getContainer(0).getData().append(query.eval());
        writer.close();

        for (String chr : all_loci.keySet()) {
            System.out.println("chromosome: " + chr);
            System.out.println("there are " + all_loci.get(chr).size() + " loci");
            //  for(Pair<Integer, Integer> locus : all_loci.get(chr))
            // {
            //    System.out.println("locus: " + locus.getKey() + " : " + locus.getValue());
            //}
        }
        // get a list of ordered target sequences in order of chromosome followed by start position

        //List<Sequence_Entity> sequences = sequence_service.executeSQL(this.in_targetQuery.getContainer(0).getData().toString());
        // keep a list of loci (chromosome + start and end position)
        // loop through the target sequences: if the start position extends beyond the last loci end position (inc. offset) then create new loci
        // for each sequence in the target input
        //   for (Sequence_Entity sequence : sequences) {
        // get all of the alignments
        //     for (Aligned_Sequences_Entity aligned_sequence : sequence.getUnique_sequence().getAlignedSequenceRelationships()) {
        //      }
        // }
        this.complete = true;
        this.lociController.updateUI();
        lociController.write2Log("INFORMATION: Module complete.");

    }

    /*
     * returns a query of the PAREsnip results for display purposes
     */
    public HQLQuery generateOutput() throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {

        HQLQueryComplex hqlQuery = new HQLQueryComplex(this.out_query.getContainer(0).getData());
        hqlQuery.addSelect("A.RNA_Sequence", "RNA_Sequence");
        return hqlQuery;
    }

    public static void printQuery(List<Map<String, Object>> result, String header, File f) throws FileNotFoundException {
        PrintWriter w = new PrintWriter(f);
        w.println(header);
        for (Map<String, Object> record : result) {
            for (String key : record.keySet()) {
                w.print(key + ":" + record.get(key) + " ");
            }
            w.println();
        }
        w.close();
    }

}
