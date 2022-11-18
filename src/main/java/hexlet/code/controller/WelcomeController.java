package hexlet.code.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${base-url}")
public class WelcomeController {

    @GetMapping("/welcome")
    public final String index() {
        return "Welcome to Spring";
    }
}
