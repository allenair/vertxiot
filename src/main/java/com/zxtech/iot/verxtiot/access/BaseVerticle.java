package com.zxtech.iot.verxtiot.access;

import java.util.Map;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;
import com.zxtech.iot.verxtiot.common.IotParseTools;
import com.zxtech.iot.verxtiot.common.LoadConfig;
import com.zxtech.iot.verxtiot.common.RedisUtil;
import com.zxtech.iot.verxtiot.service.ElevatorServiceImpl;
import com.zxtech.iot.verxtiot.service.FtServiceImpl;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;

public abstract class BaseVerticle extends AbstractVerticle {
	protected ElevatorServiceImpl elService;
	protected FtServiceImpl ftService;
	protected JsonObject jsonConfig;
	
	public void init() throws Exception {
		LoadConfig loc = new LoadConfig();
		jsonConfig = loc.loadConfig();
		Map<String, String>  sqlMap = loc.loadSqlMap();
		
		elService = new ElevatorServiceImpl(vertx, jsonConfig, sqlMap);
		ftService = new FtServiceImpl(vertx, jsonConfig, sqlMap);
		
		RedisUtil.initCache(jsonConfig.getString("redis.cache.host"));
	}
	
	protected JsonObject ftData(TransferElevatorParameter parameter) {
		if(IotParseTools.checkFtParameter(parameter)) {
			ftService.handler(parameter);
			vertx.<String>rxExecuteBlocking(handler->{
				ftService.handler(parameter);
			}, false).subscribe();
			
		}else {
			return parameterWrongResponse();
		}
		
		return okResponse();
	}

	protected JsonObject elData(TransferElevatorParameter parameter) {
		if(IotParseTools.checkElParameter(parameter)) {
			elService.handler(parameter);
			vertx.<String>rxExecuteBlocking(handler->{
				elService.handler(parameter);
			}, false).subscribe();
			
		}else {
			return parameterWrongResponse();
		}
		
		return okResponse();
	}
	
	protected JsonObject postWrongResponse() {
		JsonObject json = new JsonObject();
		json.put("code", 400);
		json.put("msg", "{\"result\":\"ERR: wrong request\"}");
		return json;
	}
	
	private JsonObject okResponse() {
		return this.okResponse("OK");
	}
	protected JsonObject okResponse(String msg) {
		JsonObject json = new JsonObject();
		json.put("code", 200);
		json.put("msg", "{\"result\":\""+msg+"\"}");
		return json;
	}
	
	protected JsonObject parameterWrongResponse() {
		JsonObject json = new JsonObject();
		json.put("code", 400);
		json.put("msg", "{\"result\":\"ERR: wrong parameter format\"}");
		return json;
	}
}
