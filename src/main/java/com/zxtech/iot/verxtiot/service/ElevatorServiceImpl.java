package com.zxtech.iot.verxtiot.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;
import com.zxtech.iot.verxtiot.common.IotParseTools;
import com.zxtech.iot.verxtiot.common.RedisUtil;
import com.zxtech.iot.verxtiot.common.RestClientTools;
import com.zxtech.iot.verxtiot.db.ElevatorDaoImpl;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public class ElevatorServiceImpl {
	private static final Logger logger = LoggerFactory.getLogger(ElevatorServiceImpl.class);
	private final RestClientTools restApiClient;
	private final ElevatorDaoImpl elevatorDao;
	private static HashMap<String, String> errorDescriptMap = new HashMap<>();
	
	public ElevatorServiceImpl(Vertx vertx, JsonObject config, Map<String, String> sqlMap) {
	    this.restApiClient = new RestClientTools(vertx, config);
		this.elevatorDao = new ElevatorDaoImpl(vertx, config, sqlMap);
		
		errorDescriptMap.put("101", "门区外停车困人");
		errorDescriptMap.put("102", "冲顶困人");
		errorDescriptMap.put("103", "蹲底困人");
		errorDescriptMap.put("104", "运行中开门困人");
		errorDescriptMap.put("105", "超速困人");
		errorDescriptMap.put("106", "轿厢意外移动困人");
		errorDescriptMap.put("107", "门锁回路断路困人");
		errorDescriptMap.put("108", "停电困人");
		errorDescriptMap.put("109", "报警困人");
		
		errorDescriptMap.put("201", "门区外停车");
		errorDescriptMap.put("202", "电梯冲顶");
		errorDescriptMap.put("203", "电梯蹲底");
		errorDescriptMap.put("204", "运行中开门");
		errorDescriptMap.put("205", "电梯速度异常");
		errorDescriptMap.put("206", "轿厢意外移动");
		errorDescriptMap.put("207", "门锁回路断路");
		errorDescriptMap.put("208", "电梯停电");
		errorDescriptMap.put("209", "制动力检测警告");
		errorDescriptMap.put("210", "制动力检测故障");
		errorDescriptMap.put("211", "平层光电开关故障");
		errorDescriptMap.put("212", "1LV 光电故障");
		errorDescriptMap.put("213", "2LV 光电故障");
//		errorDescriptMap.put("214", "制动单元故障");
//		errorDescriptMap.put("215", "变频器功率读取失败");
		errorDescriptMap.put("216", "综合故障");
		errorDescriptMap.put("217", "开门故障");
		errorDescriptMap.put("218", "关门故障");
		errorDescriptMap.put("219", "主机热敏开关动作");
		errorDescriptMap.put("220", "电梯频繁复位");
		errorDescriptMap.put("221", "门锁短接");
		errorDescriptMap.put("222", "强迫减速丢失");
		errorDescriptMap.put("223", "上下强减动作");
		errorDescriptMap.put("224", "安全开关动作");
	}
	
	public void handler(TransferElevatorParameter parameter){
		JsonObject analyBean = IotParseTools.getAnalysisElBean(parameter);

//		Completable coldb = elevatorDao.insertCollectDb(parameter);
//		Completable analydb = elevatorDao.insertAnalysisDb(analyBean);
//		coldb.andThen(analydb).subscribe(() -> {
//			dealData(analyBean);
//		}, err -> {
//			logger.error("EL:{}, Some Errors happen {}", parameter.getElevatorId(), err.getMessage());
//		});
		
		logger.info("EL=Data>> "+parameter.toJson().encode());
		dealData(analyBean);
	}
	
	private void dealData(JsonObject analyBean) {
		// 不是维修状态才进行预警判断
		if("0".equals(analyBean.getString("room_maintain_flag"))){
			JsonObject lastAnalyBean=null;
			String jsonAny = RedisUtil.get("hard-analy:" + analyBean.getString("elevator_code"));
			if (StringUtils.isNotBlank(jsonAny)) {
				lastAnalyBean = new JsonObject(jsonAny);
			}
			
			analyBean.put("people_flag", checkPeopleFlag(analyBean));
			
			saveErrorInfo(analyBean, lastAnalyBean);
			
			List<String> errorCodeList = isSendAlarm(analyBean, lastAnalyBean);
			if(errorCodeList.size()>0){
				for (String errorCode : errorCodeList) {
//					restApiClient.callElFix(analyBean.getString("elevator_code"), errorDescriptMap.get(errorCode), analyBean.getString("people_flag"));
					RedisUtil.set("call-hard-code:" + analyBean.getString("elevator_code"), errorCode, 1800);
				}
			}else {
				RedisUtil.del("call-hard-code:" + analyBean.getString("elevator_code"));
			}
		}
		RedisUtil.set("hard-analy:" + analyBean.getString("elevator_code"), analyBean.encode(), 1800);
	}
	
	private String checkPeopleFlag(JsonObject analyBean) {
		String peopleFlag = analyBean.getString("people_flag");
		String p11 = analyBean.getString("do_p");
		String p12 = analyBean.getString("dol");
		String p15 = analyBean.getString("dz");

		String elevatorCode = analyBean.getString("elevator_code");
		// 初始化缓存中的是否有人标记
		String realPeopleFlag = RedisUtil.get("people_flag:" + elevatorCode);
		if(StringUtils.isEmpty(realPeopleFlag)) {
			realPeopleFlag = peopleFlag;
		}

		if ("0".equals(p11)) {
			if ("1".equals(peopleFlag)) {
				RedisUtil.set("people_flag:" + elevatorCode, "1");
				return "1";
			} 
			
		} else if ("1".equals(p11) && "1".equals(p12) && "1".equals(p15)) {
			RedisUtil.set("people_flag:" + elevatorCode, "0");
			return "0";
		}
		
		RedisUtil.set("people_flag:" + elevatorCode, realPeopleFlag);
		return realPeopleFlag;
	}
	
	private void saveErrorInfo(JsonObject analyBean, JsonObject lastAnalyBean) {
		List<String> errorCodeList = new ArrayList<>();

		// 上一条为null或者与本条不相同则记录
		if ("1".equals(analyBean.getString("nav"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("nav").equals(analyBean.getString("nav"))) {
				errorCodeList.add("P8");
			}
		}
		
		// P376 
		if ("1".equals(analyBean.getString("safe_circle"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("safe_circle").equals(analyBean.getString("safe_circle"))) {
				errorCodeList.add("P376");
			}
		}
		// P377
		if ("1".equals(analyBean.getString("open_fault"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("open_fault").equals(analyBean.getString("open_fault"))) {
				errorCodeList.add("P377");
			}
		}
		// P378
		if ("1".equals(analyBean.getString("close_fault"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("close_fault").equals(analyBean.getString("close_fault"))) {
				errorCodeList.add("P378");
			}
		}
		// P379
		if ("1".equals(analyBean.getString("up_switch"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("up_switch").equals(analyBean.getString("up_switch"))) {
				errorCodeList.add("P379");
			}
		}
		// P380
		if ("1".equals(analyBean.getString("down_switch"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("down_switch").equals(analyBean.getString("down_switch"))) {
				errorCodeList.add("P380");
			}
		}
		// P381
		if ("1".equals(analyBean.getString("stop_fault"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("stop_fault").equals(analyBean.getString("stop_fault"))) {
				errorCodeList.add("P381");
			}
		}
		// P382
		if ("1".equals(analyBean.getString("lock_broken"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("lock_broken").equals(analyBean.getString("lock_broken"))) {
				errorCodeList.add("P382");
			}
		}
		// P384
		if ("1".equals(analyBean.getString("speed_fault"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("speed_fault").equals(analyBean.getString("speed_fault"))) {
				errorCodeList.add("P384");
			}
		}
		// P386
		if ("1".equals(analyBean.getString("go_top"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("go_top").equals(analyBean.getString("go_top"))) {
				errorCodeList.add("P386");
			}
		}
		// P387
		if ("1".equals(analyBean.getString("go_down"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("go_down").equals(analyBean.getString("go_down"))) {
				errorCodeList.add("P387");
			}
		}
		// 驱动故障 E31~E99 与E220~E255
		if (!"0".equals(analyBean.getString("driver_fault"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("driver_fault").equals(analyBean.getString("driver_fault"))) {
				errorCodeList.add(analyBean.getString("driver_fault"));
			}
		}
		// 逻辑故障 E100~E150
		if (!"0".equals(analyBean.getString("logic_fault"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("logic_fault").equals(analyBean.getString("logic_fault"))) {
				errorCodeList.add(analyBean.getString("logic_fault"));
			}
		}
		// 逻辑状态 E151~E219
		if (!"0".equals(analyBean.getString("logic_status"))) {
			if(lastAnalyBean==null || !lastAnalyBean.getString("logic_status").equals(analyBean.getString("logic_status"))) {
				errorCodeList.add(analyBean.getString("logic_status"));
			}
		}

		if (errorCodeList.size() > 0) {
			for (String errorCode : errorCodeList) {
//				restApiClient.sendElErrorInfo(analyBean.getString("elevator_code"), errorCode);
			}
		}
	}
	
	private List<String> isSendAlarm(JsonObject analyBean, JsonObject lastAnalyBean){
		List<String> errorCodeList = new ArrayList<>();
		
		boolean flagP381_10 = isContinueSeconds(analyBean, "P381", analyBean.getString("stop_fault"), 10); // 101, 201
		boolean flagP386 = "1".equals(analyBean.getString("go_top")); // 102, 202
		boolean flagP387 = "1".equals(analyBean.getString("go_down")); // 103, 203
		boolean flagP382NotP386 = isNotShow(analyBean, "P382", analyBean.getString("lock_broken"), "P386", analyBean.getString("go_top"), 15); // 104, 204
		boolean flagP384NotP386 = isNotShow(analyBean, "P384", analyBean.getString("speed_fault"), "P386", analyBean.getString("go_top"), 10); // 105, 205
		boolean flagE107 = "E107".equalsIgnoreCase(analyBean.getString("logic_fault")); //106, 206
		boolean flagP382_10 = isContinueSeconds(analyBean, "P382", analyBean.getString("lock_broken"), 10); // 107, 207
		boolean flagElec = "0".equals(analyBean.getString("room_electric_flag")) && !"E157".equalsIgnoreCase(analyBean.getString("logic_status")); //108, 208
		boolean flagAlarm = isAlarmNew(analyBean, 10); // 109
		
		boolean flagE126 = "E126".equalsIgnoreCase(analyBean.getString("logic_fault")); // 209
		boolean flagE75 = "E75".equalsIgnoreCase(analyBean.getString("driver_fault")); // 209
		
		boolean flagE125 = "E125".equalsIgnoreCase(analyBean.getString("logic_fault")); // 210
		boolean flagE76 = "E76".equalsIgnoreCase(analyBean.getString("driver_fault")); // 210
		
		boolean flagE64 = "E64".equalsIgnoreCase(analyBean.getString("driver_fault")); //211
		boolean flagE46 = "E46".equalsIgnoreCase(analyBean.getString("driver_fault")); //212
		boolean flagE47 = "E47".equalsIgnoreCase(analyBean.getString("driver_fault")); //213
//		boolean flagP8 = "1".equalsIgnoreCase(analyBean.getNav()); //216
		boolean flagP8 = isContinueSeconds(analyBean, "P8", analyBean.getString("nav"), 20); // 216
		
		boolean flagP377_500 = isContinueSeconds(analyBean, "P377", analyBean.getString("open_fault"), 500); //217
		boolean flagP378_500 = isContinueSeconds(analyBean, "P378", analyBean.getString("close_fault"), 500); //218
		boolean flagE109 = isShowInTimescope219(analyBean, lastAnalyBean, 2*60*60); //219
		boolean flagE152 = isShowInTimescope220(analyBean, lastAnalyBean, 24*60*60); //220
		boolean flagE120 = "E120".equalsIgnoreCase(analyBean.getString("logic_fault")); // 221
		boolean flagE104 = "E104".equalsIgnoreCase(analyBean.getString("logic_fault")); // 222
		boolean flagE105 = "E105".equalsIgnoreCase(analyBean.getString("logic_fault")); // 223
		boolean flagP136 = "1".equalsIgnoreCase(analyBean.getString("es")); // 224
		
		
		if(flagP381_10) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("101");
			}else {
				errorCodeList.add("201");
			}
		}
		
		if(flagP386) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("102");
			}else {
				errorCodeList.add("202");
			}
		}
		
		if(flagP387) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("103");
			}else {
				errorCodeList.add("203");
			}
		}
		
		if(flagP382NotP386) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("104");
			}else {
				errorCodeList.add("204");
			}
		}
		
		if(flagP384NotP386) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("105");
			}else {
				errorCodeList.add("205");
			}
		}
		
		if(flagE107) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("106");
			}else {
				errorCodeList.add("206");
			}
		}
		
		if(flagP382_10) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("107");
			}else {
				errorCodeList.add("207");
			}
		}
		
		if(flagElec) {
			if("1".equals(analyBean.getString("people_flag"))) {
				errorCodeList.add("108");
			}else {
				errorCodeList.add("208");
			}
		}
		
		if(flagAlarm) {
			errorCodeList.add("109");
		}
		
		if(flagE126 || flagE75) {
			errorCodeList.add("209");
		}
		
		if(flagE125 || flagE76) {
			errorCodeList.add("210");
		}
		
		if(flagE64) {
			errorCodeList.add("211");
		}
			
		if(flagE46) {
			errorCodeList.add("212");
		}	
			
		if(flagE47) {
			errorCodeList.add("213");
		}	
		
		if(flagP8) {
			errorCodeList.add("216");
		}
		
		if(flagP377_500) {
			errorCodeList.add("217");
		}
			
		if(flagP378_500) {
			errorCodeList.add("218");
		}	
		
		if(flagE109) {
			errorCodeList.add("219");
		}
		
		if(flagE152) {
			errorCodeList.add("220");
		}
		
		if(flagE120) {
			errorCodeList.add("221");
		}
		
		if(flagE104) {
			errorCodeList.add("222");
		}
		
		if(flagE105) {
			errorCodeList.add("223");
		}
			
		if(flagP136) {
			errorCodeList.add("224");
		}	

		return errorCodeList;
	}
	
	/**
	 * 错误errcode持续指定秒数，使用时间戳处理
	 * （1）如果收到的不是指定的errcode，则将缓存清空
	 * （2）如果收到的是指定的errcode
	 * A、如果缓存中没有记录，则表示此为第一次出现，因此需要把当时的时间戳（精确到毫秒）进行记录
	 * B、如果缓存中有记录，则表示前一个数据包已经出现了此errcode，因此需要计算一下缓存中的时间戳与当前时间戳的差值，
	 * 差值如果大于或等于指定的毫秒数则会触发报警，同时需要清除缓存状态信息
	 * */
	private boolean isContinueSeconds(JsonObject analyBean, String paramCode, String val, int secondes){
		boolean flag=false;
		if("1".equals(val)) {
			flag=true;
		}
		
		if(!flag){
			RedisUtil.del("#parameter#"+ paramCode +"-lasttime:"+ analyBean.getString("elevator_code"));
			return false;
			
		}else{
			String tmp = RedisUtil.get("#parameter#"+ paramCode +"-lasttime:"+ analyBean.getString("elevator_code"));
			if(StringUtils.isBlank(tmp)){
				RedisUtil.set("#parameter#"+ paramCode +"-lasttime:"+ analyBean.getString("elevator_code"), ""+System.currentTimeMillis(), secondes+10);
				return false;
				
			}else{
				long nowTime = System.currentTimeMillis();
				long lastTime = Long.parseLong(tmp);
				
				if((nowTime-lastTime)>=secondes*1000L){
					RedisUtil.del("#parameter#"+ paramCode +"-lasttime:"+ analyBean.getString("elevator_code"));
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isNotShow(JsonObject analyBean, String conditionCode, String conditionVal, String notShowCode, String notShowVal, int seconds) {
		boolean conditionBool = false;
		if("1".equals(conditionVal)) {
			conditionBool=true;
		}
		boolean notShowBool = false;
		if("1".equals(notShowVal)) {
			notShowBool=true;
		}
		
		// =========设置启动条件，判断前置错误信息
		// 此处是判断是否为条件判断的起始包
		String conditionFlag = RedisUtil.get("#parameter-condition#" + conditionCode + ":" + analyBean.getString("elevator_code"));
		String lastTimeStr;
		if (StringUtils.isBlank(conditionFlag)) {
			// 为空并且符合条件就开始计数
			if (conditionBool) {
				RedisUtil.set("#parameter-condition#" + conditionCode + ":" + analyBean.getString("elevator_code"), "1", seconds * 2);
				RedisUtil.set("#parameter#" + conditionCode + "&" + notShowCode + "-lasttime:" + analyBean.getString("elevator_code"), "" + System.currentTimeMillis(), seconds * 2);
				return false;
			}

		} else { // 如果不空说明已经开始判断
			// 如果出现notShowCode，那么直接返回false，因为此errCode任何情况不应出现
			if (notShowBool) {
				RedisUtil.del("#parameter-condition#" + conditionCode + ":" + analyBean.getString("elevator_code"));
				RedisUtil.del("#parameter#" + conditionCode + "&" + notShowCode + "-lasttime:" + analyBean.getString("elevator_code"));
				return false;

			} else {
				lastTimeStr = RedisUtil.get("#parameter#" + conditionCode + "&" + notShowCode + "-lasttime:" + analyBean.getString("elevator_code"));
				if (StringUtils.isNotBlank(lastTimeStr)) {
					long lastTime = Long.parseLong(lastTimeStr);
					long nowTime = System.currentTimeMillis();
					// 满足条件后需要初始化缓存
					if ((nowTime - lastTime) >= seconds * 1000) {
						RedisUtil.del("#parameter-condition#" + conditionCode + ":" + analyBean.getString("elevator_code"));
						RedisUtil.del("#parameter#" + conditionCode + "&" + notShowCode + "-lasttime:" + analyBean.getString("elevator_code"));
						return true;
					}
				}
			}
		}

		return false;
	}
	
	// 10秒出现3次以上
    private boolean isAlarmNew(JsonObject analyBean, int seconds) {
    	String tmpCount = RedisUtil.get("#parameter#ALARM#Count:"+ analyBean.getString("elevator_code"));
        String tmpLastTime = RedisUtil.get("#parameter#ALARM#LastTime:"+ analyBean.getString("elevator_code"));
        if (StringUtils.isNotBlank(tmpCount)) {
            int countNum = Integer.parseInt(tmpCount);
            
            if("1".equals(analyBean.getString("alarm"))){
                countNum++;
                RedisUtil.set("#parameter#ALARM#Count:"+ analyBean.getString("elevator_code"), ""+countNum, seconds);
                RedisUtil.set("#parameter#ALARM#LastTime:"+ analyBean.getString("elevator_code"), ""+System.currentTimeMillis(), seconds);
            }else{
                return false;
            }

            long nowTime = System.currentTimeMillis();
            long lastTime = Long.parseLong(tmpLastTime);
            if(countNum>3 && (nowTime-lastTime)<seconds*1000L){
                RedisUtil.del("#parameter#ALARM#Count:"+ analyBean.getString("elevator_code"));
                RedisUtil.del("#parameter#ALARM#LastTime:"+ analyBean.getString("elevator_code"));
                return true;
                
            }else if((nowTime-lastTime)>=seconds*1000L){
                RedisUtil.del("#parameter#ALARM#Count:"+ analyBean.getString("elevator_code"));
                RedisUtil.del("#parameter#ALARM#LastTime:"+ analyBean.getString("elevator_code"));
                return false;
            }
            
        }else{
            if("1".equals(analyBean.getString("alarm"))){
                RedisUtil.set("#parameter#ALARM#Count:"+ analyBean.getString("elevator_code"), "1", seconds);
                RedisUtil.set("#parameter#ALARM#LastTime:"+ analyBean.getString("elevator_code"), ""+System.currentTimeMillis(), seconds);
                return false;
            }
        }
        
        return false;
    }
    
	private boolean isShowInTimescope219(JsonObject analyBean, JsonObject lastAnalyBean, int seconds){
		String tmpCount = RedisUtil.get("#parameter#E109#Count:"+ analyBean.getString("elevator_code"));
		String tmpLastTime = RedisUtil.get("#parameter#E109#LastTime:"+ analyBean.getString("elevator_code"));
		String err = analyBean.getString("logic_fault");
		String lastErr = "";
		if(lastAnalyBean!=null) {
			lastErr = lastAnalyBean.getString("logic_fault");
		}
		
		if (StringUtils.isNotBlank(tmpCount)) {
			int countNum = Integer.parseInt(tmpCount);
			
			if("E109".equalsIgnoreCase(err) && !"E109".equalsIgnoreCase(lastErr)){
				countNum++;
				RedisUtil.set("#parameter#E109#Count:"+ analyBean.getString("elevator_code"), ""+countNum, seconds);
				RedisUtil.set("#parameter#E109#LastTime:"+ analyBean.getString("elevator_code"), ""+System.currentTimeMillis(), seconds);
			}else{
				return false;
			}
	
			long nowTime = System.currentTimeMillis();
			long lastTime = Long.parseLong(tmpLastTime);
			if (countNum > 3 && (nowTime - lastTime) < seconds * 1000L) {
				RedisUtil.del("#parameter#E109#Count:"+ analyBean.getString("elevator_code"));
				RedisUtil.del("#parameter#E109#LastTime:"+ analyBean.getString("elevator_code"));
				return true;
				
			}else if((nowTime-lastTime)>=seconds*1000L){
				RedisUtil.del("#parameter#E109#Count:"+ analyBean.getString("elevator_code"));
				RedisUtil.del("#parameter#E109#LastTime:"+ analyBean.getString("elevator_code"));
				return false;
			}
			
		}else{
			if("E109".equalsIgnoreCase(err) && !"E109".equalsIgnoreCase(lastErr)){
				RedisUtil.set("#parameter#E109#Count:"+ analyBean.getString("elevator_code"), "1", seconds);
				RedisUtil.set("#parameter#E109#LastTime:"+ analyBean.getString("elevator_code"), ""+System.currentTimeMillis(), seconds);
				return false;
			}
		}
		
		return false;
	}
	
	private boolean isShowInTimescope220(JsonObject analyBean, JsonObject lastAnalyBean, int seconds){
		boolean e152Flag = "E152".equalsIgnoreCase(analyBean.getString("logic_status"));
		boolean lastE152Flag = false;
		if(lastAnalyBean!=null) {
			lastE152Flag = "E152".equalsIgnoreCase(lastAnalyBean.getString("logic_status"));
		}
		
		String tmpCount = RedisUtil.get("#parameter#E152#Count:"+ analyBean.getString("elevator_code"));
		String tmpLastTime = RedisUtil.get("#parameter#E152#LastTime:"+ analyBean.getString("elevator_code"));
		
		if (StringUtils.isNotBlank(tmpCount)) {
			int countNum = Integer.parseInt(tmpCount);
			// 上次不能是E152才能计数2017-11-16
			if(e152Flag && !lastE152Flag){
				countNum++;
				RedisUtil.set("#parameter#E152#Count:"+ analyBean.getString("elevator_code"), ""+countNum, seconds);
				RedisUtil.set("#parameter#E152#LastTime:"+ analyBean.getString("elevator_code"), ""+System.currentTimeMillis(), seconds);
			}else{
				return false;
			}
	
			long nowTime = System.currentTimeMillis();
			long lastTime = Long.parseLong(tmpLastTime);
			if (countNum > 5 && (nowTime - lastTime) < seconds * 1000L) {
				RedisUtil.del("#parameter#E152#Count:"+ analyBean.getString("elevator_code"));
				RedisUtil.del("#parameter#E152#LastTime:"+ analyBean.getString("elevator_code"));
				return true;
				
			}else if((nowTime-lastTime)>=seconds*1000L){
				RedisUtil.del("#parameter#E152#Count:"+ analyBean.getString("elevator_code"));
				RedisUtil.del("#parameter#E152#LastTime:"+ analyBean.getString("elevator_code"));
				return false;
			}
			
		}else{
			if(e152Flag && !lastE152Flag){
				RedisUtil.set("#parameter#E152#Count:"+ analyBean.getString("elevator_code"), "1", seconds);
				RedisUtil.set("#parameter#E152#LastTime:"+ analyBean.getString("elevator_code"), ""+System.currentTimeMillis(), seconds);
				return false;
			}
		}
		
		return false;
	}
}
