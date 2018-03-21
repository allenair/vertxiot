package com.zxtech.iot.verxtiot.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class HttpAccessVerticle extends BaseVerticle {
	private static final Logger logger = LoggerFactory.getLogger(HttpAccessVerticle.class);

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.init();
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

	private Router registerRouter() {
		Router router = Router.router(vertx);
		router.get("/").handler(this::showIndex);
		router.get("/essiot").handler(this::showIndex);
		router.post().handler(BodyHandler.create());
		
		router.post("/essiot/ftiotdata.io").handler(context->{
			if(context.getBody().length()<1) {
				this.sendResponse(context, this.postWrongResponse());
			}
			
			JsonObject page = context.getBodyAsJson();
			TransferElevatorParameter parameter = new TransferElevatorParameter(page);
			JsonObject resObj = this.ftData(parameter);
			
			this.sendResponse(context, resObj);
		});
		
		router.post("/essiot/eliotdata.io").handler(context->{
			if(context.getBody().length()<1) {
				this.sendResponse(context, this.postWrongResponse());
			}
			
			JsonObject page = context.getBodyAsJson();
			TransferElevatorParameter parameter = new TransferElevatorParameter(page);
			JsonObject resObj = this.elData(parameter);
			
			this.sendResponse(context, resObj);
		});

		return router;
	}

	private void showIndex(RoutingContext context) {
		sendResponse(context, this.okResponse("你好，欢迎访问ESSIOT！"));
	}

	private void sendResponse(RoutingContext context, JsonObject json) {
		HttpServerResponse resp = context.response();
		resp.setStatusCode(json.getInteger("code"));
		resp.putHeader("Content-Type", "application/json;charset=utf-8");
		resp.end(json.getString("msg"));
		resp.close();
	}
}
