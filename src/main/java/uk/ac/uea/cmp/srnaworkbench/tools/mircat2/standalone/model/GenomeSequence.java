package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model;

import java.util.Objects;

public class GenomeSequence
{
  private String id;
  private StringBuilder sequence;
  private int beg;
  private int end;
  
  public StringBuilder getSequence()
  {
    return this.sequence;
  }
  
  public void setSequence(StringBuilder sequence)
  {
    this.sequence = sequence;
  }
  
  public GenomeSequence(String id, int beg, int end)
  {
    this.id = id;
    this.beg = beg;
    this.end = end;
  }
  
  public GenomeSequence(String id, StringBuilder seq, int beg, int end)
  {
    this.id = id;
    this.sequence = seq;
    this.beg = beg;
    this.end = end;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public void setId(String id)
  {
    this.id = id;
  }
  
  public int getBeg()
  {
    return this.beg;
  }
  
  public void setBeg(int beg)
  {
    this.beg = beg;
  }
  
  public String getSubSequence(int beg, int end)
  {
    return this.sequence.substring(beg, end + 1);
  }
  
  public int getEnd()
  {
    return this.end;
  }
  
  public void setEnd(int end)
  {
    this.end = end;
  }
  
  public String toString()
  {
    return "id = " + this.id + ", beg = " + this.beg + ", end = " + this.end;
  }
  
  public String myToString()
  {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < this.end - this.beg; i++) {
      str.append("-");
    }
    return str.toString();
  }
  
  public int hashCode()
  {
    int hash = 5;
    hash = 37 * hash + Objects.hashCode(this.id);
    
    hash = 37 * hash + this.beg;
    hash = 37 * hash + this.end;
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
    GenomeSequence other = (GenomeSequence)obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (this.beg != other.beg) {
      return false;
    }
    if (this.end != other.end) {
      return false;
    }
    return true;
  }
}
