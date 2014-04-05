package io.spring.demo.doge.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;

/**
 * Handles things related to presenting information tot he client, such as websockets and views
 *
 * @author Josh Long
 */
@Controller
public class DogeMvcController {

    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public DogeMvcController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    @RequestMapping("/client")
    String client(){
        return "client";
    }

    @RequestMapping("/go")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void go() {
        this.messagingTemplate.convertAndSend(
                "/topic/alarms",  (String.format("Hello, %tc", new Date())));

    }


}
