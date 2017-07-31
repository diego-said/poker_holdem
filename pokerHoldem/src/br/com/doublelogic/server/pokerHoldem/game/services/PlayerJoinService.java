package br.com.doublelogic.server.pokerHoldem.game.services;

import java.util.List;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.player.Player;

/**
 * Service responsible for logic executed when a new player wants to enter the game
 * 
 * @author diego.said
 *
 */
public class PlayerJoinService extends GameService {

	public PlayerJoinService(GameCore core) {
		super(core);
	}

	public void execute(int newPlayerId) {
		// creating the new player
		Player newPlayer = new Player(newPlayerId);
		getGameCore().addPlayer(newPlayer);

		// sends to the player information regarding the other players in the game
		Player buttonPlayer = getGameCore().getButtonPlayer();
		List<Player> players = getGameCore().getSeatPlayers();

		getGameCore().getGameServer().sendPlayersData(newPlayerId, buttonPlayer.getId(), players);
	}

}