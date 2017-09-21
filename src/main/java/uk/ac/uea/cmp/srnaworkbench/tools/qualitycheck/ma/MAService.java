package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.MADAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.UniqueSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.MA_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.MA_NR_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NoSuchExpressionValueException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;

/**
 *
 * @author Matthew
 */
@Service("MAService")
@Transactional
public class MAService {
    @Autowired
    private AlignedSequenceDAOImpl alignedSeqDao;

    @Autowired
    private UniqueSequenceDAOImpl uniqueSeqDao;
    
    @Autowired
    private MADAOImpl maDao;
    
    Logarithm MA_LOG = Logarithm.BASE_2;
    
    public void buildNonRedundantListOnRaw(String referenceFileID, Collection<String> observedFileIDs, Map<String, Double> fileTotals, Collection<String> annotationTypes, List<Integer> offsets, List<Logarithm> logarithms){
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
        Batcher batcher = new Batcher(session);
        StopWatch sw = new StopWatch("MAs");
        sw.start();
        
        // Hibernate requires that I name all columns in a self-join, otherwise it will confuse column names as all beloning to the reference join
        String maSQL = 
        "SELECT ref.Abundance AS ra, obs.Abundance AS oa, ref.File_ID AS rf, obs.File_ID AS of, count(ref.RNA_Sequence), ref.Type, ref.RNA_Size "+
            "FROM (SELECT S.RNA_Sequence, S.Abundance, S.File_ID, U.Type, U.RNA_Size FROM Sequences S "
                + "JOIN Unique_Sequences U ON S.RNA_Sequence=U.RNA_Sequence AND U.Type IN (:atypes) AND S.File_ID=:ref) AS ref "+
        "JOIN (SELECT T.RNA_Sequence, T.Abundance, T.File_ID FROM Sequences T WHERE T.File_ID IN (:obs)) AS obs ON ref.RNA_Sequence=obs.RNA_Sequence  "+
        "GROUP BY  ra, oa, ref.Type, ref.RNA_Size, of";

        ScrollableResults mas = session.createSQLQuery(maSQL).setParameter("ref", referenceFileID).setParameterList("obs", observedFileIDs).setParameterList("atypes", annotationTypes).scroll();
        sw.lap("Query for NR MAs");
        while (mas.next()) {

            double refExp = (int) mas.get(0);
            double obsExp = (int) mas.get(1);
            String refSample = (String) mas.get(2);
            String obsSample = (String) mas.get(3);
            int count = ((BigInteger) mas.get(4)).intValue();
            String type = (String) mas.get(5);
            int size = (int) mas.get(6);

            for (int offset : offsets) {
                for (Logarithm log : logarithms) {
                    double m = MAelement.calcM(refExp, obsExp, log.getBase(), offset);
                    double a = MAelement.calcA(refExp, obsExp, log.getBase(), offset);
                    MA_NR_Entity ma = new MA_NR_Entity(m, a, refExp, obsExp, new FilePair(refSample, obsSample), NormalisationType.NONE, type, count, offset, log.getBase(), size);

                    double w = MAelement.calcWeighting(refExp, fileTotals.get(refSample), obsExp, fileTotals.get(obsSample));
                    ma.setWeighting(w);

                    session.save(ma);
                    batcher.batchFlush();
                }
            }
        }
        batcher.finish();
        sw.lap("Inserting NR MAs");
        sw.stop();
        sw.printTimes();
        
        session.close();
    }
    
    public void clearMAdata()
    {
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
        session.createQuery("delete from MA_NR_Entity").executeUpdate();
        session.close();
    }
    
