/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceSizeClass;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.FilenameDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.SizeClassDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.UniqueSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Size_Class_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;

/**
 * A service layer that builds the SizeClass table and writes results to file
 * @author Matthew Beckers
 */
@Service("SizeClassDistributionCalculator")
@Transactional
public class SizeClassDistributionServiceLayer implements GenericService<Size_Class_Entity, Long>{
    @Autowired
    private SizeClassDAO sdtDao;
    
    @Autowired
    private UniqueSequenceDAOImpl uniqueSeqDao;
    
    @Autowired 
    private FilenameDAOImpl fileDao;
    
    /**
     * Build size class distributions for combinations of properties:
     *      filename, normType, all annotation types, all RNA sizes
     * 
     * Note that this will include using sequences that are not mapped, but these
     * will appear as "none" annotation type
     * @param filename
     * @param normType 
     */
    public void buildAllDistribution(Collection<String> fileIDs, Collection<NormalisationType> normTypes, Collection<String> annotations)
    {
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
        FilenameServiceImpl fileServ = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        Query query = session.createQuery("select seq.fileID, u.RNA_Size, e.normType, t, count(e.expression), sum(e.expression) "
                + "from Sequence_Entity seq "
                + "join seq.expressions e "
                + "join seq.unique_sequence u "
                + "join u.type t "
                + "where seq.fileID in (:filenames) and e.normType in (:normTypes) "
                + "and t.id.type in (:annotations) group by seq.fileID, e.normType, t.id.type, u.RNA_Size ")
                .setParameterList("filenames", fileIDs)
                .setParameterList("normTypes", normTypes)
                .setParameterList("annotations", annotations);
        
        List<Object[]> results = query.list();
        for (Object[] result : results) {
            String filename = (String) result[0];
            int size = (int) result[1];
            NormalisationType normType = (NormalisationType) result[2];
            int nrCount = ((Long) result[4]).intValue();
            double rCount = ((Number) result[5]).doubleValue();
            String type = ((Annotation_Type_Entity) result[3]).getId().getType();
            
            boolean mapped = !type.equals(ReferenceSequenceManager.NO_TYPE_NAME);
            Filename_Entity thisFile = fileServ.getFileById(filename);
            Size_Class_Entity sdte = new Size_Class_Entity(thisFile, normType, size, rCount, nrCount, mapped, type);
            sdtDao.createOrUpdate(sdte);
        }
                
        session.close();
    }
    
