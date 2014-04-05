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

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourceOperation;
import io.spring.demo.doge.filesystem.mongo.MongoFolder;
import io.spring.demo.doge.server.photos.DogePhoto;
import io.spring.demo.doge.server.photos.DogePhotoRepository;
import io.spring.demo.doge.server.users.User;
import io.spring.demo.doge.server.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

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
    public DogeService(UserRepository userRepository, DogePhotoRepository dogePhotoRepository, MongoFolder resources ) {
        this.userRepository = userRepository;
        this.dogePhotoRepository = dogePhotoRepository;
        this.folder = resources;
    }

    public DogePhoto getDogePhotoById(BigInteger id) {
        return this.dogePhotoRepository.findOne(id);
    }

    public User getUserById(String user ) {
        return this.userRepository.findOne(user);
    }

    public void addDogePhoto(String user ,
                             String title,
                             MediaType mediaType,
                             byte[] contents) {

         File file = folder.getFile( "doge" + System.currentTimeMillis() );
        file.performOperation(  new ResourceOperation<Resource>() {
            @Override
            public void perform(Resource resource) {

            }
        });

      /*  List<GridFSDBFile> gridFSDBFiles = gridFSDBFiles(userId);
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
*/
    }

    public DogePhoto readDogePhoto(String user, BigInteger dogeId) {
        return null;
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
