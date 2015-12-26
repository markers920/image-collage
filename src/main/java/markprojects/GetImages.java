package markprojects;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetImages {

	private static final String OUTPUT_IMAGE_PATH = ".\\src\\main\\resources\\imgur\\%s\\image_%d_%d.jpg";

	public static void main(String[] args) throws MalformedURLException, IOException {
		String keyword = "cats";
		int maxPage = 15;

		for(int pageNumber = 1; pageNumber <= maxPage; pageNumber++) {
			String urlString = String.format("http://imgur.com/r/%s/?p=%d", keyword, pageNumber);
			System.out.println(urlString);
			
			URL url = new URL(urlString);
			Document doc = null;
			int trialIndex = 0;
			boolean keepTrying = true;
			while(keepTrying) {
				try {
					doc = Jsoup.parse(url, 2000);
					keepTrying = false;
				} catch(HttpStatusException e) {
					System.out.println("damn... trying again: " + urlString);
					trialIndex++;
					keepTrying = trialIndex < 5;
				}
			}
			
			if(doc == null) {		//try another page, and skip this one
				continue;
			}
			
			
			Elements jpgs = doc.select("img[src$=.jpg]");
			System.out.println("found: " + jpgs.size() + " images");
			int imageLinkElementIndex = 1;
			for (Element imageLinkElement : jpgs) {
				//System.out.println(imageLinkElement.toString());
				String imageUrlString = imageLinkElement.attr("src");
				//imageUrlString = imageUrlString.substring(2);
				imageUrlString = "http:" + imageUrlString;
				System.out.println("\t" + imageUrlString);

				URL imageUrl = new URL(imageUrlString);
				Image image = ImageIO.read(imageUrl);

				String outputFileName = String.format(OUTPUT_IMAGE_PATH, keyword, pageNumber, imageLinkElementIndex);
				System.out.println("outputFileName: " + outputFileName);

				// Create a buffered image with transparency
				BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);

				// Draw the image on to the buffered image
				Graphics2D bGr = bimage.createGraphics();
				bGr.drawImage(image, 0, 0, null);
				bGr.dispose();

				File outputfile = new File(outputFileName);
				ImageIO.write(bimage, "jpg", outputfile);

				//System.out.println();
				imageLinkElementIndex++;
			}
		}
    
	}

}
