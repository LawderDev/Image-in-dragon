package pdl.imageprocessing;

import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

class ColorProcessing {

  /**
   * Convert colored image to grayscale image.
   * 
   * @param input the Planar<GrayU8> image to edit
   */
  static void RGBtoGray(Planar<GrayU8> input) {
    if (input.getNumBands() == 3) {
      for (int y = 0; y < input.height; ++y) {
        for (int x = 0; x < input.width; ++x) {
          int val = (int) (input.getBand(0).get(x, y) * 0.3 +
              input.getBand(1).get(x, y) * 0.59 +
              input.getBand(2).get(x, y) * 0.11);
          for (int i = 0; i < 3; ++i) {
            input.getBand(i).set(x, y, val);
          }
        }
      }
    }
  }

  /**
   * Convert rgb values to hsv array
   * 
   * @param r   from rgb values
   * @param g   from rgb values
   * @param b   from rgb values
   * @param hsv float[] with hsvvalues corresponding to the r, g and b values
   *            entered
   */
  private static void rgbToHsv(int r, int g, int b, float[] hsv) {
    hsv[2] = Math.max(r, Math.max(g, b));
    float min = Math.min(r, Math.min(g, b));
    hsv[1] = (hsv[2] == 0) ? 0 : 1 - (min / hsv[2]);
    if (hsv[2] == min)
      hsv[0] = 0;
    else if (hsv[2] == r)
      hsv[0] = (60 * (g - b) / (hsv[2] - min) + 360) % 360;
    else if (hsv[2] == g)
      hsv[0] = 60 * (b - r) / (hsv[2] - min) + 120;
    else
      hsv[0] = 60 * (r - g) / (hsv[2] - min) + 240;
  }

  /**
   * Stores r, g and b values in rgb array
   * 
   * @param rgb the rgb array
   * @param r   the red value
   * @param g   the green value
   * @param b   the blue value
   */
  private static void setRgb(int[] rgb, int r, int g, int b) {
    rgb[0] = r;
    rgb[1] = g;
    rgb[2] = b;
  }

  /**
   * Convert hsv values to rgb array
   * 
   * @param h   from the hsv values
   * @param s   from the hsv values
   * @param v   from the hsv values
   * @param rgb the rgb array
   */
  private static void hsvToRgb(float h, float s, float v, int[] rgb) {
    int ti = (int) (Math.floor(h / 60)) % 6;
    float f = h / 60 - ti;
    int l = (int) (v * (1 - s));
    int m = (int) (v * (1 - f * s));
    int n = (int) (v * (1 - (1 - f) * s));
    int vInt = (int) v;
    switch (ti) {
      case 0:
        setRgb(rgb, vInt, n, l);
        break;
      case 1:
        setRgb(rgb, m, vInt, l);
        break;
      case 2:
        setRgb(rgb, l, vInt, n);
        break;
      case 3:
        setRgb(rgb, l, m, vInt);
        break;
      case 4:
        setRgb(rgb, n, l, vInt);
        break;
      case 5:
        setRgb(rgb, vInt, l, m);
        break;
    }
  }

  /**
   * Apply a hue filter on a position depending on the hue, the minimum and the
   * maximun saturation
   * 
   * @param input the Planar<GrayU8> image to edit
   * @param h     the hue value
   * @param smin  the minimum saturation
   * @param smax  the maximum saturation
   * @param x     the x position
   * @param y     the y position
   */
  static void filter(Planar<GrayU8> input, float h, float smin, float smax, int x, int y) {
    int[] rgb = new int[3];
    float[] hsv = new float[3];
    rgbToHsv(input.getBand(0).get(x, y),
        input.getBand(1).get(x, y),
        input.getBand(2).get(x, y), hsv);

    if (hsv[1] < smin)
      hsv[1] = smin;
    else if (hsv[1] > smax)
      hsv[1] = smax;

    hsvToRgb(h, hsv[1], hsv[2], rgb);
    for (int i = 0; i < 3; ++i) {
      input.getBand(i).set(x, y, rgb[i]);
    }
  }

