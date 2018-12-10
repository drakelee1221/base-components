package com.base.components.common.util;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * @author <a href="tecyun@foxmail.com">Huangyunyang</a>
 * @version 1.0.0, 2018/5/17 0017 14:00
 */
public class AliasMethod {

  /**
   * 存放概率数组
   */
  private final double[] probability;
  /**
   * 存放alias数组，即不到1概率的部分对应的奖品
   */
  private final int[] alias;
  private final int length;
  private final Random rand;


  public AliasMethod(List<Double> prob) {
    this(prob, new Random());
  }


  public AliasMethod(List<Double> prob, Random rand) {
    /* Begin by doing basic structural checks on the inputs. */
    if (prob == null || rand == null) {
      throw new NullPointerException();
    }
    if (prob.size() == 0) {
      throw new IllegalArgumentException("Probability vector must be nonempty.");
    }

    this.rand = rand;
    this.length = prob.size();
    this.probability = new double[length];
    this.alias = new int[length];


    double[] probtemp = new double[length];
    Deque<Integer> small = new ArrayDeque<Integer>();
    Deque<Integer> large = new ArrayDeque<Integer>();

    /* 生成用于生成alias table的列数据 */
    for (int i = 0; i < length; i++) {
      /* initial probtemp */
      probtemp[i] = prob.get(i) * length;
      if (probtemp[i] < 1.0) {
        small.add(i);
      } else {
        large.add(i);
      }
    }


    while (!small.isEmpty() && !large.isEmpty()) {
      int less = small.pop();
      int more = large.pop();
      probability[less] = probtemp[less];
      alias[less] = more;
      probtemp[more] = probtemp[more] - (1.0 - probability[less]);
      if (probtemp[more] < 1.0) {
        small.add(more);
      } else {
        large.add(more);
      }
    }
    /*
     * At this point, everything is in one list, which means that the
     * remaining probabilities should all be 1/n. Based on this, set them
     * appropriately.
     */
    while (!small.isEmpty()) {
      probability[small.pop()] = 1.0;
    }
    while (!large.isEmpty()) {
      probability[large.pop()] = 1.0;
    }
  }

  /**
   * Samples a value from the underlying distribution.
   */
  public int next() {
    /* 取奖品table的某一列 */
    int column = rand.nextInt(length);
    /* 取奖品table这一列的下方数据还是上方数据 */
    boolean coinToss = rand.nextDouble() < probability[column];
    return coinToss ? column : alias[column];
  }


  /* 概率测试 */
  public static void main(String[] argv) {
    List<Double> prob = new ArrayList<Double>();

    prob.add(0.001);//一等奖
    prob.add(0.05);//二等奖
    prob.add(0.1);//三等奖
    prob.add(0.3);
    prob.add(0.549);
    String[] str = {"一等奖","二等奖", "三等奖", "四等奖", "未中奖"};
    AliasMethod am = new AliasMethod(prob);
    int a = 0;
    int b = 0;
    int c = 0;
    int d = 0;
    int e = 0;
    for (int i = 0; i < 100; i++) {
      if(am.next() == 0){
        a++;
      } else if(am.next() == 1){
        b++;
      } else if(am.next() == 2){
        c++;
      } else if(am.next() == 3){
        d++;
      } else{
        e++;
      }
    }
    System.out.println("一等奖 " + a);
    System.out.println("二等奖 " + b);
    System.out.println("三等奖 " + c);
    System.out.println("四等奖 " + d);
    System.out.println("未中奖 " + e);
  }
}
