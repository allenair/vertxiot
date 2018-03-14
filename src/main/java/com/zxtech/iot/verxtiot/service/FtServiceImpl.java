package com.zxtech.iot.verxtiot.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zxtech.iot.verxtiot.bean.TransferElevatorParameter;
import com.zxtech.iot.verxtiot.common.IotParseTools;
import com.zxtech.iot.verxtiot.common.RestClientTools;
import com.zxtech.iot.verxtiot.db.FtDaoImpl;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public class FtServiceImpl {
	private static final Logger logger = LoggerFactory.getLogger(FtServiceImpl.class);
	private final RestClientTools restApiClient;
	private final FtDaoImpl ftDao;

	public FtServiceImpl(Vertx vertx, JsonObject config, Map<String, String> sqlMap) {
		this.restApiClient = new RestClientTools(vertx, config);
		this.ftDao = new FtDaoImpl(vertx, config, sqlMap);
	}

	public void handler(TransferElevatorParameter parameter) {
		JsonObject analyBean = IotParseTools.getAnalysisFtBean(parameter);

		Completable coldb = ftDao.insertCollectDb(parameter);
		Completable analydb = ftDao.insertAnalysisDb(analyBean);
		coldb.andThen(analydb).subscribe(() -> {
			if (analyBean.getInteger("error_code") > 0) {
				restApiClient.sendFtErrorInfo(analyBean);
			}

		}, err -> {
			logger.error("FT:{}, Some Errors happen {}", parameter.getElevatorId(), err.getMessage());
		});
	}

	
}
