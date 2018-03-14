package com.zxtech.iot.verxtiot.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;
import com.zxtech.iot.verxtiot.common.IotParseTools;
import com.zxtech.iot.verxtiot.common.LoadConfig;
import com.zxtech.iot.verxtiot.common.RedisUtil;
import com.zxtech.iot.verxtiot.service.ElevatorServiceImpl;
import com.zxtech.iot.verxtiot.service.FtServiceImpl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class HttpServerVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

	private ElevatorServiceImpl elService;
	private FtServiceImpl ftService;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		JsonObject jsonConfig = serverInit();
		Router router = registerRouter();

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).rxListen(jsonConfig.getInteger("http.server.port")).subscribe(s -> {
			logger.info("HTTP server running on port {}", jsonConfig.getInteger("http.server.port"));
			startFuture.complete();
		}, t -> {
			logger.error("Could not start a HTTP server", t);
			startFuture.fail(t);
		});
	}

	private JsonObject serverInit() throws Exception{
		LoadConfig loc = new LoadConfig();
		JsonObject jsonConfig = loc.loadConfig();
		Map<String, String>  sqlMap = loc.loadSqlMap();
		
		logger.info(jsonConfig.encodePrettily());
		sqlMap.keySet().forEach(logger::info);
		
		elService = new ElevatorServiceImpl(vertx, jsonConfig, sqlMap);
		ftService = new FtServiceImpl(vertx, jsonConfig, sqlMap);
		
		RedisUtil.initCache(jsonConfig.getString("redis.cache.host"));
		return jsonConfig;
	}
	
	private Router registerRouter() {
		Router router = Router.router(vertx);
		router.get("/").handler(this::showIndex);
		router.get("/essiot").handler(this::showIndex);
		router.post().handler(BodyHandler.create());
		router.post("/essiot/ftiotdata.io").handler(this::ftData);
		router.post("/essiot/eliotdata.io").handler(this::elData);
		
		return router;
	}
	
	private void ftData(RoutingContext context) {
		if(context.getBody().length()<1) {
			apiResponse(context, 400, "result", "ERR: wrong request");
			return;
		}
		
		JsonObject page = context.getBodyAsJson();
		TransferElevatorParameter parameter = new TransferElevatorParameter(page);
		if(IotParseTools.checkFtParameter(parameter)) {
			// 如果接口检查正确则直接返回ok，后续错误与接口数据无关
			apiResponse(context, 200, "result", "OK");
			
			ftService.handler(parameter);
		}else {
			apiResponse(context, 400, "result", "ERR: wrong parameter format");
		}
	}

	private void elData(RoutingContext context) {
		JsonObject page = context.getBodyAsJson();
//		sendMessage(context, this.SERVICE_QUEUE, page, "el-data");
	}
	
	private void showIndex(RoutingContext context) {
		apiResponse(context, 200, "result", "你好！");
	}

	private void apiResponse(RoutingContext context, int statusCode, String jsonField, String jsonData) {
		HttpServerResponse resp = context.response();
		resp.setStatusCode(statusCode);
		resp.putHeader("Content-Type", "application/json;charset=utf-8");
		resp.end(new JsonObject().put(jsonField, jsonData).encode());
		resp.close();
	}
	
}
