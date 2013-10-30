package com.ermys.echo;

/**
 * @author ANER0310
 *         Date: 10/25/13
 */
public interface Callback<T, J> {
	public void call(Event<T> event, J submitter);
}