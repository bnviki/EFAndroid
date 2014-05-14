package com.mpeers.utils;

public class RestResponse {
	public String message;
	public int responseCode;
	public String response;
	
	public RestResponse(int responseCode, String response, String message){
		this.message = message;
		this.responseCode = responseCode;
		this.response = response;
	}
	
	public RestResponse(){
		
	}
}
