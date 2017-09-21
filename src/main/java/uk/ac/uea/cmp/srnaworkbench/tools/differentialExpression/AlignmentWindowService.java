package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression;

import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NotYetCalculatedException;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedFilenameWindowDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignmentWindowDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.FilenameDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.KLDao;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Filename_Window_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Alignment_Window_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.KL_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NormalisationNotCalculatedException;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;

/**
 *
 * @author Matthew
 */
@Service("AlignmentWindowService")
@Transactional
public class AlignmentWindowService {
    
    @Autowired
    private AlignedSequenceDAOImpl alignedSeqDao;

    @Autowired
    private AlignmentWindowDAO alignmentWindowDao;

    @Autowired
    private AlignedFilenameWindowDAO alignedFilenameWindowDao;

    @Autowired
    private KLDao klDao;
    
    @Autowired
    private FilenameDAOImpl fileDao;
    
    public void writeKLtoJson(Path path) throws IOException {

        JsonFactory jf = DatabaseWorkflowModule.getInstance().getJsonFactory();
        JsonGenerator jg = jf.createGenerator(path.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();

        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        ScrollableResults results = session.createQuery("from KL_Entity kl order by kl.id.filename asc, kl.id.expression asc").scroll(ScrollMode.FORWARD_ONLY);
        jg.writeStartArray();
        while (results.next()) {
            KL_Entity kl = (KL_Entity) results.get(0);
            jg.writeStartObject();
            jg.writeStringField("Filename", kl.getId().getFilename().getName());
            jg.writeNumberField("Expression", kl.getId().getExpression());
            jg.writeNumberField("KL", kl.getKl());
            jg.writeNumberField("KLS", kl.getKlSmoothed());
            jg.writeEndObject();
        }
        jg.writeEndArray();
        jg.close();
        session.close();
    }

    public void printFilenameWindows() {
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(Aligned_Filename_Window_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while (results.next()) {
            Aligned_Filename_Window_Entity window = (Aligned_Filename_Window_Entity) results.get(0);
            System.out.println(window);
        }
        session.close();
    }

    public void printKLvalues() {
        Session session = this.klDao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(KL_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while (results.next()) {
            KL_Entity kl = (KL_Entity) results.get(0);
            System.out.println(kl);
        }
        session.close();
    }
    
    public void createWindowsv2(int windowLength) {
        StopWatch sw = new StopWatch();
        sw.start();
        Session session = this.alignmentWindowDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session);
        
        session.createQuery("update Aligned_Sequences_Entity a set a.alignment_window = (a.start");
        
        session.flush();
        session.close();
        sw.stop();
        sw.printTimes();
    }

    /**
     * Generate and store alignment windows for all currently aligned sequences.
     * An aligned sequence may be in multiple windows of varying lengths.
     *
     * @param windowLength the sliding window length used to generate these
     * windows
     */
    public void createWindows(int windowLength) {
        StopWatch sw = new StopWatch();
        sw.start();
        Session session = this.alignmentWindowDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session);
        Query orderedAlignmentQuery = session.createQuery(
                "from Aligned_Sequences_Entity a "
                + "order by a.id.reference_sequence asc "
                + ", a.id.chrom asc "
                + ", a.id.start asc "
                + ", a.id.end asc "
        );
        ScrollableResults orderedAlignments = orderedAlignmentQuery.scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Querying aligned sequences");
        if (orderedAlignments.next()) {
            Aligned_Sequences_Entity alignedSequence = (Aligned_Sequences_Entity) orderedAlignments.get(0);
            int windowId = calculateWindow(alignedSequence, windowLength);

            Alignment_Window_Entity.Id currentWindowPK = new Alignment_Window_Entity.Id(alignedSequence.getChromosome(), alignedSequence.getId().getReference(), windowId, windowLength);
            Alignment_Window_Entity currentWindow = new Alignment_Window_Entity();
            currentWindow.setId(currentWindowPK);
            //this.alignmentWindowDao.merge(currentWindow);

            //this.alignedSeqDao.update(alignedSequence);
            currentWindow.addAlignedSequence(alignedSequence);
            alignedSequence.setAlignmentWindow(currentWindow);

            while (orderedAlignments.next()) {
                alignedSequence = (Aligned_Sequences_Entity) orderedAlignments.get(0);
                windowId = calculateWindow(alignedSequence, windowLength);
                
                // Check to see if window id, seqid and reference are still all the same
                if ((windowId != currentWindowPK.getWindowId()
                        || !alignedSequence.getChromosome().equals(currentWindowPK.getSeqid()))
                        || !alignedSequence.getId().getReference().equals(currentWindowPK.getReference())) {
                    // finish this window and start on a new one.
                    this.alignmentWindowDao.merge(currentWindow);
                    batch.batchFlush(); // batching here instead of flushing everytime makes a HUGE difference
                    currentWindowPK = new Alignment_Window_Entity.Id(alignedSequence.getChromosome(), alignedSequence.getId().getReference(), windowId, windowLength);
                    currentWindow = new Alignment_Window_Entity();
                    currentWindow.setId(currentWindowPK);
                    currentWindow.addAlignedSequence(alignedSequence);
                    alignedSequence.setAlignmentWindow(currentWindow);

                } else {
                    currentWindow.addAlignedSequence(alignedSequence);
                    alignedSequence.setAlignmentWindow(currentWindow);
                }
            }
            this.alignmentWindowDao.merge(currentWindow);
            batch.finish();
        }
        session.flush();
        session.close();
        sw.lap("Assigning windows");
        sw.stop();
        sw.printTimes();
    }

    private int calculateWindow(Aligned_Sequences_Entity alignedSequence, int windowLength) {
        int start = alignedSequence.getStart();
        int end = alignedSequence.getEnd();
        int middle = (int) ((start + end) / 2);
        return (int) (middle / windowLength);
    }

    /**
     * Calculate strand biases for alignment windows of the specified sequence
     * files
     *
     * @param filenames the filenames to calculated windowed strand biases for
     * @param normType the normalisation method used to create the required
     * expressions
     */
    public void calculateStrandBiasPerWindow(Collection<Filename_Entity> files, NormalisationType normType) throws NormalisationNotCalculatedException {
        StopWatch sw = new StopWatch("Strand bias per window");
        sw.start();
        Session session = this.alignedFilenameWindowDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session);
        Query allWindowsQuery = session.createQuery(
                "select w, s.filename_sequence, a.id.strand, count(e.expression), sum(e.expression) from Alignment_Window_Entity w "
                + "join w.aligned_sequences a "
                + "join a.aligned_sequence u "
                + "join u.sequenceRelationships s "
//                        + "join s.filename_sequence f "
                + "join s.expressions e where e.normType=:normType "
                + "and s.filename_sequence in (:filenames) "
                + "group by w.id, a.id.strand, s.filename_sequence "
                + "order by s.filename_sequence asc, w.id asc, a.id.strand asc ").setParameterList("filenames", files).setParameter("normType", normType);

        ScrollableResults allWindows = allWindowsQuery.scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Querying window summaries");

        if (allWindows.next()) {

            //Alignment_Window_Entity window = (Alignment_Window_Entity) allWindows.get(0);
            // create filename alignment windows by finding the total abundance and strand bias
            Alignment_Window_Entity window = (Alignment_Window_Entity) allWindows.get(0);
            Alignment_Window_Entity previousWindow = window;

            Filename_Entity filename = (Filename_Entity) allWindows.get(1);
            Filename_Entity previousFilename = filename;

            String strand = (String) allWindows.get(2);
            int count = ((Long) allWindows.get(3)).intValue();
            double total = (double) allWindows.get(4);
            boolean firstStrand = true;

            while (allWindows.next()) {
                window = (Alignment_Window_Entity) allWindows.get(0);
                filename = (Filename_Entity) allWindows.get(1);

                if (firstStrand && window.getId().equals(previousWindow.getId()) && previousFilename.equals(filename)) {
                    // this is the second of two strands

                    // currently now on the second strand
                    firstStrand = false;

                    // values are for a different strand
                    int otherCount = ((Long) allWindows.get(3)).intValue();
                    total += (double) allWindows.get(4);
                    double strandbias;

                    // calculate strand bias from values of both strands
                    if (strand.equals("+")) {
                        strandbias = calculateStrandBias(count, otherCount);
                    } else {
                        strandbias = calculateStrandBias(otherCount, count);
                    }

                    Aligned_Filename_Window_Entity afw = new Aligned_Filename_Window_Entity(window, filename, strandbias, total);
//                    System.out.println("Window with two strands " + afw);
                    window.addAlignedFilenameWindow(afw);
                    session.save(afw);
                    batch.batchFlush();

                } else {
                    // this is here because two strands have been OR there was only one strand
                    if (firstStrand && previousFilename.equals(filename)) {
                        // if last strand was the firstStrand, then simulate an empty second strand and create a filename window
                        double strandbias;
                        if (strand.equals("+")) {
                            strandbias = calculateStrandBias(count, 0);
                        } else {
                            strandbias = calculateStrandBias(0, count);
                        }
                        Aligned_Filename_Window_Entity afw = new Aligned_Filename_Window_Entity(previousWindow, previousFilename, strandbias, total);
                        previousWindow.addAlignedFilenameWindow(afw);
//                        System.out.println("Window with one strand " + afw);
                        session.save(afw);
                        batch.batchFlush();
                    }

                    // currently now on the first strand of this window
                    firstStrand = true;
                }

                // Store values for this strand in prep for next strand
                previousWindow = window;
                previousFilename = filename;
                filename = (Filename_Entity) allWindows.get(1);
                window = (Alignment_Window_Entity) allWindows.get(0);
                strand = (String) allWindows.get(2);
                count = ((Long) allWindows.get(3)).intValue();
                total = (double) allWindows.get(4);

            }
            if (firstStrand) {
                double strandbias;
                if (strand.equals("+")) {
                    strandbias = calculateStrandBias(count, 0);
                } else {
                    strandbias = calculateStrandBias(0, count);
                }
                Aligned_Filename_Window_Entity afw = new Aligned_Filename_Window_Entity(window, filename, strandbias, total);
                window.addAlignedFilenameWindow(afw);
//                System.out.println("Last: " +afw);
                session.save(afw);
            }
            batch.finish();
        }
        sw.lap("Calculating strand biases");
//        List testwindows = session.createQuery("from Aligned_Filename_Window_Entity").list();
        session.close();
        sw.stop();
        sw.printTimes();
    }

