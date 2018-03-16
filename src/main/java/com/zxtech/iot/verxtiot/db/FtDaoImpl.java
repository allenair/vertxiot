package com.zxtech.iot.verxtiot.db;

import java.time.Instant;
import java.util.Map;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;
import com.zxtech.iot.verxtiot.common.RedisUtil;

import io.reactivex.Completable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;

public class FtDaoImpl {
	private final JDBCClient jdbcClient;
	private final Map<String, String> sqlMap;
	
	public FtDaoImpl(Vertx vertx, JsonObject config, Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
		this.jdbcClient = JDBCClient.createShared(vertx, config);
	}
	
	public Completable insertCollectDb(TransferElevatorParameter parameter) {
		JsonArray array = new JsonArray();
		array.add(Instant.now());
		array.add(parameter.getElevatorId());
		array.add(parameter.getParameterStr());
		array.add(parameter.getTime());
		
		return jdbcClient.rxUpdateWithParams(sqlMap.get("up_hard_collection_ft#insert"), array).toCompletable();
	}

	public Completable insertAnalysisDb(JsonObject analyBean) {
		RedisUtil.set("hard-analy:" + analyBean.getString("elevator_code"), analyBean.encode(), 1800);

		JsonArray array = new JsonArray();
		array.add(Instant.now());
		array.add(analyBean.getString("elevator_code"));
		array.add(analyBean.getString("error_flag"));
		array.add(analyBean.getString("up"));
		array.add(analyBean.getString("down"));
		array.add(analyBean.getString("stop_flag"));
		array.add(analyBean.getString("chk_flag"));
		array.add(analyBean.getString("sud_stop"));
		array.add(analyBean.getString("high_speed"));
		array.add(analyBean.getString("low_speed"));
		array.add(analyBean.getString("work_freq"));
		array.add(analyBean.getString("chg_freq"));
		array.add(analyBean.getString("star_type"));
		array.add(analyBean.getString("tria_type"));
		array.add(analyBean.getString("self_start"));
		array.add(analyBean.getString("m01"));
		array.add(analyBean.getString("m02"));
		array.add(analyBean.getString("m03"));
		array.add(analyBean.getString("m04"));
		array.add(analyBean.getString("m05"));
		array.add(analyBean.getString("m06"));
		array.add(analyBean.getString("m07"));
		array.add(analyBean.getString("m08"));
		array.add(analyBean.getString("m09"));
		array.add(analyBean.getString("j_flg"));
		array.add(analyBean.getString("n1"));
		array.add(analyBean.getString("n3"));
		array.add(analyBean.getInteger("run_speed"));
		array.add(analyBean.getInteger("left_hand_speed"));
		array.add(analyBean.getInteger("right_hand_speed"));
		array.add(analyBean.getInteger("error_code"));
		array.add(analyBean.getInteger("run_time"));
		array.add(analyBean.getString("all_data"));

		return jdbcClient.rxUpdateWithParams(sqlMap.get("up_hard_analysis_ft#insert"), array).toCompletable();
	}
}
