package markprojects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SubImage {

	private static final int[] WIDTH_NUMERATOR    = {1, 2, 3, 4, 1, 3, 1, 2, 4, 1, 3};
	private static final int[] HEIGHT_DENOMINATOR = {1, 1, 1, 1, 2, 2, 3, 3, 3, 4, 4};
	public static final int SIZE_ONE = 80;

	//private BufferedImage image;
	private int origWidth;
	private int origHeight;

	private double origRatio;
	private int bestRatioIndex;
	private double bestRatio;

	private BufferedImage cleanImage;


	private double avgRed = 0.0;
	private double avgGreen = 0.0;
	private double avgBlue = 0.0;
	private double avgAlpha = 0.0;


	public SubImage(File file) throws IOException {
		BufferedImage image = ImageIO.read(file);
		this.origWidth = image.getWidth();
		this.origHeight = image.getHeight();

		origRatio = (double)origWidth / origHeight;
		bestRatioIndex = -1;
		double smallestDifference = Double.MAX_VALUE;
		for(int ratioIndex = 0; ratioIndex < WIDTH_NUMERATOR.length; ratioIndex++) {
			double testRatio = (double)WIDTH_NUMERATOR[ratioIndex] / HEIGHT_DENOMINATOR[ratioIndex];
			double difference = Math.abs(origRatio - testRatio);
			if(difference < smallestDifference) {
				bestRatioIndex = ratioIndex;
				smallestDifference = difference;
			}
		}
		bestRatio = (double)WIDTH_NUMERATOR[bestRatioIndex] / HEIGHT_DENOMINATOR[bestRatioIndex];

		cropImage(image);
		
		getAverageColor();
	}
	
	public int cellWidth() {
		return WIDTH_NUMERATOR[bestRatioIndex];
	}
	
	public int cellHeight() {
		return HEIGHT_DENOMINATOR[bestRatioIndex];
	}

	private void cropImage(BufferedImage image) {
		int x, y, w, h;

		if(origRatio < bestRatio) {
			//width is smaller than expected
			w = origWidth;
			h = (int)(w / bestRatio);
			x = 0;
			y = (int)((origHeight - h)/2.0);
		} else {
			//height is smaller than expected
			h = origHeight;
			w = (int)(h * bestRatio);
			y = 0;
			x = (int)((origWidth - w)/2.0);
		}

		BufferedImage croppedImage = image.getSubimage(x, y, w, h);
		
		cleanImage = new BufferedImage(
				SIZE_ONE * WIDTH_NUMERATOR[bestRatioIndex], 
				SIZE_ONE * HEIGHT_DENOMINATOR[bestRatioIndex], 
				croppedImage.getType());

		// scales the input image to the output image
		Graphics2D g2d = cleanImage.createGraphics();
		g2d.drawImage(
				croppedImage, 
				0, 0, 
				SIZE_ONE * WIDTH_NUMERATOR[bestRatioIndex], 
				SIZE_ONE * HEIGHT_DENOMINATOR[bestRatioIndex], 
				null);
		g2d.dispose();
	}


	public BufferedImage getImage() {
		//return this.croppedImage;
		return this.cleanImage;
	}

	private void getAverageColor() {
		avgRed = 0.0;
		avgGreen = 0.0;
		avgBlue = 0.0;
		avgAlpha = 0.0;
		int numConsidderd = 0;

		for(int imageX = 0; imageX < cleanImage.getWidth(); imageX++) {
			for(int imageY = 0; imageY < cleanImage.getHeight(); imageY++) {
				Color c = new Color(cleanImage.getRGB(imageX, imageY));
				avgRed += c.getRed() / 255.0;
				avgGreen += c.getGreen() / 255.0;
				avgBlue += c.getBlue() / 255.0;
				avgAlpha += c.getAlpha() / 255.0;
				numConsidderd++;
			}
		}

		avgRed /= numConsidderd;
		avgGreen /= numConsidderd;
		avgBlue /= numConsidderd;
		avgAlpha /= numConsidderd;
	}
	
	public double getColorDistance(Color c) {
		double ret = 0.0;
		ret += Math.pow(avgRed - (c.getRed()/255.0), 2.0);
		ret += Math.pow(avgGreen - (c.getGreen()/255.0), 2.0);
		ret += Math.pow(avgBlue - (c.getBlue()/255.0), 2.0);
		ret += Math.pow(avgAlpha - (c.getAlpha()/255.0), 2.0);
		return Math.sqrt(ret);
	}
}
