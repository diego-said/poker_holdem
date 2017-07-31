package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.GameStates;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;
import br.com.doublelogic.server.pokerHoldem.game.server.BlindTypes;

public class BlindService extends GameService {

	public BlindService(GameCore core) {
		super(core);
	}

	public void execute(int playerId, String action, boolean paid) {
		if("small".equals(action)) {
			Seat smallBlindSeat = getGameCore().getGameTable().getSmallBlindSeat();
			if(	smallBlindSeat != null &&
					smallBlindSeat.getPlayer().getId() == playerId &&
					!getGameCore().isSmallBlind() ) {
				if(paid) {
					smallBlindSeat.getPlayer().addWallet(-getGameCore().getSmallBet());
					smallBlindSeat.getPlayer().setBet(getGameCore().getSmallBet());
				} else {
					smallBlindSeat.setFolded();
					getGameCore().getCounterFolded().increment();
				}
				getGameCore().setSmallBlind(true);
				getGameCore().getGameServer().sendSmallBlindMessage(paid);
				getGameCore().askBlinds(BlindTypes.BIG_BLIND);
			}
		} else if("big".equals(action)) {
			Seat bigBlindSeat = getGameCore().getGameTable().getBigBlindSeat();
			if(	bigBlindSeat != null &&
					bigBlindSeat.getPlayer().getId() == playerId &&
					!getGameCore().isBigBlind() ) {
				if(paid) {
					if(bigBlindSeat.getState() == SeatStates.WAITING) {
						bigBlindSeat.setState(SeatStates.PLAYING);
						getGameCore().getCounterPlaying().increment();
					}

					bigBlindSeat.getPlayer().addWallet(-getGameCore().getBigBet());
					bigBlindSeat.getPlayer().setBet(getGameCore().getBigBet());
				} else {
					if(bigBlindSeat.getState() == SeatStates.PLAYING) {
						bigBlindSeat.setFolded();
						getGameCore().getCounterFolded().increment();
					}
				}
				getGameCore().setBigBlind(true);
				getGameCore().getGameServer().sendBigBlindMessage(paid);
				getGameCore().askBlinds(BlindTypes.WAITING_LIST);
			}
		} else if("waitingPlayer".equals(action) && getGameCore().isBigBlind() && getGameCore().isSmallBlind()) {
			Seat playerSeat = getGameCore().getPlayerSeat(playerId);
			if(playerSeat != null && playerSeat.getState() == SeatStates.WAITING) {
				if(paid) {
					playerSeat.setState(SeatStates.PLAYING);
					getGameCore().getCounterPlaying().increment();

					playerSeat.getPlayer().addWallet(-getGameCore().getBigBet());
					playerSeat.getPlayer().setBet(getGameCore().getBigBet());
				}
				getGameCore().getGameServer().sendWaitingPlayerBlindMessage(playerId, paid);
				getGameCore().getCounterPlayersWaiting().decrement();
			}
		} else {
			String errorMsg = "";
			if("small".equals(action) && getGameCore().isSmallBlind()) {
				errorMsg = "The small blind has already been paid. ";
			} else if("big".equals(action) && getGameCore().isBigBlind()) {
				errorMsg = "The big blind has already been paid. ";
			} else {
				errorMsg = "You are not the big blind nor the small blind. ";
			}

			getGameCore().getGameServer().sendErrorMessage(playerId, errorMsg);
		}

		if(getGameCore().isSmallBlind() &&
				getGameCore().isBigBlind() &&
				getGameCore().getCounterPlayersWaiting().value() == 0 &&
				getGameCore().getGameState() == GameStates.BLINDS) {
			getGameCore().setGameState(GameStates.PRE_FLOP);
		}
	}
}