    private static double calculateStrandBias(int numberOfPositive, int numberOfNegative) {
        int total = numberOfPositive + numberOfNegative;
        double pos = numberOfPositive / (double) total;
        double neg = numberOfNegative / (double) total;

        double p = Math.abs(0.5 - pos);
        double n = Math.abs(0.5 - neg);
        return p + n;
    }

    public void calculateKulbackLeiblerDivergence(Filename_Entity filename, double maxExpression, int numberOfStrandBiasBins) {
        double strandBiasIncrement = 1 / (double) numberOfStrandBiasBins;
        //double logSbInc = Math.log(strandBiasIncrement) / Math.log(2);
        Logarithm log2 = Logarithm.BASE_2;

        Session session = this.alignedFilenameWindowDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session);
        
        // Return number of sequences in each strand bias bin and each abundance
        // Note that the abundances of windows are now rounded down to the nearest count (or integer)
        //  if this is not done, each "abundance bin" becomes too small and the divergence goes all over the place
        Query windowsForThisExpressionQuery = session.createQuery(
                " select floor(w.abundance), count(*) "
                + "from Aligned_Filename_Window_Entity w "
                + "where floor(w.abundance)<:expression and w.id.filename=:filename "
                + "group by floor(w.abundance), floor(w.strandBias/" + strandBiasIncrement + ") order by floor(w.abundance) asc"
        ).setParameter("expression", maxExpression).setParameter("filename",filename);

