package com.ermys.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.base.Throwables.propagate;

/**
 * @author ANER0310
 *         Date: 10/25/13
 */
@Singleton
public class EventLoop {
	private static final Logger log = LoggerFactory.getLogger(EventLoop.class);

	private final BlockingQueue<EventDescriptor> descriptors = new LinkedBlockingQueue<EventDescriptor>();

	public <T, J> void submit(Event<T> event, J sender){
		descriptors.offer(new EventDescriptor<T, J>(event, sender));
	}

	public <T, J> EventDescriptor<T, J> take(){
		try {
			return descriptors.take();
		} catch (InterruptedException e) {
			log.error("Was interrupted", e);
			throw propagate(e);
		}
	}
}