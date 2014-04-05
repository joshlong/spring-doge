package io.spring.demo.doge.photo.manipulate;

import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.ResourcePhoto;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Tests for {@link DogePhotoManipulator}.
 *
 * @author Phillip Webb
 * @author Josh Long
 */
public class DogePhotoManipulatorTest {

    private PhotoManipulator manipulator = new DogePhotoManipulator();

    @Test
    public void testDogeService() throws Exception {

        Photo photo = new ResourcePhoto(
                new ClassPathResource("thehoff.jpg"));

        Photo manipulatedPhoto = manipulator.manipulate(photo);
        File target = new File(System.getProperty("java.io.tmpdir"));

        File file = new File(target, "manipulatedhoff.jpg");

        System.out.println (
                "writing out file to " + file.getAbsolutePath());
        try (InputStream inputStream = manipulatedPhoto.getInputStream();
             OutputStream outputStream = new FileOutputStream(file)) {
            FileCopyUtils.copy(inputStream, outputStream);
        }
    }

}
