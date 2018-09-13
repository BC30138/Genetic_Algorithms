package com.bc30138.geneticalg;

import java.util.*;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import java.io.*;
import javafx.util.Pair; 


public class GA {

    int bit_number;
    double left_, right_;
    Vector<Vector<Integer>> binary_numbers = new Vector<Vector<Integer>>();
    Vector<Vector<Integer>> temp_numbers = new Vector<Vector<Integer>>();
    Vector<Pair<Double,Double>> points = new Vector<>(); 
    double summ;
    Pair<Double,Double> min;
    Vector<Integer> bin_min;
    double max;
    String line_func;

    public void plot()
    {
        final Runtime run = Runtime.getRuntime();
        try {
            String fpath = "gnuplot src/com/bc30138/geneticalg/script.gp";
            Process p = run.exec(fpath);
            int result = p.waitFor();
         } catch (final IOException e) {
            e.printStackTrace();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
    }

    private double func(double x) 
    { 
        line_func = "sin(2*x) / (x * x)";
        return Math.sin(2 * x) / Math.pow(x, 2); 
    }

    public void to_file(PrintWriter printWriter)
    {
        for (int it = 0; it < points.size(); ++it)
        {
            printWriter.printf("%.4f \t %.4f \n",points.get(it).getKey(), points.get(it).getValue());
        }
        printWriter.print(summ + "\t" + max);
        printWriter.print("\n\n");
    }

    public GA(int strength, double precission, double left, double right, double cros_prob, double mut_prob)
    {
        right_ = right;
        left_ = left;

        bit_number = (int)Math.ceil(
            Math.log((right - left) / precission) /
            Math.log(2)
            );

        for (int it = 0; it < strength; ++it)
        {
            binary_numbers.addElement(randomizer());
            Pair<Double,Double> temp = new Pair<>(bit_to_num(binary_numbers.get(it)), 
                                            func(bit_to_num(binary_numbers.get(it))));
            points.addElement(temp);
        } 

        try {
            FileWriter fileWriter = new FileWriter("out/points.dat");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            to_file(printWriter);

            update_min_max_summ();
            
            // while ( (func(max + precission) > func(max) ) || (func(max - precission) > func(max)) )
            // {
            reproduction();
            crossing(cros_prob);
            mutation(mut_prob);
            screenings(strength, precission);
            to_file(printWriter);
            // plot();
            // }
            printWriter.close();
        }   catch (final IOException e) {
            e.printStackTrace();
        }
    }  

    private void reproduction()
    {
        temp_numbers.clear();

        for (int it = 0; it < binary_numbers.size(); ++it)
        {
            int wheel;
            if (min.getValue() < 0) wheel = (int)Math.round( (points.get(it).getValue() + min.getValue()) 
                                            * binary_numbers.size() / (summ + binary_numbers.size() * min.getValue()) );
            else wheel = (int)Math.round( points.get(it).getValue() * binary_numbers.size() / summ );
            for (int jt = 0; jt < wheel; ++jt)
            {
            temp_numbers.addElement(binary_numbers.get(it));
            }
        }
    }

    private void update_min_max_summ()
    {
        summ = 0;
        max = 0;
        min = new Pair<Double,Double>(points.get(0).getKey(),points.get(0).getValue());
        for (int it = 1; it < points.size(); ++it)
        {
            summ += points.get(it).getValue();
            if (points.get(it).getValue() > max) max = points.get(it).getValue();
            if (points.get(it).getValue() < min.getValue()) 
            {
                min = points.get(it);
                bin_min = binary_numbers.get(it);
            } 
            
        }
    }

    private void crossing(double probability) //not only once
    {
        if( new Random().nextDouble() <= probability) {  
            int first_p_n = (int)(Math.random() * (temp_numbers.size()));
            int second_p_n = 0;
            while (first_p_n == second_p_n) {second_p_n = (int)(Math.random() * (temp_numbers.size()));}
            Vector<Integer> first_p = temp_numbers.get(first_p_n);
            Vector<Integer> second_p = temp_numbers.get(second_p_n);
            for (int it = (int)(1 + Math.random() * (bit_number - 1)); it < bit_number; ++it)
            {
                Integer swap_ = first_p.get(it);
                first_p.set(it, second_p.get(it)); 
                second_p.set(it, swap_);
            }
            Pair<Double,Double> first_point = new Pair<>(bit_to_num(temp_numbers.get(first_p_n)), 
                                                func(bit_to_num(temp_numbers.get(first_p_n))));
            Pair<Double,Double> second_point = new Pair<>(bit_to_num(temp_numbers.get(second_p_n)), 
                                                func(bit_to_num(temp_numbers.get(second_p_n))));                 
            binary_numbers.addElement(first_p);
            binary_numbers.addElement(second_p);
            points.addElement(first_point);
            points.addElement(second_point);
         }
    }

    private void mutation(double probability)
    {
        if( new Random().nextDouble() <= probability) {
        int it = (int)(Math.random() * (temp_numbers.size()));
        int k =  (int)(Math.random() * (bit_number));
        Vector<Integer> temp = temp_numbers.get(it);
        if (temp.get(k) == 0) temp.set(k, 1);
        else temp.set(k,0);
        Pair<Double,Double> new_point = new Pair<>(bit_to_num(temp_numbers.get(it)), 
                                            func(bit_to_num(temp_numbers.get(it))));                 
        binary_numbers.addElement(temp);
        points.addElement(new_point);
        }
    }

    private void screenings(double strength, double precision)
    {
        // System.out.println(points.size() + "\t" + binary_numbers.size() + "\t" + strength);
        while (points.size() > strength)
        {
            update_min_max_summ();
            points.remove(min);
            binary_numbers.remove(bin_min);
        }
        // System.out.println(points.size() + "\t" + binary_numbers.size() + "\t" + strength);
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
        GA test = new GA(10, 0.01, -20, -3.1, 1, 0.001);
    }
}

