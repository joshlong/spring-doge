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
	String client(Model model) {
		// FIXME try to remove this, replace with configurrurrruruer
		return "client";
	}

}
