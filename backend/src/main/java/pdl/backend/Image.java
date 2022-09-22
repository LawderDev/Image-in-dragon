package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.springframework.http.MediaType;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public class Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private byte[] data;
  private MediaType type;
  private String size;

  /**
   * Constructor Image
   * @param name String
   * @param data byte[]
   * @throws IOException
   */
  public Image(final String name, final byte[] data) throws IOException {
    id = count++;
    this.name = name.split("\\.")[0];
    this.data = data;
    this.type = getType(name);
    this.size = createSize();
  }

  /**
   * Form the image's size as "width x height x channels"
   * @return String
   * @throws IOException
   */
  private String createSize() throws IOException {

    InputStream inputStream = new ByteArrayInputStream(this.data);

    BufferedImage img = ImageIO.read(inputStream);
    Planar<GrayU8> planImg = ConvertBufferedImage.convertFromPlanar(img, null, true, GrayU8.class);

    String width = String.valueOf(img.getWidth());
    String height = String.valueOf(img.getHeight());

    return width + 'x' + height + 'x' + planImg.getNumBands();
  }

  /**
   * Getter Id
   * @return long
   */
  public long getId() {
    return id;
  }

  /**
   * Setter Id
   * @param newId long
   */
  public void setId(long newId) {
    this.id = newId;
  }

  /**
   * Getter Name
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Setter Name
   * @param name String
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Getter Data
   * @return byte[]
   */
  public byte[] getData() {
    return data;
  }

  /**
   * Getter Type
   * @return MediaType
   */
  public MediaType getType() {
    return type;
  }

  /**
   * Getter Size
   * @return String
   */
  public String getSize() {
    return size;
  }

  private MediaType getType(String fileName) {

    int index = fileName.lastIndexOf('.');
    String extension = "";
    if (index > 0) {
      extension = fileName.substring(index + 1);
    }

    if (extension.equals("jpeg"))
      extension = "jpg";

    return extension.equals("jpg") ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG;
  }

  /**
   * Update count
   * @param i int
   */
  public static void updateCount(int i) {
    count = count + i;
  }

  /**
   * Getter Count
   * @return long
   */
  public static long getCount() {
    return count;
  }

}
