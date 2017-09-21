package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.Serializable;

/**
 *
 * @author Josh
 */
public class ResizableIntArray implements Serializable {

    int[] arr;
    double size;
    int currentPos;
    private static final long serialVersionUID = 101;

    public ResizableIntArray() {
        size = 2000;
        arr = new int[(int) size];
        currentPos = 0;
    }

    public void add(int i) {
        if (currentPos < (int) size) {
            arr[currentPos] = i;
            currentPos++;
        } else {
            //System.out.println("Resizing");
            size = size * 1.5;
            currentPos = 0;
            int[] temp = new int[(int) size];

            for (int j = 0; j < arr.length; j++) {
                temp[j] = arr[j];
                currentPos++;
            }

            arr = temp;

            arr[currentPos] = i;
            currentPos++;
        }

    }

    public boolean contains(int key) {

        int low = 0;
        int high = currentPos - 1;

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
