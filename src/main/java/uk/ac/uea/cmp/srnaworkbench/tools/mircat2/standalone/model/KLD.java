package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model;

import java.util.HashMap;

public class KLD
{
  public static double calculateKLD(HashMap<Integer, Double> probs, double kldProb)
  {
    double sum = 0.0D;
    for (Integer i : probs.keySet()) {
      sum += Math.abs(((Double)probs.get(i)).doubleValue() * Math.log(((Double)probs.get(i)).doubleValue() / kldProb));
    }
    return sum;
  }
}
