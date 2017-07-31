package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;

/**
 * Service responsible for logic executed when a player wants to exit the game
 * 
 * @author diego.said
 *
 */
public class PlayerDetachService extends GameService {

	public PlayerDetachService(GameCore core) {
		super(core);
	}

	public boolean execute(int playerId) {
		// removing the player
		boolean removed = getGameCore().removePlayer(playerId);

		// if the player has been removed notify the other players
		if(removed) {
			getGameCore().getGameServer().sendPlayerDetach(playerId);
		}

		return removed;
	}

}
