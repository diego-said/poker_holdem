package br.com.doublelogic.server.pokerHoldem.game;

/**
 * It stores all the states that the game can take.
 * 
 * @author diego.said
 *
 */
public enum GameStates {

	/**
	 * State in which the game will start, you must have at least two players
	 */
	GAME_START(0),

	/**
	 * State in which each player must pay to play the game hand
	 */
	BLINDS(1),

	/**
	 * State in which each player makes his bet according to your hole cards
	 */
	PRE_FLOP(2),

	/**
	 * State in which three table cards are shown, each player must place your bet
	 */
	FLOP(3),

	/**
	 * State in which the 4th card of the table is shown, each player must place your bet
	 */
	TURN(4),

	/**
	 * State in which the last card of the table is shown, each player must place your bet
	 */
	RIVER(5),

	/**
	 * State on which to determine the players' hands, determining who won
	 */
	SHOWDOWN(6),

	/**
	 * State in which each player shows his hand
	 */
	SHOWDOWN_CARDS(7),

	/**
	 * Initial state of the game, there is no player
	 */
	GAME_STOP(8)
	;

	private final int value;

	private GameStates(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
