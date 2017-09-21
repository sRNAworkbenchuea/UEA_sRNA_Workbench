/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author w0445959
 */
public class ByteUtils
{
  public static int byteArrayToInt( byte[] b )
  {
    final ByteBuffer bb = ByteBuffer.wrap( b );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    return bb.getInt();
  }
}
