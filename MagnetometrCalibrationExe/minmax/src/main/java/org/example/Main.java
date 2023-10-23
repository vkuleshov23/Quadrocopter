package org.example;

import java.io.*;

public class Main {

    static String fileName = "C:\\Users\\Вадим\\Desktop\\PET\\Quadrocopter\\RaspberryController\\data_for_magneto_calibtation.txt";
    static double min_x = 32767;
    static double min_y = 32767;
    static double min_z = 32767;
    static double max_x = -32767;
    static double max_y = -32767;
    static double max_z = -32767;

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();

            while (line != null) {
                String[] xyz = line.split(" ");
                minMax(Double.parseDouble(xyz[0]), Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2]));
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("MIN_X: " + min_x);
        System.out.println("MIN_Y: " + min_y);
        System.out.println("MIN_Z: " + min_z);
        System.out.println("MAX_X: " + max_x);
        System.out.println("MAX_Y: " + max_y);
        System.out.println("MAX_Z: " + max_z);

        System.out.println("MEAN_X: " + (max_x - min_x)/2.0);
        System.out.println("MEAN_Y: " + (max_y-min_y)/2.0);
        System.out.println("MEAN_Z: " + (max_z-min_z)/2.0);
    }

    public static void minMax(double x, double y, double z) {
        min_x = Math.min(min_x, x);
        min_y = Math.min(min_y, y);
        min_z = Math.min(min_z, z);
        max_x = Math.max(max_x, x);
        max_y = Math.max(max_y, y);
        max_z = Math.max(max_z, z);
    }

}