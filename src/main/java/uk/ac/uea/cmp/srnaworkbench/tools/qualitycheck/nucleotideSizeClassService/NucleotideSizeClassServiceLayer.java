package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.nucleotideSizeClassService;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.IllegalNucleotideException;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.Nucleotide;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.NucleotidePosition;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.NucleotideSizeClassDAO;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Nucleotide_Size_Class_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.io.JsonWriter;
import uk.ac.uea.cmp.srnaworkbench.tools.TimeableTool;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author mka07yyu
 */
@Service("NucleotideSizeClassService")
public class NucleotideSizeClassServiceLayer extends TimeableTool{

    @Autowired
    private NucleotideSizeClassDAO nucdao;
    
    private String stageName = "";
    
    public NucleotideSizeClassServiceLayer()
    {
        super("NucleotideSizeClassTool");
    }
    
    public void setStageNameAndTracker(String name, JFXStatusTracker tracker)
    {
        setTracker(tracker);
        stageName = name;
    }
    
    @Override
    protected void processWithTimer() throws Exception {
        trackerInitUnknownRuntime("Calculating Abundance Windows");
        buildSizeClass();
        writeToJson(Paths.get(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR  + stageName + "nucscd.json"));
        trackerReset();
    }
    
    public boolean containsData()
    {
        Session session = this.nucdao.getSessionFactory().openSession();
        boolean empty = session.createQuery("select 1 from Nucleotide_Size_Class_Entity").list().isEmpty();
        session.close();
        return empty;
    }
    
    public void buildSizeClass() throws IllegalNucleotideException
    {
        Session session = nucdao.getSessionFactory().openSession();
        Batcher batcher = new Batcher(session);
        Criteria crit = nucdao.getAlignedSequencesCriteria().getExecutableCriteria(session)
                .setProjection(Projections.projectionList()
                .add(Projections.property("useq.RNA_Size"))
                .add(Projections.property("filename"))
                .add(Projections.property("RNA_Sequence"))
                .add(Projections.property("abundance")))
                .addOrder(Order.asc("useq.RNA_Size")).addOrder(Order.asc("filename"));
        
        ScrollableResults results = crit.scroll(ScrollMode.FORWARD_ONLY);
        if(results.next())
        {
            ArrayList<NucleotidePosition> positionCount;
            int currentSize = results.getInteger(0);
            String currentFilename = results.getString(1);
            String seq = results.getString(2);
            int abundance = results.getInteger(3);
            
            positionCount = new ArrayList<>();
            for(int i=0; i < currentSize; i++)
            {
                positionCount.add(new NucleotidePosition(i));
            }
            char[] seqchars = seq.toCharArray();
            for(int pos = 0; pos < seqchars.length; pos++)
            {
                NucleotidePosition thisPos = positionCount.get(pos);
                Nucleotide thisN = Nucleotide.fromChar(seqchars[pos]);
                thisPos.addNucleotide(thisN, abundance);
            }
            
            while(results.next())
            {
                String filename = results.getString(1);
                int size = results.getInteger(0);
                seq = results.getString(2);
                seqchars = seq.toCharArray();
                abundance = results.getInteger(3);
                
                if(currentSize == size && currentFilename.equals(filename))
                {
                    for (int pos = 0; pos < seqchars.length; pos++) 
                    {
                        NucleotidePosition thisPos = positionCount.get(pos);
                        Nucleotide thisN = Nucleotide.fromChar(seqchars[pos]);
                        
                        thisPos.addNucleotide(thisN, abundance);
                    }
                }
                else
                {
                    for (NucleotidePosition np : positionCount)
                    {
                        // add to database
                        for(Nucleotide n : Nucleotide.values())
                        {
                            Nucleotide_Size_Class_Entity ne = new Nucleotide_Size_Class_Entity(currentFilename, n, currentSize, np.getPosition(), np.getProp(n) );
                            this.nucdao.createOrUpdate(ne);
                            batcher.batchFlush();
                        }
                    }
                    
                    // new entity
                    positionCount = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        positionCount.add(new NucleotidePosition(i));
                    }
                    seqchars = seq.toCharArray();
                    for (int pos = 0; pos < seqchars.length; pos++) {
                        positionCount.get(pos).addNucleotide(Nucleotide.fromChar(seqchars[pos]), abundance);
                    }
                }
                currentSize = size;
                currentFilename = filename;
            }
            for (NucleotidePosition np : positionCount) {
                // add to database
                for (Nucleotide n : Nucleotide.values()) {
                    Nucleotide_Size_Class_Entity ne = new Nucleotide_Size_Class_Entity(currentFilename, n, currentSize, np.getPosition(), np.getProp(n));
                    this.nucdao.createOrUpdate(ne);
                    batcher.batchFlush();
                }
            }
        }
        
                
                    
        batcher.finish();
        session.close();
    }
    
    public void writeToJson(Path path) throws IOException
    {
        JsonGenerator jg = JsonWriter.jsonFactory.createGenerator(path.toFile(), JsonEncoding.UTF8);

        Session session = nucdao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(Nucleotide_Size_Class_Entity.class)
                .scroll(ScrollMode.FORWARD_ONLY);
        jg.writeStartArray();
        
        while(results.next())
        {
            jg.writeStartObject();
            Nucleotide_Size_Class_Entity ne = (Nucleotide_Size_Class_Entity) results.get(0);
            jg.writeNumberField("Size", ne.getRnaSize());
            jg.writeStringField("File", ne.getFilename());
            jg.writeStringField("Nucleotide", ne.getNucleotide().toString());
            jg.writeNumberField("Position", ne.getSeqPos());
            jg.writeNumberField("Count", ne.getCount());
            jg.writeEndObject();
        }
        jg.writeEndArray();
        jg.close();
        session.close();
    }
    
    public void printSCDTable()
    {
        Session session = nucdao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(Nucleotide_Size_Class_Entity.class)
                .scroll(ScrollMode.FORWARD_ONLY);
        
        while(results.next())
        {
            Nucleotide_Size_Class_Entity ne = (Nucleotide_Size_Class_Entity) results.get(0);
            System.out.println(ne.toString());
        }
        session.close();
    }
    
}
