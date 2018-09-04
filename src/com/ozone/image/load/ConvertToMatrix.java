package com.ozone.image.load;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ConvertToMatrix {

	public static void main(String[] args) {
		display(convertToImage(deriveMatrix(convertToMatrix(read()))));
	}
	
	public static void display(BufferedImage image){
		JFrame frame = new JFrame();
		frame.setSize(image.getWidth(), image.getHeight());
		JLabel label = new JLabel(new ImageIcon(image));
		frame.add(label);
		frame.setVisible(true);		
	}
	
	public static BufferedImage read(){
		return read(new File(System.getProperty("user.dir") + "/raw-training/").listFiles()[1].getName());
	}
	
	public static BufferedImage read(String imageName){
		BufferedImage image = null;
		try {
			image = ImageIO.read(Files.newInputStream(Paths.get(System.getProperty("user.dir") + "/raw-training/" + imageName)));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public enum Pixel{
		RED,
		GREEN,
		BLUE,
		ALPHA
	}
	
	public static HashMap<Pixel, int[][]> convertToMatrix(BufferedImage image){
		int width = image.getWidth();
		int height = image.getHeight();
		int [][] alpha = new int[height][width];
		int[][] red = new int[height][width];
		int[][] green = new int[height][width];
		int[][] blue = new int[height][width];
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				Color color = new Color(image.getRGB(j, i));
				alpha[i][j] = color.getAlpha();
				red[i][j] = color.getRed();
				green[i][j] = color.getGreen();
				blue[i][j] = color.getBlue();
			}
		}
		
		HashMap<Pixel, int[][]> matrix = new HashMap<Pixel, int[][]>();
		
		matrix.put(Pixel.RED, red);
		matrix.put(Pixel.GREEN, green);
		matrix.put(Pixel.BLUE, blue);
		matrix.put(Pixel.ALPHA, alpha);
		
		return matrix;
	}
	
	public static BufferedImage convertToImage(HashMap<Pixel, int[][]> matrix){
		int height = matrix.get(Pixel.RED).length;
		int width = matrix.get(Pixel.RED)[0].length;
		int[][] red = matrix.get(Pixel.RED);
		int[][] green = matrix.get(Pixel.GREEN);
		int[][] blue = matrix.get(Pixel.BLUE);
		int[][] alpha = matrix.get(Pixel.ALPHA);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				Color color = new Color(validate(red[i][j]), validate((int)(green[i][j])) , validate(blue[i][j]), validate(alpha[i][j]));
				image.setRGB(j, i, color.getRGB());
			}
		}
		return image;
	}
	
	public static int validate(int value){
		return value > 255 ? 255 : (value < 0 ? 0 : value);
	}
	
	public static int threshold(int value, double threshold){
		return value > threshold ? value : 0;
	}
	
	public static HashMap<Pixel, int[][]> deriveMatrix(HashMap<Pixel, int[][]> matrix){
		HashMap<Pixel, int[][]> derived = new HashMap<Pixel, int[][]>();
		derived.put(Pixel.RED, contrast(dydx(matrix.get(Pixel.RED))));
		derived.put(Pixel.GREEN, contrast(dydx(matrix.get(Pixel.GREEN))));
		derived.put(Pixel.BLUE, contrast(dydx(matrix.get(Pixel.BLUE))));
		derived.put(Pixel.ALPHA, matrix.get(Pixel.ALPHA));
		return derived;
	}
	
	public static int[][] dydx(int[][] f){
		int height = f.length;
		int width = f[0].length;
		int[][] dydx = new int[height-2][width-2];
		
		for(int i=1;i<height-1;i++){
			for(int j=1;j<width-1;j++){
				dydx[i-1][j-1] = f[i+1][j+1] + f[i-1][j-1] - f[i-1][j+1] - f[i+1][j-1];
			}
		}
		
		return dydx;
	}
	
	public static int[][] contrast(int[][] matrix){
		int height = matrix.length;
		int width = matrix[0].length;
		int[][] contrast = new int[height][width];
		
		int max = 0;
		int min = 255;
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				max = Math.max(max, matrix[i][j]);
				min = Math.min(min, matrix[i][j]);
			}
		}

		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				contrast[i][j] = (int)Math.round((255*(matrix[i][j]-min))/(double)(max-min));
			}
		}
		
		return contrast;
	}

}