    public void buildNonRedundantList(List<FilePair> filePairs, List<NormalisationType> normTypes, Collection<String> annotationTypes, List<Integer> offsets, List<Logarithm> logarithms, 
            boolean proportions, boolean weighting) {
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
        Batcher batcher = new Batcher(session);
        
        List<String> whereBits = new ArrayList<>();
        List<String> refFiles = new ArrayList<>();
        List<String> obsFiles = new ArrayList<>();
        
        for(FilePair pair : filePairs){
            whereBits.add("(ref.File_ID='" + pair.getReference() + "' AND obs.File_ID='" + pair.getObserved() + "')");
            if(!refFiles.contains(pair.getReference()))
                refFiles.add(pair.getReference());
            
            if(!refFiles.contains(pair.getObserved()))
                obsFiles.add(pair.getObserved());
        }
        String where = " AND (" + String.join(" OR ", whereBits) + ") ";
        
        List<Integer> normOrds = new ArrayList<>();
        for(NormalisationType norm : normTypes)
        {
            normOrds.add(norm.ordinal());
        }
            
            
            String maSQL1 = 
        "SELECT ref.Expression AS ra, obs.Expression AS oa, ref.File_ID AS rf, obs.File_ID AS of, ref.Normalisation_Type, count(ref.RNA_Sequence), ref.Type, ref.RNA_Size "
        + "FROM ( "
          + "SELECT S.RNA_Sequence, E.Normalisation_Type, E.Expression, S.File_ID, U.Type, U.RNA_Size "
          + "FROM Sequences S "
          + "JOIN Unique_Sequences U ON S.RNA_Sequence=U.RNA_Sequence  JOIN Sequence_Expressions E ON S.Sequence_Id=E.Sequence_Id AND E.Normalisation_Type IN (:normTypes) AND U.Type IN (:atypes) "
        + ") AS ref "
        + "JOIN ( "
          + "SELECT T.RNA_Sequence, E.Normalisation_Type, E.Expression, T.File_ID FROM Sequences T JOIN Sequence_Expressions E ON E.Sequence_Id=T.Sequence_Id "
        + ") AS obs ON ref.RNA_Sequence=obs.RNA_Sequence AND ref.Normalisation_Type=obs.Normalisation_Type "
        + where 
        + "GROUP BY  ra, oa, ref.Type, ref.RNA_Size, ref.Normalisation_Type, of";
            
        String maSQL = 
                "SELECT ref.Expression AS ra, "
+ "       obs.Expression AS oa, "
+ "	   ref.File_ID AS rf, "
+ "	   obs.File_ID AS of, "
+ "	   ref.Normalisation_Type, "
+ "	   count(ref.RNA_Sequence), "
+ "	   ref.Type, ref.RNA_Size "
+ "FROM ("
+ " SELECT S.RNA_Sequence, "
+ "        E.Normalisation_Type, "
+ "		E.Expression, "
+ "		S.File_ID, U.Type, U.RNA_Size"
+ " FROM Sequences AS S INNER JOIN "
+ "      Unique_Sequences AS U ON S.RNA_Sequence=U.RNA_Sequence AND U.Type IN (:atypes) INNER JOIN "
+ "      Sequence_Expressions E ON S.Sequence_Id=E.Sequence_Id AND E.Normalisation_Type IN (:normTypes)"
+ " WHERE s.File_ID IN (:refFiles) "
+ ") AS ref INNER JOIN ( "
+ " SELECT T.RNA_Sequence,"
+ "        E.Normalisation_Type, "
+ "		E.Expression, "
+ "		T.File_ID"
+ " FROM Sequences AS T INNER JOIN "
+ "      Sequence_Expressions AS E ON E.Sequence_Id=T.Sequence_Id"
+ " WHERE t.File_ID IN (:obsFiles) "
+ ") AS obs ON ref.RNA_Sequence=obs.RNA_Sequence AND ref.Normalisation_Type=obs.Normalisation_Type  "
                + where
+ "GROUP BY  ref.Expression, "
+ "          obs.Expression, "
+ "		  ref.Type, "
+ "		  ref.RNA_Size, "
+ "		  ref.Normalisation_Type, "
+ "		  obs.File_ID, "
                + "REF.FILE_ID ";
        
         StopWatch sw = new StopWatch("MA");  
            ScrollableResults mas = session.createSQLQuery(maSQL)
                    .setParameterList("normTypes", normOrds)
                    .setParameterList("atypes", annotationTypes)
                    .setParameterList("refFiles", refFiles)
                    .setParameterList("obsFiles", obsFiles)
                    .scroll();
            
            sw.lap("Query");
            while(mas.next())
            {
                double refExp = (double) mas.get(0);
                double obsExp = (double) mas.get(1);
                String refSample = (String) mas.get(2);
                String obsSample = (String) mas.get(3);
                NormalisationType normType = NormalisationType.values()[(int) mas.get(4)];
                BigInteger count = (BigInteger) mas.get(5);
                String type = (String) mas.get(6);
                int size = (int) mas.get(7);
                
                for(int offset : offsets){
                    for(Logarithm log : logarithms){
                        double m = MAelement.calcM(refExp, obsExp, log.getBase(), offset);
                        double a = MAelement.calcA(refExp, obsExp, log.getBase(), offset);
                        MA_NR_Entity ma = new MA_NR_Entity(m, a, refExp, obsExp, new FilePair(refSample, obsSample), normType, type, count.intValue(), offset, log.getBase(), size);
                        session.save(ma);
                        batcher.batchFlush();
                    }
                }
            }
        sw.lap("Scroll");
        sw.stop();
        sw.printTimes();
        batcher.finish();
        session.close();
    }
        
