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

package io.spring.demo.doge.server.domain;

import io.spring.demo.doge.server.Application;
import io.spring.demo.doge.server.DogeService;
import io.spring.demo.doge.server.photos.DogePhoto;
import io.spring.demo.doge.server.photos.DogePhotoRepository;
import io.spring.demo.doge.server.users.User;
import io.spring.demo.doge.server.users.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * @author Phillip Webb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@MongoDbIntegrationTest
public class DogeServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DogeService dogeService;

    @Autowired
    private DogePhotoRepository dogePhotoRepository;


    private User phil, josh;

    @Before
    public void before() {

        this.josh = this.userRepository.findOne("joshlong") ;
        this.phil = this.userRepository.findOne("philwebb") ;
    }

    @Test
    public void addUser() throws Exception {
        assertThat(phil.getName(), equalTo("Phil Webb"));
    }

    @Test
    public void addUserAndDoge() throws Exception {
        dogeService.addDogePhoto(josh.getId(), "test", MediaType.IMAGE_JPEG, () -> null);
        Collection<DogePhoto> found = this.dogePhotoRepository.findByUser(josh);
        assertThat(found.size(), greaterThan(0));
        assertThat(found.iterator().next().getTitle(), equalTo("test"));
    }

    @Test
    public void findDoge() throws Exception {
        User user = josh;
        DogePhoto dogePhoto = new DogePhoto(user, MediaType.IMAGE_JPEG_VALUE, "test");
        user = this.userRepository.save(user);
        dogePhoto = this.dogePhotoRepository.save(dogePhoto);
        System.out.println(dogePhoto.getId());
        DogePhoto findOne = this.dogePhotoRepository.findOne(dogePhoto.getId());
        System.out.println(findOne);
    }

}
