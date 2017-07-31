package br.com.doublelogic.server.pokerHoldem.game.seat;

/**
 * Represents the states in which a seat can be. The seat can only take one state at a time.
 * 
 * @author diego.said
 *
 */
public enum SeatStates {

	/**
	 * The seat is empty, there is no player.
	 */
	EMPTY(0),

	/**
	 * The seat is reserved for a player, but this is not yet active.
	 */
	RESERVED(1),

	/**
	 * The player is in the seat waiting for more players to start playing.
	 */
	WAITING(2),

	/**
	 * The player is playing certain game.
	 */
	PLAYING(3),

	/**
	 * The player who did not participate in a particular game hand.
	 */
	FOLDED(4),

	/**
	 * The player bets all his chips in a game hand.
	 */
	ALL_IN(5),

	/**
	 * The player continues to occupy a place in the game, but is not in his seat at the moment.
	 */
	SIT_OUT(6),
	;

	private final int value;

	private SeatStates(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}