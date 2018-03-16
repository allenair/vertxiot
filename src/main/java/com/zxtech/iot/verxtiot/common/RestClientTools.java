package com.zxtech.iot.verxtiot.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.WebClient;

public class RestClientTools {
	private static final Logger logger = LoggerFactory.getLogger(RestClientTools.class);
			
	private final JsonObject config;
	private final WebClient webClient;
	
	public RestClientTools(Vertx vertx, JsonObject config) {
		this.webClient = WebClient.create(vertx);
		this.config = config;
	}
	
	public void sendFtErrorInfo(JsonObject analyBean) {
		JsonObject json = new JsonObject();
		json.put("hardCode", analyBean.getString("elevator_code"));
		json.put("errorCode", analyBean.getInteger("error_code"));
		
		int port = this.config.getInteger("web.stub.port");
		String host = this.config.getString("web.stub.host");
		String requestURI = this.config.getString("web.stub.ft.err") ;
		
		HttpRequest<Buffer> req = this.webClient.post(port, host, requestURI);
		req.putHeader("Content-Type", "application/json;charset=UTF-8").rxSendJsonObject(json).subscribe(resp -> {
			if(resp.statusCode()!=200) {
				logger.error("Server is Error!!! statusCode is {}, content is {}", resp.statusCode(), resp.bodyAsString());
			}
		}, err -> {
			logger.error("Server is Error!!! err is {}", err.getMessage());
		});
	}
	
	public void sendElErrorInfo(String hardCode, String errorCode) {
		JsonObject json = new JsonObject();
		json.put("hardCode", hardCode);
		json.put("errorCode", errorCode);
		
		int port = this.config.getInteger("web.stub.port");
		String host = this.config.getString("web.stub.host");
		String requestURI = this.config.getString("web.stub.el.err") ;
		
		HttpRequest<Buffer> req = this.webClient.post(port, host, requestURI);
		req.putHeader("Content-Type", "application/json;charset=UTF-8").rxSendJsonObject(json).subscribe(resp -> {
			if(resp.statusCode()!=200) {
				logger.error("Server is Error!!! statusCode is {}, content is {}", resp.statusCode(), resp.bodyAsString());
			}
		}, err -> {
			logger.error("Server is Error!!! err is {}", err.getMessage());
		});
	}
	
	public void callElFix(String hardCode, String errorDescript, String peopleFlag) {
		JsonObject json = new JsonObject();
		json.put("hardCode", hardCode);
		json.put("errorDescript", errorDescript);
		json.put("peopleFlag", peopleFlag);
		
		int port = this.config.getInteger("web.stub.port");
		String host = this.config.getString("web.stub.host");
		String requestURI = this.config.getString("web.stub.el.callfix") ;
		
		HttpRequest<Buffer> req = this.webClient.post(port, host, requestURI);
		req.putHeader("Content-Type", "application/json;charset=UTF-8").rxSendJsonObject(json).subscribe(resp -> {
			if(resp.statusCode()!=200) {
				logger.error("Server is Error!!! statusCode is {}, content is {}", resp.statusCode(), resp.bodyAsString());
			}
		}, err -> {
			logger.error("Server is Error!!! err is {}", err.getMessage());
		});
	}
}
