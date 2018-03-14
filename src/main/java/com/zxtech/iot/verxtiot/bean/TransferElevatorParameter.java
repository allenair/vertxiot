package com.zxtech.iot.verxtiot.bean;

import java.io.Serializable;

import io.vertx.core.json.JsonObject;

public class TransferElevatorParameter implements Serializable {
	private static final long serialVersionUID = -8176554013218648205L;
	
	private String elevatorId;
	private String parameterStr;
	private String time;
	private String electric;
	private String people;
	private String roomElectric;
	private String roomMaintain;
	private String topElectric;
	private String topMaintain;
	private String alarm;
	private String maintenance;
	private String errInfo;

	public TransferElevatorParameter() {
		
	}
	
	public TransferElevatorParameter(TransferElevatorParameter other) {
		this.elevatorId = other.elevatorId;
		this.parameterStr = other.parameterStr;
		this.time = other.time;
		this.electric = other.electric;
		this.people = other.people;
		this.roomElectric = other.roomElectric;
		this.roomMaintain = other.roomMaintain;
		this.topElectric = other.topElectric;
		this.topMaintain = other.topMaintain;
		this.alarm = other.alarm;
		this.maintenance = other.maintenance;
		this.errInfo = other.errInfo;
	}
	
	public TransferElevatorParameter(JsonObject json) {
		this.elevatorId = json.getString("elevatorId", "");
		this.parameterStr = json.getString("parameterStr", "");
		this.time = json.getString("time", "");
		this.electric = json.getString("electric", "");
		this.people = json.getString("people", "");
		this.roomElectric = json.getString("roomElectric", "");
		this.roomMaintain = json.getString("roomMaintain", "");
		this.topElectric = json.getString("topElectric", "");
		this.topMaintain = json.getString("topMaintain", "");
		this.alarm = json.getString("alarm", "");
		this.maintenance = json.getString("maintenance", "");
		this.errInfo = json.getString("errInfo", "");
	}
	
	public JsonObject toJson() {
	    JsonObject json = new JsonObject();
	    json.put("elevatorId", this.elevatorId);
	    json.put("parameterStr", this.parameterStr);
	    json.put("time", this.time);
	    json.put("electric", this.electric);
	    json.put("people", this.people);
	    json.put("roomElectric", this.roomElectric);
	    json.put("roomMaintain", this.roomMaintain);
	    json.put("topElectric", this.topElectric);
	    json.put("topMaintain", this.topMaintain);
	    json.put("alarm", this.alarm);
	    json.put("maintenance", this.maintenance);
	    json.put("errInfo", this.errInfo);
	    return json;
	  }
	
	public String getErrInfo() {
		return errInfo;
	}

	public void setErrInfo(String errInfo) {
		this.errInfo = errInfo;
	}

	public String getRoomElectric() {
		return roomElectric;
	}

	public void setRoomElectric(String roomElectric) {
		this.roomElectric = roomElectric;
	}

	public String getRoomMaintain() {
		return roomMaintain;
	}

	public void setRoomMaintain(String roomMaintain) {
		this.roomMaintain = roomMaintain;
	}

	public String getTopElectric() {
		return topElectric;
	}

	public void setTopElectric(String topElectric) {
		this.topElectric = topElectric;
	}

	public String getTopMaintain() {
		return topMaintain;
	}

	public void setTopMaintain(String topMaintain) {
		this.topMaintain = topMaintain;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getElectric() {
		return electric;
	}

	public void setElectric(String electric) {
		this.electric = electric;
	}

	public String getPeople() {
		return people;
	}

	public void setPeople(String people) {
		this.people = people;
	}

	public String getElevatorId() {
		return elevatorId;
	}

	public void setElevatorId(String elevatorId) {
		this.elevatorId = elevatorId;
	}

	public String getParameterStr() {
		return parameterStr;
	}

	public void setParameterStr(String parameterStr) {
		this.parameterStr = parameterStr;
	}

	public String getAlarm() {
		return alarm;
	}

	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}

	public String getMaintenance() {
		return maintenance;
	}

	public void setMaintenance(String maintenance) {
		this.maintenance = maintenance;
	}

}
