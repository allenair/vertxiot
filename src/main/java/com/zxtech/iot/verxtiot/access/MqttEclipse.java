package com.zxtech.iot.verxtiot.access;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class MqttEclipse extends BaseVerticle {
	private static final Logger logger = LoggerFactory.getLogger(MqttEclipse.class);
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.init();
		logger.info("=====Eclipse mqtt verticel start======");
		ftsub("base-id-ft", "/iotdata/ft/+");  
		elsub("base-id-el", "/iotdata/el/+");  
	}
	
	private MqttClient connect(String clientId) throws MqttException{  
        MemoryPersistence persistence = new MemoryPersistence();  
        MqttConnectOptions connOpts = new MqttConnectOptions();  
        connOpts.setCleanSession(false);  
        connOpts.setConnectionTimeout(10);  
        connOpts.setKeepAliveInterval(20);  
        MqttClient mqttClient = new MqttClient("tcp://192.168.0.214:1883", clientId, persistence);  
//        MqttClient mqttClient = new MqttClient("tcp://localhost:1883", clientId, persistence);  
        mqttClient.connect(connOpts);  
        return mqttClient;  
    }  
      
    private void ftsub(String clientId, String topic) throws MqttException{  
        MqttClient mqttClient = connect(clientId);  
        if(mqttClient != null){  
        	mqttClient.subscribe(topic, 0, (top, msg)->{
              	this.ftData(this.getBeanByJson(new String(msg.getPayload(),"UTF-8")));
               });
        }  
    }  
    private void elsub(String clientId, String topic) throws MqttException{  
        MqttClient mqttClient = connect(clientId);  
        if(mqttClient != null){  
        	mqttClient.subscribe(topic, 0, (top, msg)->{
              	this.elData(this.getBeanByJson(new String(msg.getPayload(),"UTF-8")));
               });
        }  
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
