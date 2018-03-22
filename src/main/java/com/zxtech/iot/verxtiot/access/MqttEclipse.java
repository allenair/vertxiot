package com.zxtech.iot.verxtiot.access;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;

import io.vertx.core.json.JsonObject;

public class MqttEclipse extends BaseVerticle {
	private static final Logger logger = LoggerFactory.getLogger(MqttEclipse.class);
	
	@Override
	public void start() throws Exception {
		super.init();
		logger.info("=====Eclipse mqtt verticel start======");
//		ftsub("base-id-ft", "/iotdata/ft/+");  
//		elsub("base-id-el", "/iotdata/el/+");  
		runsub("base-id-client", new String[]{"/iotdata/ft/+","/iotdata/el/+"});
	}
	
	private MqttClient connect(String clientId) throws MqttException{  
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(false);
		connOpts.setConnectionTimeout(10);
		connOpts.setKeepAliveInterval(20);
		MqttClient mqttClient = new MqttClient(
				"tcp://" + jsonConfig.getString("mqtt.broker.host") + ":" + jsonConfig.getInteger("mqtt.broker.port"),
				clientId, new MemoryPersistence());
		mqttClient.connect(connOpts);
		return mqttClient;
    }  
      
//    private void ftsub(String clientId, String topic) throws MqttException{  
//        MqttClient mqttClient = connect(clientId);  
//        if(mqttClient != null){  
//        	mqttClient.subscribe(topic, 0, (top, msg)->{
//              	this.ftData(this.getBeanByJson(new String(msg.getPayload(),"UTF-8")));
//               });
//        }  
//    }  
//    private void elsub(String clientId, String topic) throws MqttException{  
//        MqttClient mqttClient = connect(clientId);  
//        if(mqttClient != null){  
//        	mqttClient.subscribe(topic, 0, (top, msg)->{
//              	this.elData(this.getBeanByJson(new String(msg.getPayload(),"UTF-8")));
//               });
//        }  
//    }  
    
    private void runsub(String clientId, String[] topic) throws MqttException{  
    	int[] qos= {0,0};
        MqttClient mqttClient = connect(clientId);  
        IMqttMessageListener ftlis = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				ftData(getBeanByJson(new String(message.getPayload(),"UTF-8")));
//				sendByEventBus("/ftdata", new String(message.getPayload(),"UTF-8"));
			}
		};
		IMqttMessageListener ellis = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				elData(getBeanByJson(new String(message.getPayload(),"UTF-8")));
//				sendByEventBus("/eldata", new String(message.getPayload(),"UTF-8"));
			}
		};
		IMqttMessageListener[] listArr = {ftlis, ellis};
        if(mqttClient != null){  
        	mqttClient.subscribe(topic, qos, listArr);
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
    
    private void sendByEventBus(String address, String msgBody) {
//    	vertx.eventBus().rxSend(type, msgBody).subscribe();
    	vertx.eventBus().publish(address, msgBody);
    }
    
}
