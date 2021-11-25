package de.foellix.aql.system;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.core.MediaType;

import kong.unirest.Unirest;

public class UnirestHandler {
	private static final int CONNECTION_TIMEOUT = 10000;
	private static UnirestHandler instance = new UnirestHandler();
	private static boolean configured = false;

	private final Lock lock;
	private int counter;

	private UnirestHandler() {
		this.lock = new ReentrantLock();
	}

	public static UnirestHandler getInstance() {
		return instance;
	}

	public void start(AQLSystem aqlSystem) {
		final long timeoutConverted = (aqlSystem.getTaskScheduler().getTimeout() == -1 ? Long.MAX_VALUE
				: aqlSystem.getTaskScheduler().getTimeout() * 1000 + CONNECTION_TIMEOUT);
		if (!configured) {
			configured = true;
			Unirest.config().connectTimeout(CONNECTION_TIMEOUT).socketTimeout((int) timeoutConverted)
					.setDefaultHeader("Accept", MediaType.TEXT_XML);
		}
	}

	public synchronized void stop() {
		this.lock.lock();
		this.counter--;
		if (this.counter == 0) {
			Unirest.shutDown();
		}
		this.lock.unlock();
	}
}
