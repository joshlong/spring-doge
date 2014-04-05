/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.demo.doge.server;

import com.mongodb.gridfs.GridFSDBFile;
import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.mongo.MongoFolder;
import io.spring.demo.doge.server.photos.DogePhoto;
import io.spring.demo.doge.server.photos.DogePhotoRepository;
import io.spring.demo.doge.server.users.User;
import io.spring.demo.doge.server.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * TODO figure out how to use the filesystem bits -JL
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Service
public class DogeService {

    private final UserRepository userRepository;
    private final DogePhotoRepository dogePhotoRepository;
    private final MongoFolder folder;

    @Autowired
    public DogeService(UserRepository userRepository, DogePhotoRepository dogePhotoRepository, MongoFolder resources) {
        this.userRepository = userRepository;
        this.dogePhotoRepository = dogePhotoRepository;
        this.folder = resources;
    }


    public DogePhoto getDogePhotoById(BigInteger id) {
        return this.dogePhotoRepository.findOne(id);
    }

    public User getUserById(String user) {
        return this.userRepository.findOne(user);
    }

    public DogePhoto addDogePhoto(String username,
                             String title,
                             MediaType mediaType,
                             byte[] contents) {
        User user = this.userRepository.findOne(username);
        String finalTitle = StringUtils.hasText(title) ? title : "";
        DogePhoto photo = this.dogePhotoRepository.save(new DogePhoto(user, mediaType.toString(), finalTitle));
        BigInteger photoId = photo.getId();
        File file = folder.getFile(fileNameForFile(photoId));
        file.createIfMissing();
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contents);
                OutputStream fileOutputStream = file.getContent().asOutputStream()) {
            StreamUtils.copy(byteArrayInputStream, fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return photo;
    }

    private String fileNameForFile(BigInteger photoId) {
        return photoId.toString();
    }


    public InputStream readDogePhotoContents( String username, BigInteger bigInteger ){
        File file = this.folder.getFile(fileNameForFile( bigInteger));
        return file.getContent().asInputStream();
    }

    public DogePhoto readDogePhoto(String username, BigInteger dogeId) {
        return this.dogePhotoRepository.findOne(dogeId);
    }


    /* private final GridFsTemplate fileSytem;

    private final DogePhotoRepository photoRepository;

    @Autowired
    public DogePhotoService(GridFsTemplate fileSytem, DogePhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
        this.fileSytem = fileSytem;
    }

    public void writePhoto(User u, MediaType mediaType, byte[] photo)
            throws IOException {

        List<GridFSDBFile> gridFSDBFiles = gridFSDBFiles(userId);
        for (GridFSDBFile gridFSDBFile : gridFSDBFiles) {
            this.fileSytem.delete(new Query().addCriteria(GridFsCriteria.whereMetaData()
                    .is(gridFSDBFile.getMetaData())));
        }
        DBObject dbObject = new BasicDBObject();
        dbObject.put("userId", userId);
        dbObject.put("when", new Date().getTime());
        dbObject.put("contentType", mediaType.toString());
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(photo)) {

            this.fileSytem.store(byteArrayInputStream, Long.toString(userId), dbObject);

            this.photoRepository.save(new DogePhoto(userId, mediaType.toString()));
        }
    }

    public DogePhoto readPhoto(long userId) throws IOException {
        List<GridFSDBFile> gridFSDBFileList = gridFSDBFiles(userId);
        Assert.isTrue(gridFSDBFileList.size() <= 1,
                "there should be 0-1 records returned");
        GridFSDBFile gridFSDBFile = gridFSDBFileList.iterator().next();

        try (InputStream inputStream = gridFSDBFile.getInputStream()) {
            DBObject metaData = gridFSDBFile.getMetaData();
            String mediaType = (String) metaData.get("contentType");
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);

//            return new Photo(userId, mediaType, bytes);
        }
    }

    private List<GridFSDBFile> gridFSDBFiles(long userId) {
        Criteria query = GridFsCriteria.whereFilename().is(Long.toString(userId));
        return this.fileSytem.find(new Query().addCriteria(query));
    }

	public DogePhoto addDoge(User user, Photo photo) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}*/

}
