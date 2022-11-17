package hexlet.code.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${base-url}")
public final class WelcomeController {

    @GetMapping("/welcome")
    public String index() {
        return "Welcome to Spring";
    }
}
