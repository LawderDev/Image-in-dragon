package pdl.imageprocessing;

import boofcv.struct.image.GrayU8;

class GrayLevelProcessing {

	/**
	 * Modify the brightness of a GrayU8 band
	 * 
	 * @param input the GrayU8 band
	 * @param delta
	 */
	static void luminosity(GrayU8 input, int delta) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y) + delta;
				if (gl > 255)
					gl = 255;
				else if (gl < 0)
					gl = 0;
				input.set(x, y, gl);
			}
		}
	}

	/**
	 * Find the minimum value of a band
	 * 
	 * @param input the GrayU8 band
	 * @return int the min value
	 */
	static int min(GrayU8 input) {
		int min = 255;
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl < min)
					min = gl;
			}
		}
		return min;
	}

	/**
	 * Find the maximum value of a band
	 * 
	 * @param input the GrayU8 band
	 * @return int
	 */
	static int max(GrayU8 input) {
		int max = 0;
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl > max)
					max = gl;
			}
		}
		return max;
	}

	/**
	 * Apply a dynamic contrast on a band
	 * 
	 * @param input    the GrayU8 band
	 * @param min
	 * @param max
	 * @param minHisto
	 * @param maxHisto
	 */
	static void contrastLUT(GrayU8 input, int min, int max, int minHisto, int maxHisto) {
		int LUT[] = new int[256];
		for (int i = 0; i < 256; i++) {
			LUT[i] = ((max - min) * (i - minHisto) / (maxHisto - minHisto)) + min;
			if (LUT[i] > 255)
				LUT[i] = 255;
			else if (LUT[i] < 0)
				LUT[i] = 0;
		}
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				input.set(x, y, LUT[input.get(x, y)]);
			}
		}
	}

	/**
	 * Apply a threshold on a band
	 * 
	 * @param input the GrayU8 band
	 * @param t     the threshold
	 */
	static void threshold(GrayU8 input, int t) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				gl = (gl < t) ? 0 : 255;
				input.set(x, y, gl);
			}
		}
	}

	/**
	 * Reverse values on a band
	 * 
	 * @param input the GrayU8 band
	 */
	static void reverse(GrayU8 input) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = 255 - input.get(x, y);
				input.set(x, y, gl);
			}
		}
	}

	/**
	 * Calculate the histogram of a band
	 * 
	 * @param input the GrayU8 band
	 * @return int[]
	 */
	static int[] histogram(GrayU8 input) {
		int values[] = new int[256];
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				values[input.get(x, y)]++;
			}
		}
		return values;
	}

	/**
	 * Calculate the cumulative histogram of an histogram
	 * 
	 * @param hist the histogram
	 * @return int[]
	 */
	static int[] histogramCumulGeneric(int[] histo) {
		int tailleTab = histo.length;
		int histoCum[] = new int[tailleTab];
		histoCum[0] = histo[0];
		for (int i = 1; i < tailleTab; i++) {
			histoCum[i] = histo[i] + histoCum[i - 1];
		}
		return histoCum;
	}

	/**
	 * Calculate the cumulative histogram of a band
	 * 
	 * @param input the GrayU8 band
	 * @return int[]
	 */
	static int[] histogramCumul(GrayU8 input) {
		return histogramCumulGeneric(histogram(input));
	}

	/**
	 * Calculate an egalization of a band
	 * 
	 * @param input      the GrayU8 band
	 * @param histoCumul the cumulative histogram
	 */
	static void egalisation(GrayU8 input, int[] histoCumul) {
		int[] egal = new int[256];
		for (int i = 0; i < 256; i++) {
			egal[i] = (histoCumul[i] * 255) / (input.height * input.width);
		}
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				input.set(x, y, egal[input.get(x, y)]);
			}
		}
	}

	/**
	 * Apply the water color threshlod on a band
	 * 
	 * @param input the GrayU8 band
	 */
	public static void thresholdsWaterColor(GrayU8 input) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl < 61) { // dark values become white
					input.set(x, y, 255);
				} else if (gl < 122) { // values between 61 and 122 are cleared to the step 122
					input.set(x, y, 122);
				} else if (gl < 183) { // values between 122 and 183 are cleared to the step 183
					input.set(x, y, 183);
				} else // bright values become white
					input.set(x, y, 255);
			}
		}
	}

	/**
	 * Apply a rotation on a band
	 * 
	 * @param input the GrayU8 band
	 * @param theta the angle of rotation
	 */
	static void rotate(GrayU8 input, double theta) {
		// x0 and y0 are the origin of the rotation
		int x0 = input.width / 2;
		int y0 = input.height / 2;
		GrayU8 tmp = input.clone();
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int p = (int) ((x - x0) * Math.cos(theta) + (y - y0) * Math.sin(theta) + x0);
				int q = (int) (-(x - x0) * Math.sin(theta) + (y - y0) * Math.cos(theta) + y0);
				if (p < input.width && q < input.height && p >= 0 && q >= 0)
					input.set(x, y, tmp.get(p, q));
				else
					input.set(x, y, 0);
			}
		}
	}

	/**
	 * Apply a perspective effect on a band
	 * 
	 * @param input       the GrayU8 band
	 * @param d           the factor of the deformation
	 * @param perspective the type of perspective
	 */
	static void perspective(GrayU8 input, double d, Perspective perspective) {
		// x0 and y0 are the origin of the perspective
		int x0 = x0declarationToPerspective(input, d, perspective);
		int y0 = y0declarationToPerspective(input, d, perspective);
		perspectiveTreatement(input, d, x0, y0);
	}

	/**
	 * The x origin of the deformation
	 * 
	 * @param input       the GrayU8 band
	 * @param d           the factor of the deformation
	 * @param perspective the type of perspective
	 * @return int
	 */
	private static int x0declarationToPerspective(GrayU8 input, double d, Perspective perspective) {
		if (perspective.equals(Perspective.LEFT) || perspective.equals(Perspective.TOPLEFT)
				|| perspective.equals(Perspective.BOTTOMLEFT)) {
			return (int) (input.width / 2 * d);
		}
		if (perspective.equals(Perspective.RIGHT) || perspective.equals(Perspective.TOPRIGHT)
				|| perspective.equals(Perspective.BOTTOMRIGHT)) {
			return (int) (input.width - input.width / 2 * d);
		}
		return input.width / 2;
	}

	/**
	 * The y origin of the deformation
	 * 
	 * @param input       the GrayU8 band
	 * @param d           the factor of the deformation
	 * @param perspective the type of perspective
	 * @return int
	 */
	private static int y0declarationToPerspective(GrayU8 input, double d, Perspective perspective) {
		if (perspective.equals(Perspective.TOP) || perspective.equals(Perspective.TOPLEFT)
				|| perspective.equals(Perspective.TOPRIGHT)) {
			return (int) (input.height / 2 * d);
		}
		if (perspective.equals(Perspective.BOTTOM) || perspective.equals(Perspective.BOTTOMLEFT)
				|| perspective.equals(Perspective.BOTTOMRIGHT)) {
			return (int) (input.height - input.height / 2 * d);
		}
		return input.height / 2;
	}

	/**
	 * Do the treatement of the perspective
	 * 
	 * @param input the GrayU8 band
	 * @param d     the factor of the deformation
	 * @param x0    the x origin of the perspective
	 * @param y0    the y origin of the perspective
	 */
	private static void perspectiveTreatement(GrayU8 input, double d, int x0, int y0) {
		GrayU8 tmp = input.clone();
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int p = (int) (d * radious(x, y, x0, y0) * (x - x0) + x);
				int q = (int) (d * radious(x, y, x0, y0) * (y - y0) + y);
				if (p < input.width && q < input.height && p >= 0 && q >= 0)
					input.set(x, y, tmp.get(p, q));
				else
					input.set(x, y, 0);
			}
		}
	}

	/**
	 * Apply a vortex transformation
	 * 
	 * @param input        the GrayU8 band
	 * @param vortexFactor the factor of the transformation
	 * @param x0           The x origin of the vortex
	 * @param y0           The y origin of the vortex
	 */
	static void vortex(GrayU8 input, float vortexFactor, int x0, int y0) {
		x0 *= input.width / 100;
		y0 *= input.height / 100;
		GrayU8 tmp = input.clone();
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				double theta = 10 * Math.exp(-vortexFactor * radious(x, y, x0, y0));
				int p = (int) ((x - x0) * Math.cos(theta) + (y - y0) * Math.sin(theta) + x0);
				int q = (int) (-(x - x0) * Math.sin(theta) + (y - y0) * Math.cos(theta) + y0);
				if (p < input.width && q < input.height && p >= 0 && q >= 0)
					input.set(x, y, tmp.get(p, q));
				else
					input.set(x, y, 0);
			}
		}
	}

	/**
	 * @param x  the x position
	 * @param y  the y position
	 * @param x0 The x origin of the deformation
	 * @param y0 The y origin of the deformation
	 * @return double
	 */
	private static double radious(int x, int y, int x0, int y0) {
		return Math.sqrt(Math.pow(x - x0, 2) + Math.pow(y - y0, 2));
	}
}