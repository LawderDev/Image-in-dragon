package pdl.imageprocessing;

import boofcv.concurrency.BoofConcurrency;
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayU8;

class Convolution {

  /**
   * Apply the sobel transformation on a band
   * 
   * @param input  the input band
   * @param output the output band
   */
  static void gradientImageSobel(GrayU8 input, GrayU8 output) {
    int h1[][] = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
    int h2[][] = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
    int bord = 1;
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        if (x < bord || x >= input.width - bord || y < bord || y >= input.height - bord) {
          output.set(x, y, input.get(x, y));
        } else {
          int valx = 0;
          int valy = 0;
          for (int ky = -bord; ky <= bord; ky++) {
            for (int kx = -bord; kx <= bord; kx++) {
              valx += input.get(x + kx, y + ky) * h1[ky + bord][kx + bord];
              valy += input.get(x + kx, y + ky) * h2[ky + bord][kx + bord];
            }
          }
          int v = (int) Math.sqrt(valx * valx + valy * valy);
          if (v < 0)
            v = 0;
          else if (v > 255)
            v = 255;
          output.set(x, y, v);
        }
      }
    }
  }

  /**
   * Apply a gaussian blur transformation on a band
   * 
   * @param input      the input band
   * @param output     the output band
   * @param sigma      the standard deviation
   * @param kernel     the convolution kernel
   * @param borderType the border management type
   */
  public static void gaussianBlurGrayU8(GrayU8 input, GrayU8 output, float sigma, double[][] kernel,
      BorderType borderType) {
    int size = kernel.length;
    if (size % 2 == 1) {
      int edge = (size - 1) / 2;
      BoofConcurrency.loopFor(0, input.height, y -> {
        for (int x = 0; x < input.width; ++x) {
          if (x < edge || x >= input.width - edge || y < edge || y >= input.height - edge) {
            gaussianBorderTreatement(x, y, kernel, sigma, input, output, borderType);
          } else {
            int val = 0;
            for (int ky = -edge; ky <= edge; ky++) {
              for (int kx = -edge; kx <= edge; kx++) {
                val += input.get(x + kx, y + ky) * kernel[ky + edge][kx + edge];
              }
            }
            output.set(x, y, val);
          }
        }
      });
    } else {
      System.err.println("la taille du noyau doit être impair");
    }
  }

  /**
   * Create a gaussian kernel depending on the size and the standard deviation
   * 
   * @param size  the size of the kernel
   * @param sigma the standard deviation
   * @return double[][]
   */
  public static double[][] gaussianKernel(int size, float sigma) {
    double[][] kernel = new double[size][size];
    int bord = (size - 1) / 2;
    double denominateur1 = 2 * sigma * sigma;
    double sumCoef = 0;
    for (int ky = -bord; ky <= bord; ky++) {
      for (int kx = -bord; kx <= bord; kx++) {
        kernel[ky + bord][kx + bord] = Math.exp(-(((kx * kx) + (ky * ky)) / denominateur1));
        sumCoef += kernel[ky + bord][kx + bord];
      }
    }
    for (int ky = 0; ky < size; ky++) {
      for (int kx = 0; kx < size; kx++) {
        kernel[ky][kx] /= sumCoef;
      }
    }
    return kernel;
  }

  /**
   * Apply a mean blur filter
   * 
   * @param input      the input band
   * @param output     the output band
   * @param size       the size of the kernel
   * @param borderType the border management type
   */
  static void meanFilterWithBorders(GrayU8 input, GrayU8 output, int size, BorderType borderType) {
    if (size % 2 == 1) {
      int edge = (size - 1) / 2;
      for (int y = 0; y < input.height; ++y) {
        for (int x = 0; x < input.width; ++x) {
          if (x < edge || x >= input.width - edge || y < edge || y >= input.height - edge) {
            meanBorderTreatement(x, y, size, input, output, borderType);
          } else {
            meanFilterTreatement(x, y, size, input, output);
          }
        }
      }
    } else {
      System.err.println("size doit être impair");
    }
  }

  /**
   * Do the treatement of the mean blur filter without edge
   * 
   * @param x      the x position
   * @param y      the y position
   * @param size   the size of the kernel
   * @param input  the input band
   * @param output the output band
   */
  private static void meanFilterTreatement(int x, int y, int size, GrayU8 input, GrayU8 output) {
    int edge = size / 2;
    int val = 0;
    for (int ky = -edge; ky <= edge; ky++) {
      for (int kx = -edge; kx <= edge; kx++) {
        val += input.get(x + kx, y + ky);
      }
    }
    output.set(x, y, val / (size * size));
  }

  /**
   * Apply the border treatement for the mean blur filter
   * 
   * @param x          the x position
   * @param y          the y position
   * @param size       the size of the kernel
   * @param input      the input band
   * @param output     the output band
   * @param borderType the border management type
   */
  private static void meanBorderTreatement(int x, int y, int size, GrayU8 input, GrayU8 output,
      BorderType borderType) {
    int edge = size / 2;
    switch (borderType) {
      case SKIP:
      default:
        skip(x, y, input, output);
        break;
      case NORMALIZED:
        normalizedMean(x, y, edge, input, output);
        break;
      case EXTENDED:
        extendedMean(x, y, size, input, output);
        break;
      case REFLECT:
        reflectMean(x, y, size, input, output);
        break;
    }
  }

  /**
   * Apply the border treatement for the gaussian blur filter
   * 
   * @param x          the x position
   * @param y          the y position
   * @param kernel     the convolution kernel
   * @param sigma      the standard deviation
   * @param input      the input band
   * @param output     the output band
   * @param borderType the border management type
   */
  private static void gaussianBorderTreatement(int x, int y, double[][] kernel, double sigma,
      GrayU8 input, GrayU8 output, BorderType borderType) {
    switch (borderType) {
      case SKIP:
      default:
        skip(x, y, input, output);
        break;
      case NORMALIZED:
        normalizedGaussian(x, y, input, output, kernel, sigma);
        break;
      case EXTENDED:
        extendedGaussian(x, y, input, output, kernel);
        break;
      case REFLECT:
        reflectGaussian(x, y, input, output, kernel);
        break;
    }
  }

  /**
   * Apply skip edge treatement
   * 
   * @param x      the x position
   * @param y      the y position
   * @param input  the input band
   * @param output the output band
   */
  private static void skip(int x, int y, GrayU8 input, GrayU8 output) {
    output.set(x, y, input.get(x, y));
  }

  /**
   * Apply normalized edge treatement for the mean blur filter
   * 
   * @param x      the x position
   * @param y      the y position
   * @param edge   the edge of the kernel
   * @param input  the input band
   * @param output the output band
   */
  private static void normalizedMean(int x, int y, int edge, GrayU8 input, GrayU8 output) {
    int val = 0;
    int nbPix = 0;
    for (int ky = -edge; ky <= edge; ky++) {
      if (y + ky >= 0 && y + ky < input.height) {
        for (int kx = -edge; kx <= edge; kx++) {
          if (x + kx >= 0 && x + kx < input.width) {
            val += input.get(x + kx, y + ky);
            nbPix++;
          }
        }
      }
    }
    output.set(x, y, val / nbPix);
  }

  /**
   * Apply normalized edge treatement for the gaussian blur filter
   * 
   * @param x      the x position
   * @param y      the y position
   * @param input  the input band
   * @param output the output band
   * @param kernel the convolution kernel
   * @param sigma  the standard deviation
   */
  private static void normalizedGaussian(int x, int y, GrayU8 input, GrayU8 output, double[][] kernel, double sigma) {
    int edge = kernel.length / 2;
    double coefEntierKernel = 1 / kernel[edge][edge];
    int size = kernel.length;
    double[][] kernel2 = new double[size][size];
    for (int ky = 0; ky < size; ky++) {
      for (int kx = 0; kx < size; kx++) {
        kernel2[ky][kx] = kernel[ky][kx] * coefEntierKernel;
      }
    }
    int val = 0;
    double coef = 0;
    for (int ky = -edge; ky <= edge; ky++) {
      if (y + ky >= 0 && y + ky < input.height) {
        for (int kx = -edge; kx <= edge; kx++) {
          if (x + kx >= 0 && x + kx < input.width) {
            val += input.get(x + kx, y + ky) * kernel2[ky + edge][kx + edge];
            coef += kernel2[ky + edge][kx + edge];
          }
        }
      }
    }
    output.set(x, y, (int) (val / coef));
  }

  /**
   * Apply extended edge treatement for the mean blur filter
   * 
   * @param x      the x position
   * @param y      the y position
   * @param size   the size of the kernel
   * @param input  the input band
   * @param output the output band
   */
  private static void extendedMean(int x, int y, int size, GrayU8 input, GrayU8 output) {
    int edge = size / 2;
    int valE = 0;
    int xE, yE = 0;
    for (int ky = -edge; ky <= edge; ky++) {
      yE = y + ky;
      if (yE < 0)
        yE = 0;
      else if (yE >= input.height)
        yE = input.height - 1;
      for (int kx = -edge; kx <= edge; kx++) {
        xE = x + kx;
        if (xE < 0)
          xE = 0;
        else if (xE >= input.width)
          xE = input.width - 1;
        valE += input.get(xE, yE);

      }
    }
    output.set(x, y, valE / (size * size));
  }

  /**
   * Apply extended edge treatement for the gaussian blur filter
   * 
   * @param x      the x position
   * @param y      the y position
   * @param input  the input band
   * @param output the output band
   * @param kernel the convolution kernel
   */
  private static void extendedGaussian(int x, int y, GrayU8 input, GrayU8 output, double[][] kernel) {
    int edge = kernel.length / 2;
    int valE = 0;
    int xE, yE = 0;
    for (int ky = -edge; ky <= edge; ky++) {
      yE = y + ky;
      if (yE < 0)
        yE = 0;
      else if (yE >= input.height)
        yE = input.height - 1;
      for (int kx = -edge; kx <= edge; kx++) {
        xE = x + kx;
        if (xE < 0)
          xE = 0;
        else if (xE >= input.width)
          xE = input.width - 1;
        valE += input.get(xE, yE) * kernel[ky + edge][kx + edge];

      }
    }
    output.set(x, y, valE);
  }

  /**
   * Apply reflect edge treatement for the mean blur filter
   * 
   * @param x      the x position
   * @param y      the y position
   * @param size   the size of the kernel
   * @param input  the input band
   * @param output the output band
   */
  private static void reflectMean(int x, int y, int size, GrayU8 input, GrayU8 output) {
    int edge = size / 2;
    int valE = 0;
    int xE, yE = 0;
    for (int ky = -edge; ky <= edge; ky++) {
      yE = y + ky;
      if (yE < 0)
        yE = -yE;
      else if (yE >= input.height) {
        yE = input.height - 1 - (yE - input.height);
      }
      for (int kx = -edge; kx <= edge; kx++) {
        xE = x + kx;
        if (xE < 0)
          xE = -xE;
        else if (xE >= input.width)
          xE = input.width - 1 - (xE - input.width);
        valE += input.get(xE, yE);

      }
    }
    output.set(x, y, valE / (size * size));
  }

  /**
   * Apply reflect edge treatement for the gaussian blur filter
   * 
   * @param x      the x position
   * @param y      the y position
   * @param input  the input band
   * @param output the output band
   * @param kernel the convolution kernel
   */
  private static void reflectGaussian(int x, int y, GrayU8 input, GrayU8 output,
      double[][] kernel) {
    int edge = kernel.length / 2;
    int valE = 0;
    int xE, yE = 0;
    for (int ky = -edge; ky <= edge; ky++) {
      yE = y + ky;
      if (yE < 0)
        yE = -yE;
      else if (yE >= input.height) {
        yE = input.height - 1 - (yE - input.height);
      }
      for (int kx = -edge; kx <= edge; kx++) {
        xE = x + kx;
        if (xE < 0)
          xE = -xE;
        else if (xE >= input.width)
          xE = input.width - 1 - (xE - input.width);
        valE += input.get(xE, yE) * kernel[ky + edge][kx + edge];

      }
    }
    output.set(x, y, valE);
  }
}
