package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.service.LogoScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LogoScrapingController {

    @Autowired
    private LogoScrapingService logoScrapingService;

    @GetMapping("/api/logos")
    public ResponseEntity<List<String>> getCompanyLogos(@RequestParam String url) {
        List<String> logos = logoScrapingService.getCompanyLogos(url);
        return ResponseEntity.ok(logos);
    }
}