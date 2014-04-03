package demo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // beans we need for the application
    @Bean
    MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }
}

interface UserRepository extends JpaRepository<User, Long> {
}

@Entity
class Photo {

    @Id
    @GeneratedValue
    private Long id;

    @Transient
    private byte[] photo;

    private long userId;
    private String contentType;

    public Photo(long userId, String contentType) {
        this.contentType = contentType;
        this.userId = userId;
    }

    public Photo(long userId, String contentType, byte[] photo) {
        this.userId = userId;
        this.contentType = contentType;
        this.photo = photo;
    }

    Photo() {
    }

    @Override
    public String toString() {
        return "Photo{" +
                "userId=" + userId +
                ", photo=" + Arrays.toString(photo) +
                '}';
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public String getContentType() {
        return contentType;
    }
}


@Entity
class User {
    @Id
    @GeneratedValue
    private Long id;

    private String email;

    private String password;

    private boolean enabled = false;

}

@RestController
@RequestMapping(value = PhotoUploadRestController.PHOTO_URI)
class PhotoUploadRestController {

    public static final String PHOTO_URI = "/users/{user}/photo";

    private final PhotoService photoService;

    @Autowired
    public PhotoUploadRestController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<byte[]> read(@PathVariable long user) throws IOException {
        Photo photo = photoService.readPhoto(user);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(photo.getContentType()));
        return new ResponseEntity<>(photo.getPhoto(), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    HttpEntity<Void> write(@PathVariable long user, @RequestParam MultipartFile file) throws Throwable {
        byte bytesForProfilePhoto[] = FileCopyUtils.copyToByteArray(file.getInputStream());

        photoService.writePhoto(user, MediaType.parseMediaType(file.getContentType()), bytesForProfilePhoto);

        HttpHeaders httpHeaders = new HttpHeaders();
        URI uriOfPhoto = ServletUriComponentsBuilder.fromCurrentContextPath()
                .pathSegment(PhotoUploadRestController.PHOTO_URI.substring(1))
                .buildAndExpand(Collections.singletonMap("user", user))
                .toUri();
        httpHeaders.setLocation(uriOfPhoto);

        return new ResponseEntity<Void>(httpHeaders, HttpStatus.CREATED);
    }


}

interface PhotoRepository extends JpaRepository<Photo, Long> {
    Photo findByUserId(long userId);
}

@Service
class PhotoService implements InitializingBean {

    // todo externalize this?
    private final String dir = "/Users/jlong/Desktop/images/";
    private final PhotoRepository photoRepository;

    @Autowired
    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void writePhoto(long userId, MediaType mediaType, byte[] photo) throws IOException {
        Photo savedPhoto = this.photoRepository.save(new Photo(userId, mediaType.toString()));
        try (OutputStream outputStream = new FileOutputStream(photo(userId))) {
            FileCopyUtils.copy(photo, outputStream);
        }
    }

    private File photo(long userId) {
        return new File(new File(dir), Long.toString(userId));
    }

    public Photo readPhoto(long userId) throws IOException {
        File fileForPhoto = this.photo(userId);
        Photo photo = this.photoRepository.findByUserId(userId);
        byte[] photoBytes = FileCopyUtils.copyToByteArray(fileForPhoto);
        return new Photo(userId, photo.getContentType(), photoBytes);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        File dirForPhotos = new File(this.dir);
        Assert.isTrue(dirForPhotos.exists() || dirForPhotos.mkdirs(),
                String.format("you must create a working directory ('%s') for files to be uploaded.", dirForPhotos.getAbsolutePath()));

    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserProfilePhotoReadException extends RuntimeException {
    public UserProfilePhotoReadException(String message, Throwable cause) {
        super(message, cause);
    }
}

/*
@RestController
@RequestMapping(value = "/users/{user}/photo")
class UserProfilePhotoController {

    CrmService crmService;
    UserResourceAssembler userResourceAssembler;

    @Autowired
    UserProfilePhotoController(CrmService crmService,
                               UserResourceAssembler userResourceAssembler) {
        this.crmService = crmService;
        this.userResourceAssembler = userResourceAssembler;
    }

    @RequestMapping(method = POST)
    HttpEntity<Void> writeUserProfilePhoto(@PathVariable Long user, @RequestParam MultipartFile file) throws Throwable {
        byte bytesForProfilePhoto[] = FileCopyUtils.copyToByteArray(file.getInputStream());
        this.crmService.writeUserProfilePhoto(user, MediaType.parseMediaType(file.getContentType()), bytesForProfilePhoto);


        Resource<User> userResource = this.userResourceAssembler.toResource(crmService.findById(user));
        List<Link> linkCollection = userResource.getLinks();
        Links wrapperOfLinks = new Links(linkCollection);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Link", wrapperOfLinks.toString());  // we can't encode the links in the body of the response, so we put them in the "Links:" header.
        httpHeaders.setLocation(URI.create(userResource.getLink("photo").getHref())); // "Location: /users/{userId}/photo"

        return new ResponseEntity<Void>(httpHeaders, HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = GET)
    HttpEntity<byte[]> loadUserProfilePhoto(@PathVariable Long user) throws Exception {
        ProfilePhoto profilePhoto = this.crmService.readUserProfilePhoto(user);
        if (profilePhoto != null) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(profilePhoto.getMediaType());
            return new ResponseEntity<byte[]>(profilePhoto.getPhoto(), httpHeaders, HttpStatus.OK);
        }
        throw new UserProfilePhotoReadException(user);

    }

}*/
