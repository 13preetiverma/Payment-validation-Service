package com.mycomp.validation.service.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.mycomp.validation.http.HttpRequest;
import com.mycomp.validation.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
@RequiredArgsConstructor
public class CompletePaymentHelper {
	
private final JsonUtil jsonUtil;
	
	@Value("${processing.capture.payment.url}")
	private String processingCaptureOrderUrlTemplate;
	

	public HttpRequest prepareHttpRequest(
			String txnReference
			) {
		log.info("Preparing HttpRequest to call processing service for completing payment..:"
				+ "||txnReference:{}",
				txnReference);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String captureOrderUrl = processingCaptureOrderUrlTemplate.replace(
				"{txnReference}", txnReference);

		// create HttpRequest
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(captureOrderUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody("");
		
		log.info("Prepared HttpRequest for Processing captureOrder: {}", httpRequest);
		return httpRequest;
	}

	
}
