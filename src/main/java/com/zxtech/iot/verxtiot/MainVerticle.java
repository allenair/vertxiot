package com.zxtech.iot.verxtiot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.http.HttpServerVerticle;

import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		logger.info("---START---");
		vertx.rxDeployVerticle(HttpServerVerticle.class.getName()).subscribe(id -> {
			startFuture.complete();
			logger.info("---DEPLOYED---");
		}, startFuture::fail);
	}
}
