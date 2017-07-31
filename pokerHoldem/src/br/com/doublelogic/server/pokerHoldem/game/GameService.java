package br.com.doublelogic.server.pokerHoldem.game;

public abstract class GameService {

	private final GameCore core;

	public GameService(GameCore core) {
		this.core = core;
	}

	protected GameCore getGameCore() {
		return core;
	}

}