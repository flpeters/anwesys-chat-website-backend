package de.tub.ise.anwsys.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public ResponseEntity<?> getHelloWorld(@RequestParam(name = "me", required = false) String me) {
        if ((me == null) || me.isEmpty()) {
            return ResponseEntity.ok("Hello, world!");
        }
        return ResponseEntity.ok(String.format("Hello, %s!", me));
    }
}
