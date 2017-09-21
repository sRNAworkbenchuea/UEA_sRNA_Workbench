/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io.decoders;

import java.nio.ByteBuffer;

/**
 *
 * @author M.B Stocks
 * @param <T>
 */
public interface Decoder<T> {

    public T decode(ByteBuffer buffer);
}

