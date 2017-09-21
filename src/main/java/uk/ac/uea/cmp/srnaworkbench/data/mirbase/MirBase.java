/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.duplex.Duplex;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;

/**
 *
 * @author ezb11yfu
 */
public class MirBase
{
  private File maturefile;
  private File hairpinfile;
  private File organismfile;
  private List<MirBaseHeader> mature_seqs;
  private List<MirBaseHeader> hairpin_seqs;
  private OrganismMap org_map;
  private Map<String, MirBaseEntry> mirnas;

  // Category enum, useful for handling different miRNA groups: ALL, ANIMAL, PLANT, VIRUS
  public enum Category
  {
    ALL
    {
      @Override
      public String getFileString()
      {
        return "";
      }

      @Override
      public String getFilterString()
      {
        return "";
      }
    },
    ANIMAL
    {
      @Override
      public String getFileString()
      {
        return "_animal";
      }

      @Override
      public String getFilterString()
      {
        return "Metazoa";
      }
    },
    PLANT
    {
      @Override
      public String getFileString()
      {
        return "_plant";
      }

      @Override
      public String getFilterString()
      {
        return "Viridiplantae";
      }
    },
    VIRUS
    {
      @Override
      public String getFileString()
      {
        return "_virus";
      }

      @Override
      public String getFilterString()
      {
        return "Viruses";
      }
    };

    public abstract String getFileString();

    public abstract String getFilterString();

    public static Category parse( String category )
    {
      if ( category.equalsIgnoreCase( "All" ) )
      {
        return Category.ALL;
      }
      else
      {
        if ( category.equalsIgnoreCase( "Animal" ) )
        {
          return Category.ANIMAL;
        }
        else
        {
          if ( category.equalsIgnoreCase( "Plant" ) )
          {
            return Category.PLANT;
          }
          else
          {
            if ( category.equalsIgnoreCase( "Virus" ) )
            {
              return Category.VIRUS;
            }
          }
        }
      }

      return Category.ALL;
    }
  }
  // ********** Static functions for handling versions and files ***********
  public static final File MIRBASE_ROOT_FOLDER = new File( Tools.DATA_DIR.getPath() + DIR_SEPARATOR + "mirbase" );

  static
  {
    if ( !MIRBASE_ROOT_FOLDER.exists() )
    {
      if ( MIRBASE_ROOT_FOLDER.mkdir() )
      {
        WorkbenchLogger.LOGGER.log( Level.INFO, "created mirbase directory" );
      }
    }
  }

  public static boolean localVersionExists( String version )
  {
    if ( version == null || version.isEmpty() )
    {
      return false;
    }
    return new File( MirBase.MIRBASE_ROOT_FOLDER.getPath() + DIR_SEPARATOR + version ).exists();
  }

  public static String getLatestLocalVersion() throws Exception
  {
    File[] folders = MirBase.MIRBASE_ROOT_FOLDER.listFiles();

    double latest = -1.0;
    String latest_str = null;
    for ( File version : folders )
    {
      String ver_str = version.getName();

      try
      {
        double ver = Double.parseDouble( ver_str );

        if ( ver > latest )
        {
          latest = ver;
          latest_str = ver_str;
        }
      }
      catch ( NumberFormatException e )
      {
        // Just ignore if we find some unexpected folder.
      }
    }

    return latest_str;
  }

  public static File getMirBaseFolder() throws Exception
  {
    return new File( MIRBASE_ROOT_FOLDER.getPath() + DIR_SEPARATOR + getLatestLocalVersion() );
  }

  public static File getMirBaseFolder( String version ) throws Exception
  {
    return new File( MIRBASE_ROOT_FOLDER.getPath() + DIR_SEPARATOR + version );
  }

  public static File getMatureFile( Category category, boolean dnaform ) throws Exception
  {
    return getMirbaseFile( getLatestLocalVersion(), false, category, dnaform );
  }

  public static File getMatureFile( String version, Category category, boolean dnaform ) throws Exception
  {
    return getMirbaseFile( version, false, category, dnaform );
  }

  public static File getHairpinFile( Category category, boolean dnaform ) throws Exception
  {
    return getMirbaseFile( getLatestLocalVersion(), true, category, dnaform );
  }

  public static File getHairpinFile( String version, Category category, boolean dnaform ) throws Exception
  {
    return getMirbaseFile( version, true, category, dnaform );
  }

  private static File getMirbaseFile( String version, boolean hairpin, Category category, boolean dnaform ) throws Exception
  {
    String mb_folder = MIRBASE_ROOT_FOLDER.getPath() + DIR_SEPARATOR + version + DIR_SEPARATOR;

    String prefix = hairpin ? "hairpin" : "mature";
    String cat = category.getFileString();
    String dna = dnaform ? "_dnaform" : "";
    String ext = ".fa";

    return new File( mb_folder + prefix + cat + dna + ext );
  }

  private static File getOrganismFile() throws Exception
  {
    return getOrganismFile( getLatestLocalVersion() );
  }

