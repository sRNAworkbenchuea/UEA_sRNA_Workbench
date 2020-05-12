/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.util.ArrayList;
import java.util.regex.*;

/**
 *
 * @author ezb11yfu
 */
public class MirBaseCodeEntry
{

    public enum Arm 
    {
        THREE_PRIME {
            @Override
            public String toString()    {return "3p";}
        }, 
        FIVE_PRIME {
            @Override
            public String toString()    {return "5p";}
        }, 
        UNKNOWN {
            @Override
            public String toString()    {return "";}
        }
    }
    
    private String original_code;
    private String species;
    private String family;
    private String variant;
    private String copy_number;
    private Arm arm;
    private boolean star;
    
    private String seq;

    public MirBaseCodeEntry()
    {
        this("", "", "", "", "", Arm.UNKNOWN, false, "");
    }
    
    public MirBaseCodeEntry(String orig, String species, String family, String variant, String copy_number, Arm arm, boolean star, String seq)
    {
        this.original_code = orig;
        this.species = species;
        this.family = family;
        this.variant = variant;
        this.copy_number = copy_number;
        this.arm = arm;
        this.star = star;
    }
    
    public int hashcode()
    {
        int hash = 1;
        hash *= 31 + (this.original_code == null ? 0 : this.original_code.hashCode());
        hash *= 31 + (this.species == null ? 0 : this.species.hashCode());
        hash *= 31 + (this.family == null ? 0 : this.family.hashCode());
        hash *= 31 + (this.variant == null ? 0 : this.variant.hashCode());
        hash *= 31 + (this.copy_number == null ? 0 : this.copy_number.hashCode());
        hash *= 31 + (this.arm == null ? 0 : this.arm.hashCode());
        hash *= 31 + (this.star ? 0 : 1);
        hash *= 31 + (this.seq == null ? 0 : this.seq.hashCode());
        return hash;
    }
    
    public String getOriginalCode()     {return original_code;}
    public String getSpecies()          {return species;}
    public String getFamily()           {return family;}
    public String getVariant()          {return variant;}
    public String getCopy()             {return copy_number;}
    public Arm getArm()                 {return arm;}
    public boolean isStar()             {return star;}
    public String getSequence()         {return seq;}

    public static MirBaseCodeEntry create(String code, String seq)
    {
        MirBaseCodeEntry mbce = MirBaseCodeEntry.parseCode(code);
        return new MirBaseCodeEntry(code, mbce.species, mbce.family, mbce.variant, mbce.copy_number, mbce.arm, mbce.star, seq);
    }

    public static MirBaseCodeEntry parseCode(String str) throws IllegalArgumentException
    {
        String org = "", mir_str = "", id = "", fam_mem = "", variant = "", copy = "", orientation = "";
        boolean star = false;
        Arm arm = Arm.UNKNOWN;

        // Trim off the organism prefix (should always be present)
        int beginID = str.indexOf("-")+1;
        org = str.substring(0,beginID-1);
        
        // Trim off the star if present
        int endID = str.length();
        if (str.endsWith("*"))
        {
            endID--;
            star = true;
        }
        else
        {
            star = false;
        }
        
        String trimmedID = str.substring(beginID, endID).toLowerCase();
        
        // Trim off the orientation if present
        if (trimmedID.endsWith("-5p") || trimmedID.endsWith("-3p"))
        {
            orientation = trimmedID.substring(trimmedID.length()-3);
            arm = orientation.equals("-5p") ? Arm.FIVE_PRIME : Arm.THREE_PRIME;
            trimmedID = trimmedID.substring(0, trimmedID.length()-3);
        }
        
        // Trim off mir_str (must be present otherwise exception is thrown)
        Matcher om = Pattern.compile("^(mir-?|let-?|iab-?|bantam-?|lin-?|lsy-?)").matcher(trimmedID);
        if (om.find())
        {
            mir_str = om.group();
            trimmedID = trimmedID.substring(om.end());
        }
        else
            throw new IllegalArgumentException("Unknown miRBase ID encountered: " + str);

        // Trim off the id part if present
        if (!trimmedID.equals(""))
        {
            Matcher m_id = Pattern.compile("^(\\d+|[a-z]+\\d*)").matcher(trimmedID);
            if (m_id.find())
            {
                id = m_id.group();
                trimmedID = trimmedID.substring(m_id.end());
            }
            else
                throw new IllegalArgumentException("Unknown miRBase ID encountered: " + str);
        }
        
        // Trim off the variant if present
        if (!trimmedID.equals(""))
        {
            // Sometimes there might be more than 26 variants!
            Matcher m_id = Pattern.compile("^([a-z][a-z]|[a-z])").matcher(trimmedID);
            if (m_id.find())
            {
                variant = m_id.group();
                trimmedID = trimmedID.substring(m_id.end());
            }
            // Otherwise if no variant found just carry on... this isn't a problem as some
            // miRNAs do not contains variant information.
        }
        
        // What's left is the miRNA variant code (ignore the first character which will be a hash
        if (!trimmedID.equals(""))
        {
            copy = trimmedID.substring(1);
        }
        
        // Put everything together in the object.  Note that mir_str and id are combined,
        // as are variant and orientation... I can't think of any good reason to keep
        // these groups separate at the moment.
        return new MirBaseCodeEntry(str, org, mir_str + id, variant, copy, arm, star, "");
    }
    
