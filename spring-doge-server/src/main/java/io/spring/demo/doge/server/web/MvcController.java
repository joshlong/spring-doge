package io.spring.demo.doge.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for the client UI
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Controller
public class MvcController {

    public static final String MONITOR = "monitor";

    public static final String CLIENT = "client";

    @RequestMapping("/" + MONITOR)
    public String monitor() {
        return MONITOR;
    }

    @RequestMapping("/" + CLIENT)
    public String client(Model model) {
        model.addAttribute("userId", "joshlong"); // todo reinstant once theres a security context
        return CLIENT;
    }
}
