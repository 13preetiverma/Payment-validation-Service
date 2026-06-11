package com.mycomp.validation.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data

public class CompletePaymentRes {

	private String txnReference;
	private int txnStatusId;
	
}
