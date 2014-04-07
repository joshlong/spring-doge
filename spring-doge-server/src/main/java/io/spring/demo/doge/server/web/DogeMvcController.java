package io.spring.demo.doge.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles things related to presenting information to he client, such as websockets and
 * views
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Controller
public class DogeMvcController {

	@RequestMapping("/client")
	String client(Model model) {
		// FIXME derive this from security principal once integrated
		model.addAttribute("userId", "joshlong");
		return "client";
	}

}
