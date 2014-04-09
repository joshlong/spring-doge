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

	@RequestMapping("/monitor")
	public String monitor() {
		return "monitor";
	}

	@RequestMapping("/client")
	public String client(Model model) {
		model.addAttribute("userId", "joshlong");
		return "client";
	}
}
