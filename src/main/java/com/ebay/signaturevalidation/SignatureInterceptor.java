package com.ebay.signaturevalidation;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SignatureInterceptor implements ClientHttpRequestInterceptor {
    private final SignatureService signatureService;

    public SignatureInterceptor(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            signatureService.signMessage(request, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return execution.execute(request, body);
    }
}
