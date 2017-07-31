package br.com.doublelogic.server.pokerHoldem.game.server;

public enum GameServerCommands {

	/**
	 * Command used when the client responds to the request for blind
	 */
	BLIND("blind"),

	/**
	 * Command used when the client responds to the request for bet
	 */
	BET("bet"),

	/**
	 * Command used when the client tells what seat the player chose
	 */
	SEAT_SELECT("seatSelect"),

	/**
	 * Command used when the client reports that the player will occupy a seat
	 */
	SITTING("sitting"),

	/**
	 * Command used when the client sends the player's cards to be validated
	 */
	PLAYER_HAND("playerHand"),

	/**
	 * Command used when the player informs if shows his cards or not
	 */
	SHOW_OR_MUCK("showOrMuck"),

	/**
	 * Command used by the client to inform that the player is not in his seat
	 */
	SIT_OUT("sit-out"),

	/**
	 * Command used by the client to inform the player are waiting for the next hand
	 */
	WAITING("waiting"),

	/**
	 * Used to respond to a command not supported by server
	 */
	UNKNOWN(""),
	;

	private final String name;

	private GameServerCommands(String name) {
		this.name = name;
	}

	public static GameServerCommands getGameServerCommand(String name) {
		for(GameServerCommands command : values()) {
			if(command.getName().equals(name)) {
				return command;
			}
		}
		return UNKNOWN;
	}

	public String getName() {
		return name;
	}

}