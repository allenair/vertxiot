package com.zxtech.other.cassandra;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;

public class SessionRepository {
	private static Session instance = null;
	private static Cluster cluster = null;
	private static Lock lock = new ReentrantLock();

	private SessionRepository() {
	}

	public static Session getSession() {
		if (null == instance) {
			try {
				lock.lock();

				if (null == instance) {
					PoolingOptions pool = new PoolingOptions();
					pool.setMaxQueueSize(1024);
					pool.setMaxConnectionsPerHost(HostDistance.LOCAL, 4);
					
//					cluster = Cluster.builder().addContactPoint("127.0.0.1").withCredentials("admin", "admin").build();
					cluster = Cluster.builder().addContactPoint("127.0.0.1").withPoolingOptions(pool).build();
					
					
					
					PoolingOptions pool2 = cluster.getConfiguration().getPoolingOptions();
					System.out.println(pool2.getMaxQueueSize());
					System.out.println(pool2.getMaxConnectionsPerHost(HostDistance.LOCAL));
					System.out.println(pool2.getCoreConnectionsPerHost(HostDistance.LOCAL));
					
					
					
					
					instance = cluster.connect();
					// 也可以针对一个特定的keyspace获取一个session
					// instance = cluster.connect("mycas");
				}
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	public static void close() {
		if (null == cluster) {
			try {
				lock.lock();

				if (null == cluster) {
					cluster.close();
				}
			} finally {
				lock.unlock();
			}
		}
	}
}
