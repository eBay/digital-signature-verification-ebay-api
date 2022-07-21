package com.ebay.signaturevalidation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {Application.class, RestTemplate.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ApplicationTestsIT {

	@Autowired
	private RestTemplate restTemplateWithSignature;

	@LocalServerPort
	private String port;

	private String getLocalhostUrl() {
		return "http://localhost:" + port;
	}


	@Test
	void testSigning() throws Exception {
		String body = "{\"hello\": \"world\"}";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Date", "Tue, 20 Apr 2021 02:07:55 GMT");
		headers.set("Content-type", "application/json");
		headers.set("Content-length", Integer.toString(body.length()));

		HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);


		ResponseEntity<String> response = restTemplateWithSignature.exchange(getLocalhostUrl() + "/verifysignature", HttpMethod.POST, requestEntity, String.class);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody(), "OK");
	}

}
