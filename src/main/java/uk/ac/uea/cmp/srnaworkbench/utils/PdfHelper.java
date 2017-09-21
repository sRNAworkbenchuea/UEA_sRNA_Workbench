/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Component;
import java.awt.Graphics2D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.commons.io.IOUtils;
import org.apache.fop.svg.PDFTranscoder;

/**
 * Provide a simple wrapper around the iText PDF library
 *
 * @author prb07qmu
 */
public final class PdfHelper {

    private final File _file;
    private final Document _pdfDocument;
    private PdfWriter _pdfWriter;

    /**
     * Store the filename and create a PDF document object (A4-sized)
     *
     * Examples from the 'iText in Action' book can be found at
     * http://itextpdf.com/book/
     */
    public PdfHelper(File file, boolean portrait) {
        _file = file;
        _pdfDocument = new Document(portrait ? PageSize.A4 : PageSize.A4.rotate());
        _pdfWriter = null;
    }

    public boolean initialise() {
        try {
            _pdfWriter = PdfWriter.getInstance(_pdfDocument, new FileOutputStream(_file));
        } catch (Exception ex) {
            WorkbenchLogger.LOGGER.log(Level.WARNING, null, ex);

            return false;
        }

        _pdfDocument.open();

        return true;
    }

    public void addComponentImage(Component cmp) {
        PdfContentByte canvas = _pdfWriter.getDirectContent();

        Graphics2D g2 = canvas.createGraphics(cmp.getWidth(), cmp.getHeight());

        cmp.paint(g2);

        g2.dispose();
    }

    public void addComponentImagePage(Component cmp) {
        PdfContentByte canvas = _pdfWriter.getDirectContent();

        Graphics2D g2 = canvas.createGraphics(PageSize.A4.getHeight(), PageSize.A4.getWidth());

        cmp.paint(g2);

        g2.dispose();

        newPage();
    }

    public void newPage() {
        _pdfDocument.newPage();
    }

    public void dispose() {
        if (_pdfDocument.isOpen()) {
            _pdfDocument.close();
        }

        if (_pdfWriter != null) {
            _pdfWriter.close();
        }
    }

    public static void main(String... args) {
        showExampleUsage();
    }

    /**
     * Example usage of PdfHelper<br/>
     * - Creates a frame with a button<br/>
     * - Clicking the button creates a PDF with an image of the frame and an
     * image of the central button
     */
    private static void showExampleUsage() {
        final javax.swing.JFrame f = new javax.swing.JFrame("Hello");
        f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        f.setPreferredSize(new java.awt.Dimension((int) PageSize.A4.getHeight(), (int) PageSize.A4.getWidth()));
        f.setMinimumSize(f.getPreferredSize());

        f.setLayout(new java.awt.BorderLayout());

        f.add(new javax.swing.JButton("Top"), java.awt.BorderLayout.PAGE_START);

        final javax.swing.JButton btn = new javax.swing.JButton();
        f.add(btn, java.awt.BorderLayout.CENTER);

        final String FILENAME = "../Frame.pdf";

        btn.setAction(new javax.swing.AbstractAction("Click to create " + FILENAME) {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                PdfHelper ph = new PdfHelper(new File(FILENAME), false);

                if (ph.initialise()) {
                    ph.addComponentImage(f);
                    ph.newPage();

                    ph.addComponentImage(btn);
                    ph.newPage();

                    ph.dispose();
                }
            }
        });

        f.pack();
        f.setVisible(true);
    }

    /* Chris removed this function from PlotSelectionSceneController and made it a bit more flexible */
    public static void saveToPDF(Scene scene, String svgXML, int width, int height) throws IOException, TranscoderException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to PDF");
        File file = fileChooser.showSaveDialog(scene.getWindow());

        Files.write(Paths.get(file.toString() + ".svg"), svgXML.getBytes("UTF-8"));

        Transcoder transcoder = new PDFTranscoder();
        transcoder.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, (float) height);
        transcoder.addTranscodingHint(PDFTranscoder.KEY_WIDTH, (float) width);
        try (InputStream toInputStream = IOUtils.toInputStream(svgXML, "UTF-8");
                FileOutputStream fileOutputStream = new FileOutputStream(new File(file.toString() + ".pdf"))) {
            TranscoderInput transcoderInput = new TranscoderInput(toInputStream);

            TranscoderOutput transcoderOutput = new TranscoderOutput(fileOutputStream);
            transcoder.transcode(transcoderInput, transcoderOutput);

        }

    }

}
