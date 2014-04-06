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
import io.spring.demo.doge.server.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * @author Phillip Webb
 * @author Josh Long
 */
@RestController
@RequestMapping("/users")
public class DogeRestController {

    private static final DogeRestController userControllerProxy =
            MvcUriComponentsBuilder.controller(DogeRestController.class);

    private final DogeService dogeService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public DogeRestController(DogeService dogeService, SimpMessagingTemplate simpMessagingTemplate) {
        this.dogeService = dogeService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public User getUser(@PathVariable String id) {
        return this.dogeService.getUserById(id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "{id}/doge")
    public Callable<ResponseEntity<?>> putDoge(@PathVariable String id,
                                               @RequestParam(required = false) String title,
                                               @RequestParam MultipartFile file) throws IOException {
        return () -> {
            DogePhoto dogePhoto = this.dogeService.addDogePhoto(id, title, MediaType.parseMediaType(file.getContentType()), file);

            UriComponents location = MvcUriComponentsBuilder.fromMethodCall(
                    userControllerProxy.getDoge(id, dogePhoto.getId())).build();

            URI uri = location.toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(uri);

            this.simpMessagingTemplate.convertAndSend(
                    "/topic/alarms", Collections.singletonMap("dogePhotoUri", uri));


            return new ResponseEntity<>(headers, HttpStatus.CREATED);
        };

    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}/doge/{dogeId}")
    public ResponseEntity<byte[]> getDoge(@PathVariable String id,
                                          @PathVariable BigInteger dogeId) throws IOException {
        User user = this.dogeService.getUserById(id);
        DogePhoto dogePhoto = this.dogeService.getDogePhotoById(dogeId);
        try (InputStream dogePhotoInputStream = this.dogeService.readDogePhotoContents(id, dogePhoto.getId())) {
            byte[] bytes = FileCopyUtils.copyToByteArray(dogePhotoInputStream);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.parseMediaType(dogePhoto.getMediaType()));
            return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
        }
    }

}
