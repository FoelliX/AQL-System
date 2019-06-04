package de.foellix.aql.system.task;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mashape.unirest.http.Unirest;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class UnirestHandler {
	private static UnirestHandler instance = new UnirestHandler();

	private final Lock lock;
	private int counter;

	private UnirestHandler() {
		this.lock = new ReentrantLock();
	}

	public static UnirestHandler getInstance() {
		return instance;
	}

	public void start(System aqlSystem) {
		final long timeoutConverted = (aqlSystem.getScheduler().getTimeout() == -1 ? Long.MAX_VALUE
				: aqlSystem.getScheduler().getTimeout() * 1000 + 10000);
		Unirest.setTimeouts(10000, timeoutConverted);
	}

	public synchronized void stop() {
		this.lock.lock();
		this.counter--;
		if (this.counter == 0) {
			try {
				Unirest.shutdown();
			} catch (final IOException e) {
				Log.warning("Could not close connection to external tool: " + e.getMessage());
			}
		}
		this.lock.unlock();
	}
}
