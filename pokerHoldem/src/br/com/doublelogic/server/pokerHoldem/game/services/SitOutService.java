package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

public class SitOutService extends GameService {

	public SitOutService(GameCore core) {
		super(core);
	}

	public void execute(int playerId) {
		// retrieving the player's seat
		Seat seat = getGameCore().getPlayerSeat(playerId);

		if(seat != null) {
			// just send the command if the player is playing the hand
			if(seat.getState() == SeatStates.PLAYING || seat.getState() == SeatStates.FOLDED) {
				getGameCore().getCounterPlaying().decrement();
				seat.setState(SeatStates.SIT_OUT);

				getGameCore().getGameServer().sendSitOut(playerId);

				// verifies that the player was the last to bet
				if(playerId == getGameCore().getLastAskBetPlayerIndex()) {
					getGameCore().askBet();
				}
			}
		}
	}

}