    public boolean isAnimal()
    {
        if (family.startsWith("miR-"))
            return true;
        
        if (family.startsWith("let") || family.startsWith("bantam"))
            return true;
        
        return false;
    }

    
    @Override
    public String toString()
    {
//        String a = "";
//        if (this.arm == Arm.FIVE_PRIME)
//        {
//            a = "-5p";
//        }
//        else if (this.arm == Arm.THREE_PRIME)
//        {
//            a = "-3p";
//        }
//        
//        String copy = "";
//        if (this.copy_number.length() > 0)
//        {
//            copy = "-" + this.copy_number;
//        }
//        
//        return this.species + "-" + this.family + this.variant + copy + a + (this.star ? "*" : "");
        return this.original_code;
    }
    
    public static void main(String[] args)
    {
        ArrayList<MirBaseCodeEntry> list = new ArrayList<MirBaseCodeEntry>();
        list.add(MirBaseCodeEntry.parseCode("bmo-bantam"));
        list.add(MirBaseCodeEntry.parseCode("lgi-miR-279"));
        list.add(MirBaseCodeEntry.parseCode("tca-miR-279b"));
        list.add(MirBaseCodeEntry.parseCode("dps-miR-263b"));
        list.add(MirBaseCodeEntry.parseCode("lgi-miR-279"));
        list.add(MirBaseCodeEntry.parseCode("lmi-miR-8*"));
        list.add(MirBaseCodeEntry.parseCode("zma-miR160f*"));
        list.add(MirBaseCodeEntry.parseCode("zma-miR156k"));
        list.add(MirBaseCodeEntry.parseCode("dre-let-7a")); 
        list.add(MirBaseCodeEntry.parseCode("tgu-miR-301-5p"));
        list.add(MirBaseCodeEntry.parseCode("tgu-miR-301-5p*"));
        list.add(MirBaseCodeEntry.parseCode("dre-let-7a*"));
        list.add(MirBaseCodeEntry.parseCode("dre-miR-125a-5p"));
        list.add(MirBaseCodeEntry.parseCode("dre-miR219-1-3p"));
        list.add(MirBaseCodeEntry.parseCode("ebv-miR-BHRF1-1*"));
        list.add(MirBaseCodeEntry.parseCode("mghv-mir-M1-1*"));
        list.add(MirBaseCodeEntry.parseCode("rlcv-mir-rL1-18-5p"));
        list.add(MirBaseCodeEntry.parseCode("bhv1-miR-B8-5p"));
        list.add(MirBaseCodeEntry.parseCode("bhv1-miR-b8-5p"));
        list.add(MirBaseCodeEntry.parseCode("hiv1-miR-TAR-3p"));
     
        
        for(MirBaseCodeEntry ce : list)
        {
            System.out.println(ce);
        }
        
    }

//
    //    # capture organism, family and variant information
//    # from the miRNA ID
//    $match_string=~/
//      ^(\w+?)-             # organism acronym, ath, cre etc. - we need this later
//      (miR-?)?             # in almost all cases the name starts with "miR-" (animals)
//                           # or "miR" (plants) but dme-bantam or cel-let-7
//                           # are also valid as is miR-iab.
//      (\d+|\w+(?:-\d+)?)   # main miRNA ID
//                           # plants: miR123
//                           # animals: miR-123 or let,bantam or miR-iab
//                           # astring name can be followed by a number (let-7)
//                           # but don't capture that number separatedly
//      ([a-z])?             # Family member information (if any), examples:
//                           # plants: miR162a
//                           # animals: let-7a, miR-29a
//      ([^(]*)              # Anything between the ID (family member)
//                           # and the number of mismatches, starting with "("
//                           # is the "variant",
//                           # e.g. miR-125a-5p is the 5' mature sequence
//                           # mir161.1 and 161.2 are overlapping mature seq
//                           # from same hairpin
//                           # and miR219-1-3p and miR219-2-3p are the same mature
//                           # seqs from two distinct precursors
//      (\(\d+\))?           # number of mismatches to sRNA in brackets (from miRProf)
//    /ix ;
}
