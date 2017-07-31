package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.GameStates;
import br.com.doublelogic.server.pokerHoldem.game.cards.Card;
import br.com.doublelogic.server.pokerHoldem.game.cards.CardHandRanking;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

public class PlayerHandService extends GameService {

	public PlayerHandService(GameCore core) {
		super(core);
	}

	public void execute(int playerId, Card[] cards) {
		// retrieving the player's seat
		Seat seat = getGameCore().getPlayerSeat(playerId);

		if(getGameCore().getGameState() == GameStates.SHOWDOWN && seat != null &&
				(seat.getState() == SeatStates.PLAYING || seat.getState() == SeatStates.ALL_IN)) {
			if(getGameCore().validateCards(seat.getPlayer().getId(), cards)) {
				seat.getPlayer().setStrengthHand(CardHandRanking.calculateStrengthHand(cards));
				seat.getPlayer().setStrengthCards(CardHandRanking.calculateStrengthCards(cards));
			}
			getGameCore().getCounterPlayersRemaining().decrement();
			if(getGameCore().getCounterPlayersRemaining().value() == 0) {
				getGameCore().setGameState(GameStates.SHOWDOWN_CARDS);
			}
		}
	}

}
