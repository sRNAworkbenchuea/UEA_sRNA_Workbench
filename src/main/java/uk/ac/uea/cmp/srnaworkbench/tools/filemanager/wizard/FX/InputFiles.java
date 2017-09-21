package uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javafx.util.Pair;

import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * Holds a collection of all files that can be input using the Wizard. The object
 * is easily serialized and deserialized to save previous inputs
 * @author Matt B/S
 */
class InputFiles implements Serializable {
    
    public DatabaseWorkflowModule.Mapper mapper;
    private Map<String, List<File>> sRNA_samples = new LinkedHashMap<>();
    private Map<String, List<File>> mRNA_samples = new LinkedHashMap<>();
    private Map<String, List<Pair<File, Integer>>> degradomes = new LinkedHashMap<>();
    
    private List<File> GFFs = new ArrayList<>();
    private Set<String> annotations = new LinkedHashSet<>();
    private Set<String> otherAnnotations = new LinkedHashSet<>();

    private File genome = null;
    private File mappingFile = null;
    private File transcriptome = null;
    
    /**
     * Does this set of InputFiles have a genome or mapping file
     * @return true or false
     */
    public boolean genomeIsValid(DatabaseWorkflowModule.Mapper mapper)
    {
        switch (mapper)
        {
            case PATMAN:
                return genome != null && Files.exists(genome.toPath());

            case BOWTIE:
                return genome != null && Files.exists(Paths.get(genome.toString() + ".1.ebwt"));

            default:
                RuntimeException e = new RuntimeException();
                LOGGER.log(Level.SEVERE, "Mapper not recognised", e);
                return false;
        }
    }
    
    public boolean transcriptomeIsValid()
    {
        return this.transcriptome == null || Files.exists(transcriptome.toPath());
    }
    