  /**
   * Apply an equalization depending on V from hsv values.
   * 
   * @param image the Planar<GrayU8> image to edit
   */
  static void equalizationColorV(Planar<GrayU8> image) {
    int[] egal = new int[256];
    int[] histoCumul = histogramCumulV(image);
    for (int i = 0; i < 256; i++) {
      egal[i] = histoCumul[i] * 255 / (image.height * image.width);
    }
    for (int y = 0; y < image.height; ++y) {
      for (int x = 0; x < image.width; ++x) {
        int rgb[] = { image.getBand(0).get(x, y), image.getBand(1).get(x, y), image.getBand(2).get(x, y) };
        float[] hsv = new float[3];
        rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
        hsvToRgb(hsv[0], hsv[1], egal[Math.round(hsv[2])], rgb);
        for (int i = 0; i < 3; i++) {
          image.getBand(i).set(x, y, rgb[i]);
        }
      }
    }
  }

  /**
   * Apply an equalization depending on S from hsv values.
   * 
   * @param image the Planar<GrayU8> image to edit
   */
  static void equalizationColorS(Planar<GrayU8> image) {
    float[] egal = new float[101];
    int[] histoCumul = histogramCumulS(image);
    egal[0] = 0;
    for (int i = 1; i < 101; i++) {
      egal[i] = histoCumul[i] * 100 / (image.height * image.width);
    }
    for (int y = 0; y < image.height; ++y) {
      for (int x = 0; x < image.width; ++x) {
        int rgb[] = { image.getBand(0).get(x, y), image.getBand(1).get(x, y), image.getBand(2).get(x, y) };
        float[] hsv = new float[3];
        rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
        hsvToRgb(hsv[0], egal[(int) (hsv[1] * 100)] / 100, hsv[2], rgb);
        for (int i = 0; i < 3; i++) {
          image.getBand(i).set(x, y, rgb[i]);
        }
      }
    }
  }

  /**
   * Calculate the cumulative histogram of V value
   * 
   * @param input the Planar<GrayU8> image to edit
   * @return int[]
   */
  private static int[] histogramCumulV(Planar<GrayU8> input) {
    return GrayLevelProcessing.histogramCumulGeneric(histogramV(input));
  }

  /**
   * Calculate the cumulative histogram of S value
   * 
   * @param input the Planar<GrayU8> image to edit
   * @return int[]
   */
  private static int[] histogramCumulS(Planar<GrayU8> input) {
    return GrayLevelProcessing.histogramCumulGeneric(histogramS(input));
  }

  /**
   * Calculate the histogram of V value
   * 
   * @param image the Planar<GrayU8> image to edit
   * @return int[]
   */
  private static int[] histogramV(Planar<GrayU8> image) {
    int values[] = new int[256];
    for (int y = 0; y < image.height; ++y) {
      for (int x = 0; x < image.width; ++x) {
        float v = getVfromRgb(x, y, image);
        values[Math.round(v)]++;
      }
    }
    return values;
  }

  /**
   * Calculate the histogram of S value
   * 
   * @param image the Planar<GrayU8> image to edit
   * @return int[]
   */
  private static int[] histogramS(Planar<GrayU8> image) {
    int values[] = new int[101];
    for (int y = 0; y < image.height; ++y) {
      for (int x = 0; x < image.width; ++x) {
        float s = getSfromRgb(x, y, image);
        values[Math.round(s * 100)]++;
      }
    }
    return values;
  }

  /**
   * Get the hsv array from rgb values of an image position
   * 
   * @param x     the x position
   * @param y     the y position
   * @param image the Planar<GrayU8> image to edit
   * @return float[] the hsv array
   */
  private static float[] getHSVfromRgb(int x, int y, Planar<GrayU8> image) {
    int rgb[] = { image.getBand(0).get(x, y), image.getBand(1).get(x, y), image.getBand(2).get(x, y) };
    float[] hsv = new float[3];
    rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
    return hsv;
  }

  /**
   * Get the V value from rgb values of an image position
   * 
   * @param x     the x position
   * @param y     the y position
   * @param image the Planar<GrayU8> image to edit
   * @return float
   */
  private static float getVfromRgb(int x, int y, Planar<GrayU8> image) {
    float[] hsv = getHSVfromRgb(x, y, image);
    return hsv[2];
  }

  /**
   * Get the S value from rgb values of an image position
   * 
   * @param x     the x position
   * @param y     the y position
   * @param image the Planar<GrayU8> image to edit
   * @return float
   */
  private static float getSfromRgb(int x, int y, Planar<GrayU8> image) {
    float[] hsv = getHSVfromRgb(x, y, image);
    return hsv[1];
  }
}