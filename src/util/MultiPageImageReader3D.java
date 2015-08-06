package util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Helper class for reading a 3D image from a multi-page image file.
 * 
 * @author Aaron Spettl, Institute of Stochastics, Ulm University
 */
public class MultiPageImageReader3D {
	
	/**
	 * Reads a 3D image from a multi-page image file.
	 * 
	 * @param fileName  the file name of the image file
	 * @return the 3D image as a 3D array
	 * @throws IOException if an I/O error occurs
	 */
	public static short[][][] read(String fileName) throws IOException {
		try (ImageInputStream imageStream = ImageIO.createImageInputStream(new File(fileName))) {
			if (imageStream == null || imageStream.length() == 0){
				throw new IOException("Image file does not exist or it is empty.");
			}
			
			Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageStream);
			if (iterator == null || !iterator.hasNext()) {
				throw new IOException("Image file format is not supported by ImageIO.");
			}
			// use the first image reader that was detected
			ImageReader reader = iterator.next();
			iterator = null;
			reader.setInput(imageStream);
			
			// obtain number of slices
			final int sizeZ = reader.getNumImages(true);
			
			short[][][] image = null;
			
			for (int z = 0; z < sizeZ; z++) {
				BufferedImage bufferedImage = reader.read(z);
				
				if (image == null) {
					image = new short[bufferedImage.getWidth()][bufferedImage.getHeight()][sizeZ];
				} else {
					if (image.length != bufferedImage.getWidth() || image[0].length != bufferedImage.getHeight()) {
						throw new IOException("All image slices must have the same dimensions.");
					}
				}
				
				WritableRaster raster = bufferedImage.getRaster();
				for (int x = 0; x < bufferedImage.getWidth(); x++) {
					for (int y = 0; y < bufferedImage.getHeight(); y++) {
						int value = raster.getSample(x, y, 0);
						if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
							throw new IOException("Only 8-bit and 16-bit integer grayscale images are supported.");
						}
						
						image[x][y][z] = (short)value;
						
						// check that all color values of other available bands are the same
						for (int i = 1; i < raster.getNumBands(); i++) {
							if (value != raster.getSample(x, y, i)) {
								throw new IOException("More than one color value available! (Not a grayscale image or alpha channel available?)");						
							}
						}
					}
				}
			}
			
			return image;
		}
	}
	
}