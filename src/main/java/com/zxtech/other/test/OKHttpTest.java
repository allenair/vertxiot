package com.zxtech.other.test;

import java.io.IOException;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttpTest {
	private static Logger log = LoggerFactory.getLogger(OKHttpTest.class);
	
	public static final OkHttpClient client = new OkHttpClient();
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	
	public static void main(String[] args) throws Exception {
		 
		
//		for(int i=0;i<10;i++) {
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					for(int k=0;k<10;k++) {
//						try {
//							new OKHttpTest().getFunAsyn("http://localhost:8080/ess/api/inserterrordataforiot.io?hardCode=80008&errorCode=E300-"+k);
//							
//							Thread.sleep(10);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//					
//				}
//			}).start();
//		}
		
//		String url = "http://localhost:8080/ess/api/savecallfixdataforiot.io?hardCode=80063&errorDescript=aaaaallllleeeeennnnn" + "&peopleFlag=1";
//		new OKHttpTest().getFunAsyn(url);
		
		final OKHttpTest okhttp = new OKHttpTest();
//		final String url = "http://localhost:9999/essiot/ftiotdata.io";
//		final String json="{\"elevatorId\":\"allentest123\",\"parameterStr\":\"ExX+D31ubgAAY3oBAA==\",\"time\":\"12345678\"}";
		
		final String url = "http://localhost:9999/essiot/eliotdata.io";
		final String json="{\"elevatorId\":\"el123\",\"parameterStr\":\"AMEAfwDEtwAA5AwAAAMAAACkgQEPLS0AFwAAAMgBzAAABQACAQEBAAAAAKqqqqoAAAAAAGkAAAAAAAAAAAAAcg==\",\"time\":\"123456789\",\"electric\":\"1\",\"people\":\"1\",\"roomElectric\":\"1\",\"roomMaintain\":\"0\",\"topElectric\":\"1\",\"topMaintain\":\"0\",\"alarm\":\"0\",\"errInfo\":\"100\"}";
		
//		okhttp.post(url, json);
		
		int deviceCount=3000;
		int dataCount = 5;
		
		for (int k = 0; k < deviceCount; k++) {
			new Thread(() -> {
				for (int n = 0; n < dataCount; n++) {
					try {
						okhttp.post(url, json);
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			
		}
		
		
		System.out.println("========FIN==========");

	}
	
	public void getFunAsyn(String url) throws Exception {
		Request request = new Request.Builder().url(url).build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if(response.isSuccessful()) {
					log.info(response.body().string());
				}else {
					throw new IOException("Some Error Happen: " + response);
				}
			}
			@Override
			public void onFailure(Call call, IOException err) {
				log.error(err.getMessage());
			}
		});
	}
	
	public String post(String url, String json) throws IOException {
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder().url(url).post(body).build();
		Response response = client.newCall(request).execute();
		return response.body().string();
	}
	
	public void postAsyn(String url, String json) throws IOException {
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder().url(url).post(body).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if(response.isSuccessful()) {
					log.info(response.body().string());
					System.out.println("===========TTTTT==="+System.currentTimeMillis());
				}else {
					throw new IOException("Some Error Happen: " + response);
				}
			}
			@Override
			public void onFailure(Call call, IOException err) {
				log.error(err.getMessage());
			}
		});
		
	}
	
	
}
