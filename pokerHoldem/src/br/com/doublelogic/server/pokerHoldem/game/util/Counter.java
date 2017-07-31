package br.com.doublelogic.server.pokerHoldem.game.util;

/**
 * Simple counter synchronized
 * 
 * @author diego.said
 *
 */
public class Counter {

	private int count;

	public Counter() {
		this(0);
	}

	public Counter(int value) {
		count = value;
	}

	/**
	 * Increases the counter value by one
	 */
	public synchronized void increment() {
		count++;
	}

	/**
	 * Decreases the counter value by one
	 */
	public synchronized void decrement() {
		count--;
	}

	/**
	 * Gets the current counter value
	 * @return counter value
	 */
	public synchronized int value() {
		return count;
	}
}
