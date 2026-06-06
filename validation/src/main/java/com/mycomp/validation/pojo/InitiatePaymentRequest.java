package com.mycomp.validation.pojo;

import lombok.Data;

@Data
public class InitiatePaymentRequest {
	private String successUrl; 
	private String cancelUrl; 
}
