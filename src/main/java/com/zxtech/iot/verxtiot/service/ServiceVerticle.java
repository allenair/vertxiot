package com.zxtech.iot.verxtiot.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;
import com.zxtech.iot.verxtiot.common.LoadConfig;
import com.zxtech.iot.verxtiot.common.RedisUtil;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;

public class ServiceVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(ServiceVerticle.class);
			
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
	@Override
	public void start() throws Exception {
		logger.info("=====Service verticel start======");
		this.init();
		
		EventBus ebus = vertx.eventBus();
		ebus.consumer("/ftdata").toFlowable().subscribe(msg ->{
			ftService.handler(getBeanByJson(msg.body().toString()));
		});
		ebus.consumer("/eldata").toFlowable().subscribe(msg ->{
			Single<String> s = vertx.<String>rxExecuteBlocking(handler->{
				elService.handler(getBeanByJson(msg.body().toString()));
			}, false);
			
			s.subscribe();
		});
	}
	
	private TransferElevatorParameter getBeanByJson(String msg) {
		TransferElevatorParameter parameter = new TransferElevatorParameter();
		if (StringUtils.isNotBlank(msg)) {
			JsonObject page = new JsonObject(msg);
			parameter = new TransferElevatorParameter(page);
		}
		return parameter;
	}
}