  public static File getOrganismFile( String version ) throws Exception
  {
    return new File( MirBase.getMirBaseFolder( version ).getPath() + DIR_SEPARATOR + "organism.txt" );
  }

  // *************  Constructors **************
  public MirBase() throws Exception
  {
    this( getLatestLocalVersion() );
  }

  public MirBase( String version ) throws Exception
  {
    this( version, MirBase.Category.ALL, false );
  }

  public MirBase( String version, Category category, boolean dnaform ) throws Exception
  {
    File mature = MirBase.getMatureFile( version, category, dnaform );
    File hairpin = MirBase.getHairpinFile( version, category, dnaform );
    File organism = MirBase.getOrganismFile( version );

    if ( mature == null || !mature.exists() )
    {
      throw new IllegalArgumentException( "Mature miRNA file not specified or does not exist." );
    }

    if ( hairpin == null || !hairpin.exists() )
    {
      throw new IllegalArgumentException( "miRNA hairpin file not specified or does not exist." );
    }

    if ( organism == null || !organism.exists() )
    {
      throw new IllegalArgumentException( "Organism map file not specified or does not exist." );
    }

    this.maturefile = mature;
    this.hairpinfile = hairpin;
    this.organismfile = organism;

    readFiles();
  }

  private void readFiles() throws IOException
  {
    mature_seqs = new MirBaseFastaReader( maturefile ).process();
    hairpin_seqs = new MirBaseFastaReader( hairpinfile ).process();
    org_map = new OrganismMap( organismfile );
  }

  public HashMap<String, MirBaseHeader> getHairpinMap( boolean DNA )
  {
    HashMap<String, MirBaseHeader> returnMe = new HashMap<String, MirBaseHeader>();

    for ( MirBaseHeader mbh : hairpin_seqs )
    {
      if ( DNA )
      {
        mbh.setSeq( mbh.getSeq().replaceAll( "U", "T" ) );
      }


      returnMe.put( mbh.getSeq(), mbh );


    }

    return returnMe;
  }

  public HashMap<String, ArrayList<MirBaseHeader>> getMatureMap( boolean DNA )
  {
    HashMap<String, ArrayList<MirBaseHeader>> returnMe = new HashMap<String, ArrayList<MirBaseHeader>>();

    for ( MirBaseHeader mbh : mature_seqs )
    {
      if ( DNA )
      {
        mbh.setSeq( mbh.getSeq().replaceAll( "U", "T" ) );
      }

      if ( returnMe.containsKey( mbh.getSeq() ) )
      {
        returnMe.get( mbh.getSeq() ).add( mbh );
      }
      else
      {
        ArrayList<MirBaseHeader> newList = new ArrayList<MirBaseHeader>();
        newList.add( mbh );
        returnMe.put( mbh.getSeq(), newList );
      }
    }

    return returnMe;
  }

  // Method that tries to link mature miRNAs to hairpins
  // There are multiple ways of doing this and I'll try to allow the user to configure
  // some of those ways for themselves: via species and variants.  If the values to
  // any of these params is true then the variant mirna seqs will be bundled up
  // with the mirna family name.  In other words, grouping causes fewer entries in
  // the returned hashmap, but more seqs within each entry.
  public Map<String, MirBaseEntry> buildMirnas( boolean group_species, boolean group_variants, String filter ) throws Exception
  {
    HashMap<String, MirBaseEntry> m = new HashMap<String, MirBaseEntry>();
    for ( MirBaseHeader mbh : orgFilter( this.mature_seqs, filter ) )
    {
      MirBaseCodeEntry mbce = mbh.getMircode();

      String key = ( group_species ? "" : ( mbce.getSpecies() + "-" ) )
        + mbce.getFamily()
        + ( group_variants ? "" : mbce.getVariant() );

      MirBaseEntry mbe = m.get( key );
      if ( mbe == null )
      {
        mbe = new MirBaseEntry(
          key,
          null,
          null,
          null );
      }

//            String variant = mbce.getSpecies()
//                    + (group_variants ? (!mbce.getVariant().equals("") ? "-" + mbce.getVariant() : "") : "")
//                    + (!mbce.getCopy().equals("") ? "-" + mbce.getCopy() : "")
//                    + (!mbce.getArm().equals(MirBaseCodeEntry.Arm.UNKNOWN) ? "-" + mbce.getArm().toString() : "");
      String variant = mbce.getOriginalCode();

      if ( mbce.isStar() )
      {
        mbe.addMirnaStarSeq( variant, mbh.getSeq() );
      }
      else
      {
        mbe.addMirnaSeq( variant, mbh.getSeq() );
      }

      m.put( key, mbe );
    }

    for ( MirBaseHeader mbh : orgFilter( this.hairpin_seqs, filter ) )
    {
      MirBaseCodeEntry mbce = mbh.getMircode();

      String key = ( group_species ? "" : ( mbce.getSpecies() + "-" ) )
        + mbce.getFamily()
        + ( group_variants ? "" : mbce.getVariant() );

      MirBaseEntry mbe = m.get( key );
      int hairpin_only_count = 0;
      if ( mbe == null )
      {
        hairpin_only_count++;
        throw new Exception( "miRBase files corrupted.  Hairpin found without associated miRNA or miRNA*." );
        // Or maybe we should create a new entry.??
      }
//            String variant = mbce.getSpecies()
//                    + (group_variants ? (!mbce.getVariant().equals("") ? "-" + mbce.getVariant() : "") : "")
//                    + (!mbce.getCopy().equals("") ? "-" + mbce.getCopy() : "")
//                    + (!mbce.getArm().equals(MirBaseCodeEntry.Arm.UNKNOWN) ? "-" + mbce.getArm().toString() : "");
      String variant = mbce.getOriginalCode();
      mbe.addHairpinSeq( variant, mbh.getSeq() );
      m.put( key, mbe );
    }

    return m;
  }

