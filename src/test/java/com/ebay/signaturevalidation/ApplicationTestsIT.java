package com.ebay.signaturevalidation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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
	void testSigningPOST() throws Exception {
		String body = "{\"hello\": \"world\"}";

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplateWithSignature.exchange(getLocalhostUrl() + "/verifysignature", HttpMethod.POST, requestEntity, String.class);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody(), "OK");
	}

	@Test
	void testSigningGET() throws Exception {

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplateWithSignature.exchange(getLocalhostUrl() + "/verifysignature", HttpMethod.GET, requestEntity, String.class);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody(), "OK");
	}
}
