package com.ermys.echo;

/**
 * @author ANER0310
 *         Date: 10/25/13
 */
public class EventDescriptor<T, J>{
	private final Event<T> event;
	private final J sender;

	public EventDescriptor(Event<T> event, J sender) {
		this.event = event;
		this.sender = sender;
	}

	public Event<T> getEvent() {
		return event;
	}

	public J getSender() {
		return sender;
	}
}