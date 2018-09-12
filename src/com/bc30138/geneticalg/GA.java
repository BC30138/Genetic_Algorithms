package com.bc30138.geneticalg;

import java.util.*;
import java.io.*;
import com.panayotis.gnuplot.JavaPlot;


public class GA {

    int bit_number;
    double left_, right_;
    Vector<Vector<Integer>> numbers = new Vector<Vector<Integer>>(); 
    double summ;
    String line_func;

    public void plot()
    {

    }

    private double func(double x) 
    { 
        line_func = "sin(2*x) / (x * x)";
        return Math.sin(2 * x) / Math.pow(x, 2); 
    }

    public GA(int strength, double precission, double left, double right)
    {
        right_ = right;
        left_ = left;
        summ = 0;

        bit_number = (int)Math.ceil(
            Math.log((right - left) / precission) /
            Math.log(2)
            );

        for (int it = 0; it < strength; ++it)
        {
            numbers.addElement(randomizer());
            summ += bit_to_num(numbers.get(it));
        } 

        reproduction();
    }  

    private void reproduction()
    {
        Vector<Vector<Integer>> new_numbers = new Vector<Vector<Integer>>(bit_number);
        double new_summ = 0;

        for (int it = 0; it < numbers.size(); ++it)
        {
            long wheel = Math.round( func( bit_to_num(numbers.get(it)) ) 
                                        * numbers.size() / summ ); 
            for (int jt = 0; jt < wheel; ++jt)
            {
                new_numbers.addElement(numbers.get(it));
                new_summ += bit_to_num(numbers.get(it));
            }
        }

        summ = new_summ;
        numbers = new_numbers;
    }

    private Vector<Integer> randomizer()
    {
        Random random_ = new Random();
        Vector<Integer> number = new Vector<Integer>(bit_number);
        for(int i = 0; i < bit_number; ++i)
        {
            int val = random_.nextBoolean() ? 1 : 0;
            number.addElement(val);
        }
        return number;
    } 

    private double bit_to_num(Vector<Integer> binary_num)
    {
        double number = 0;

        for (int jt = 0; jt < bit_number; ++jt)
        {
            number += binary_num.get(jt) * Math.pow(2, jt);
        }

        return left_ + number * (right_ - left_) / (Math.pow(2, bit_number) - 1);
    }

    public static void main(String[] args)
    {
        GA test = new GA(10, 0.01, -20, -3.1);
        JavaPlot p = new JavaPlot();

        p.addPlot(test.line_func);

        p.plot();
    }
}

