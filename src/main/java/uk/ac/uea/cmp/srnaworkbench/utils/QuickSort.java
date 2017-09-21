/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author w0445959
 */
public class QuickSort <T extends Comparable<T>>
{
   public void sort(T[] array, ArrayList<String[]> toSort)
   {
       //System.out.println("in sort class before: " + array[0]);
//      array = quicksort(array, 0, array.length-1);
//
      array = quicksort(array, toSort, 0, array.length-1);
      //System.out.println("in sort class after: " + array[0]);
   }

   private T[] quicksort(T[] array, ArrayList<String[]> toSort, int lo, int hi)
   {
      if (hi > lo)
      {
         int partitionPivotIndex = (int)(Math.random()*(hi-lo) + lo);
         int newPivotIndex = partition(array, toSort, lo, hi, partitionPivotIndex);
         quicksort(array, toSort, lo, newPivotIndex-1);
         quicksort(array, toSort, newPivotIndex+1, hi);
      }
      return (T[]) array;
   }

   private int partition(T[] array, ArrayList<String[]> toSort, int lo, int hi, int pivotIndex)
   {
      T pivotValue = array[ pivotIndex ];

      swap(array, toSort, pivotIndex, hi); //send to the back

      int index = lo;

      for (int i = lo; i < hi; i++)
      {
         if ( (array[i]).compareTo(pivotValue) <= 0 )
         {
            swap(array, toSort, i, index);
            index++;
         }
     }

      swap(array, toSort, hi, index);

      return index;
   }

   private void swap(T[] array, ArrayList<String[]> toSort, int i, int j)
   {
      T temp = array[i];
      array[i] = array[j];
      array[j] = temp;

      Collections.swap(toSort, j , i);
   }

//   public void sort(T[] array)
//   {
//      System.out.println("in sort class before: " + array[0]);
//      array = quicksort(array, 0, array.length-1);
//      System.out.println("in sort class after: " + array[0]);
//   }
//
//   private T[] quicksort(T[] array, int lo, int hi)
//   {
//      if (hi > lo)
//      {
//         int partitionPivotIndex = (int)(Math.random()*(hi-lo) + lo);
//         int newPivotIndex = partition(array, lo, hi, partitionPivotIndex);
//         quicksort(array, lo, newPivotIndex-1);
//         quicksort(array, newPivotIndex+1, hi);
//      }
//      return (T[]) array;
//   }
//
//   private int partition(T[] array, int lo, int hi, int pivotIndex)
//   {
//      T pivotValue = array[ pivotIndex ];
//
//      swap(array, pivotIndex, hi); //send to the back
//
//      int index = lo;
//
//      for (int i = lo; i < hi; i++)
//      {
//         if ( (array[i]).compareTo(pivotValue) <= 0 )
//         {
//            swap(array, i, index);
//            index++;
//         }
//     }
//
//      swap(array, hi, index);
//
//      return index;
//   }
//
//   private void swap(T[] array, int i, int j)
//   {
//      T temp = array[i];
//      array[i] = array[j];
//      array[j] = temp;
//   }

}
