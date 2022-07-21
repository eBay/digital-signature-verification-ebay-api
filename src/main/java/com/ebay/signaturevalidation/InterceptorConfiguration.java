package com.ebay.signaturevalidation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    private VerificationInterceptor verificationInterceptor;

    public InterceptorConfiguration(VerificationInterceptor verificationInterceptor) {
        this.verificationInterceptor = verificationInterceptor;
    }


    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verificationInterceptor);
    }
}