    // TODO: Check that this set of MA Elements is already cached in database before
    // calculating
    /**
     * Builds a table of MA entries based on the specified file pairs, normalisations,
     * annotation types, offsets, logarithms and whether the counts should be proportions of
     * the total library sizes or not.
     * 
     * A warning is produced if proportions are used with an offset that is not 0. This is because this behaviour
     * is untested.
     */
    public void buildList(List<FilePair> filePairs, List<NormalisationType> normTypes, Collection<String> annotationTypes, List<Integer> offsets, List<Logarithm> logarithms, 
            boolean proportions, boolean weighting) {

        System.out.println("Building MA table");
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
        Batcher batcher = new Batcher(session);
        session.createSQLQuery("DELETE FROM MA_VALUES").executeUpdate();
        
        Query query = session.createQuery(
                "from Sequence_Entity s "
                        + "join s.unique_sequence u "
                        + "join u.type t "
                        + "join s.expressions e "
                        + "where t.id.type in (:types) "
                        + "order by s.RNA_Sequence asc")
                .setParameterList("types", annotationTypes);

        ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
        DatabaseWorkflowModule.getInstance().printLap("MA query");
        
        for(int offset : offsets)
        {
            if (offset != 0 && proportions) {
                LOGGER.log(Level.WARNING, "MA values: Using an offset of {0} when also using proportional counts. This behaviour in untested and can potentially produce unexpected results.", offset);
            }
        }

        // stores the current sequence
        String currentSeq = "";
        // Build a map of files that contain the current sequence
        Map<String, Sequence_Entity> currentSeqH = new HashMap<>();

        System.out.println("Scrolling results");
        while (result.next()) {
            //Map map = (Map) iter.next();
            //Object[] obj = (Object[]) result.get(0);
            Sequence_Entity seq = (Sequence_Entity) result.get(0);
            
            String thisseq = seq.getRNA_Sequence();
            if (currentSeq.isEmpty()) {
                // first sequence in iteration
                currentSeq = thisseq;
            }

            if (currentSeq.equals(thisseq)) {
                // we are still on the current seq. Add to hash
                // FIXME: Managing to find 0.0 in normalised sequence columns.
                // suggests unmapped sequences are not being correctly filtered!
                if (seq.getAbundance() != 0.0) {
                    currentSeqH.put(seq.getFilename(), seq);
                }
            } else {
                // we are on a new sequence. Check to see if this sequence
                // is in the correct files and create an MA element if it is.
                createMAs(currentSeqH, filePairs, normTypes, offsets, logarithms, proportions, weighting);
                batcher.batchFlush();
                seq = (Sequence_Entity) session.merge(seq);
                // clear the hashmap ready for the next sequence
                currentSeqH = new HashMap<>();
                currentSeqH.put(seq.getFilename(), seq);
                // next sequence
                currentSeq = thisseq;
                
            }
        }
        // Last sequence update
        createMAs(currentSeqH, filePairs, normTypes, offsets, logarithms, proportions, weighting);
        batcher.finish();
        session.close();
        DatabaseWorkflowModule.getInstance().printLap("Scrolling MAs");
    }
    
    private void createMAs(Map<String, Sequence_Entity> seqEntityHash, List<FilePair> filePairs, List<NormalisationType> normTypes, List<Integer> offsets, List<Logarithm> logarithms, 
                boolean proportions, boolean weighting) {
        for (FilePair pair : filePairs) {

            if (seqEntityHash.containsKey(pair.getReference()) && seqEntityHash.containsKey(pair.getObserved())) {
                for (NormalisationType norm : normTypes) {

                    Sequence_Entity ref = seqEntityHash.get(pair.getReference());
                    Sequence_Entity obs = seqEntityHash.get(pair.getObserved());

                    Filename_Entity refF = ref.getFilename_sequence();
                    Filename_Entity obsF = obs.getFilename_sequence();

                    try {
                        // Check that the correct norm columns does not contain 0 somehow.
                        if (ref.getExpression(norm) != 0 && obs.getExpression(norm) != 0) {
                            for(int offset : offsets){
                                for(Logarithm logarithm : logarithms){
                                    createMA(ref, obs, norm, offset, logarithm, proportions, weighting);
                                }
                            }
                        }
                    } catch (NoSuchExpressionValueException ex) {
                        LOGGER.log(Level.SEVERE, "Expression value for normalisation " + norm + " is not in database and will be skipped");
                    }

                }
                // add entity to database
            }
        }
    }
    