  public List<Duplex> getDuplexIDs()
  {
    List<Duplex> duplexes = new ArrayList<Duplex>();

    for ( Map.Entry<String, MirBaseEntry> me : this.mirnas.entrySet() )
    {
      Map<String, String> mirnas = me.getValue().getMirnaSeqs();
      Map<String, String> mirna_stars = me.getValue().getMirnaStarSeqs();

      if ( mirnas.size() == 1 && mirna_stars.size() == 1 )
      {
        String id1 = "", id2 = "", seq1 = "", seq2 = "";
        for ( Map.Entry<String, String> me1 : mirnas.entrySet() )
        {
          id1 = me1.getKey();
          seq1 = me1.getValue();
        }
        for ( Map.Entry<String, String> me2 : mirna_stars.entrySet() )
        {
          id2 = me2.getKey();
          seq2 = me2.getValue();
        }

        duplexes.add( new Duplex( id1, seq1, id2, seq2 ) );
      }

    }

    return duplexes;
  }

  public Map<String, MirBaseEntry> getByMirnaSeq() throws Exception
  {
    Map<String, MirBaseEntry> mirnaseq = new HashMap<String, MirBaseEntry>();
    for ( Map.Entry<String, MirBaseEntry> me : this.mirnas.entrySet() )
    {
      Map<String, String> seqs = me.getValue().getMirnaSeqs();

      if ( seqs == null || seqs.values().isEmpty() )
      {
        WorkbenchLogger.LOGGER.log( Level.WARNING, "No mature miRNAs in entry: " + me.getValue().getMirnaID() );
        
        //throw new Exception("No mature miRNAs in entry: " + me.getValue().getCode().getID()");
        continue;
      }
      for ( String seq : seqs.values() )
      {
        if ( mirnaseq.get( seq ) != null )
        {
          //System.out.println("Warning: Already have mature miRNA seq in hash: " + seq);
          //throw new Exception("Already have mature miRNA seq in hash: " + seq);
        }

        mirnaseq.put( seq, me.getValue() );
      }
    }
    return mirnaseq;
  }

  public List<MirBaseHeader> getFilteredMatureSeqs( String filter )
  {
    return orgFilter( this.mature_seqs, filter );
  }

  public List<MirBaseHeader> getFilteredHairpinSeqs( String filter )
  {
    return orgFilter( this.hairpin_seqs, filter );
  }

  private List<MirBaseHeader> orgFilter( List<MirBaseHeader> items, String filter )
  {
    ArrayList<MirBaseHeader> filtered = new ArrayList<MirBaseHeader>();

    Map<String, OrganismEntry> map = this.org_map.organismsInFamily( filter );

    for ( MirBaseHeader mbh : items )
    {
      String name_code = mbh.getMircode().getSpecies();

      if ( map.get( name_code ) != null )
      {
        filtered.add( mbh );
      }
    }

    return filtered;
  }

  private List<MirBaseHeader> mirFilter( List<MirBaseHeader> items, String filter )
  {
    ArrayList<MirBaseHeader> filtered = new ArrayList<MirBaseHeader>();

    for ( MirBaseHeader mbh : items )
    {
      String fam_code = mbh.getMircode().getFamily();

      if ( fam_code.equals( filter ) )
      {
        filtered.add( mbh );
      }
    }

    return filtered;
  }

  public void createFilteredMatureFile( File out_file, String filter ) throws IOException
  {
    PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( out_file ) ) );

    for ( MirBaseHeader mbh : orgFilter( this.mature_seqs, filter ) )
    {
      pw.write( ">" + mbh.getMircode() + "\n" );// " + mbh.getBinomialLatinName() + "\n");
      pw.write( mbh.getSeq() + "\n" );
    }
    pw.flush();
    pw.close();
  }

  public void createFilteredHairpinFile( File out_file, String filter ) throws IOException
  {
    PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( out_file ) ) );

    for ( MirBaseHeader mbh : orgFilter( this.hairpin_seqs, filter ) )
    {
      pw.write( ">" + mbh.getMircode() + "\n" );// " + mbh.getBinomialLatinName() + "\n");
      pw.write( mbh.getSeq() + "\n" );
    }
    pw.flush();
    pw.close();
  }
}
