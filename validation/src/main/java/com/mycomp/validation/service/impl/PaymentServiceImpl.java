package com.mycomp.validation.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mycomp.validation.http.HttpRequest;
import com.mycomp.validation.http.HttpServiceEngine;
import com.mycomp.validation.pojo.CreatePaymentReq;
import com.mycomp.validation.pojo.InitiatePaymentRequest;
import com.mycomp.validation.pojo.PaymentResponse;
import com.mycomp.validation.processing.ProcessingCreatePaymentRes;
import com.mycomp.validation.service.helper.CompletePaymentHelper;
import com.mycomp.validation.service.helper.CreatePaymentHelper;
import com.mycomp.validation.service.interfaces.PaymentService;
import com.mycomp.validation.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
	
	private final CreatePaymentHelper createPaymentHelper;
	private final CompletePaymentHelper completePaymentHelper; 
	private final HttpServiceEngine httpService;
	private final JsonUtil jsonUtil;

	@Override
	public PaymentResponse createPayment(CreatePaymentReq createPaymentReq) {
		log.info("Creating payment in PaymentServiceImpl|| createPaymentReq:{}",
				createPaymentReq);
		
		HttpRequest httprequest = createPaymentHelper.prepareCreateOrderHttpRequest(createPaymentReq);
		log.info("Prepared HttpRequest for create payment call: {}", httprequest);
		
		ResponseEntity<String> successResponse = httpService.makeHttpCall(httprequest);
		log.info("HTTP response from HttpServiceEngine: {}", successResponse);
		
		ProcessingCreatePaymentRes processingCreatePaymentRes = jsonUtil.fromJson(
				successResponse.getBody(), ProcessingCreatePaymentRes.class);
		
		PaymentResponse paymentResponse = createPaymentHelper.toOrderResponse(processingCreatePaymentRes);
		log.info("prepared PaymentResponse for create payment call: {}", paymentResponse);
		return paymentResponse;
		
	}

	@Override
	public PaymentResponse initiatePayment(InitiatePaymentRequest intiatePaymentRequest,String txnReference) {
		log.info("Initiating payment in PaymentServiceImpl... txnReference: {}",
				txnReference);
		HttpRequest httprequest = createPaymentHelper.prepareInitiateOrderHttpRequest(intiatePaymentRequest, txnReference);
		log.info("Prepared HttpRequest for initiate payment call: {}", httprequest);
		
		ResponseEntity<String> successResponse = httpService.makeHttpCall(httprequest);
		log.info("HTTP response from HttpServiceEngine: {}", successResponse);
		
		//directly converting to PaymentResponse since processing initiate response is same as PaymentResponse
		PaymentResponse paymentResponse = jsonUtil.fromJson(
				successResponse.getBody(), PaymentResponse.class);
		log.info("prepared PaymentResponse for initiate payment call: {}", paymentResponse);
		return paymentResponse;
		
		
	}

	@Override
	public PaymentResponse completePayment(String txnReference) {
		
		HttpRequest httpReq = completePaymentHelper.prepareHttpRequest(
				txnReference);
		PaymentResponse response;
		try {
			ResponseEntity<String> httpResponse = httpService.makeHttpCall(httpReq);
			log.info("HTTP response from HttpServiceEngine: {}", httpResponse);
			
			response = jsonUtil.fromJson(
					httpResponse.getBody(), PaymentResponse.class);
			
	
		} catch (Exception e) {
			log.error("Error occurred while making captureOrder HTTP call to Processing Service: ", e);
			
			// Note, dont change the status to FAILED since user already APPROVED.
			// Let reconciliation job handle such cases.
			// In case reconciliation also resolved it as failed, 
			//then manually back-office can handle this payment..
			// just throw error back
			
			throw e; // rethrow the exception after updating status
		}
		return response;
	}
	
}
