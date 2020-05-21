package com.novruz;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {


    public static BufferedImage image;

    public static ArrayList<Pixel> wellLocations;

    public static void main(String[] args) {

        image = null;

        wellLocations = new ArrayList<>();

        ArrayList<Pixel> owc = new ArrayList<>();

        File f = null;

        try{
            f = new File("C:\\Users\\Novruz Engineer\\Desktop\\Capture2.PNG");

            image = ImageIO.read((f));
        }catch(IOException e){
            System.out.println(e.getMessage());
        }

        image = convertToARGB(image);

        int width = image.getWidth();
        int height = image.getHeight();

        //Detecting well locations
        Main main = new Main();
        int[] rgb = new int[3];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                rgb = main.getRGBByPixel(i, j);
                if((rgb[0]+rgb[1]+rgb[2]) < 60){
                    main.setRGBByPixel(i, j, 255, 0, 0);
                    wellLocations.add(new Pixel(i, j));
                }

                if(rgb[1] > 100 && (rgb[0]+rgb[2] < 150)){
                    owc.add(new Pixel(i, j));
                }
            }
        }



        try{
            f = new File("C:\\Users\\Novruz Engineer\\Desktop\\Well Locations.PNG");
            ImageIO.write(image, "PNG", f);
            System.out.println("Successful!");
        }catch(IOException e){
            System.out.println(e);
        }


        int arrayCount = 6;
//                                    4     2    6    1    5    3
        double[] waterSaturations = {0.26, 0.19, 0.21, 0.25, 0.21, 0.23};
        double min = 0.15;
        double max = 0.3;
        double[] distances = new double[arrayCount];

        double[] weightFactors = new double[arrayCount];

        double[] averagedWaterSaturations = new double[width*height];
        double averWaterSat = 0;

        double distanceSum = 0;

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                distanceSum = 0;
                distances = new double[arrayCount];
                for(int z = 0; z < 6; z++){
                    distances[z] = Math.sqrt((i - wellLocations.get(z).x)*(i - wellLocations.get(z).x) + (j - wellLocations.get(z).y)*(j - wellLocations.get(z).y));
                    distances[z] = (1 / distances[z])*(1 / distances[z]);

                    distanceSum += distances[z];
                }
                weightFactors = new double[arrayCount];
                averWaterSat = 0;
                for(int z = 0; z < 6; z++){
                    weightFactors[z] = distances[z]/distanceSum;
                    averWaterSat += weightFactors[z] * waterSaturations[z];
                }
                int percentage = (int) ((averWaterSat-min)/(max-min)*100);

                int[] result = main.GetBlendedColor(percentage);

                main.setRGBByPixel(i, j, result[0], result[1], result[2]);
            }
        }


        for(int i = 0; i < owc.size(); i++){
            main.setRGBByPixel(owc.get(i).x, owc.get(i).y, 0, 0, 0);
        }


        //Saving the final image
        try{
            f = new File("C:\\Users\\Novruz Engineer\\Desktop\\Porosity saturation.PNG");
            ImageIO.write(image, "PNG", f);
            System.out.println("Successful!");
        }catch(IOException e){
            System.out.println(e);
        }


    }


    public void setRGBByPixel(int x, int y, int r, int g, int b){
        int a = 255;
        int pixel = (a<<24) | (r<<16) | (g<<8) | b;

        image.setRGB(x, y, pixel);
    }


    public int[] getRGBByPixel(int x, int y){
        int[] result = new int[3];

        int pixel = image.getRGB(x, y);

        //alpha value
        //int a = (pixel>>24) & 0xff;

        //red value
        result[0] = (pixel>>16) & 0xff;

        //green value
        result[1] = (pixel>>8) &0xff;

        //blue value
        result[2] = pixel & 0xff;

        return result;
    }

    public static BufferedImage convertToARGB(BufferedImage image)
    {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public int[] GetBlendedColor(int percentage)
    {

        if (percentage < 50)
            return Interpolate(Color.RED, Color.yellow, percentage / 50.0);
        return Interpolate(Color.yellow, Color.GREEN, (percentage - 50) / 50.0);
    }

    private int[] Interpolate(Color color1, Color color2, double fraction)
    {
        double r = Interpolate(color1.getRed(), color2.getRed(), fraction);
        double g = Interpolate(color1.getGreen(), color2.getGreen(), fraction);
        double b = Interpolate(color1.getBlue(), color2.getBlue(), fraction);
        int[] result = new int[3];
        result[0] = (int)Math.round(r);
        result[1] = (int)Math.round(g);
        result[2] = (int)Math.round(b);

        return result;
    }

    private double Interpolate(double d1, double d2, double fraction)
    {
        return d1 + (d2 - d1) * fraction;
    }
}

class Pixel{

    public int x, y;



    public Pixel(int x, int y){
        this.x = x;
        this.y = y;
    }

}


