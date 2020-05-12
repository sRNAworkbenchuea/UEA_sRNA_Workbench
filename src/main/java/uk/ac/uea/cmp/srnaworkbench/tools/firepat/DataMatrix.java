/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;
import java.io.*;
import java.util.*;

/**
 *
 * @author a019349
 */
public class DataMatrix {
    int m; //m is the number of rows
    int n; //n is the number of columns
    double[][] ExpressionMatrix = {{1},{2}};
    
    DataMatrix()
    {
        ;
    }
    
    DataMatrix(double[][] inputdata)
    {
        ExpressionMatrix = inputdata;
        m = ExpressionMatrix.length;
        n = ExpressionMatrix[1].length;
    }
    
    //return the number of lines
    int getM()
    {
        return m;
    }
    
    //return the number of columns
    int getN()
    {
        return n;
    }
    
    //return a whole row (i)
    double[][] getMatrix()
    {
        return ExpressionMatrix;
    }
    
    double[] getRow(int i)
    {
        return ExpressionMatrix[i];
    }
    
    void printMatrix()
    {
        for(int i=0; i < m; i++)
        {
            for(int j=0; j < n-1; j++)
            {
                System.out.print(ExpressionMatrix[i][j]+",");
            }
            System.out.println(ExpressionMatrix[i][n-1]);
        }
    }
}