    public void createMA(Sequence_Entity ref, Sequence_Entity obs, NormalisationType normType, int offset, Logarithm logarithm, boolean proportions, boolean weighting) throws NoSuchExpressionValueException
    {
//        MA_Entity ma = new MA_Entity(ref, obs, ref.getFilename_sequence().getNormalisedTotalAbundance(normType), 
//                                               obs.getFilename_sequence().getNormalisedTotalAbundance(normType), offset, normType);
        try {
//            double refCount = normType.getDatabaseAbundance(ref) + offset;
//            double obsCount = normType.getDatabaseAbundance(obs) + offset;
            double refCount = ref.getExpression(normType) + offset;
            double obsCount = obs.getExpression(normType) + offset;
            double refTotal =  ref.getFilename_sequence().getNormalisedTotalAbundance(normType);
            double obsTotal = obs.getFilename_sequence().getNormalisedTotalAbundance(normType);
            if(proportions)
            {
                refCount = refCount / refTotal;
                obsCount = obsCount / obsTotal;
            }

            
            double m = MAelement.calcM(refCount, obsCount, logarithm.getBase());
            double a = MAelement.calcA(refCount, obsCount, logarithm.getBase());
            

            // Only propagate MA elements that have a finite result. Discard inifinite results.
            if(Double.isFinite(m)){
                MA_Entity ma = new MA_Entity(ref, obs, m, a, normType, offset);
                ma.setAnnoType(ref.getUnique_sequence().getConsensus_annotation_type().getId().getType());
                if (weighting) {
                    ma.setWeighting(MAelement.calcWeighting(refCount, refTotal, obsCount, obsTotal));
                }
                this.maDao.createOrUpdate(ma);
            }
        } catch (org.hibernate.LazyInitializationException e) {
            LOGGER.log(Level.SEVERE, "Lazy initialization error for " + ref.getRNA_Sequence() + "/" + obs.getRNA_Sequence() + ", " + normType + ", sequence has been ignored", e);
        }
    }
    
    public void writeNrAbundanceToJson(Path outputFile, AnnotationSetList annotations) throws IOException {
        writeNrToJson2(outputFile, annotations, false);
    }
    
    public void writeNrMaToJson(Path outputFile, AnnotationSetList annotations) throws IOException {
        writeNrToJson2(outputFile, annotations, true);
    }
    
