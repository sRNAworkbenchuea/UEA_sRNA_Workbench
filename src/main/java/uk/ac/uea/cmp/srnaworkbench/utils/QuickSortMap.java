/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;
import java.util.ArrayList;
/**
 *
 * @author w0445959
 */
public class QuickSortMap {
    private static int partition(ArrayList< ArrayList<String> > arr, int left, int right)
    {

          int i = left, j = right;

          ArrayList<String> tmp;

          int pivot = Integer.parseInt(arr.get((left + right) / 2).get(1));


          while (i <= j) {

                while (Integer.parseInt(arr.get(i).get(1)) < pivot)

                      i++;

                while (Integer.parseInt(arr.get(j).get(1)) > pivot)

                      j--;

                if (i <= j) {

                      tmp = arr.get(i);

                      arr.set(i, arr.get(j));

                      arr.set(j, tmp);

                      i++;

                      j--;

                }

          }


          return i;

    }



    /**
     *
     * @param data
     * @param left
     * @param right
     */
    public static void quickSort(ArrayList< ArrayList<String> > data, int left, int right) {

          int index = partition(data, left, right);

          if (left < index - 1)

                quickSort(data, left, index - 1);

          if (index < right)

                quickSort(data, index, right);

    }
}
