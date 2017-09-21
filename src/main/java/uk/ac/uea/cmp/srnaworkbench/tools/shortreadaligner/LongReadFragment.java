/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.shortreadaligner;

import uk.ac.uea.cmp.srnaworkbench.exceptions.SequenceContainsInvalidBaseException;


/**
 *
 * @author rew13hpu
 */
public class LongReadFragment extends Sequence {

    public LongReadFragment(String s) throws SequenceContainsInvalidBaseException {
        
        s = s.toUpperCase();
        sequence = super.toBits(s);
        stringSeq = s;

    }

    @Override
    protected long setBit(int bit, long target, char c) throws SequenceContainsInvalidBaseException {

        switch (c) { 
            case 'A':
                target |= 0L << bit | 0L << bit + 1;
                break;
            case 'C':
                target |= 1L << bit | 0L << bit + 1;
                break;
            case 'T':
                target |= 1L << bit | 1L << bit + 1;
                break;
            case 'U':
                target |= 1L << bit | 1L << bit + 1;
                break;
            case 'G':
                target |= 0L << bit | 1L << bit + 1;
                break;
            default:
                throw new SequenceContainsInvalidBaseException();
        }

        return target;

    }

}
