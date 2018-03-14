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

		json.put("http.server.port", Integer.parseInt(queriesProps.getProperty("http.server.port")));

		json.put("driver_class", queriesProps.getProperty("jdbc.driver_class"));
		json.put("url", queriesProps.getProperty("jdbc.url"));
		json.put("user", queriesProps.getProperty("jdbc.username"));
		json.put("password", queriesProps.getProperty("jdbc.password"));
		json.put("max_pool_size", Integer.parseInt(queriesProps.getProperty("jdbc.max_pool_size")));

		json.put("redis.cache.host", queriesProps.getProperty("redis.cache.host"));
		
		json.put("web.stub.host", queriesProps.getProperty("web.stub.host"));
		json.put("web.stub.port", Integer.parseInt(queriesProps.getProperty("web.stub.port")));
		json.put("web.stub.ft.err", queriesProps.getProperty("web.stub.ft.err"));

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
