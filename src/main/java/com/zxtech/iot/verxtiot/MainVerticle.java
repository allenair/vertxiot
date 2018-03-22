package com.zxtech.iot.verxtiot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.access.HttpAccessVerticle;
import com.zxtech.iot.verxtiot.access.MqttAccessVerticle;
import com.zxtech.iot.verxtiot.access.MqttEclipse;
import com.zxtech.iot.verxtiot.service.ServiceVerticle;

import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		logger.info("---START---");
		Single<String> httpDep = vertx.rxDeployVerticle(HttpAccessVerticle.class.getName());
//		Single<String> mqttDep = vertx.rxDeployVerticle(MqttAccessVerticle.class.getName());
//		Single<String> mqttDep = vertx.rxDeployVerticle(MqttEclipse.class.getName());
		
//		Single<String> serviceDep = vertx.rxDeployVerticle(ServiceVerticle.class.getName());
		
//		Single<String> httpDep = Single.<String>just("11");
		Single<String> mqttDep = Single.<String>just("11");
		Single<String> serviceDep = Single.<String>just("11");

		httpDep.flatMap(id -> {
			return mqttDep;
		}).flatMap(id->{
			return serviceDep;
		}).subscribe(id -> {
			startFuture.complete();
			logger.info("---DEPLOYED---");
		}, startFuture::fail);
	}
}
