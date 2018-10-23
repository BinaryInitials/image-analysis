package com.ozone.image.load;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

 public class ImageRecognition {
	
	public static final int SIZE =50;
	 
	public static void main(String[] args) {
		Date tic = new Date();
//		String image1 = (args == null || args.length < 2) ? "raw-people/tadas1.jpg" : args[0];
//		String image2 = (args == null || args.length < 2) ? "raw-people/tadas2.jpg" : args[1];
		String image1 = (args == null || args.length < 2) ? "raw-people/olivier1.jpg" : args[0];
		String image2 = (args == null || args.length < 2) ? "raw-people/olivier4.jpg" : args[1];
//		String image1 = (args == null || args.length < 2) ? "raw-people/test.jpg" : args[0];
//		String image2 = (args == null || args.length < 2) ? "raw-people/download1.jpeg" : args[1];
//		String image2 = (args == null || args.length < 2) ? "raw-people/download2.jpeg" : args[1];
		
		
		int[][] original0 = convertToMatrix(read(image1));
		int[][] original1 = convertToMatrix(read(image2));
		
		int[][] resized0 = resize(original0, SIZE, SIZE);
		int[][] resized1 = resize(original1, SIZE, SIZE);
				
//		display(convertToImage(original0));
//		display(convertToImage(original1));
		
		int windowSize = 20;
		
		for(int i=0;i<windowSize;i++) {
			for(int j=0;j<windowSize;j++) 
				System.out.print(original0[i][j] + "  ");
			System.out.println();
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		for(int i=0;i<resized0.length;i++) {
			for(int j=0;j<resized0[i].length;j++) 
				System.out.print(resized0[i][j] + "  ");
			System.out.println();
		}
//		for(int i=0;i<windowSize;i++) {
//			for(int j=0;j<windowSize;j++) 
//				System.out.print(resized1[i][j] + "\t");
//			System.out.println();
//		}
		
		int kernelSize = 100;
		
//		display(convertToImage(blur(resized0, kernelSize)));
//		display(convertToImage(blur(resized1, kernelSize)));

		double score = compareImages(blur(resized0, kernelSize), blur(resized1, kernelSize));
		System.out.println("score=" + String.format("%.2f", score) + "%");
		Date toc = new Date();
		double elapsed = (toc.getTime() - tic.getTime())/1000.0;
		System.out.println("Ellapsed time: " + String.format("%.2f", elapsed)+ " seconds.");
	}
	
	public static int[][] gradient(int[][] m){
		int[][] dmx = new int[m.length-1][m[0].length-1];
		int[][] dmy = new int[m.length-1][m[0].length-1];
		int[][] dm = new int[m.length-1][m[0].length-1];
		
		for(int i=0;i<m.length-1;i++)
			for(int j=0;j<m[0].length-1;j++) 
				dmx[i][j] = m[i+1][j] - m[i][j];
		for(int i=0;i<m.length-1;i++)
			for(int j=0;j<m[0].length-1;j++)
				dmy[i][j] = m[i][j+1] - m[i][j];
		for(int i=0;i<m.length-1;i++)
			for(int j=0;j<m[0].length-1;j++)
				dm[i][j] = validate((int)Math.round(Math.sqrt(dmx[i][j]*dmx[i][j] + dmy[i][j]*dmy[i][j])));
		return dm;
	}
	
	public static int calculateAverage(int[][] m) {
		int average = 0;
		for(int i=0;i<m.length;i++) 
			for(int j=0;j<m[0].length;j++) 
				average += m[i][j]; 
		average /= (m.length*m[0].length);
		return average;
	}
	
	public static int[][] blur(int[][] m, int kernelSize){
		int kernelW = 2*(int)Math.round(Math.sqrt(kernelSize));
		int kernelH = 2*(int)Math.round(Math.sqrt(kernelSize));
		int[][] blurredImage = new int[m.length-kernelW][m[0].length-kernelH];
		
		for(int i=0;i<m.length-kernelW;i++)
			for(int j=0;j<m[0].length-kernelH;j++){
				int sum=0;
				for(int k=0;k<kernelW;k++)
					for(int l=0;l<kernelH;l++)
						sum+=m[i+k][j+l];
				blurredImage[i][j] = sum/(kernelW*kernelH);
			}
		return blurredImage;
	}
	
	public static double compareImages(int[][] matrix1, int[][] matrix2){
		double sum = 0.0;
		int w = matrix1.length;
		int h = matrix1[0].length;
		
		for(int i=0; i<w;i++)
			for(int j=0;j<h;j++) 
				sum += Math.pow(matrix1[i][j] - matrix2[i][j], 2.0);
		double variance = 100.0-Math.sqrt(sum/(h*w))/2.55;
		return variance;
	}
	
	
	public static void display(BufferedImage image){
		JFrame frame = new JFrame();
		frame.setSize(image.getWidth(), image.getHeight());
		JLabel label = new JLabel(new ImageIcon(image));
		frame.add(label);
		frame.setVisible(true);		
	}
	
	public static BufferedImage read(String imageName){
		BufferedImage image = null;
		try {
			image = ImageIO.read(Files.newInputStream(Paths.get(imageName)));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public static int[][] convertToMatrix(BufferedImage image){
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] matrix = new int[height][width];
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				Color color = new Color(image.getRGB(j, i));
				matrix[i][j] = (color.getRed() + color.getGreen() + color.getBlue())/3;
			}
		}
		return matrix;
	}
	
	public static BufferedImage convertToImage(int[][] m){
		int height = m.length;
		int width = m[0].length;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for(int i=0;i<height;i++)
			for(int j=0;j<width;j++){
				Color color = new Color(validate(m[i][j]), validate((int)(m[i][j])) , validate(m[i][j]));
				image.setRGB(j, i, color.getRGB());
			}
		return image;
	}
	
	public static int[][] threshold(int[][] m, int threshold){
		int[][] o = new int[m.length][m[0].length];
		for(int i=0;i<m.length;i++) 
			for(int j=0;j<m[0].length;j++) 
				o[i][j] = m[i][j] > threshold ? 255:0; 
		return o;
	}
	
	public static int validate(int value){
		return value > 255 ? 255 : (value < 0 ? 0 : value);
	}
		
	public static int[][] resize(int[][] matrix, int w, int h){
		int[][] resized = new int[w][h];
		int[][] pixels = new int[w][h]; 
		for(int i=0;i<w;i++)
			for(int j=0;j<h;j++)
				pixels[i][j] = 0;
		
		HashMap<String, Integer> pixelCount = new HashMap<String, Integer>();
		for(int i=0;i<w;i++) 
			for(int j=0;j<h;j++) 
				pixelCount.put(i+"-"+j, 0);
		
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[0].length;j++){
				int i2 = (i*w)/matrix.length;
				int j2 = (j*h)/matrix[0].length;
				resized[i2][j2] += matrix[i][j];
				pixelCount.put(i2 + "-" + j2, pixelCount.get(i2 + "-" + j2)+1);
			}
		for(int i=0;i<w;i++)
			for(int j=0;j<h;j++)
				resized[i][j] /= pixelCount.get(i+"-"+j) == 0?1:pixelCount.get(i+"-"+j);
		
		return resized;
	}
}