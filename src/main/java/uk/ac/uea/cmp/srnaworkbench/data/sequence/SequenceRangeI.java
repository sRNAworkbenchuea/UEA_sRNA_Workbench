/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import java.util.*;

/**
 * Interface for various sequences, e.g. FASTA records, GFF records, etc.
 * Or something more general like a window onto a governing sequence.
 *
 * @author prb07qmu
 */
public interface SequenceRangeI
{
  /**
   * The sequence's start index (zero-based)
   * @return
   */
  public int getStartIndex();

  /**
   * The sequence's end index (zero-based)
   * @return
   */
  public int getEndIndex();

  /**
   * Get the sequence bases
   * @return
   */
  public String getSequence();

  /**
   * Get the length (the sequence may not be set but a length may be valid)
   * @return
   */
  public int getSequenceLength();

  /**
   * The sequence's strandedness
   * @return
   */
  public SequenceStrand getSequenceStrand();

  /**
   * Get the string identifier for the sequence
   * @return
   */
  public String getSequenceId();

  /**
   * Get the height scaling factor
   * @return A percentage in the range [ 0, 100 ]
   */
  public float getGlyphHeightScalePercent();

  /**
   * Set the height scaling factor
   * @param percentage In the range [ 0, 100 ]
   */
  public void setGlyphHeightScalePercent( float percentage );

  /**
   * Get the sequence's attributes
   * @return A map representing a set of key-value pairs
   */
  public Map<String,String> getAttributes();

  /**
   * Add a key-value pair to the attributes map.<br/>
   * If the key already exists in the map then it is overwritten.<br/>
   * @param key
   * @param value
   */
  public void addAttribute( String key, String value );

  /**
   * Get an attribute given a key.<br/>
   * @return Return {@code null} if the key isn't found
   */
  public String getAttribute( String key );

  /**
   * Get the parent sequence range
   * @return An object which implements {@code SequenceRangeI}
   */
  public SequenceRangeI getParentSequenceRangeI();
}
