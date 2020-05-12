package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Patman
  extends ArrayList<sRNA>
{
  private sRNA mostAbundant = null;
  private boolean mostAbundantSet = false;
  private int beg = Integer.MAX_VALUE;
  private int end = Integer.MIN_VALUE;
  private int begRel = Integer.MAX_VALUE;
  private int endRel = Integer.MIN_VALUE;
  
  public Map<String, Patman> buildChromosomeMap()
  {
    HashMap<String, Patman> chr_map = new HashMap();
    for (int i = 0; i < size(); i++)
    {
      sRNA pe = (sRNA)get(i);
      String chromID = pe.getId();
      if (!chr_map.containsKey(chromID))
      {
        Patman p = new Patman();
        p.add(pe);
        chr_map.put(chromID, p);
      }
      else
      {
        ((Patman)chr_map.get(chromID)).add(pe);
      }
    }
    return chr_map;
  }
  
  public void sortByAbundance()
  {
    Collections.sort(this, new Comparator<sRNA>()
    {
      public int compare(sRNA p1, sRNA p2)
      {
        return p2.getAb() - p1.getAb();
      }
    });
  }
  
  public int length()
  {
    if (this.beg == Integer.MAX_VALUE)
    {
      if (isEmpty()) {
        return 0;
      }
      int min = ((sRNA)get(0)).getBeg();
      int max = ((sRNA)get(0)).getEnd();
      for (int i = 1; i < size(); i++)
      {
        if (min > ((sRNA)get(i)).getBeg()) {
          min = ((sRNA)get(i)).getBeg();
        }
        if (max < ((sRNA)get(i)).getEnd()) {
          max = ((sRNA)get(i)).getEnd();
        }
      }
      return max - min;
    }
    return this.end - this.beg;
  }
  
  public int getBeg()
  {
    if (this.beg == Integer.MAX_VALUE)
    {
      int b = Integer.MAX_VALUE;
      for (sRNA s : this) {
        if (b > s.getBeg()) {
          b = s.getBeg();
        }
      }
      this.begRel = b;
      this.beg = b;
      return b;
    }
    return this.begRel;
  }
  
  public int getEnd()
  {
    if (this.end == Integer.MIN_VALUE)
    {
      int e = Integer.MIN_VALUE;
      for (sRNA s : this) {
        if (e < s.getEnd()) {
          e = s.getEnd();
        }
      }
      this.endRel = e;
      this.end = e;
      return e;
    }
    return this.endRel;
  }
  
  public void flipBegEndRel(int b, int e)
  {
    this.endRel = (b + (e - this.end) + length() - 1);
    this.begRel = (b + (e - this.end));
  }
  
  public void resetBegEndRel()
  {
    this.begRel = this.beg;
    this.endRel = this.end;
  }
  
  public sRNA mostAbundantSRNA()
  {
    if (this.mostAbundantSet == true) {
      return this.mostAbundant;
    }
    int max = Integer.MIN_VALUE;
    int index = -1;
    for (int i = 0; i < size(); i++)
    {
      int ab = ((sRNA)get(i)).getAb();
      if (ab > max)
      {
        max = ab;
        index = i;
      }
    }
    this.mostAbundantSet = true;
    if (index >= 0) {
      this.mostAbundant = ((sRNA)get(index));
    }
    return this.mostAbundant;
  }
  
  public int getAb()
  {
    int sum = 0;
    for (sRNA s : this) {
      sum += s.getAb();
    }
    return sum;
  }
  
  public boolean add(sRNA e)
  {
    if (this.mostAbundantSet) {
      if (this.mostAbundant == null) {
        this.mostAbundant = e;
      } else if (e.getAb() > this.mostAbundant.getAb()) {
        this.mostAbundant = e;
      }
    }
    if (this.beg > e.getBeg())
    {
      this.beg = e.getBeg();
      this.begRel = this.beg;
    }
    if (this.end < e.getEnd())
    {
      this.end = e.getEnd();
      this.endRel = this.end;
    }
    return super.add(e);
  }
  
  public void sort()
  {
    Collections.sort(this, new Comparator<sRNA>()
    {
      @Override
      public int compare(sRNA o1, sRNA o2)
      {
        if (o1.getId().compareTo(o2.getId()) < 0) {
          return -1;
        }
        if (o1.getId().compareTo(o2.getId()) > 0) {
          return 1;
        }
        if (o1.getBeg() > o2.getBeg()) {
          return 1;
        }
        if (o1.getBeg() < o2.getBeg()) {
          return -1;
        }
        return Integer.compare(o1.getEnd(), o2.getEnd());
      }
       
    });
  }
  
  public int hashCode()
  {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.mostAbundant);
    hash = 31 * hash + (this.mostAbundantSet ? 1 : 0);
    hash = 31 * hash + this.beg;
    hash = 31 * hash + this.end;
    for (sRNA s : this) {
      hash = 31 * hash + Objects.hashCode(s);
    }
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
    Patman other = (Patman)obj;
    if (!Objects.equals(this.mostAbundant, other.mostAbundant)) {
      return false;
    }
    if (this.mostAbundantSet != other.mostAbundantSet) {
      return false;
    }
    if (this.beg != other.beg) {
      return false;
    }
    if (this.end != other.end) {
      return false;
    }
    if (size() != other.size()) {
      return false;
    }
    for (int i = 0; i < size(); i++)
    {
      sRNA s1 = (sRNA)get(i);
      sRNA s2 = (sRNA)other.get(i);
      if (!s1.equals(s2)) {
        return false;
      }
    }
    return true;
  }
}
