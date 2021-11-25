package io.jzheaux.springsecurity.resolutions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@GetMapping("/test")
	public String test(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal authUser) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		System.out.println("auth principal");
		System.out.println(mapper.writeValueAsString(authUser));
		return "good work";
	}


}
