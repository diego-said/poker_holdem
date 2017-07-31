package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

public class WaitingService extends GameService {

	public WaitingService(GameCore core) {
		super(core);
	}

	public void execute(int playerId) {
		// retrieving the player's seat
		Seat seat = getGameCore().getPlayerSeat(playerId);

		if(seat != null) {
			// just send the command if the player is out of your seat
			if(seat.getState() == SeatStates.SIT_OUT) {
				getGameCore().getCounterPlaying().increment();
				seat.setState(SeatStates.WAITING);

				getGameCore().getGameServer().sendWaiting(playerId);
			}
		}
	}

}
