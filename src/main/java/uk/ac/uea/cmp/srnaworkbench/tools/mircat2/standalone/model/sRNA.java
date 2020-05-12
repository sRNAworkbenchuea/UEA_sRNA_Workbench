package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model;

import java.util.Objects;

public class sRNA
{
  protected int beg;
  protected int end;
  private int begRel;
  private int endRel;
  protected String sequence;
  protected char strand;
  protected int missmatches;
  protected String id;
  protected int length;
  protected int ab;
  
  public char getStrand()
  {
    return this.strand;
  }
  
  private boolean isFlipped = false;
  
  public boolean isFlipped()
  {
    return this.isFlipped;
  }
  
  public void flip()
  {
    this.isFlipped = (!this.isFlipped);
  }
  
  public void setStrand(char strand)
  {
    this.strand = strand;
  }
  
  public int getMissmatches()
  {
    return this.missmatches;
  }
  
  public boolean isSameStrand(sRNA s)
  {
    return s.strand == this.strand;
  }
  
  public void setMissmatches(int missmatches)
  {
    this.missmatches = missmatches;
  }
  
  public sRNA(String seq, int beg, int end, int ab)
  {
    this.sequence = seq;
    
    this.beg = beg;
    this.end = end;
    
    this.begRel = beg;
    this.endRel = end;
    
    this.length = (end - beg + 1);
    this.ab = ab;
  }
  
  public sRNA(int beg, int end, int ab, String ID)
  {
    this(null, beg, end, ab, ID);
  }
  
  public sRNA(String seq, int beg, int end, int ab, String ID)
  {
    this.sequence = seq;
    
    this.beg = beg;
    this.end = end;
    
    this.begRel = beg;
    this.endRel = end;
    
    this.length = (end - beg + 1);
    this.ab = ab;
    this.id = ID;
  }
  
  public int getBeg()
  {
    return this.begRel;
  }
  
  public int getBegplus1()
  {
    return this.begRel + 1;
  }
  
  public void setBeg(int beg)
  {
    this.beg = beg;
    this.begRel = beg;
    this.length = (this.end - beg + 1);
  }
  
  public int getLength()
  {
    return this.length;
  }
  
  public void setLength(int length)
  {
    this.length = length;
  }
  
  public int getAb()
  {
    return this.ab;
  }
  
  public void setAb(int ab)
  {
    this.ab = ab;
  }
  
  public int getEnd()
  {
    return this.endRel;
  }
  
  public int getEndplus1()
  {
    return this.endRel + 1;
  }
  
  public void setEnd(int end)
  {
    this.end = end;
    this.endRel = end;
    this.length = (end - this.beg + 1);
  }
  
  public String getSequence()
  {
    return this.sequence;
  }
  
