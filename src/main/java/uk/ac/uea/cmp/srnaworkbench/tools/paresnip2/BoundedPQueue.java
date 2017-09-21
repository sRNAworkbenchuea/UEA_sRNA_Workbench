/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author rew13hpu
 * @param 
 */
public class BoundedPQueue<AlignmentPath extends Comparable<AlignmentPath>> {

    PriorityQueue<AlignmentPath> queue;
    int size = 0;

    public BoundedPQueue(int capacity) {
        queue = new PriorityQueue(capacity, new CustomComparator());
        size = capacity;

    }

    public boolean offer(AlignmentPath e) {

        AlignmentPath vl = null;
        if (queue.size() >= size) {
            vl = queue.poll();
            if (vl.compareTo(e) < 0) {
                e = vl;
            }
        }

        return queue.offer(e);

    }

    public AlignmentPath poll() {

        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public List<AlignmentPath> toSortedList() {
        if (queue.isEmpty()) {
            return new ArrayList<AlignmentPath>();
        }

        ArrayList<AlignmentPath> sorted = new ArrayList(queue.size());
        while (!queue.isEmpty()) {
            sorted.add(queue.poll());
        }

        Collections.reverse(sorted);
        return sorted;
    }

    public static class CustomComparator<E extends Comparable<E>> implements Comparator<E> {

        @Override
        public int compare(E o1, E o2) {
            //give me a max heap
            return o1.compareTo(o2) * -1;

        }
    }
}
