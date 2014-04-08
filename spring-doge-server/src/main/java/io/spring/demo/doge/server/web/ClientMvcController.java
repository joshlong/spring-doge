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
public class ClientMvcController {

	@RequestMapping("/client")
	public String client(Model model) {
		// FIXME derive this from security principal once integrated
		model.addAttribute("userId", "joshlong");
		return "client";
	}

}
