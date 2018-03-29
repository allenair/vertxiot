package com.zxtech.other.cassandra;

import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class CassandraTest {
	private String insert_str = "insert into alleniot_ks.ft_data_collect( up_time, hard_id, parameter_str, hard_time)  values(?,?,?,?);";

	private AtomicInteger intCount = new AtomicInteger(1);
	
	public static void main(String[] args) {
		CassandraTest tt = new CassandraTest();
//		tt.testInsert();
		tt.testInsertParallel();
	}

	public void testInsert() {
		System.out.println("==========STA============"+System.currentTimeMillis());
		IntStream.range(0, 10000).forEach(index -> {
			this.insertFtData(System.currentTimeMillis() + index*10000);
		});
		System.out.println("==========FIN============"+System.currentTimeMillis());
		SessionRepository.close();
	}
	
	public void testInsertParallel() {
		System.out.println("==========STA============"+System.currentTimeMillis());
		for(int i=0;i<1000;i++) {
			new Thread(()->{
				int count = intCount.incrementAndGet();
				Random rnd = new Random();
				IntStream.range(1, 101).forEach(index -> {
					this.insertFtData(System.currentTimeMillis() + index*99*rnd.nextInt(10000) + count*33);
				});
			}).start();
		}
		
		System.out.println("==========FIN============"+System.currentTimeMillis());
	}
	
	public void insertFtData(long time) {
		Session session = SessionRepository.getSession();
		PreparedStatement prepareStatement = session.prepare(this.insert_str);
		BoundStatement bindStatement = new BoundStatement(prepareStatement);
		bindStatement.bind(new Timestamp(time), "90000080027", "asdfghjklqwertyui==", 123456L);
		session.executeAsync(bindStatement);
//		session.execute(bindStatement);
	}
}
