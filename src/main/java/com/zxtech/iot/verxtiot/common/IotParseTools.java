package com.zxtech.iot.verxtiot.common;

import java.time.LocalDateTime;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;

import io.vertx.core.json.JsonObject;

public class IotParseTools {
	private static final Logger logger = LoggerFactory.getLogger(IotParseTools.class);
	public static boolean checkElParameter(TransferElevatorParameter parameter){
		// 以下四个字段不能为空
		if(StringUtils.isBlank(parameter.getElevatorId())){
			return false;
		}
		if(StringUtils.isBlank(parameter.getParameterStr())){
			return false;
		}	
		if(StringUtils.isBlank(parameter.getTime())){
			return false;
		}	
		if(StringUtils.isBlank(parameter.getPeople()) || parameter.getPeople().length()>1){
			return false;
		}
		
		// 以下字段由于都是标志位，因此长度最长为1
		if(StringUtils.isNotBlank(parameter.getRoomElectric()) && parameter.getRoomElectric().length()>1){
			return false;
		}
		if(StringUtils.isNotBlank(parameter.getRoomMaintain()) && parameter.getRoomMaintain().length()>1){
			return false;
		}
		if(StringUtils.isNotBlank(parameter.getTopElectric()) && parameter.getTopElectric().length()>1){
			return false;
		}
		if(StringUtils.isNotBlank(parameter.getTopMaintain()) && parameter.getTopMaintain().length()>1){
			return false;
		}
		if(StringUtils.isNotBlank(parameter.getAlarm()) && parameter.getAlarm().length()>1){
			return false;
		}
		if(StringUtils.isNotBlank(parameter.getErrInfo()) && parameter.getErrInfo().length()>3){
			return false;
		}
		
		return true;
	}
	
	public static boolean checkFtParameter(TransferElevatorParameter parameter){
		if(StringUtils.isBlank(parameter.getElevatorId())){
			return false;
		}
		if(StringUtils.isBlank(parameter.getParameterStr())){
			return false;
		}	
		if(StringUtils.isBlank(parameter.getTime())){
			return false;
		}	
		
		String srcParam = getParameterBitValue(parameter.getParameterStr());
		if (srcParam.length() < 96) {
			logger.error("FT:{} Parameter is too short! {}", parameter.getElevatorId(), srcParam);
			return false;
		}
		
		return true;
	}
	
	public static JsonObject getAnalysisFtBean(TransferElevatorParameter parameter) {
		String srcParam = getParameterBitValue(parameter.getParameterStr());
		
		JsonObject analyBean = new JsonObject();
		analyBean.put("elevator_code", parameter.getElevatorId());
		analyBean.put("up_time", LocalDateTime.now().toString());
		
		// 此处是指从srcParam字符串中，从第0位开始，截取8个字符,此处系统保留不记录
		analyBean.put("error_flag", getSubString(srcParam, 7, 1));
		analyBean.put("up", getSubString(srcParam, 6, 1));
		analyBean.put("down", getSubString(srcParam, 5, 1));
		analyBean.put("stop_flag", getSubString(srcParam, 4, 1));
		analyBean.put("chk_flag", getSubString(srcParam, 3, 1));
		analyBean.put("sud_stop", getSubString(srcParam, 2, 1));
		getSubString(srcParam, 1, 1);
		getSubString(srcParam, 0, 1);
				
		analyBean.put("high_speed", getSubString(srcParam, 15, 1));
		analyBean.put("low_speed", getSubString(srcParam, 14, 1));
		analyBean.put("work_freq", getSubString(srcParam, 13, 1));
		analyBean.put("chg_freq", getSubString(srcParam, 12, 1));
		analyBean.put("star_type", getSubString(srcParam, 11, 1));
		analyBean.put("tria_type", getSubString(srcParam, 10, 1));
		analyBean.put("self_start", getSubString(srcParam, 9, 1));
		getSubString(srcParam, 8, 1);
		
		analyBean.put("m01", getSubString(srcParam, 23, 1));
		analyBean.put("m02", getSubString(srcParam, 22, 1));
		analyBean.put("m03", getSubString(srcParam, 21, 1));
		analyBean.put("m04", getSubString(srcParam, 20, 1));
		analyBean.put("m05", getSubString(srcParam, 19, 1));
		analyBean.put("m06", getSubString(srcParam, 18, 1));
		analyBean.put("m07", getSubString(srcParam, 17, 1));
		analyBean.put("m08", getSubString(srcParam, 16, 1));
		
		analyBean.put("m09", getSubString(srcParam, 31, 1));
		analyBean.put("j_flg", getSubString(srcParam, 30, 1));
		analyBean.put("n1", getSubString(srcParam, 29, 1));
		analyBean.put("n3", getSubString(srcParam, 28, 1));
		getSubString(srcParam, 27, 1);
		getSubString(srcParam, 26, 1);
		getSubString(srcParam, 25, 1);
		getSubString(srcParam, 24, 1);
		
		analyBean.put("run_speed", getIntByString(getSubString(srcParam, 32, 8)));
		analyBean.put("left_hand_speed", getIntByString(getSubString(srcParam, 40, 8)));
		analyBean.put("right_hand_speed", getIntByString(getSubString(srcParam, 48, 8)));
		
		getSubString(srcParam, 56, 8);
		getSubString(srcParam, 64, 8);
		
		analyBean.put("error_code", getIntByString(getSubString(srcParam, 72, 8)));
		analyBean.put("run_time", getIntByString(getSubString(srcParam, 80, 16)));
		analyBean.put("all_data", srcParam);
		
		return analyBean;
	}
	
	private static String getSubString(String srcStr, int start, int len) {
		return srcStr.substring(start, start + len);
	}

	// 上传的ParameterStr是经过base64编码，因此此处需要先处理编码，然后再将bit位处理成字符，以便后续处理
	// 因此处理后整数如果是4个字节，那么就是32个字符
	private static String getParameterBitValue(String baseParam) {
		StringBuilder strBuffer = new StringBuilder();
		byte[] srcArr = Base64.getDecoder().decode(baseParam);
		for (byte b : srcArr) {
			strBuffer.append("" + (b >> 7 & 0x1));
			strBuffer.append("" + (b >> 6 & 0x1));
			strBuffer.append("" + (b >> 5 & 0x1));
			strBuffer.append("" + (b >> 4 & 0x1));
			strBuffer.append("" + (b >> 3 & 0x1));
			strBuffer.append("" + (b >> 2 & 0x1));
			strBuffer.append("" + (b >> 1 & 0x1));
			strBuffer.append("" + (b >> 0 & 0x1));
		}
		return strBuffer.toString();
	}

	// 按照小端法取值,只有获取整数(涉及多个字节问题)时候需要这样处理
	// 序列是，。。。0x78 0x56 0x34 0x12。。。，按照0x12345678来解析
	private static int getIntByString(String str) {
		int res = -1;
		int len = str.length();
		int byteNum = len / 8;
		StringBuilder sb = new StringBuilder();

		for (int i = byteNum - 1; i >= 0; i--) {
			sb.append(str.substring(i * 8, (i + 1) * 8));
		}

		try {
			res = Integer.parseInt(sb.toString(), 2);
		} catch (Exception e) {
			res = -1;
		}
		return res;
	}
}
