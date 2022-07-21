package com.ebay.signaturevalidation;


import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VerificationInterceptor implements HandlerInterceptor {

    private VerificationService verificationService;

    public VerificationInterceptor(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String body = IOUtils.toString(request.getReader());
            URI uri = Tools.getURI(request);
            Map<String, String> headers = Collections.list(request.getHeaderNames())
                    .stream()
                    .collect(Collectors.toMap(String::toLowerCase, request::getHeader));
            return verificationService.verifyMessage(body, headers, uri, request.getMethod());
        } catch (Exception ex) {
            return false;
        }
    }


}
