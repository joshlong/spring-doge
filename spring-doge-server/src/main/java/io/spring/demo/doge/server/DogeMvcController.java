package io.spring.demo.doge.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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
    String client(Model model) { // todo derive this from security principal once integrated
        model.addAttribute("userId", "joshlong");
        return "client" ;
    }


}
