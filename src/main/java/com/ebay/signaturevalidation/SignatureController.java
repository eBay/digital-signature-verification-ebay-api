package com.ebay.signaturevalidation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController("/")
public class SignatureController {
    private final Logger logger = LoggerFactory.getLogger(SignatureController.class.getName());

    @PostMapping("/verifysignature")
    public String verifySignature() {
        return "OK";
    }

}
