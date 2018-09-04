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

	public static final int RESIZE = 50;
	
	public static void main(String[] args) {
		Date tic = new Date();
		HashMap<Channel, int[][]> resized0 = resize(convertToMatrix(read(args[0])), RESIZE, RESIZE);
		HashMap<Channel, int[][]> resized1 = resize(convertToMatrix(read(args[1])), RESIZE, RESIZE);
//		display(convertToImage(blur(resized0)));
//		display(convertToImage(blur(resized1)));

		double score = compareImages(blur(resized0), blur(resized1));
		System.out.println("score=" + String.format("%.2f", score) + "%");
		Date toc = new Date();
		double elapsed = (toc.getTime() - tic.getTime())/1000.0;
		System.out.println("Ellapsed time: " + String.format("%.2f", elapsed)+ " seconds.");
	}
	
	public static HashMap<Channel, int[][]> blur(HashMap<Channel, int[][]> matrix){
		int kernelW = 2*(int)Math.round(Math.sqrt(RESIZE));
		int kernelH = 2*(int)Math.round(Math.sqrt(RESIZE));
		HashMap<Channel, int[][]> blurredImage = new HashMap<Channel, int[][]>();
		for(Channel c : Channel.values())
			blurredImage.put(c, new int[matrix.get(c).length-kernelW][matrix.get(c)[0].length-kernelH]);
		
		for(Channel c : Channel.values())
			for(int i=0;i<matrix.get(c).length-kernelW;i++)
				for(int j=0;j<matrix.get(c)[0].length-kernelH;j++){
					int sum=0;
					for(int k=0;k<kernelW;k++)
						for(int l=0;l<kernelH;l++)
							sum+=matrix.get(c)[i+k][j+l];
					blurredImage.get(c)[i][j] = sum/(kernelW*kernelH);
				}
		return blurredImage;
	}
	
	public static double compareImages(HashMap<Channel, int[][]> matrix1, HashMap<Channel, int[][]> matrix2){
		double sum = 0.0;
		int w = matrix1.get(Channel.GREEN).length;
		int h = matrix1.get(Channel.GREEN)[0].length;
		for(Channel channel : Channel.values())
			for(int i=0; i<w;i++)
				for(int j=0;j<h;j++)
					sum += Math.pow(matrix1.get(channel)[i][j] - matrix2.get(channel)[i][j], 2.0);
		double variance = 100.0-Math.sqrt(sum/(3.0*h*w))/2.5;
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
	
	public enum Channel{
		RED,
		GREEN,
		BLUE,
	}
	
	public static HashMap<Channel, int[][]> convertToMatrix(BufferedImage image){
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
		
		HashMap<Channel, int[][]> matrix = new HashMap<Channel, int[][]>();
		
		matrix.put(Channel.RED, red);
		matrix.put(Channel.GREEN, green);
		matrix.put(Channel.BLUE, blue);
		
		return matrix;
	}
	
	public static BufferedImage convertToImage(HashMap<Channel, int[][]> matrix){
		int height = matrix.get(Channel.RED).length;
		int width = matrix.get(Channel.RED)[0].length;
		int[][] red = matrix.get(Channel.RED);
		int[][] green = matrix.get(Channel.GREEN);
		int[][] blue = matrix.get(Channel.BLUE);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				Color color = new Color(validate(red[i][j]), validate((int)(green[i][j])) , validate(blue[i][j]));
				image.setRGB(j, i, color.getRGB());
			}
		}
		return image;
	}
	
	public static int validate(int value){
		return value > 255 ? 255 : (value < 0 ? 0 : value);
	}
	
//	public static int threshold(int value, double threshold){
//		return value > threshold ? value : 0;
//	}
	
//	public static HashMap<Channel, int[][]> deriveMatrix(HashMap<Channel, int[][]> matrix){
//		HashMap<Channel, int[][]> derived = new HashMap<Channel, int[][]>();
//		derived.put(Channel.RED, contrast(dydx(matrix.get(Channel.RED))));
//		derived.put(Channel.GREEN, contrast(dydx(matrix.get(Channel.GREEN))));
//		derived.put(Channel.BLUE, contrast(dydx(matrix.get(Channel.BLUE))));
//		return derived;
//	}
	
//	public static int[][] dydx(int[][] f){
//		int height = f.length;
//		int width = f[0].length;
//		int[][] dydx = new int[height-2][width-2];
//		
//		for(int i=1;i<height-1;i++){
//			for(int j=1;j<width-1;j++){
//				dydx[i-1][j-1] = f[i+1][j+1] + f[i-1][j-1] - f[i-1][j+1] - f[i+1][j-1];
//			}
//		}
//		
//		return dydx;
//	}
	
	public static HashMap<Channel, int[][]> resize(HashMap<Channel, int[][]> matrix, int w, int h){
		HashMap<Channel, int[][]> resized = new HashMap<Channel, int[][]>();
		for(Channel channel : Channel.values()){
			int[][] pixels = new int[w][h]; 
			for(int i=0;i<w;i++)
				for(int j=0;j<h;j++)
					pixels[i][j] = 0;
			resized.put(channel, pixels);
		}
		for(Channel channel : Channel.values())
			for(int i=0;i<matrix.get(channel).length;i++)
				for(int j=0;j<matrix.get(channel)[0].length;j++){
//					System.out.println(j + "\t" + h + "\t" + matrix.get(channel).length + "\t" + (int)Math.floor((j*h)/(0.0+matrix.get(channel)[0].length)));
					resized.get(channel)[(int)Math.floor((i*w)/(0.0+matrix.get(channel).length))][(int)Math.floor((j*h)/(0.0+matrix.get(channel)[0].length))] += matrix.get(channel)[i][j];
				}
		
		int numberOfPixelsInROI = (matrix.get(Channel.GREEN).length*matrix.get(Channel.GREEN)[0].length)/(w*h); 
		
		for(Channel channel : Channel.values())
			for(int i=0;i<w;i++)
				for(int j=0;j<h;j++)
					resized.get(channel)[i][j] /= numberOfPixelsInROI;
		
		return resized;
	}
	
//	public static int[][] contrast(int[][] matrix){
//		int height = matrix.length;
//		int width = matrix[0].length;
//		int[][] contrast = new int[height][width];
//		
//		int max = 0;
//		int min = 255;
//		
//		for(int i=0;i<height;i++){
//			for(int j=0;j<width;j++){
//				max = Math.max(max, matrix[i][j]);
//				min = Math.min(min, matrix[i][j]);
//			}
//		}
//
//		for(int i=0;i<height;i++){
//			for(int j=0;j<width;j++){
//				contrast[i][j] = (int)Math.round((255*(matrix[i][j]-min))/(double)(max-min));
//			}
//		}
//		
//		return contrast;
//	}

}