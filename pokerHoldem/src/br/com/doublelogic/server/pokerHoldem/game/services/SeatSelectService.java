package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.player.Player;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

/**
 * Service responsible for logic executed when a player wants to be sitting in a particular seat
 * 
 * @author diego.said
 *
 */
public class SeatSelectService extends GameService {

	public SeatSelectService(GameCore core) {
		super(core);
	}

	public boolean execute(int playerId, int seatNumber) {
		Seat seat = getGameCore().getGameTable().getSeat(seatNumber);
		if(seat.getState() == SeatStates.EMPTY) {
			Player player = getGameCore().getPlayers().get(playerId);
			seat.setPlayer(player);
			seat.reserveSeat();

			// marking that player in seat
			getGameCore().addPlayerSeat(player.getId(), seat.getSeatNumber());

			return true;
		} else {
			getGameCore().getGameServer().sendErrorMessage(playerId, "Seat taken");
			return false;
		}
	}

}
