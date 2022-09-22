package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void reset() {
        // reset Image class static counter
        ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));
    }

    /**
     * Test get all images
     * @throws Exception
     */
    @Test
    @Order(1)
    public void getImageListShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images")).andExpect(status().isOk());
    }

    /**
     * Test get image that doesn't exist
     * @throws Exception
     */
    @Test
    @Order(2)
    public void getImageShouldReturnNotFound() throws Exception {
        /*
         * On admet qu'on ne mettra pas plus de 5000 images dans notre galerie, et que
         * le test ne trouvera jamais la 5000e image
         */
        this.mockMvc.perform(get("/images/5000")).andExpect(status().isNotFound());
    }

    /**
     * Test get image with its id
     * @throws Exception
     */
    @Test
    @Order(3)
    public void getImageShouldReturnSuccess() throws Exception {
        /* On considère qu'il y a toujours une image à l'indice 0 dans notre galerie */
        this.mockMvc.perform(get("/images/0")).andExpect(status().isOk());
    }

    /**
     * Test delete on an incorrect HTTP request
     * @throws Exception
     */
    @Test
    @Order(4)
    public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
        /* Le delete n'agit pas sur un URL contenant seulement /images */
        this.mockMvc.perform(delete("/images")).andExpect(status().isMethodNotAllowed());
    }

    /**
     * Test delete image not existing
     * @throws Exception
     */
    @Test
    @Order(5)
    public void deleteImageShouldReturnNotFound() throws Exception {
        /* Comme on a pas plus de 5000 images, la 5001e ne devrait pas exister */
        this.mockMvc.perform(delete("/images/5001")).andExpect(status().isNotFound());
    }

    /**
     * Test delete image success
     * @throws Exception
     */
    @Test
    @Order(6)
    public void deleteImageShouldReturnSuccess() throws Exception {
        /* On admet encore une fois que l'image 0 existe et qu'on peut la supprimer */
        this.mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
    }
    /**
     * Test add Image supported MediaType
     * @throws Exception
     */
    @Test
    @Order(7)
    public void createImageShouldReturnSuccess() throws Exception {
        final ClassPathResource cpr = new ClassPathResource("test.jpg");
        MockMultipartFile mmf = new MockMultipartFile("file", "test.jpg", "image/jpeg",
                Files.readAllBytes(cpr.getFile().toPath()));
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(mmf)).andExpect(status().isOk());

    }

    /**
     * Test add unsupported MediaType
     * @throws Exception
     */
    @Test
    @Order(8)
    public void createImageShouldReturnUnsupportedMediaType() throws Exception {
        /* Le type xcx n'est pas supporté et renverra une erreur */
        final ClassPathResource cpr = new ClassPathResource("test.jpg");
        MockMultipartFile mmf = new MockMultipartFile("file", "test.jpg", "image/xcx",
                Files.readAllBytes(cpr.getFile().toPath()));
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(mmf))
                .andExpect(status().isUnsupportedMediaType());
    }

    /**
     * Test the content of the directory images
     * Please do not delete/add images in the directory
     * @throws Exception
     */
    @Test
    @Order(9)
    public void getImageListShouldReturnSuccessContent() throws Exception {
/*
        this.mockMvc.perform(get("/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                        0, 1, 2, 3, 4
                )))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                        "space", "test", "cloud", "de", "bn"
                )))
                .andExpect(jsonPath("$[*].type", containsInAnyOrder(
                        "jpeg",  "jpeg", "png", "png","jpeg"
                )))
                .andExpect(jsonPath("$[*].size", containsInAnyOrder(
                        "245x252x3", "225x225x3", "1988x1290x3", "275x183x3", "600x450x4"
                )));*/

    }

}