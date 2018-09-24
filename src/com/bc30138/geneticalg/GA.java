package com.bc30138.geneticalg;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import javafx.util.Pair; 


class point implements Comparable
{
    Vector<Integer> binary_x;
    Double x_value;
    Double y_value;

    public int compareTo(Object o)
	{
        point e = (point) o;

        int result = this.y_value.compareTo(e.y_value);
    
        if (result < 0)
        {
            return -1;
        }
        else 
        {
            if (result == 0)
            {
                return 0;
            } 
            else return 1;
        }   
	}
};

public class GA extends point{
    int bit_number;
    String line_func;
    Vector<point> points = new Vector<point>();
    Vector<Integer> wheel_temp_p = new Vector<Integer>();
    Vector<Integer> wheel_only_p = new Vector<Integer>();
    double summ;
    double left_, right_;

    private point init_point(Vector<Integer> bin)
    {
        point new_point = new point();
        new_point.binary_x = bin;
        new_point.x_value = bin_to_num(bin);
        new_point.y_value = func(new_point.x_value);
        return new_point;
    }

    private double func(double x) 
    { 
        line_func = "(sin(2*x)/(x*x))";
        return Math.sin(2 * x) / Math.pow(x, 2); 
    }

    private double bin_to_num(Vector<Integer> binary_num)
    {
        double number = 0;

        for (int it = 0; it < bit_number; ++it)
        {
            number += binary_num.get(it) * Math.pow(2, it);
        }

        return left_ + number * (right_ - left_) / (Math.pow(2, bit_number) - 1);
    }
    
    private point randomizer()
    {
        point new_point = new point();
        Random random_ = new Random();
        new_point.binary_x = new Vector<Integer>(bit_number);
        for(int i = 0; i < bit_number; ++i)
        {
            int val = random_.nextBoolean() ? 1 : 0;
            new_point.binary_x.addElement(val);
        }
        new_point.x_value = bin_to_num(new_point.binary_x);
        new_point.y_value = func(new_point.x_value);
        return new_point;
    } 

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

    public void to_file(PrintWriter printWriter)
    {
        for (int it = 0; it < points.size(); ++it)
        {
            printWriter.printf("%.4f \t %.4f \n",points.get(it).x_value, points.get(it).y_value);
        }
        printWriter.print("\n\n");
    }

    public GA(int strength, double precision, int step_count, double left, double right, double cros_prob, double mut_prob)
    {
        right_ = right;
        left_ = left;
        summ = 0;

        bit_number = (int)Math.ceil(
            Math.log((right - left) / precision) /
            Math.log(2)
            );

        for (int it = 0; it < strength; ++it)
        {
            point temp = new point();
            temp = randomizer();
            points.addElement(temp);
            summ += temp.y_value;
        } 

        try {
            FileWriter fileWriter = new FileWriter("out/points.dat");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            // to_file(printWriter);
            sort_points();
            for (int it = 0; it < step_count; ++it)
            {
            to_file(printWriter);
            reproduction();
            crossing(cros_prob);
            mutation(mut_prob);
            screenings(strength, precision);
            // to_file(printWriter);
            }
            printWriter.close();
        }   catch (final IOException e) {
            e.printStackTrace();
        }
        plot();
    }

    private void sort_points()
    {
        Collections.sort(points); 
    }

    private void reproduction()
    {
        wheel_only_p.clear();
        wheel_temp_p.clear();

        for (int it = 0; it < points.size(); ++it)
        {
            int wheel;
            wheel = (int)Math.round( (points.get(it).y_value + Math.abs(points.get(0).y_value)) 
                                            * points.size() / (summ + points.size() * Math.abs(points.get(0).y_value)) );
            if (wheel != 0) 
            {
                wheel_only_p.addElement(it);
                for (int jt = 0; jt < wheel; ++jt)
                {
                wheel_temp_p.addElement(it);
                }
            }
        }
    }

    private boolean iseq_bin(Vector<Integer> a, Vector<Integer> b)
    {
        boolean Is_Equal = true;
        for (int it = 0; it < bit_number; ++it)
        {
            if (a.get(it) != b.get(it)) 
            {
                Is_Equal = false; 
                it = bit_number;
            }
        }
        return Is_Equal; 
    }

    private void crossing(double probability) //not only once
    {
        Vector<String> used = new Vector<String>();
        for (int it = 0; it < wheel_only_p.size(); ++it)
        {
                if( new Random().nextDouble() <= probability) {
                    int jt =  ThreadLocalRandom.current().nextInt(0, wheel_temp_p.size());
                    int find = used.indexOf(String.valueOf(wheel_only_p.get(it)) +
                                         String.valueOf(wheel_temp_p.get(jt)));
                    while ( (wheel_only_p.get(it) == wheel_temp_p.get(jt)) || (find != -1) ) 
                        {
                            jt = ThreadLocalRandom.current().nextInt(0, wheel_temp_p.size());
                            find = used.indexOf(String.valueOf(wheel_only_p.get(it)) +
                                         String.valueOf(wheel_temp_p.get(jt)));
                        }    
                    used.addElement(String.valueOf(wheel_only_p.get(it)) + String.valueOf(wheel_temp_p.get(jt)));
                    used.addElement(String.valueOf(wheel_temp_p.get(jt)) + String.valueOf(wheel_only_p.get(it)));
                    Vector<Integer> first_bin = new Vector<>(bit_number);
                    Vector<Integer> second_bin = new Vector<>(bit_number);
                    first_bin = points.get(wheel_only_p.get(it)).binary_x;
                    second_bin = points.get(wheel_temp_p.get(jt)).binary_x;
                    int rand = ThreadLocalRandom.current().nextInt(1, bit_number);;
                    for (int k = rand; k < bit_number; ++k)
                    {
                        int swap_ = first_bin.get(k);
                        first_bin.set(k, second_bin.get(k)); 
                        second_bin.set(k, swap_);
                    }
                    point first_point = new point();
                    point second_point = new point();        
                    first_point = init_point(first_bin);
                    second_point = init_point(second_bin);
                    points.addElement(first_point);
                    points.addElement(second_point);
                    
                }
         }
    }

    private void udpate_summ()
    {
        summ = 0; 
        for (int it = 0; it < points.size(); ++it)
        {
            summ += points.get(it).y_value;
        }
    }
    
    private void mutation(double probability)
    {
        for (int it = 0; it < wheel_temp_p.size(); ++it)
        {
            if( new Random().nextDouble() <= probability) {
            int k =  (int)(Math.random() * (bit_number));
            Vector<Integer> temp = points.get(wheel_temp_p.get(it)).binary_x;
            if (temp.get(k) == 0) temp.set(k, 1);
            else temp.set(k, 0);
            point new_point = new point();
            new_point = init_point(temp);                 
            points.addElement(new_point);
            }
        }
    }

    private void screenings(double strength, double precision)
    {
        sort_points();
        while (points.size() > strength)
        {
            points.remove(0);
        }
        udpate_summ();
    }

    public static void main(String[] args)
    {
        GA test = new GA(20, 0.001, 30, -20, -3.1, 0.5, 0.01);
    }
}
