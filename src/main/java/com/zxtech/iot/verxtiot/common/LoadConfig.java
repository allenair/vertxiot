package com.zxtech.iot.verxtiot.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import io.vertx.core.json.JsonObject;

public class LoadConfig {
	public JsonObject loadConfig() throws IOException {
		JsonObject json = new JsonObject();
		InputStream queriesInputStream = getClass().getResourceAsStream("/config.properties");

		Properties queriesProps = new Properties();
		queriesProps.load(queriesInputStream);
		queriesInputStream.close();

		queriesProps.forEach((key, val)->{
			json.put(key.toString(), val.toString());
		});
		
		json.put("http.server.port", Integer.parseInt(json.getString("http.server.port")));
		json.put("max_pool_size", Integer.parseInt(json.getString("max_pool_size")));
		json.put("web.stub.port", Integer.parseInt(json.getString("web.stub.port")));

		return json;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, String> loadSqlMap() throws Exception {
		Map<String, String> sqlMap = new HashMap<>();
		
		Document doc = null;
		SAXReader read = new SAXReader();
		doc = read.read(getClass().getResourceAsStream("/sql.xml"));

		Element root = doc.getRootElement();
		String key;
//		String nameSpace = root.attributeValue("namespace");
		
		for (Iterator<Element> element = root.elementIterator(); element.hasNext();) {
			Element sql = element.next();
			if ("sqlElement".equals(sql.getName())) {
				key = sql.attribute("key").getValue();
				sqlMap.put(key, sql.getText());
			}
		}
		
		return sqlMap;
	}
}
