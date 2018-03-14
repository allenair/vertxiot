package com.zxtech.iot.verxtiot.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;
import com.zxtech.iot.verxtiot.common.IotParseTools;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;

public class ElevatorServiceImpl {
	private static final Logger logger = LoggerFactory.getLogger(ElevatorServiceImpl.class);
	
	private final Vertx vertx;
	private final JsonObject config;
	private final JDBCClient client;
	private final Map<String, String> sqlMap;
	
	public ElevatorServiceImpl(Vertx vertx, JsonObject config, Map<String, String> sqlMap) {
		this.vertx = vertx;
	    this.config = config;
	    this.sqlMap = sqlMap;
	    this.client = JDBCClient.createShared(vertx, config);
	}
	
	public void handle(TransferElevatorParameter parameter) throws Throwable {
		if (IotParseTools.checkElParameter(parameter)) {
			
		}else {
			throw new Throwable("Parameter is wrong!");
		}
			
	}
}