    private void writeNrToJson2(Path outputFile, AnnotationSetList annotations, boolean ma) throws IOException {
        int roundAmount = 1;
        JsonFactory jfac = DatabaseWorkflowModule.getInstance().getJsonFactory();
        JsonGenerator jg = jfac.createGenerator(outputFile.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        Session session = maDao.getSessionFactory().openSession();
        
        String dataType = "MA";
        String refType = "M";
        String obsType = "A";
        if(!ma){
            dataType = "Expression";
            refType = "refExpression";
            obsType = "obsExpression";
        }
        String query = "select ma."+refType+", ma."+obsType+", "
                // Not grouping by RNA size anymore. Potentially creates too many points with large datasets
                // + "ma.RNA_Size, "
                + "ma.filePair, ma.normType, ma.annoType, ma.offset, sum(ma.count) "
                + "from MA_NR_Entity ma "
                + "group by ma."+refType+", ma."+obsType+", "
                // Not grouping by RNA size anymore. Potentially creates too many points with large datasets
                // + "ma.RNA_Size, "
                + "ma.filePair, ma.normType, ma.annoType, ma.offset "
                + "order by ma.filePair.reference, ma.filePair.observed, ma.normType, ma.annoType, "
                // Not grouping by RNA size anymore. Potentially creates too many points with large datasets
                // + "ma.RNA_Size, "
                + "ma.offset, ma."+refType+" asc, ma."+obsType+" asc";
        
        String sqlQuery = "SELECT ROUND(ma."+refType+","+roundAmount+") x, ROUND(ma."+obsType+", "+roundAmount+") y, "
                + "ma.ref_file ref, ma.obs_file obs, ma.normType norm, ma.annoType anno, ma.m_value_offset, SUM(ma.count) n "
                + "FROM nonRedundant_MAs ma "
                + "GROUP BY ROUND(ma."+refType+","+roundAmount+"), ROUND(ma."+obsType+", "+roundAmount+"), ma.ref_file, ma.obs_file, ma.normType, ma.annoType, ma.m_value_offset "
                + "ORDER BY ma.ref_file, ma.obs_file, ma.normType, ma.annoType, ma.m_value_offset";
                
                
//        ScrollableResults mas = session.createQuery(query)
//                .scroll(ScrollMode.FORWARD_ONLY);
        ScrollableResults mas = session.createSQLQuery(sqlQuery)
                .scroll(ScrollMode.FORWARD_ONLY);
        
        NormalisationType[] normOrdinals = NormalisationType.values();
        jg.writeStartArray();
        if (mas.next()) {
            double m = (double) mas.get(0);
            double a = (double) mas.get(1);

//            int size = mas.getInteger(2);
            FilePair pair = new FilePair((String) mas.get(2), (String)mas.get(3));
            NormalisationType normType = normOrdinals[(int) mas.get(4)];
            String annoType = (String) mas.get(5);
            double offset = (double) mas.get(6);
//            int base = mas.getInteger(6);
            long count = ((BigInteger) mas.get(7)).longValue();
            if (!ma) {
                m = MA_LOG.calculate(m);
                a = MA_LOG.calculate(a);
            }
                       
            jg.writeStartObject();
            jg.writeNumberField("Offset", offset);
            jg.writeStringField("Normalisation", normType.getAbbrev());
            jg.writeStringField("AnnotationType", annoType);
            jg.writeArrayFieldStart("Annotation");
            for (AnnotationSet set : annotations.getAllSetsForType(annoType)) {
                jg.writeString(set.getName());
            }
            jg.writeEndArray();
//            jg.writeNumberField("Size", size);
            jg.writeStringField("Pair", pair.getPairKey());
            jg.writeArrayFieldStart(dataType);
            jg.writeStartObject();
            jg.writeNumberField(refType, m);
            jg.writeNumberField(obsType, a);
            jg.writeNumberField("Count", count);
            jg.writeEndObject();
            
            while(mas.next())
            {
                m = (double) mas.get(0);
                a = (double) mas.get(1);
                
//                int nextSize = mas.getInteger(2);
                FilePair nextPair = new FilePair( (String) mas.get(2), (String) mas.get(3));
                NormalisationType nextNormType = normOrdinals[(int) mas.get(4)];
                String nextAnnoType = (String) mas.get(5);
                double nextOffset = (Double) mas.get(6);
                count = ((BigInteger) mas.get(7)).longValue();
//                int nextBase = mas.getInteger(6);
                
                
                if (!ma) {
                    m = MA_LOG.calculate(m);
                    a = MA_LOG.calculate(a);
                }
                
                if (!pair.equals(nextPair)
                        || !normType.equals(nextNormType)
                        || !annoType.equals(nextAnnoType)
                        || offset != nextOffset ){
//                        || base != nextBase) {//|| nextSize != size) {
                    pair=nextPair;
                    normType=nextNormType;
                    annoType = nextAnnoType;
                    offset=nextOffset;
//                    base=nextBase;
//                    size = nextSize;
                    
                    jg.writeEndArray();
                    jg.writeEndObject();
                    jg.writeStartObject();
                    jg.writeNumberField("Offset", offset);
                    jg.writeStringField("Normalisation", normType.getAbbrev());
                    jg.writeStringField("AnnotationType", annoType);
                    jg.writeArrayFieldStart("Annotation");
                    for (AnnotationSet set : annotations.getAllSetsForType(annoType)) {
                        jg.writeString(set.getName());
                    }
                    jg.writeEndArray();
//                    jg.writeNumberField("Size", size);
                    jg.writeStringField("Pair", pair.getPairKey());
                    jg.writeArrayFieldStart(dataType);
                }
                jg.writeStartObject();
                jg.writeNumberField(refType, m);
                jg.writeNumberField(obsType, a);
                jg.writeNumberField("Count", count);
                jg.writeEndObject();
            }
        }        
        jg.writeEndArray();
        jg.close();
        session.close();
    }
    
    public void writeNrToJSON(Path outputFile, AnnotationSetList annotations) throws IOException {
        JsonFactory jfac = DatabaseWorkflowModule.getInstance().getJsonFactory();
        JsonGenerator jg = jfac.createGenerator(outputFile.toFile(), JsonEncoding.UTF8);
                Logarithm log = Logarithm.BASE_2;
        jg.useDefaultPrettyPrinter();
        Session session = maDao.getSessionFactory().openSession();
        ScrollableResults mas = session.createQuery("from MA_NR_Entity ma order by ma.filePair.reference, ma.filePair.observed, ma.normType, ma.annoType ").scroll(ScrollMode.FORWARD_ONLY);
        jg.writeStartArray();
        if(mas.next())
        {
            MA_NR_Entity ma = (MA_NR_Entity) mas.get(0);
            jg.writeStartObject();
            jg.writeNumberField("Offset", ma.getOffset());
            jg.writeStringField("Normalisation", ma.getNormType().getAbbrev());
            jg.writeArrayFieldStart("Annotation");
            for (AnnotationSet set : annotations.getAllSetsForType(ma.getAnnoType())) {
                jg.writeString(set.getName());
            }
            jg.writeEndArray();
            jg.writeStringField("Pair", ma.getPair().getPairKey());
            jg.writeArrayFieldStart("MA");
            jg.writeStartObject();
            jg.writeNumberField("M", ma.getM());
            jg.writeNumberField("A", ma.getA());
            jg.writeNumberField("ref", log.calculate(ma.getRefExpression()));
            jg.writeNumberField("obs", log.calculate(ma.getObsExpression()));
            jg.writeEndObject();
            while(mas.next())
            {
                MA_NR_Entity nextMa = (MA_NR_Entity) mas.get(0);
                if(!ma.getPair().equals(nextMa.getPair()) 
                        || !ma.getNormType().equals(nextMa.getNormType()) 
                        || !ma.getAnnoType().equals(nextMa.getAnnoType()) 
                        || ma.getOffset() != nextMa.getOffset()
                        || ma.getLogBase() != nextMa.getLogBase())
                {
                    ma = nextMa;
                    jg.writeEndArray();
                    jg.writeEndObject();
                    jg.writeStartObject();
                    jg.writeNumberField("Offset", ma.getOffset());
                    jg.writeStringField("Normalisation", ma.getNormType().getAbbrev());
                    jg.writeArrayFieldStart("Annotation");
                    for (AnnotationSet set : annotations.getAllSetsForType(ma.getAnnoType())) {
                        jg.writeString(set.getName());
                    }
                    jg.writeEndArray();
                    jg.writeStringField("Pair", ma.getPair().getPairKey());
                    jg.writeArrayFieldStart("MA");
                }
                    jg.writeStartObject();
                    jg.writeNumberField("M", nextMa.getM());
                    jg.writeNumberField("A", nextMa.getA());
                    jg.writeNumberField("ref", log.calculate(nextMa.getRefExpression()));
                    jg.writeNumberField("obs", log.calculate(nextMa.getObsExpression()));
                    jg.writeEndObject(); 
                
            }
            jg.writeEndArray();
            jg.writeEndObject();
        }
        jg.writeEndArray();
        jg.close();
        session.close();
    }
    
    public void writeToJSON(Path outputFile, AnnotationSetList annotations) throws IOException {
        JsonFactory jfac = DatabaseWorkflowModule.getInstance().getJsonFactory();
        JsonGenerator jg = jfac.createGenerator(outputFile.toFile(), JsonEncoding.UTF8);
        Logarithm log = Logarithm.BASE_2;
        jg.useDefaultPrettyPrinter();
        Session session = maDao.getSessionFactory().openSession();

        // sql query to retrieve rows of distinct M an A values to prevent redundancy in plotting.
        // embedded values appear to be reffered to just by their normal name with nothing about the embedded class
        String sqlNorms = "SELECT DISTINCT(NORMTYPE) FROM MA_VALUES";

        jg.writeStartArray();

                Query query = session.createQuery("select rfile.sampleID, rfile.replicateID, ofile.sampleID, ofile.replicateID, ma.M, ma.A, ra.expression, oa.expression, ma.id.normType, ma.id.offset, ma.annoType "
                        + "from MA_Entity ma "
                        + "join ma.refSeqEntity ref join ref.expressions ra join ref.filename_sequence rfile "
                        + "join ma.obsSeqEntity obs join obs.expressions oa join obs.filename_sequence ofile "
                        + "where ma.id.normType=oa.normType and ma.id.normType=ra.normType and ma.annoType in (:annoTypes) "
                        + "group by rfile.sampleID, rfile.replicateID, ofile.sampleID, ofile.replicateID, ma.M, ma.A, ra.expression, oa.expression, ma.id.normType, ma.id.offset, ma.annoType "
                        + "order by rfile.sampleID, rfile.replicateID, ofile.sampleID, ofile.replicateID,  ma.id.offset, ma.id.normType, ma.annoType ")
                        .setParameterList("annoTypes", annotations.getAllTypes());
                
                ScrollableResults maValues = query.scroll(ScrollMode.FORWARD_ONLY);
                
                if(maValues.next())
                {

                    String pair = maValues.getString(0)+"_"+maValues.getInteger(1)+", "+maValues.getString(2)+"_"+maValues.getInteger(3);
                    NormalisationType norm = (NormalisationType) maValues.get(8);
                    int offset = (int) maValues.get(9);
                    String type = (String) maValues.get(10);
                    jg.writeStartObject();
                    jg.writeNumberField("Offset", offset);
                    jg.writeStringField("Normalisation", norm.getAbbrev());
                    jg.writeArrayFieldStart("Annotation");
                    for(AnnotationSet set : annotations.getAllSetsForType(type)){
                        jg.writeString(set.getName());
                    }
                    jg.writeEndArray();
                    jg.writeStringField("Pair", pair);

                    jg.writeArrayFieldStart("MA");

                    jg.writeStartObject();
    //                jg.writeStringField("Annotation", (String) ur.get(7));
                    jg.writeNumberField("M", (double) maValues.get(4));
                    jg.writeNumberField("A", (double) maValues.get(5));
                    jg.writeNumberField("ref", log.calculate((double)maValues.get(6)));
                    jg.writeNumberField("obs", log.calculate((double)maValues.get(7)));
                    jg.writeEndObject();
                    while (maValues.next()) {
                        String thispair = maValues.getString(0)+"_"+maValues.getInteger(1)+", "+maValues.getString(2)+"_"+maValues.getInteger(3);
                        NormalisationType thisnorm = (NormalisationType) maValues.get(8);
                        int thisoffset = (int) maValues.get(9);
                        String thistype = (String) maValues.get(10);
                        if (!pair.equals(thispair) || norm != thisnorm || offset != thisoffset || !type.equals(thistype)) {
                            jg.writeEndArray();
                            jg.writeEndObject();
                            jg.writeStartObject();
                            jg.writeNumberField("Offset", thisoffset);
                            jg.writeStringField("Normalisation", thisnorm.getAbbrev());
                            jg.writeArrayFieldStart("Annotation");
                            for(AnnotationSet set : annotations.getAllSetsForType(type)){
                                jg.writeString(set.getName());
                            }
                            jg.writeEndArray();
                            jg.writeStringField("Pair", thispair);
                            jg.writeArrayFieldStart("MA");
                            pair = thispair;
                            norm = thisnorm;
                            offset = thisoffset;
                        }
                        jg.writeStartObject();
    //                    jg.writeStringField("Annotation", (String) ur.get(7));
                        jg.writeNumberField("M", (double) maValues.get(4));
                        jg.writeNumberField("A", (double) maValues.get(5));
                        jg.writeNumberField("ref", log.calculate((double) maValues.get(6)));
                        jg.writeNumberField("obs", log.calculate((double) maValues.get(7)));
                        jg.writeEndObject();
                    }
                    jg.writeEndArray();
                    jg.writeEndObject();
                }

//            }
        
        
        jg.writeEndArray();

        jg.close();

        session.close();

    }
    public void trimByM(MASeries series, double trim, int numberOfElements) {
        trimBy(series, trim, numberOfElements, MAparam.M);
    }

    public void trimByA(MASeries series, double trim, int numberOfElements) {
        trimBy(series, trim, numberOfElements, MAparam.A);
    }
    
    public int getNumberOfMAelements(MASeries series){
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        Number size = (Number) series.setQueryParameters(session.createQuery("select sum(ma.count) " + MASeries.getFilterQuery()))
                .uniqueResult();
        session.close();
        return size.intValue();
    }
    
    private void trimBy(MASeries series, double trim, int size, MAparam p) {
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session);

//        Number size = (Number) session.createCriteria(MA_Entity.class).setProjection(Projections.rowCount()).uniqueResult();
         
        // Row numbers to retrieve between when querying MA values ordered by p
        int lowTrim = ((int) ((trim / 100) * size) + 1) - 1;
                                                             // ^ Off-by-one
        // array index adjustment
        int highTrim = ((int) (((1 - (trim / 100)) * size) + 1)) - 1;

//        ScrollableResults orderedMA = session.createCriteria((MA_NR_Entity.class)).addOrder((Order.asc(p.toString()))).scroll(ScrollMode.SCROLL_INSENSITIVE);
        Query query = session.createQuery(MASeries.getFilterQuery() + " order by " + p.toString() + " asc");
        ScrollableResults orderedMA = series.setQueryParameters(query).scroll();

        int N = 0;
        while (orderedMA.next()) {
            MA_NR_Entity ma = (MA_NR_Entity) orderedMA.get(0);
            N += ma.getCount();
            if(N < lowTrim){
                ma.setTrimmed(true);
                batch.batchFlush();
            }
            if(N > highTrim)
            {
                ma.setTrimmed(true);
                batch.batchFlush();
            }
        }

        //orderedMA.scroll(highTrim - 1);
//        while (orderedMA.next()) {
//            MA_NR_Entity ma = (MA_NR_Entity) orderedMA.get(0);
//            ma.setTrimmed(true);
//            batch.batchFlush();
//        }
        batch.finish();
        session.close();
    }
    
