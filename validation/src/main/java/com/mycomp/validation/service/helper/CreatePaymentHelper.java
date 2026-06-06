package com.mycomp.validation.service.helper;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.mycomp.validation.http.HttpRequest;
import com.mycomp.validation.pojo.CreatePaymentReq;
import com.mycomp.validation.pojo.InitiatePaymentRequest;
import com.mycomp.validation.pojo.PaymentResponse;
import com.mycomp.validation.processing.ProcessingCreatePaymentReq;
import com.mycomp.validation.processing.ProcessingCreatePaymentRes;
import com.mycomp.validation.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreatePaymentHelper {

	private final JsonUtil jsonUtil;

	@Value("${processing.create.payment.url}")
	private String createPaymentUrl;
	
	@Value("${processing.initiate.payment.url}")
	private String processingInitiatePaymentUrlTemplate;

	public HttpRequest prepareCreateOrderHttpRequest(
			CreatePaymentReq createPaymentReq) {
		
		log.info("Preparing HttpRequest for processing create payment. "
				+ "createPaymentReq: {}", createPaymentReq);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		
		ProcessingCreatePaymentReq paymentReq = new ProcessingCreatePaymentReq();
		paymentReq.setUserId(createPaymentReq.getUserId());
		paymentReq.setPaymentMethodId(createPaymentReq.getPaymentMethodId());
		paymentReq.setProviderId(createPaymentReq.getProviderId());
		paymentReq.setPaymentTypeId(createPaymentReq.getPaymentTypeId());
		paymentReq.setAmount(createPaymentReq.getAmount());
		paymentReq.setCurrency(createPaymentReq.getCurrency());
		paymentReq.setMerchantTransactionReference(createPaymentReq.getMerchantTransactionReference());
		
		String requestAsJson = jsonUtil.toJson(paymentReq);
		
		
		// create HttpRequest
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(createPaymentUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(requestAsJson);
		
		log.info("Prepared HttpRequest for processing create payment: {}", httpRequest);
		
		return httpRequest;
		
	}
	
	public HttpRequest prepareInitiateOrderHttpRequest(InitiatePaymentRequest initiatePaymentReq,String txnReference) {
		log.info("Preparing HttpRequest for initiating order. "
				+ "initiatePaymentReq: {}", initiatePaymentReq ,"txnReference: {}", txnReference);
		
		//Don't need  to make ProcessingInitiatePaymentReq as it is same as InitiatePaymentrequest  
		String requestAsJson = jsonUtil.toJson(initiatePaymentReq);
		
		String initatePaymentUrl = processingInitiatePaymentUrlTemplate.replace(
				"{txnReference}", txnReference);
		//headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		// create HttpRequest
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(initatePaymentUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(requestAsJson);
		
		log.info("Prepared HttpRequest for initiating order: {}", httpRequest);
		
		return httpRequest;
	}

	
	public PaymentResponse toOrderResponse(ProcessingCreatePaymentRes processingCreatePaymentRes) {
		log.info("Converting processingCreatePaymentRes to PaymentResponse: {}", processingCreatePaymentRes);
		
		PaymentResponse response = new PaymentResponse();
	    response.setTxnReference(processingCreatePaymentRes.getTxnReference());
	    response.setTxnStatusId(processingCreatePaymentRes.getTxnStatusId());
	    response.setProviderReference(processingCreatePaymentRes.getProviderReference());
	    response.setRedirectUrl(processingCreatePaymentRes.getRedirectUrl());

	      
	    log.info("Converted PaypalOrder to OrderResponse: {}", response);

	    return response;
	}

	/* public PaymentResponse handlePaypalResponse(ResponseEntity<String> httpResponse) {
		log.info("Handling PayPal response in PaymentServiceImpl "
				+ "httpResponse:{}", httpResponse);
		
		if(httpResponse.getStatusCode().is2xxSuccessful()) { //success

			PaypalOrderRes paypalOrder = jsonUtil.fromJson(
					httpResponse.getBody(), PaypalOrderRes.class);
			log.info("Converted response body to PaypalOrder: {}", paypalOrder);
			
			OrderResponse orderResponse = toOrderResponse(paypalOrder);
			log.info("Converted OrderResponse: {}", orderResponse);
			
			// If we get a valid response with PAYER_ACTION_REQUIRED status & url & id, then only its success else its failed.
			if(orderResponse != null 
					&& orderResponse.getOrderId() != null
					&& !orderResponse.getOrderId().isEmpty()
					&& orderResponse.getPaypalStatus() != null
					&& orderResponse.getPaypalStatus().equalsIgnoreCase(
							Constant.PAYER_ACTION_REQUIRED)
					&& orderResponse.getRedirectUrl() != null
					&& !orderResponse.getRedirectUrl().isEmpty()) {
				log.info("Order created successfully with PAYER_ACTION_REQUIRED status");
				return orderResponse;
			}
			
			log.error("Order creation failed or incomplete details received. "
					+ "orderResponse: {}", orderResponse);
			
		}
		
		// if 4xx or 5xx then proper error
		if(httpResponse.getStatusCode().is4xxClientError() 
				|| httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Received 4xx, 5xx error response from PayPal service");
			
			PaypalErrorResponse paypalErrorRes = jsonUtil.fromJson(
					httpResponse.getBody(), PaypalErrorResponse.class);
			log.info("PayPal error response details: {}", paypalErrorRes);
			
			String errorCode = ErrorCodeEnum.PAYPAL_ERROR.getErrorCode();
			String errorMessage = PaypalOrderUtil.getPaypalErrorSummary(
					paypalErrorRes);
			log.info("Generated PayPal error summary: {}", errorMessage);
			
			throw new PaypalProviderException(
					errorCode,
					errorMessage,
					HttpStatus.valueOf(
							httpResponse.getStatusCode().value()));
		}
		

		log.error("Unexpected response from PayPal service. "
				+ "httpResponse: {}", httpResponse);
		
		throw new PaypalProviderException(
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorCode(),
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorMessage(),
				HttpStatus.BAD_GATEWAY);
	}*/

}

