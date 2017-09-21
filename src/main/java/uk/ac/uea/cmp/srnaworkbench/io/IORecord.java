/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author w0445959
 */
public abstract class IORecord implements Comparable<String>
{
  public abstract void read( RandomAccessFile hFile, long iRecNo )
    throws IOException;

  public abstract void write( RandomAccessFile hFile )
    throws IOException;

  public abstract int length();

  public abstract String report();
}