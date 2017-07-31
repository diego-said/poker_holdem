package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

public class SittingService extends GameService {

	public SittingService(GameCore core) {
		super(core);
	}

	public void execute(int playerId, int wallet) {
		Seat seat = getGameCore().getPlayerSeat(playerId);
		if(seat != null && seat.isReserved()) {
			if(getGameCore().getCounterSeated().value() == 0) {
				seat.setState(SeatStates.PLAYING);
				getGameCore().getCounterPlaying().increment();
			} else {
				seat.setState(SeatStates.WAITING);
			}

			seat.getPlayer().setWallet(wallet);

			getGameCore().getCounterSeated().increment();

			getGameCore().getGameServer().sendSitting(playerId, seat.getSeatNumber(), wallet);
		}
	}

}
