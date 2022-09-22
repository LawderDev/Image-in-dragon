package pdl.imageprocessing;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public class ImageProcessing {

  /**
   * Convert a Planar<GrayU8> with 1 band to a Planar<GrayU8> with 3 bands
   * 
   * @param input the Planar<GrayU8> image
   * @return a Planar<GrayU8> image with 3 bands
   */
  private static Planar<GrayU8> grayToPlanarRGB(Planar<GrayU8> input) {
    int nbCanaux = input.getNumBands();
    if (nbCanaux == 1) {
      GrayU8[] bands = new GrayU8[3];
      for (int i = 0; i < 3; ++i)
        bands[i] = input.getBand(0).clone();
      input.setBands(bands);
    }
    return input;
  }

  /**
   * Do an egalisation of the value parameter
   * 
   * @param input a Planar<GrayU8> image
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> egalisationV(Planar<GrayU8> input) {
    input = grayToPlanarRGB(input);
    int nbCanaux = input.getNumBands();
    if (nbCanaux != 3)
      return new ResponseEntity<>("unsupported type", HttpStatus.BAD_REQUEST);
    ColorProcessing.equalizationColorV(input);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Do an egalisation of the saturation parameter
   * 
   * @param input a Planar<GrayU8> image
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> egalisationS(Planar<GrayU8> input) {
    input = grayToPlanarRGB(input);
    int nbCanaux = input.getNumBands();
    if (nbCanaux != 3)
      return new ResponseEntity<>("unsupported type", HttpStatus.BAD_REQUEST);
    ColorProcessing.equalizationColorS(input);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Do a sobel kernel application
   * 
   * @param image a Planar<GrayU8> image
   * @param color true if contours are colored
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> sobelImage(Planar<GrayU8> image, boolean color) {
    int nbCanaux = image.getNumBands();
    if (nbCanaux != 1 && !color)
      ColorProcessing.RGBtoGray(image);
    Planar<GrayU8> input = image.clone();
    for (int i = 0; i < nbCanaux; ++i) {
      Convolution.gradientImageSobel(input.getBand(i), image.getBand(i));
    }
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Modify the brightness of an image
   * 
   * @param input the Planar<GrayU8> image to edit
   * @param delta the desire difference in value
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> luminosityImage(Planar<GrayU8> input, int delta) {
    if (delta >= -255 && delta <= 255) {
      int nbCanaux = input.getNumBands();
      for (int i = 0; i < nbCanaux; ++i) {
        GrayLevelProcessing.luminosity(input.getBand(i), delta);
      }
      return new ResponseEntity<>("ok", HttpStatus.OK);
    } else
      return new ResponseEntity<>("the parameter must be between -255 and 255", HttpStatus.BAD_REQUEST);
  }

  /**
   * Apply a mean blur filter
   * 
   * @param image      the Planar<GrayU8> image to edit
   * @param size       the size of the kernel
   * @param borderType border management type
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> meanFilterWithBorders(Planar<GrayU8> image, int size, BorderType borderType) {
    if (borderType == null)
      return new ResponseEntity<>("borderType parameter is incorrect", HttpStatus.BAD_REQUEST);
    if (size % 2 == 1) {
      if (size < image.height && size < image.width) {
        int nbCanaux = image.getNumBands();
        Planar<GrayU8> input = image.clone();
        for (int i = 0; i < nbCanaux; ++i) {
          Convolution.meanFilterWithBorders(input.getBand(i), image.getBand(i), size, borderType);
        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
      } else
        return new ResponseEntity<>("the size of the kernel is too large", HttpStatus.BAD_REQUEST);
    } else
      return new ResponseEntity<>("the kernel size must be odd", HttpStatus.BAD_REQUEST);
  }

  /**
   * Apply a gaussian blur filter
   * 
   * @param image      the Planar<GrayU8> image to edit
   * @param size       the size of the kernel
   * @param sigma      the standard deviation
   * @param borderType the border management type
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> gaussianBlur(Planar<GrayU8> image, int size, float sigma, BorderType borderType) {
    if (borderType == null)
      return new ResponseEntity<>("borderType parameter is incorrect", HttpStatus.BAD_REQUEST);
    if (size % 2 == 1) {
      if (size < image.height && size < image.width) {
        int nbCanaux = image.getNumBands();
        Planar<GrayU8> input = image.clone();
        double[][] kernel = Convolution.gaussianKernel(size, sigma);
        for (int i = 0; i < nbCanaux; ++i) {
          Convolution.gaussianBlurGrayU8(input.getBand(i), image.getBand(i), sigma, kernel, borderType);
        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
      } else
        return new ResponseEntity<>("the size of the kernel is too large", HttpStatus.BAD_REQUEST);
    } else
      return new ResponseEntity<>("the kernel size must be odd", HttpStatus.BAD_REQUEST);
  }

  /**
   * Apply a filter color on an image, the saturation interval can be changed to
   * manage filter intensity.
   * 
   * @param input the Planar<GrayU8> image to edit
   * @param h     the hue desired
   * @param smin  the minimal saturation value
   * @param smax  the maximal saturation value
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> filter(Planar<GrayU8> input, float h, float smin, float smax) {
    if (h >= 0 && h <= 360) {
      if (smin <= smax && smin >= 0 && smin <= 1 && smax >= 0 && smax <= 1) {
        input = grayToPlanarRGB(input);
        int nbCanaux = input.getNumBands();
        if (nbCanaux == 3) {
          for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {
              ColorProcessing.filter(input, h, smin, smax, x, y);
            }
          }
          return new ResponseEntity<>("ok", HttpStatus.OK);
        } else
          return new ResponseEntity<>("unsupported type", HttpStatus.BAD_REQUEST);
      } else
        return new ResponseEntity<>("smin must be inferior or equal to smax and they must be between 0 and 1",
            HttpStatus.BAD_REQUEST);
    } else
      return new ResponseEntity<>("hue must be between 0 and 360", HttpStatus.BAD_REQUEST);
  }

  /**
   * Apply a rainbow filter
   * 
   * @param input the Planar<GrayU8> image to edit
   * @param smin  the minimum saturation value
   * @param smax  the maximum saturation value
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> rainbow(Planar<GrayU8> input, float smin, float smax) {
    if (smin <= smax && smin >= 0 && smin <= 1 && smax >= 0 && smax <= 1) {
      input = grayToPlanarRGB(input);
      int nbCanaux = input.getNumBands();
      if (nbCanaux == 3) {
        for (int y = 0; y < input.height; ++y) {
          float h = y * 360 / input.height;
          for (int x = 0; x < input.width; ++x) {
            ColorProcessing.filter(input, h, smin, smax, x, y);
          }
        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
      } else
        return new ResponseEntity<>("unsupported type", HttpStatus.BAD_REQUEST);
    } else
      return new ResponseEntity<>("smin must be inferior or equal to smax and they must be between 0 and 1",
          HttpStatus.BAD_REQUEST);
  }

  /**
   * Apply a dynamic contrast
   * 
   * @param image the Planar<GrayU8> image to edit
   * @param min   the minimum value of a dynamic contrast
   * @param max   the maximum value of a dynamic contrast
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> dynamicContast(Planar<GrayU8> image, int min, int max) {
    if (min > max)
      return new ResponseEntity<>("min must be less than max", HttpStatus.BAD_REQUEST);
    int nbCanaux = image.getNumBands();
    if (nbCanaux != 1 && nbCanaux != 3)
      return new ResponseEntity<>("unsupported type", HttpStatus.BAD_REQUEST);
    int minHisto, maxHisto;
    if (nbCanaux == 3) {
      Planar<GrayU8> input = image.clone();
      ColorProcessing.RGBtoGray(input);
      minHisto = GrayLevelProcessing.min(input.getBand(0));
      maxHisto = GrayLevelProcessing.max(input.getBand(0));
    } else {
      minHisto = GrayLevelProcessing.min(image.getBand(0));
      maxHisto = GrayLevelProcessing.max(image.getBand(0));
    }
    if (minHisto == maxHisto)
      return new ResponseEntity<>("dynamic contrast cannot be applied to a solid image", HttpStatus.BAD_REQUEST);
    for (int i = 0; i < nbCanaux; ++i)
      GrayLevelProcessing.contrastLUT(image.getBand(i), min, max, minHisto, maxHisto);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Apply an RGB egalisation
   * 
   * @param image the Planar<GrayU8> image to edit
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> egalisationRGB(Planar<GrayU8> image) {
    int nbCanaux = image.getNumBands();
    if (nbCanaux != 1 && nbCanaux != 3)
      return new ResponseEntity<>("unsupported type", HttpStatus.BAD_REQUEST);
    int[] histoCum;
    if (nbCanaux == 3) {
      Planar<GrayU8> input = image.clone();
      ColorProcessing.RGBtoGray(input);
      histoCum = GrayLevelProcessing.histogramCumul(input.getBand(0));
    } else
      histoCum = GrayLevelProcessing.histogramCumul(image.getBand(0));
    for (int i = 0; i < nbCanaux; ++i)
      GrayLevelProcessing.egalisation(image.getBand(i), histoCum);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Apply a threshold
   * 
   * @param image the Planar<GrayU8> image to edit
   * @param t     the threshold
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> threshold(Planar<GrayU8> image, int t) {
    if (t < 0 || t > 255)
      return new ResponseEntity<>("The threshold parameter must be between 0 and 255", HttpStatus.BAD_REQUEST);
    int nbCanaux = image.getNumBands();
    for (int i = 0; i < nbCanaux; ++i)
      GrayLevelProcessing.threshold(image.getBand(i), t);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Convert image in gray scale
   * 
   * @param image the Planar<GrayU8> image to edit
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> RGBtoGray(Planar<GrayU8> image) {
    int nbCanaux = image.getNumBands();
    if (nbCanaux == 1)
      return new ResponseEntity<>("ok", HttpStatus.OK);
    if (nbCanaux != 3)
      return new ResponseEntity<>("unsupported type", HttpStatus.BAD_REQUEST);
    ColorProcessing.RGBtoGray(image);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Apply a negative filter
   * 
   * @param image the Planar<GrayU8> image to edit
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> negativeImage(Planar<GrayU8> image) {
    int nbCanaux = image.getNumBands();
    for (int i = 0; i < nbCanaux; ++i)
      GrayLevelProcessing.reverse(image.getBand(i));
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Define the step of a pixel depending on the step parameter which define the
   * difference between 2 steps.
   * 
   * Shades are redefined according to landings.
   * The difference with the nearer landing is defined according to the biggest
   * value of a position.
   * The difference is applied for each bands of the position.
   * 
   * @param input the Planar<GrayU8> image to edit
   * @param x     the abscissa of the pixel
   * @param y     the ordinate of the pixel
   * @param step  the size of step
   */
  private static void gradient(Planar<GrayU8> input, int x, int y, int step, boolean dark) {
    int nbCanaux = input.getNumBands();
    int maxVal = getMaxValueFromAPosition(input, x, y);
    int valDark = maxVal % step; // calcul the difference between the value and the upper landing
    int valBright = step - valDark; // calcul the difference between the value and the lower landing
    for (int i = 0; i < nbCanaux; ++i) {
      int v0 = input.getBand(i).get(x, y);
      int v = (dark) ? v0 - valDark : v0 + valBright;
      if (v > 255)
        v = 255;
      else if (v < 0)
        v = 0;
      input.getBand(i).set(x, y, v);
    }
  }

  /**
   * Find the maximum value from a position
   * 
   * @param input the Planar<GrayU8> image to edit
   * @param x     the abscissa of the pixel
   * @param y     the ordinate of the pixel
   * @return the maximum value
   */
  private static int getMaxValueFromAPosition(Planar<GrayU8> input, int x, int y) {
    int nbCanaux = input.getNumBands();
    int maxVal = input.getBand(0).get(x, y);
    for (int i = 1; i < nbCanaux; ++i) {
      int val = input.getBand(i).get(x, y);
      if (val > maxVal)
        maxVal = val;
    }
    return maxVal;
  }

  /**
   * Apply a draw filter depending on the steps values.
   * 
   * @param input the Planar<GrayU8> image to edit
   * @param step  the size of step
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> draw(Planar<GrayU8> input, int step, boolean dark) {
    if (step < 1 || step > 255)
      return new ResponseEntity<>("The step must be between 1 and 255", HttpStatus.BAD_REQUEST);
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        gradient(input, x, y, step, dark);
      }
    }
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Apply a water color filter
   * 
   * @param input the Planar<GrayU8> image to edit
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> waterColor(Planar<GrayU8> input) {
    int nbCanaux = input.getNumBands();
    Planar<GrayU8> contours = input.clone();
    sobelImage(contours, false);
    negativeImage(contours);
    for (int i = 0; i < nbCanaux; ++i) {
      GrayLevelProcessing.thresholdsWaterColor(input.getBand(i));
    }
    applyMinValues(input, contours);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Apply the minimum values on the first input between two images.
   * @param input the Planar image compared and edited
   * @param input2 the Planar image compared
   */
  private static void applyMinValues(Planar<GrayU8> input, Planar<GrayU8> input2){
    int nbCanaux = input.getNumBands();
    for (int i = 0; i < nbCanaux; ++i) {
      for (int y = 0; y < input.height; ++y) {
        for (int x = 0; x < input.width; ++x) {
          int c = input2.getBand(i).get(x, y);
          int color = input.getBand(i).get(x, y);
          if (c < color)
            input.getBand(i).set(x, y, c);
        }
      }
    }
  }

  /**
   * Apply a rotation depending on the angle theta
   * 
   * @param image the Planar<GrayU8> image to edit
   * @param theta the angle of rotation
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> rotation(Planar<GrayU8> image, double theta) {
    theta = theta * Math.PI / 180;
    if (theta < -360 || theta > 360)
      return new ResponseEntity<>("The angle of rotation must be between -360 and 360", HttpStatus.BAD_REQUEST);
    int nbCanaux = image.getNumBands();
    for (int i = 0; i < nbCanaux; ++i)
      GrayLevelProcessing.rotate(image.getBand(i), theta);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Apply a perspective of the image
   * 
   * @param image       the Planar<GrayU8> image to edit
   * @param delta       the factor of the perspective
   * @param perspective the type of
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> perspective(Planar<GrayU8> image, double delta, Perspective perspective) {
    if (perspective == null)
      return new ResponseEntity<>("perspective parameter is incorrect", HttpStatus.BAD_REQUEST);
    int nbCanaux = image.getNumBands();
    if (delta < 0)
      return new ResponseEntity<>("delta must be positive", HttpStatus.BAD_REQUEST);
    for (int i = 0; i < nbCanaux; ++i)
      GrayLevelProcessing.perspective(image.getBand(i), delta, perspective);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

  /**
   * Apply a vortex effet
   * 
   * @param image            the Planar<GrayU8> image to edit
   * @param tourbillonFactor the factor of modification
   * @return a ResponseEntity
   */
  public static ResponseEntity<?> vortex(Planar<GrayU8> image, float tourbillonFactor, int x0, int y0) {
    int nbCanaux = image.getNumBands();
    if (tourbillonFactor < 0)
      return new ResponseEntity<>("the parameter must be positive", HttpStatus.BAD_REQUEST);
    for (int i = 0; i < nbCanaux; ++i)
      GrayLevelProcessing.vortex(image.getBand(i), tourbillonFactor, x0, y0);
    return new ResponseEntity<>("ok", HttpStatus.OK);
  }

}
