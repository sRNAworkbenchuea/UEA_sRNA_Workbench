package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.Arrays;

/**
 *
 * @author Josh
 */
public class ResizableLongArray {

    long[] arr;
    double size;
    int currentPos;

    public ResizableLongArray() {
        size = 100;
        arr = new long[(int)size];
        currentPos = 0;
    }

    public void add(long i) {
        if (currentPos < (int)size) {
            arr[currentPos] = i;
            currentPos++;
        } else {
            resize();
            //System.out.println("resizing");
            arr[currentPos] = i;
            currentPos++;
        }

    }

    private void resize() {
        size = size * 1.5;
        currentPos = 0;
        long[] temp = new long[(int)size];

        for (int i = 0; i < arr.length; i++) {
            temp[i] = arr[i];
            currentPos++;
        }

        arr = temp;
    }

    public void sort() {

        size = currentPos;
        currentPos = 0;
        long[] temp = new long[(int)size];

        for (int i = 0; i < (int)size; i++) {
            temp[i] = arr[i];
            currentPos++;
        }

        arr = temp;
        Arrays.sort(arr);
    }
    
    public boolean contains(long key) {

        int low = 0;
        int high = currentPos-1;

        while (high >= low) {
            int middle = (low + high) / 2;
            if (arr[middle] == key) {
                return true;
            }
            if (arr[middle] < key) {
                low = middle + 1;
            }
            if (arr[middle] > key) {
                high = middle - 1;
            }
        }
        return false;
    }

}