    public void writeToJson(Path path, Collection<String> fileIDs, Collection<NormalisationType> normTypes, AnnotationSetList annotations) throws IOException
    {
        JsonFactory jf = DatabaseWorkflowModule.getInstance().getJsonFactory();
        JsonGenerator jg = jf.createGenerator(path.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        Session session = this.sdtDao.getSessionFactory().openSession();
        
        jg.writeStartArray();
        for(AnnotationSet set : annotations.getAllSets())
        {
            if(!set.getTypes().isEmpty()){
                ScrollableResults sr = session.createQuery("select sum(s.rAbundance), sum(s.nrAbundance), s.file, s.normType, s.sizeClass from Size_Class_Entity s "
                        + " where s.file.fileID in (:files) and s.normType in (:norms) and s.type in (:annotTypes) "
                        + " group by s.file, s.normType, s.sizeClass "
                        + " order by s.sizeClass asc, s.file.sample.sampleNumber asc, s.file.replicateID asc ")
                        .setParameterList("files", fileIDs)
                        .setParameterList("norms", normTypes)
                        .setParameterList("annotTypes", set.getTypes())
                        .scroll(ScrollMode.FORWARD_ONLY);

                while (sr.next())
                {
                    double r = sr.getDouble(0);
                    int nr = sr.getLong(1).intValue();
                    Filename_Entity file = (Filename_Entity) sr.get(2);
                    NormalisationType norm = (NormalisationType) sr.get(3);
                    int size = sr.getInteger(4);

                    jg.writeStartObject();
                    jg.writeStringField("Filename", file.getFileID());
                    jg.writeStringField("Sample", file.getSampleID());
                    jg.writeNumberField("Replicate", file.getReplicateID());
                    jg.writeStringField("Normalisation", norm.getAbbrev());
                    jg.writeStringField("Annotation", set.getName());
                    jg.writeNumberField("Size", size);
                    jg.writeNumberField("Redundant", r);
                    jg.writeNumberField("Nonredundant", nr);
                    jg.writeNumberField("Complexity", nr/r);
                    jg.writeEndObject();
                }
            }
        }
        
        jg.writeEndArray();

        jg.flush();
        jg.close();
        session.close();
    }
    
    public void writeToJson(Path path) throws IOException {

        JsonFactory jf = DatabaseWorkflowModule.getInstance().getJsonFactory();
        
            JsonGenerator jg = jf.createGenerator(path.toFile(), JsonEncoding.UTF8);
            jg.useDefaultPrettyPrinter();
            Session session = this.sdtDao.getSessionFactory().openSession();
            ScrollableResults sr = session.createCriteria(Size_Class_Entity.class)
                    .addOrder(Order.asc("sample"))
                    .addOrder(Order.asc("normType"))
                    .addOrder(Order.asc("sizeClass"))
                    .addOrder(Order.asc("mapped"))
                    .scroll(ScrollMode.FORWARD_ONLY);
            
            jg.writeStartArray();
            
            while(sr.next())
            {
                Size_Class_Entity sdte = (Size_Class_Entity) sr.get(0);

                jg.writeStartObject();
                jg.writeStringField("FileID", sdte.getFile().getFileID());
                jg.writeStringField("Sample", sdte.getFile().getSampleID());
                jg.writeNumberField("Replicate", sdte.getFile().getReplicateID());
                jg.writeStringField("Normalisation", sdte.getNormType().getAbbrev());
                jg.writeStringField("Annotation", sdte.getType());
                jg.writeStringField("Status", sdte.getMappingStatus().toString());
                jg.writeNumberField("Size", sdte.getSizeClass());
                jg.writeNumberField("Redundant", sdte.getRedundantAbundance());
                jg.writeNumberField("Nonredundant", sdte.getNonRedundantAbundance());
                jg.writeNumberField("Complexity", sdte.getNonRedundantAbundance()/sdte.getRedundantAbundance());
                jg.writeEndObject();
            }
            
            jg.writeEndArray();
            
            jg.flush();
            jg.close();
            session.close();
    }
        
   
    private void _jsonWriteSizeClassObject(JsonGenerator jg, Size_Class_Entity sdte) throws IOException
    {
    

        jg.writeObjectFieldStart(sdte.getMappingStatus().toString());
        jg.writeObjectField("RedundantCount", sdte.getRedundantAbundance());
        jg.writeObjectField("NonRedundantCount", sdte.getNonRedundantAbundance());
        //FIXME: Complexity can be stored in the table if needed
        jg.writeObjectField("Complexity", sdte.getNonRedundantAbundance()/sdte.getRedundantAbundance());
        jg.writeEndObject();
        
    }
    
    public void printSizeClasses()
    {
        Session session = this.sdtDao.getSessionFactory().openSession();
        ScrollableResults result = session.createCriteria(Size_Class_Entity.class)
                .scroll(ScrollMode.FORWARD_ONLY);
        
        while(result.next())
        {
            Size_Class_Entity sdte = (Size_Class_Entity) result.get(0);
            System.out.println(sdte.toString());
        }
        session.close();
    }
    @Override
    public Size_Class_Entity findById(Long ID) {
        return this.sdtDao.read(ID);
    }

    @Override
    public List<Size_Class_Entity> findAll() {
        return this.sdtDao.findAll();
    }

    @Override
    public void save(Size_Class_Entity presistentObject) {
        this.sdtDao.create(presistentObject);
    }

    @Override
    public void saveOrUpdate(Size_Class_Entity transient_presistent_Object) {
        this.sdtDao.createOrUpdate(transient_presistent_Object);
    }

    @Override
    public void update(Size_Class_Entity transientObject) {
        this.sdtDao.update(transientObject);
    }

    @Override
    public void delete(Size_Class_Entity transient_presistent_Object) {
        this.sdtDao.delete(transient_presistent_Object);
    }

    @Override
    public void shutdown() {
        this.sdtDao.shutdown();
    }
}