    public void printMAvalues()
    {
        Session session = this.maDao.getSessionFactory().openSession();
        ScrollableResults mas = session.createCriteria(MA_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while(mas.next())
        {
            MA_Entity mae = (MA_Entity) mas.get(0);
            System.out.println(mae.toString());
        }
        
        session.close();
    }
    
    public void printNRMAvalues()
    {
        Session session = this.maDao.getSessionFactory().openSession();
        ScrollableResults mas = session.createCriteria(MA_NR_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while(mas.next())
        {
            MA_NR_Entity mae = (MA_NR_Entity) mas.get(0);
            System.out.println(mae.toString());
        }
        
        session.close();
    }
    
    /**
     * Allows easier specification and parameter passing of a particular run
     * of MA elements
     */
    public static class MASeries {
        private FilePair files;
        private NormalisationType normType;
        private AnnotationSet annotations;
        private Logarithm log;
        private double offset;

        public MASeries(FilePair files, NormalisationType normTypes, AnnotationSet annotations, Logarithm log, double offset) {
            this.files = files;
            this.normType = normTypes;
            this.annotations = annotations;
            this.log = log;
            this.offset = offset;
        }
        
        public String getReference()
        {
            return this.files.getReference();
        }
        
        public String getObserved()
        {
            return this.files.getObserved();
        }

        public FilePair getFiles() {
            return files;
        }

        public void setFiles(FilePair files) {
            this.files = files;
        }

        public NormalisationType getNormType() {
            return normType;
        }

        public void setNormTypes(NormalisationType normTypes) {
            this.normType = normTypes;
        }

        public AnnotationSet getAnnotations() {
            return annotations;
        }

        public void setAnnotations(AnnotationSet annotations) {
            this.annotations = annotations;
        }

        public Logarithm getLog() {
            return log;
        }

        public void setLog(Logarithm log) {
            this.log = log;
        }

        public double getOffset() {
            return offset;
        }

        public void setOffset(double offset) {
            this.offset = offset;
        }
        
        /**
         * @return A String that contains a template query of From ... where
         * that can be added to and then passed to setQueryParameters() to be
         * filtered using this series
         */
        public static String getFilterQuery() {
            return "from MA_NR_Entity ma "
                        + "where ma.filePair.pairKey=:pair "
                        + "and ma.normType=:norm "
                        + "and ma.annoType in (:annoTypes) "
                        + "and ma.logBase=:logbase "
                        + "and ma.offset=:offset";
        }
        
        public Query setQueryParameters(Query query)
        {
            return query.setParameter("pair", this.getFiles().getPairKey())
                    .setParameter("norm", this.getNormType())
                    .setParameterList("annoTypes", this.getAnnotations().getTypes())
                    .setParameter("logbase", this.getLog().getBase())
                    .setParameter("offset", this.getOffset());
        }
        
    }
    
}
