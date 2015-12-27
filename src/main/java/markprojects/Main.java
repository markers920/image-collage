package markprojects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Main {

	private static final Runtime runtime = Runtime.getRuntime();
	
	private static final String TARGET_IMAGE_PATH = ".\\src\\main\\resources\\target.jpg";
	private static BufferedImage targetImage = null;
	private static int targetWidth;
	private static int targetHeight;


	//private static final String SUB_IMAGE_DIR_PATH = ".\\src\\main\\resources\\orig_subs\\";
	private static final String SUB_IMAGE_DIR_PATH = ".\\src\\main\\resources\\imgur\\cats";
	private static List<SubImage> subImages = new ArrayList<SubImage>();


	private static final int SUB_IMAGE_DIMENSION = 1;	//pixels


	private static Color[][] representitiveColors;
	private static int representitiveWidth;
	private static int representitiveHeight;


	private static final String OUTPUT_IMAGE_PATH = ".\\src\\main\\resources\\output.jpg";



	public static void main(String[] args) throws IOException {
		//System.out.println("hello world");

		targetImage = ImageIO.read(new File(TARGET_IMAGE_PATH));
		targetWidth = targetImage.getWidth();
		targetHeight = targetImage.getHeight();
		
		//System.out.println("type: " + targetImage.getType());
		//System.out.println(BufferedImage.TYPE_3BYTE_BGR);
		
		getSubs(SUB_IMAGE_DIR_PATH);

		getRepresentitiveColors();

		generateOutputImage();
	}
	
	
	private static void getSubs(String dirPath) throws IOException {
		File folder = new File(dirPath);
		File[] files = folder.listFiles();
		for(int fileIndex = 0; fileIndex < files.length; fileIndex++) {
		//for(int fileIndex = 0; fileIndex < 10; fileIndex++) {
			File file = files[fileIndex];
			System.out.println(fileIndex + "/" + (files.length-1) + " - " + file + " " + runtime.freeMemory());
			
			SubImage subImage = new SubImage(file);
			subImages.add(subImage);
			
			//String outputFilePath = ".\\src\\main\\resources\\test\\" + "image_" + fileIndex + ".jpg";
			//File outputfile = new File(outputFilePath);
			//ImageIO.write(subImage.getImage(), "jpg", outputfile);
		}
	}

	private static void getRepresentitiveColors() {
		representitiveWidth = (int)Math.ceil(((double)targetWidth) / SUB_IMAGE_DIMENSION);
		representitiveHeight = (int)Math.ceil(((double)targetHeight) / SUB_IMAGE_DIMENSION);
		representitiveColors = new Color[representitiveWidth][representitiveHeight];
		

		for(int repX = 0; repX < representitiveWidth; repX++) {			//index into representative array
			for(int repY = 0; repY < representitiveHeight; repY++) {
				//System.out.println(repX + " " + repY);

				int baseX = repX * SUB_IMAGE_DIMENSION;					//top left of the representative box
				int baseY = repY * SUB_IMAGE_DIMENSION;

				int baseMaxX = Math.min(baseX + SUB_IMAGE_DIMENSION, targetWidth);
				int baseMaxY = Math.min(baseY + SUB_IMAGE_DIMENSION, targetHeight);

				//get the rep color for this region
				double avgRed = 0.0;
				double avgGreen = 0.0;
				double avgBlue = 0.0;
				double avgAlpha = 0.0;
				int numConsidderd = 0;
				for(int imageX = baseX; imageX < baseMaxX; imageX++) {
					for(int imageY = baseY; imageY < baseMaxY; imageY++) {
						Color c = new Color(targetImage.getRGB(imageX, imageY));
						avgRed += c.getRed() / 255.0;
						avgGreen += c.getGreen() / 255.0;
						avgBlue += c.getBlue() / 255.0;
						avgAlpha += c.getAlpha() / 255.0;
						numConsidderd++;
					}
				}

				representitiveColors[repX][repY] = new Color(
						(float)(avgRed / numConsidderd),
						(float)(avgGreen / numConsidderd),
						(float)(avgBlue / numConsidderd),
						(float)(avgAlpha / numConsidderd));
			}
		}
	}


	private static void generateOutputImage() throws IOException {
		int outputWidth = representitiveWidth * SubImage.SIZE_ONE;
		int outputHeight = representitiveHeight * SubImage.SIZE_ONE;
		
		BufferedImage outputImage = new BufferedImage(outputWidth, outputHeight, targetImage.getType());
		Graphics2D g = (Graphics2D)outputImage.getGraphics();
		
		boolean[][] squaresWritten = new boolean[representitiveWidth][representitiveHeight];
		//int numberOfSquaresWritten = 0;
		for(int repX = 0; repX < representitiveWidth; repX++) {			//index into representative array
			for(int repY = 0; repY < representitiveHeight; repY++) {
				squaresWritten[repX][repY] = false;
			}
		}
		
		
		
		for(int baseCellX = 0; baseCellX < representitiveWidth; baseCellX++) {
			System.out.println(baseCellX + " " + (representitiveWidth-1) + " " + runtime.freeMemory());
			for(int baseCellY = 0; baseCellY < representitiveHeight; baseCellY++) {
				//System.out.println(baseCellX + " " + baseCellY + " ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				
				//if already written on
				if(squaresWritten[baseCellX][baseCellY]) {
					//System.out.println("\talready taken");
					continue;
				}
				
				double minD = Double.MAX_VALUE;
				SubImage sub = null;
				for(SubImage si : subImages) {
					//System.out.println("sub: " + sub);
					
					//if it will fall off the image
					if(baseCellX + si.cellWidth() > representitiveWidth || baseCellY + si.cellHeight() > representitiveHeight) {
						//System.out.println("\ttoo big");
						continue;
					}
					
					//if it will write over other pieces
					boolean enoughRoom = true;
					for(int cellX = baseCellX; enoughRoom && cellX < baseCellX + si.cellWidth(); cellX++) {
						for(int cellY = baseCellY; enoughRoom && cellY < baseCellY + si.cellHeight(); cellY++) {
							//System.out.println("xxx " + cellX + " " + baseCellX + " " + cellWidth);
							//System.out.println("xxx " + cellY + " " + baseCellY + " " + cellHeight);
							//System.out.println();
							if(squaresWritten[cellX][cellY]) {
								enoughRoom = false;
							}
						}
					}
					
					if(!enoughRoom) {
						//System.out.println("\tnot enough room");
						continue;
					}
					
					double d = 0.0;
					for(int cellX = baseCellX; cellX < baseCellX + si.cellWidth(); cellX++) {
						for(int cellY = baseCellY; cellY < baseCellY + si.cellHeight(); cellY++) {
							d += si.getColorDistance(representitiveColors[cellX][cellY]);
						}
					}
					d /= (si.cellWidth() * si.cellHeight());
					
					//System.out.println("\tmade it");
					if(d < minD) {
						//System.out.println("\t\tnew val");
						//System.out.println("\t\t" + si.cellWidth() + " " + si.cellHeight());
						minD = d;
						sub = si;
					}
					
				}	//END for sub image
				
				int cellWidth = sub.cellWidth();
				int cellHeight = sub.cellHeight();
				
				for(int cellX = baseCellX; cellX < baseCellX + cellWidth; cellX++) {
					for(int cellY = baseCellY; cellY < baseCellY + cellHeight; cellY++) {
						//System.out.println("*** " + cellX + " " + baseCellX + " " + cellWidth + " " + representitiveWidth);
						//System.out.println("*** " + cellY + " " + baseCellY + " " + cellHeight + " " + representitiveHeight);
						//System.out.println();
						squaresWritten[cellX][cellY] = true;
					}
				}
					
				int imageX = baseCellX * SubImage.SIZE_ONE;
				int imageY = baseCellY * SubImage.SIZE_ONE;
				g.drawImage(sub.getImage(), null, imageX, imageY);
				
			}	// end base cell y
		}		// end base cell x
		
		
		
		System.out.println("writing out this giant fucker");
		File outputfile = new File(OUTPUT_IMAGE_PATH);
		ImageIO.write(outputImage, "jpg", outputfile);
	}
	
	
	/*private static void generateOutputImage() throws IOException {
		int outputWidth = representitiveWidth * SubImage.SIZE_ONE;
		int outputHeight = representitiveHeight * SubImage.SIZE_ONE;
		
		BufferedImage outputImage = new BufferedImage(outputWidth, outputHeight, targetImage.getType());
		Graphics2D g = (Graphics2D)outputImage.getGraphics();
		
		boolean[][] squaresWritten = new boolean[representitiveWidth][representitiveHeight];
		int numberOfSquaresWritten = 0;
		for(int repX = 0; repX < representitiveWidth; repX++) {			//index into representative array
			for(int repY = 0; repY < representitiveHeight; repY++) {
				squaresWritten[repX][repY] = false;
			}
		}
		
		Random random = new Random(0);
		int totalNumberOfCells = representitiveWidth*representitiveHeight;
		while(numberOfSquaresWritten < totalNumberOfCells) {
			int baseCellX = -1;
			int baseCellY = -1;
			int cellWidth = -1;
			int cellHeight = -1;
			
			if(numberOfSquaresWritten < 0.5*totalNumberOfCells) { 
				int r = random.nextInt(subImages.size());
				SubImage sub = subImages.get(r);
				cellWidth = sub.cellWidth();
				cellHeight = sub.cellHeight();
				
				boolean good = false;
				while(!good) {
					good = true;
					baseCellX = random.nextInt(representitiveWidth-cellWidth);	//TODO: +1 ?
					baseCellY = random.nextInt(representitiveHeight-cellHeight);
					for(int cellX = baseCellX; good && cellX < baseCellX + cellWidth; cellX++) {
						for(int cellY = baseCellY; good && cellY < baseCellY + cellHeight; cellY++) {
							if(squaresWritten[cellX][cellY]) {	//if already written on, NOT good
								good = false;
							}
						}
					}
				}	//END the search for a goos square
				
			}	//random positions
			
			else {
				
			}
			
			for(int cellX = baseCellX; cellX < baseCellX + cellWidth; cellX++) {
				for(int cellY = baseCellY; cellY < baseCellY + cellHeight; cellY++) {
					squaresWritten[cellX][cellY] = true;
				}
			}
				
			int imageX = baseCellX * SubImage.SIZE_ONE;
			int imageY = baseCellY * SubImage.SIZE_ONE;
			g.drawImage(sub.getImage(), null, imageX, imageY);
		
			
			numberOfSquaresWritten += cellWidth * cellHeight;
		}

		File outputfile = new File(OUTPUT_IMAGE_PATH);
		ImageIO.write(outputImage, "jpg", outputfile);
	}*/
	
	/*private static void generateOutputImage() throws IOException {
		int outputWidth = representitiveWidth * subWidth;
		int outputHeight = representitiveHeight * subHeight;
		//System.out.println("*** " + outputWidth + " " + outputHeight + " -> " + outputWidth*outputHeight);
		//System.out.println("*** " + outputWidth*outputHeight + " " + Integer.MAX_VALUE);
		//System.out.println("*** " + (outputWidth*outputHeight < Integer.MAX_VALUE));
		BufferedImage outputImage = new BufferedImage(outputWidth, outputHeight, targetImage.getType());
		Graphics2D g = (Graphics2D)outputImage.getGraphics();

		for(int repX = 0; repX < representitiveWidth; repX++) {			//index into representative array
			for(int repY = 0; repY < representitiveHeight; repY++) {
				System.out.println(repX + "/" + representitiveWidth + " " + repY + "/" + representitiveHeight + " -> (" + representitiveColors[repX][repY].getRed() + "," + representitiveColors[repX][repY].getGreen() + "," + representitiveColors[repX][repY].getBlue() + ", " + representitiveColors[repX][repY].getAlpha() + ")");
				//System.out.println(representitiveColors[repX][repY].getAlpha());

				int imageX = repX * subWidth;
				int imageY = repY * subHeight;
				//g.setColor(representitiveColors[repX][repY]);
				//g.fillRect(imageX, imageY, subWidth, subHeight);
				setSubImageAverageColor(representitiveColors[repX][repY]);
				g.drawImage(subImage, null, imageX, imageY);
			}
		}

		File outputfile = new File(OUTPUT_IMAGE_PATH);
		ImageIO.write(outputImage, "jpg", outputfile);
	}*/
	
	
//	private static void setSubImageAverageColor(Color desiredColor) {
//		int biggestIndex = -1;
//		if(desiredColor.getRed() >= desiredColor.getBlue() && desiredColor.getRed() >= desiredColor.getGreen()) {	//red biggest
//			biggestIndex = 0;
//		} else if(desiredColor.getGreen() >= desiredColor.getRed() && desiredColor.getGreen() >= desiredColor.getBlue()) {	//green biggest
//			biggestIndex = 1;
//		} else if(desiredColor.getBlue() >= desiredColor.getGreen() && desiredColor.getBlue() >= desiredColor.getRed()) {	//blue biggest
//			biggestIndex = 2;
//		}
//		
//		
//		/*double avgRed = 0.0;
//		double avgGreen = 0.0;
//		double avgBlue = 0.0;
//		double avgAlpha = 0.0;
//		int numConsidderd = 0;
//		for(int x = 0; x < subWidth; x++) {
//			for(int y = 0; y < subHeight; y++) {
//				Color c = new Color(subImage.getRGB(x, y));
//				avgRed += c.getRed() / 255.0;
//				avgGreen += c.getGreen() / 255.0;
//				avgBlue += c.getBlue() / 255.0;
//				avgAlpha += c.getAlpha() / 255.0;
//				numConsidderd++;
//			}
//		}
//		avgRed /= numConsidderd;
//		avgGreen /= numConsidderd;
//		avgBlue /= numConsidderd;
//		avgAlpha /= numConsidderd;
//		
//		double deltaRed = (desiredColor.getRed() / 255.0) - avgRed;
//		double deltaGreen = (desiredColor.getRed() / 255.0) - avgGreen;
//		double deltaBlue = (desiredColor.getRed() / 255.0) - avgBlue;
//		double deltaAlpha = (desiredColor.getRed() / 255.0) - avgAlpha;
//		
//		//System.out.println("\tr " + deltaRed);
//		//System.out.println("\tg " + deltaGreen);
//		//System.out.println("\tb " + deltaBlue);
//		//System.out.println("\ta " + deltaAlpha);
//		
//		
//		Graphics2D g = (Graphics2D)subImage.getGraphics();
//		for(int x = 0; x < subWidth; x++) {
//			for(int y = 0; y < subHeight; y++) {
//				Color c = new Color(subImage.getRGB(x, y));
//				
//				float rv = boundedFloat((c.getRed()/255.0) + deltaRed, 0.0, 1.0);
//				float gv = boundedFloat((c.getGreen()/255.0) + deltaGreen, 0.0, 1.0);
//				float bv = boundedFloat((c.getBlue()/255.0) + deltaBlue, 0.0, 1.0);
//				float av = boundedFloat((c.getAlpha()/255.0) + deltaAlpha, 0.0, 1.0);
//				
//				//System.out.println(x + " " + y);
//				//System.out.println("\tr " + (float)((c.getRed()/255.0) + deltaRed));
//				//System.out.println("\tg " + (float)((c.getGreen()/255.0) + deltaGreen));
//				//System.out.println("\tb " + (float)((c.getBlue()/255.0) + deltaBlue));
//				//System.out.println("\ta " + (float)((c.getAlpha()/255.0) + deltaAlpha));
//				
//				Color nc = new Color(rv, gv, bv, av);
//				g.setColor(nc);
//				g.fillRect(x, y, 1, 1);
//			}
//		}*/
//	}
//	
//	
//	private static float boundedFloat(double d, double min, double max) {
//		d = Math.min(d, 1.0);
//		d = Math.max(d, 0.0);
//		return (float)d;
//	}

}