        //List testwindows = session.createQuery("from Aligned_Filename_Window_Entity").list();
                
        // This data is returned in a list because it is assumed to be relatively small. However, this is dependent
        // on maxExpression being not too high
        List<Object[]> windows = windowsForThisExpressionQuery.list();

        Iterator<Object[]> windowIterator = windows.iterator();
        if (windowIterator.hasNext()) {
            Object[] thisWindow = windowIterator.next();
            double previousExpression = (double) thisWindow[0];

            List<Long> counts = new ArrayList<>();
            counts.add((long) thisWindow[1]);
            long thisNumberOfSeqs = counts.get(counts.size() - 1);
            while (windowIterator.hasNext()) {
                thisWindow = windowIterator.next();
                double thisExpression = (double) thisWindow[0];

                if (previousExpression != thisExpression) {

                    double kl = calculateKLDivergenceMeasure(counts, thisNumberOfSeqs, strandBiasIncrement, log2);

                    KL_Entity thisKl = new KL_Entity(filename, previousExpression, kl);
                    this.klDao.create(thisKl);
                    batch.batchFlush();
                    previousExpression = thisExpression;
                    counts = new ArrayList<>();
                    counts.add((long) thisWindow[1]);
                    thisNumberOfSeqs = counts.get(counts.size() - 1);
                } else {
                    counts.add((long) thisWindow[1]);
                    thisNumberOfSeqs += counts.get(counts.size() - 1);
                }
            }

            double kl = calculateKLDivergenceMeasure(counts, thisNumberOfSeqs, strandBiasIncrement, log2);
            KL_Entity thisKl = new KL_Entity(filename, previousExpression, kl);
            this.klDao.create(thisKl);
            batch.finish();
        }

