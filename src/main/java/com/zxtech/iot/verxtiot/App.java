package com.zxtech.iot.verxtiot;

import io.vertx.reactivex.core.Vertx;

public class App {
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.rxDeployVerticle(MainVerticle.class.getName()).subscribe();
	}

}