    public boolean GFFsAreValid()
    {
        if (!GFFs.isEmpty()) {
            //System.out.println("gffs found");
            for (File p : GFFs) {
                if (!Files.exists(p.toPath())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean sRNAsAreValid()
    {
        if (sRNA_samples.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, List<File>> e : sRNA_samples.entrySet()) {
            if (e.getValue().isEmpty()) {
                return false;
            }
            for (File f : e.getValue()) {

                if (f.toString().isEmpty() || !Files.exists(f.toPath())) {
                    return false;
                }
            }
        }

        return true;
    }
    
    public void add_sample(String sampleID)
    {

        sRNA_samples.put(sampleID, new ArrayList<>());
        mRNA_samples.put(sampleID, new ArrayList<>());
        degradomes.put(sampleID, new ArrayList<>());
    }
    
    public void add_sRNA_replicate(String sampleID, Path path)
    {
        sRNA_samples.get(sampleID).add(path.toFile());
    }
    
    public void set_sRNA_replicate(String sampleID, int replicateID, Path path)
    {
        sRNA_samples.get(sampleID).set(replicateID, path.toFile());
    }

    public void add_mRNA_replicate(String sampleID, Path path) {
        mRNA_samples.get(sampleID).add(path.toFile());
    }

    public void set_mRNA_replicate(String sampleID, int replicateID, Path path) {
        mRNA_samples.get(sampleID).set(replicateID, path.toFile());
    }

    public void add_degradome_replicate(String sampleID, Path path, int dataType) {
        degradomes.get(sampleID).add(new Pair<>(path.toFile(), dataType));
    }

    public void set_degradome_replicate(String sampleID, int replicateID, Path path, int dataType) {
        degradomes.get(sampleID).set(replicateID, new Pair<>(path.toFile(), dataType));
    }
    
    public void set_degradome_type(String sampleID, int replicateID, int dataType)
    {
        File f =  degradomes.get(sampleID).get(replicateID).getKey();
        degradomes.get(sampleID).set(replicateID, new Pair<>(f, dataType));
    }
        
    public void add_GFF(Path path)
    {
        this.GFFs.add(path.toFile());
    }
    
    public void add_annotation(String type)
    {
        this.annotations.add(type);
    }
    
    public void add_other_annotation(String type)
    {
        this.otherAnnotations.add(type);
    }
    
    public void remove_sample(String sampleID)
    {
        if(this.sRNA_samples.containsKey(sampleID))
            this.sRNA_samples.remove(sampleID);
        if (this.mRNA_samples.containsKey(sampleID)) {
            this.mRNA_samples.remove(sampleID);
        }
        if (this.degradomes.containsKey(sampleID)) {
            this.degradomes.remove(sampleID);
        }
    }


    public void set_sRNA_samples(HashMap<String, List<Path>> sRNA_samples) {
        this.sRNA_samples = pathToFile(sRNA_samples);
    }

    public void set_mRNA_samples(HashMap<String, List<Path>> mRNA_samples) {
        this.mRNA_samples = pathToFile(mRNA_samples);
    }

    public void set_degradomes(HashMap<String, List<Pair<Path, Integer>>> degradomes) {
        this.degradomes = pathToFileDeg(degradomes);
    }
    
    public void set_genome(Path path)
    {
            this.genome = path.toFile();
    }

    public void set_GFFs(List<Path> GFFs) {
        List<File> files = new ArrayList<>();
        for(Path p : GFFs)
        {
            files.add(p.toFile());
        }
        this.GFFs = files;
    }
    
    public void remove_GFF(Path path)
    {
        this.GFFs.remove(path.toFile());
    }
    
    public void remove_transcriptome()
    {
        this.transcriptome = null;
    }

    public void set_annotations(List<String> annotations) {
        this.annotations = new LinkedHashSet<>(annotations);
    }

    public void set_other_annotations(List<String> otherAnnotations) {
        this.otherAnnotations = new LinkedHashSet<>(annotations);
    }
    
    public void set_mapping_file(Path path)
    {
        this.mappingFile = path.toFile();
    }
    
    public void remove_mapping_file()
    {
        this.mappingFile = null;
    }
    
    public void set_transcriptome(Path path)
    {
        this.transcriptome = path.toFile();
    }

    public Map<String, List<Path>> get_sRNA_samples() {
        return fileToPath(sRNA_samples);
    }
    
    public void remove_sRNA_replicate(String sampleID, int replicateID)
    {
        this.sRNA_samples.get(sampleID).remove(replicateID);
    }

    public Map<String, List<Path>> get_mRNA_samples() {
        return fileToPath(mRNA_samples);
    }
    
    public void remove_deg_replicate(String sampleID, int replicateID) {
        this.degradomes.get(sampleID).remove(replicateID);
    }

    public Map<String, List<Pair<Path, Integer>>> get_degradomes() {
        return fileToPathDeg(degradomes);
    }

    public List<Path> get_GFFs() {
        List<Path> paths = new ArrayList<>();
        for(File f : GFFs)
        {
            paths.add(f.toPath());
        }
        return paths;
    }


    public List<String> get_annotations() {
        return new ArrayList<>(annotations);
    }

    public List<String> get_other_annotations() {
        return new ArrayList<>(otherAnnotations);
    }

    public Path get_genome() {
        if(this.genome != null)
        {
            return genome.toPath();
        }
        else
        {
            return null;
        }
    }

    public Path get_mapping_file() {
        if (this.mappingFile != null)
        {
            return mappingFile.toPath();
        }
        else
        {
            return null;
        }
    }

    public Path get_transcriptome() {
        if(this.transcriptome != null)
        {
        return transcriptome.toPath();
        }
        else
        {
            return null;
        }
    }
    
    private static Map<String, List<Pair<File, Integer>>> pathToFileDeg(Map<String, List<Pair<Path, Integer>>> paths)
    {
        Map<String, List<Pair<File, Integer>>> out = new HashMap<>();
        for(Entry<String, List<Pair<Path, Integer>>> e : paths.entrySet())
        {
            List<Pair<File, Integer>> f = new ArrayList<>();
            for(Pair<Path, Integer> p : e.getValue())
            {
                f.add(new Pair<>(p.getKey().toFile(), p.getValue()));
            }
            out.put(e.getKey(), f);
        }
        return out;
    }
    private static Map<String, List<Pair<Path, Integer>>> fileToPathDeg(Map<String, List<Pair<File, Integer>>> paths) {
        Map<String, List<Pair<Path, Integer>>> out = new HashMap<>();
        for (Entry<String, List<Pair<File,Integer>>> e : paths.entrySet()) {
            List<Pair<Path, Integer>> f = new ArrayList<>();
            for (Pair<File, Integer> p : e.getValue()) {
                f.add(new Pair<>(p.getKey().toPath(), p.getValue()));
            }
            out.put(e.getKey(), f);
        }
        return out;
    }
    
    private static Map<String, List<File>> pathToFile(Map<String, List<Path>> paths)
    {
        Map<String, List<File>> out = new LinkedHashMap<>();
        for(Entry<String, List<Path>> e : paths.entrySet())
        {
            List<File> f = new ArrayList<>();
            for(Path p : e.getValue())
            {
                f.add(p.toFile());
            }
            out.put(e.getKey(), f);
        }
        return out;
    }
    private static Map<String, List<Path>> fileToPath(Map<String, List<File>> paths) {
        Map<String, List<Path>> out = new LinkedHashMap<>();
        for (Entry<String, List<File>> e : paths.entrySet()) {
            List<Path> f = new ArrayList<>();
            for (File p : e.getValue()) {
                f.add(p.toPath());
            }
            out.put(e.getKey(), f);
        }
        return out;
    }

    public int getSampleSize(String sampleID)
    {
        return sRNA_samples.get(sampleID).size();
    }
}
