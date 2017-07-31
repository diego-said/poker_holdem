package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.GameStates;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

public class ShowOrMuckService extends GameService {

	public ShowOrMuckService(GameCore core) {
		super(core);
	}

	public void execute(int playerId, boolean show) {
		Seat seat = getGameCore().getPlayerSeat(playerId);
		if(getGameCore().getGameState() == GameStates.SHOWDOWN_CARDS && seat != null && seat.getState() == SeatStates.PLAYING) {
			getGameCore().getGameServer().sendShowOrMuck(playerId, show, seat.getPlayer().getHoleCards());
			getGameCore().getCounterPlayersRemaining().decrement();
		}
		if (getGameCore().getCounterPlayersRemaining().value() == 0) {
			getGameCore().endGame();
		}
	}

}
