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

import io.spring.demo.doge.server.photos.DogePhoto;
import io.spring.demo.doge.server.photos.DogePhotoRepository;
import io.spring.demo.doge.server.photos.DogePhotoService;
import io.spring.demo.doge.server.users.User;
import io.spring.demo.doge.server.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.math.BigInteger;

/**
 * @author Phillip Webb
 * @author Josh Long
 */
@RestController
@RequestMapping("/users")
public class DogeMvcController {

    private static final DogeMvcController userControllerProxy =
            MvcUriComponentsBuilder.controller(DogeMvcController.class);

    private final UserRepository userRepository;

    private final DogePhotoRepository dogePhotoRepository;

    private final DogePhotoService dogeService;

    @Autowired
    public DogeMvcController(UserRepository userRepository,
                             DogePhotoRepository dogePhotoRepository,
                             DogePhotoService dogeService) {
        this.userRepository = userRepository;
        this.dogePhotoRepository = dogePhotoRepository;
        this.dogeService = dogeService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public User getUser(@PathVariable String id) {
        return this.userRepository.findOne(id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}/doge")
    public ResponseEntity<Void> putDoge(@PathVariable String id,
                                        @RequestParam MultipartFile file) {
        User user = this.userRepository.findOne(id);
       // todo
//        DogePhoto dogePhoto =  this.dogeService.(user, new MultipartFilePhoto(file));
    DogePhoto dogePhoto  = null ;
        UriComponents location = MvcUriComponentsBuilder.fromMethodCall(
                userControllerProxy.getDoge(id, dogePhoto.getId())).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location.toUri());
        return new ResponseEntity<>(  headers, HttpStatus.FOUND);
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}/doge/{doge}")
    public ResponseEntity<DogePhoto> getDoge(@PathVariable String id,
                                            @PathVariable BigInteger dogeId) {
        User user = this.userRepository.findOne(id);
        DogePhoto dogePhoto = this.dogePhotoRepository.findByIdAndUser( dogeId, user) ;
        return null;
       //ResponseEntity<byte[]> responseEntity  = new ResponseEntity<>() ;
    }

}