  public void setSequence(String sequence)
  {
    this.sequence = sequence;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public void setId(String id)
  {
    this.id = id;
  }
  
  public int hashCode()
  {
    int hash = 7;
    hash = 23 * hash + this.beg;
    hash = 23 * hash + this.end;
    hash = 23 * hash + Objects.hashCode(this.sequence);
    hash = 23 * hash + this.strand;
    
    hash = 23 * hash + Objects.hashCode(this.id);
    hash = 23 * hash + this.length;
    hash = 23 * hash + this.ab;
    return hash;
  }
  
  public boolean equals(Object obj)
  {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    sRNA other = (sRNA)obj;
    if (this.beg != other.beg) {
      return false;
    }
    if (this.end != other.end) {
      return false;
    }
    if (!Objects.equals(this.sequence, other.sequence)) {
      return false;
    }
    if (this.strand != other.strand) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (this.length != other.length) {
      return false;
    }
    if (this.ab != other.ab) {
      return false;
    }
    return true;
  }
  
  @Override
  public String toString()
  {
    return this.id + "\t" + this.sequence + "\t" + (this.beg + 1) + "\t" + (this.end + 1) + "\t" + this.ab;
  }
  
  public String myToString(int beg)
  {
    StringBuilder str = new StringBuilder();
    
    int b = this.beg;
    for (int i = 0; i < b - beg; i++) {
      str.append(" ");
    }
    for (int i = 0; i < this.length; i++) {
      str.append("-");
    }
    str.append(" " + this.ab);
    
    return str.toString();
  }
  
  public String myToString(int beg, char ch)
  {
    StringBuilder str = new StringBuilder();
    
    int b = this.beg;
    for (int i = 0; i < b - beg; i++) {
      str.append(" ");
    }
    for (int i = 0; i < this.length; i++) {
      str.append(ch);
    }
    str.append(" " + this.ab);
    
    return str.toString();
  }
  
  public String myToString(int beg, char ch, boolean isRel)
  {
    StringBuilder str = new StringBuilder();
    int b;
    if (isRel) {
      b = this.begRel;
    } else {
      b = this.beg;
    }
    for (int i = 0; i < b - beg; i++) {
      str.append(" ");
    }
    for (int i = 0; i < this.length; i++) {
      str.append(ch);
    }
    str.append(" " + this.ab);
    
    return str.toString();
  }
  
  public String myToStringSeq(int beg)
  {
    StringBuilder str = new StringBuilder();
    
    int b = this.beg;
    for (int i = 0; i < b - beg; i++) {
      str.append(" ");
    }
    str.append(this.sequence);
    
    str.append(" " + this.ab);
    
    return str.toString();
  }
  
  public String myToStringSeq(int beg, boolean isRel)
  {
    StringBuilder str = new StringBuilder();
    int b;
    if (isRel) {
      b = this.begRel;
    } else {
      b = this.beg;
    }
    for (int i = 0; i < b - beg; i++) {
      str.append(" ");
    }
    str.append(this.sequence);
    
    str.append(" " + this.ab);
    
    return str.toString();
  }
  
  public boolean isSameSequence(sRNA other)
  {
    return (this.sequence.equals(other.sequence)) && (this.ab == other.ab);
  }
  
  public String toStringPatman()
  {
    return this.id + "\t" + this.sequence + "(" + this.ab + ")" + "\t" + (this.beg + 1) + "\t" + (this.end + 1) + "\t" + this.strand + "\t" + this.missmatches;
  }
  
  public String toStringPatman(char ch)
  {
    return this.id + ch + this.sequence + ch + this.ab + ch + (this.beg + 1) + ch + (this.end + 1) + ch + this.strand + ch + this.missmatches;
  }
  
  public static sRNA readFromPatman(String line)
  {
    String[] sa = line.split("\t");
    
    int b = 0;
    int e = 0;
    try
    {
      b = Integer.parseInt(sa[2].trim()) - 1;
      e = Integer.parseInt(sa[3].trim()) - 1;
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
    String[] sb = sa[1].split("\\(|\\)");
    
    int a = 1;
    try
    {
      a = Integer.parseInt(sb[1].trim());
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
    String seq = sb[0];
    
    char strand = sa[4].charAt(0);
    
    sRNA srna = new sRNA(seq, b, e, a, sa[0]);
    
    srna.setStrand(strand);
    int missm = 0;
    try
    {
      missm = Integer.parseInt(sa[5].trim());
    }
    catch (NumberFormatException ex)
    {
      missm = 0;
    }
    srna.setMissmatches(missm);
    
    return srna;
  }
  
  public boolean isComplex(double percent)
  {
    String seq = this.sequence;
    int[] chars = new int[5];
    
    char[] arrayOfChar = seq.toCharArray();int i = arrayOfChar.length;
    for (char c1 = '\000'; c1 < i; c1++)
    {
      char c = arrayOfChar[c1];
      if (c == 'A') {
        chars[0] += 1;
      } else if (c == 'C') {
        chars[1] += 1;
      } else if (c == 'G') {
        chars[2] += 1;
      } else if ((c == 'T') || (c == 'U')) {
        chars[3] += 1;
      } else {
        chars[4] += 1;
      }
    }
    int size = seq.length();
    
    int[] arrayOfInt1 = chars;int c1 = arrayOfInt1.length;
    for (char c = '\000'; c < c1; c++)
    {
      int val = arrayOfInt1[c];
      if (val / size >= percent) {
        return false;
      }
    }
    for (i = 0; i < 5; i++) {
      for (int j = i + 1; j < 5; j++) {
        if ((chars[i] + chars[j]) / size >= percent) {
          return false;
        }
      }
    }
    return true;
  }
  
  public void setBegEndRel(int b, int e)
  {
    this.begRel = (b + (e - this.end));
    this.endRel = (this.begRel + this.length - 1);
    flip();
  }
  
  public boolean isNegative()
  {
    return this.strand == '-';
  }
  
  public void resetBegEndRel()
  {
    this.begRel = this.beg;
    this.endRel = this.end;
    if (this.isFlipped) {
      flip();
    }
  }
}
