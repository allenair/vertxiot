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

public class ElevatorDaoImpl {
	private final JDBCClient jdbcClient;
	private final Map<String, String> sqlMap;

	public ElevatorDaoImpl(Vertx vertx, JsonObject config, Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
		this.jdbcClient = JDBCClient.createShared(vertx, config);
	}

	public Completable insertCollectDb(TransferElevatorParameter parameter) {
		JsonArray array = new JsonArray();
		array.add(Instant.now());
		array.add(parameter.getElevatorId());
		array.add(parameter.getParameterStr());
		array.add(parameter.getTime());
		array.add(parameter.getElectric());
		array.add(parameter.getPeople());
		array.add(parameter.getRoomElectric());
		array.add(parameter.getRoomMaintain());
		array.add(parameter.getTopElectric());
		array.add(parameter.getTopMaintain());
		array.add(parameter.getAlarm());
		array.add(parameter.getErrInfo());

		return jdbcClient.rxUpdateWithParams(sqlMap.get("up_hard_collection#insert"), array).toCompletable();
	}

	public Completable insertAnalysisDb(JsonObject analyBean) {
		RedisUtil.set("hard-analy:" + analyBean.getString("elevator_code"), analyBean.encode(), 1800);

		JsonArray array = new JsonArray();
		array.add(Instant.now());
		array.add(analyBean.getString("elevator_code"));
		array.add(analyBean.getString("err"));
		array.add(analyBean.getString("nav"));
		array.add(analyBean.getString("ins"));
		array.add(analyBean.getString("run"));
		array.add(analyBean.getString("do_p"));
		array.add(analyBean.getString("dol"));
		array.add(analyBean.getString("dw"));
		array.add(analyBean.getString("dcl"));
		array.add(analyBean.getString("dz"));
		array.add(analyBean.getString("efo"));
		array.add(analyBean.getString("cb"));
		array.add(analyBean.getString("up"));
		array.add(analyBean.getString("down"));
		array.add(analyBean.getInteger("fl"));
		array.add(analyBean.getInteger("cnt"));
		array.add(analyBean.getInteger("ddfw"));
		array.add(analyBean.getInteger("hxxh"));
		array.add(analyBean.getString("es"));
		array.add(analyBean.getString("se"));
		array.add(analyBean.getString("dfc"));
		array.add(analyBean.getString("tci"));
		array.add(analyBean.getString("ero"));
		array.add(analyBean.getString("lv1"));
		array.add(analyBean.getString("lv2"));
		array.add(analyBean.getString("ls1"));
		array.add(analyBean.getString("ls2"));
		array.add(analyBean.getString("dob"));
		array.add(analyBean.getString("dcb"));
		array.add(analyBean.getString("lrd"));
		array.add(analyBean.getString("dos"));
		array.add(analyBean.getString("efk"));
		array.add(analyBean.getString("pks"));
		array.add(analyBean.getString("rdol"));
		array.add(analyBean.getString("rdcl"));
		array.add(analyBean.getString("rdob"));
		array.add(analyBean.getString("rdcb"));
		array.add(analyBean.getString("others"));
		array.add(analyBean.getString("electric_flag"));
		array.add(analyBean.getString("people_flag"));
		array.add(analyBean.getString("room_electric_flag"));
		array.add(analyBean.getString("room_maintain_flag"));
		array.add(analyBean.getString("top_electric_flag"));
		array.add(analyBean.getString("top_maintain_flag"));
		array.add(analyBean.getString("alarm"));
		array.add(analyBean.getString("maintenance"));
		array.add(analyBean.getString("show_fl"));
		array.add(analyBean.getString("err_info"));
		array.add(analyBean.getString("board_type"));
		array.add(analyBean.getString("rear_en"));
		array.add(analyBean.getString("rdoo"));
		array.add(analyBean.getString("logic_err"));
		array.add(analyBean.getString("show_left"));
		array.add(analyBean.getString("show_right"));
		array.add(analyBean.getInteger("last_count"));
		array.add(analyBean.getInteger("total_time"));
		array.add(analyBean.getString("driver_err"));
		array.add(analyBean.getString("logic_lock"));
		array.add(analyBean.getString("sys_model"));
		array.add(analyBean.getInteger("xh_time"));
		array.add(analyBean.getInteger("arm_code"));
		array.add(analyBean.getInteger("dsp_code"));
		array.add(analyBean.getString("ver_code"));
		array.add(analyBean.getString("safe_circle"));
		array.add(analyBean.getString("open_fault"));
		array.add(analyBean.getString("close_fault"));
		array.add(analyBean.getString("up_switch"));
		array.add(analyBean.getString("down_switch"));
		array.add(analyBean.getString("stop_fault"));
		array.add(analyBean.getString("lock_broken"));
		array.add(analyBean.getString("speed_fault"));
		array.add(analyBean.getString("go_top"));
		array.add(analyBean.getString("go_down"));
		array.add(analyBean.getString("driver_fault"));
		array.add(analyBean.getString("logic_fault"));
		array.add(analyBean.getString("logic_status"));

		return jdbcClient.rxUpdateWithParams(sqlMap.get("up_hard_analysis#insert"), array).toCompletable();
	}
}
