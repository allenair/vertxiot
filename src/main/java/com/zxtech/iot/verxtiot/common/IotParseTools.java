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
		
		String srcParam = getParameterBitValue(parameter.getParameterStr());
		if (srcParam.length() < 512) {
			logger.error("EL:{} Parameter is too short! {}", parameter.getElevatorId(), srcParam);
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
	
	public static JsonObject getAnalysisElBean(TransferElevatorParameter parameter) {
		String srcParam = getParameterBitValue(parameter.getParameterStr());
		
		JsonObject analyBean = new JsonObject();
		analyBean.put("elevator_code", parameter.getElevatorId());
		analyBean.put("up_time", LocalDateTime.now().toString());
		analyBean.put("others", srcParam);
		
		// 此处是指从srcParam字符串中，从第0位开始，截取8个字符,此处系统保留不记录
		int intTmp = getIntByString(getSubString(srcParam, 0, 8));
		analyBean.put("err", ""+intTmp);		
		
		analyBean.put("nav", getSubString(srcParam, 15, 1));
		analyBean.put("ins", getSubString(srcParam, 14, 1));
		analyBean.put("run", getSubString(srcParam, 13, 1));
		analyBean.put("do_p", getSubString(srcParam, 12, 1));
		analyBean.put("dol", getSubString(srcParam, 11, 1));
		analyBean.put("dw", getSubString(srcParam, 10, 1));
		analyBean.put("dcl", getSubString(srcParam, 9, 1));
		analyBean.put("dz", getSubString(srcParam, 8, 1));
		
		analyBean.put("efo", getSubString(srcParam, 23, 1));
		analyBean.put("cb", getSubString(srcParam, 22, 1));
		analyBean.put("up", getSubString(srcParam, 23, 1));
		analyBean.put("down", getSubString(srcParam, 20, 1));
		
		analyBean.put("fl", getIntByString(getSubString(srcParam, 24, 16)));
		analyBean.put("cnt", getIntByString(getSubString(srcParam, 40, 32)));
		analyBean.put("ddfw", getIntByString(getSubString(srcParam, 72, 32)));
		analyBean.put("hxxh", getIntByString(getSubString(srcParam, 104, 32)));
		
		analyBean.put("es", getSubString(srcParam, 143, 1));
		analyBean.put("se", getSubString(srcParam, 142, 1));
		analyBean.put("dfc", getSubString(srcParam, 141, 1));
		analyBean.put("tci", getSubString(srcParam, 140, 1));
		analyBean.put("ero", getSubString(srcParam, 139, 1));
		analyBean.put("lv1", getSubString(srcParam, 138, 1));
		analyBean.put("lv2", getSubString(srcParam, 137, 1));
		analyBean.put("ls1", getSubString(srcParam, 136, 1));
		
		analyBean.put("ls2", getSubString(srcParam, 151, 1));
		analyBean.put("dob", getSubString(srcParam, 150, 1));
		analyBean.put("dcb", getSubString(srcParam, 149, 1));
		analyBean.put("lrd", getSubString(srcParam, 148, 1));
		analyBean.put("dos", getSubString(srcParam, 147, 1));
		analyBean.put("efk", getSubString(srcParam, 146, 1));
		analyBean.put("pks", getSubString(srcParam, 145, 1));
		analyBean.put("rdol", getSubString(srcParam, 144, 1));
		
		analyBean.put("rdcl", getSubString(srcParam, 159, 1));
		analyBean.put("rdob", getSubString(srcParam, 158, 1));
		analyBean.put("rdcb", getSubString(srcParam, 157, 1));
		analyBean.put("rear_en", getSubString(srcParam, 156, 1));
		analyBean.put("rdoo", getSubString(srcParam, 155, 1));
		analyBean.put("logic_err", ""+getIntByString(getSubString(srcParam, 160, 8)));
		
		int up=getIntByString(getSubString(srcParam, 168, 8));//显示楼层高位
		int down=getIntByString(getSubString(srcParam, 176, 8));//显示楼层低位
		analyBean.put("show_left", ""+up);
		analyBean.put("show_right", ""+down);
		analyBean.put("show_fl", dealFloor(up, down));
		
		analyBean.put("board_type", ""+getIntByString(getSubString(srcParam, 184, 8)));
		analyBean.put("last_count", getIntByString(getSubString(srcParam, 192, 32)));
		analyBean.put("total_time", getIntByString(getSubString(srcParam, 224, 32)));
		analyBean.put("driver_err", ""+getIntByString(getSubString(srcParam, 256, 8)));
		analyBean.put("logic_lock", ""+getIntByString(getSubString(srcParam, 264, 8)));
		analyBean.put("sys_model", ""+getIntByString(getSubString(srcParam, 272, 8)));
		analyBean.put("xh_time", getIntByString(getSubString(srcParam, 280, 32)));
		analyBean.put("arm_code", getIntByString(getSubString(srcParam, 312, 32)));
		analyBean.put("dsp_code", getIntByString(getSubString(srcParam, 344, 32)));

		analyBean.put("safe_circle", getSubString(srcParam, 383, 1));
		analyBean.put("open_fault", getSubString(srcParam, 382, 1));
		analyBean.put("close_fault", getSubString(srcParam, 381, 1));
		analyBean.put("up_switch", getSubString(srcParam, 380, 1));
		analyBean.put("down_switch", getSubString(srcParam, 379, 1));
		analyBean.put("stop_fault", getSubString(srcParam, 378, 1));
		analyBean.put("lock_broken", getSubString(srcParam, 377, 1));
		
		analyBean.put("speed_fault", getSubString(srcParam, 391, 1));
		analyBean.put("go_top", getSubString(srcParam, 389, 1));
		analyBean.put("go_down", getSubString(srcParam, 388, 1));
		
		int tmpCode=0;
		tmpCode = getIntByString(getSubString(srcParam, 408, 8));
		if((tmpCode>=31 && tmpCode<=99) || (tmpCode>=220 && tmpCode<=255)) {
			analyBean.put("driver_fault", "E"+tmpCode);
		}else {
			analyBean.put("driver_fault", "0");
		}
		
		tmpCode=0;
		tmpCode = getIntByString(getSubString(srcParam, 416, 8));
		if(tmpCode>=100 && tmpCode<=150) {
			analyBean.put("logic_fault", "E"+tmpCode);
		}else {
			analyBean.put("logic_fault", "0");
		}
		
		tmpCode=0;
		tmpCode = getIntByString(getSubString(srcParam, 424, 8));
		if(tmpCode>=151 && tmpCode<=219) {
			analyBean.put("logic_status", "E"+tmpCode);
		}else {
			analyBean.put("logic_status", "0");
		}
		
		analyBean.put("ver_code", ""+getIntByString(getSubString(srcParam, 504, 8)));
		
		analyBean.put("electric_flag", parameter.getElectric());
		analyBean.put("people_flag", parameter.getPeople());
		analyBean.put("room_electric_flag", parameter.getRoomElectric());
		analyBean.put("room_maintain_flag", parameter.getRoomMaintain());
		analyBean.put("top_electric_flag", parameter.getTopElectric());
		analyBean.put("top_maintain_flag", parameter.getTopMaintain());
		
		analyBean.put("alarm", parameter.getAlarm());
		analyBean.put("maintenance", parameter.getMaintenance());
		analyBean.put("err_info", parameter.getErrInfo());
		
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
	
	private static String dealFloor(int iL, int iR){
		int c=(int)'A';
		if(iL==37) {
			if(iR>=1 && iR<=9) {
				return "-"+iR;
			}
			if(iR==37) {
				return "--";
			}
		}
		if(iL==45 && iR==45) {
			return "--";
		}
		if(iL==39 && iR==39) {
			return "**";
		}
		if(iL==10) {
			if(iR>=0 && iR<=9) {
				return ""+iR;
			}
			if(iR>=11 && iR<=36 && iR!=29) {
				return ""+((char)(iR-11+c));
			}
			if(iR==10) {
				return "";
			}
			if(iR==38) {
				return "S";
			}
		}
		if(iL>=1 && iL<=7) {
			if(iR>=0 && iR<=9) {
				return iL+""+iR;
			}
			if(iR>=11 && iR<=36) {
				return iL+""+((char)(iR-11+c));
			}
		}
		if(iL==8) {
			switch (iR) {
				case 0:
					return "12A";
				case 1:
					return "12B";
				case 2:
					return "13A";
				case 3:
					return "13B";
				case 4:
					return "14A";
				case 5:
					return "14B";
				case 6:
					return "15A";
				case 7:
					return "15B";
				case 8:
					return "17A";
				case 9:
					return "17B";
			}
			if(iR>=11 && iR<=36) {
				return iL+""+((char)(iR-11+c));
			}
		}
		if(iL==9) {
			switch (iR) {
				case 0:
					return "18A";
				case 1:
					return "18B";
				case 2:
					return "23A";
				case 3:
					return "23B";
				case 4:
					return "33A";
				case 5:
					return "33B";
			}
			if(iR>=11 && iR<=36) {
				return iL+""+((char)(iR-11+c));
			}
		}
		if(iL>=11 && iL<=36) {
			if(iR>=1 && iR<=9) {
				return ((char)(iL-11+c))+""+iR;
			}
			if(iR>=11 && iR<=36){
				return ((char)(iL-11+c))+""+((char)(iR-11+c));
			}
		}
		
		return "EE";
	}
}