        session.close();
    }

    /**
     * 
     * @param filename the filename to calculate the offset for
     * @param bandwidth parameter to pass to the loess interpolator. If 0, the KL values
     *  are not smoothed and the original values are used to find an offset instead.
     * @return the calculated offset. This is also persisted to the database to the Filename_Entity. Future
     * calls to this method will overwrite this for the same filename.
     * @throws MathException if the loess interpolator throws a wobbly. It is recommended that you try{} this method
     * and if this exception is caught, retry with bandwidth=0.
     */
    public double findKLderivedOffset(Filename_Entity filename, double bandwidth) throws MathException, NotYetCalculatedException {
        //Filename_Entity file = this.fileDao.read(filename);
        Session session = this.klDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session);
        List<KL_Entity> kl_series = session.createQuery("from KL_Entity kl where kl.id.filename=:fname")
                .setParameter("fname",filename)
                .list();
        if(kl_series.isEmpty())
        {
            throw new NotYetCalculatedException("No KL values have been calculated for the file " + filename);
        }
        double[] x = new double[kl_series.size()];
        double[] y = new double[kl_series.size()];

        for (int i = 0; i < kl_series.size(); i++) {
            x[i] = kl_series.get(i).getId().getExpression();
            y[i] = kl_series.get(i).getKl();
        }
        double[] smoothedY;

        double minYs = Double.MAX_VALUE;
        double expressionAtMin = 0;
        if(bandwidth == 0)
        {
            smoothedY = y;
        }
        else
        {
            LoessInterpolator loess = new LoessInterpolator(bandwidth, LoessInterpolator.DEFAULT_ROBUSTNESS_ITERS);
            smoothedY = loess.smooth(x, y);
        }
        for (int i = 0; i < kl_series.size(); i++) {
            if (smoothedY[i] < minYs) {
                minYs = smoothedY[i];
                expressionAtMin = x[i];
            }
            kl_series.get(i).setKlSmoothed(smoothedY[i]);
            batch.batchFlush();
        }
        batch.finish();
        
        filename = (Filename_Entity) session.load(Filename_Entity.class, filename.getFilename());
        filename.setOffset(expressionAtMin);
        session.save(filename);
        batch.finish();
        session.close();
        return expressionAtMin;
    }

    private double calculateKLDivergenceMeasure(Collection<Long> counts, long totalCount, double strandBiasIncrement, Logarithm log) {
        double sumOverBins = 0;
        for (long count : counts) {
            // Calculation of KL divergence
            double p_i = count / (double) totalCount;
            //double log2_p_i = Math.log(p_i) / Math.log(2);
            double kl_i = p_i * log.calculate(p_i / strandBiasIncrement);
            sumOverBins += kl_i;
        }
        return sumOverBins;
    }

    
}
