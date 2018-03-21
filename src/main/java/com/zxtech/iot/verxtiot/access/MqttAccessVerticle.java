package com.zxtech.iot.verxtiot.access;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.mqtt.MqttClient;
import io.vertx.reactivex.mqtt.messages.MqttPublishMessage;

public class MqttAccessVerticle extends BaseVerticle {
	private static final Logger logger = LoggerFactory.getLogger(MqttAccessVerticle.class);

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.init();
		MqttClient mqttClientFt = MqttClient.create(vertx);
		MqttClient mqttClientEl = MqttClient.create(vertx);

		
		mqttClientFt.publishHandler(message -> {
			this.ftData(this.getBeanByJson(message));
		});
		mqttClientFt.connect(jsonConfig.getInteger("mqtt.broker.port"), jsonConfig.getString("mqtt.broker.host"),
				ch -> {
					if (ch.succeeded()) {
						mqttClientFt.subscribe(jsonConfig.getString("mqtt.ftdata.topic"), 0);

					} else {
						logger.error("FT Failed to connect to a server");
					}
				});
		mqttClientFt.closeHandler(e->{
			logger.error(">>>>>>>>>>>>>>>>>>Close!!!!"+e.toString());
		});
		mqttClientFt.exceptionHandler(e->{
			logger.error(">>>>>>>>>>>>>>>>>>ERROR!!!!"+e.getMessage());
		});
		
		mqttClientEl.publishHandler(message -> {
			this.elData(this.getBeanByJson(message));
		});
		mqttClientEl.connect(jsonConfig.getInteger("mqtt.broker.port"), jsonConfig.getString("mqtt.broker.host"),
				ch -> {
					if (ch.succeeded()) {
						mqttClientEl.subscribe(jsonConfig.getString("mqtt.eldata.topic"), 0);

					} else {
						logger.error("EL Failed to connect to a server");
					}
				});
	}

	private TransferElevatorParameter getBeanByJson(MqttPublishMessage message) {
		TransferElevatorParameter parameter = new TransferElevatorParameter();
		String msg = message.payload().toString();
		if (StringUtils.isNotBlank(msg)) {
			JsonObject page = new JsonObject(msg);
			parameter = new TransferElevatorParameter(page);
		}
		return parameter;
	}

}
