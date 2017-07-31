package br.com.doublelogic.server.pokerHoldem.game.services;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameService;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

public class BetService extends GameService {

	public BetService(GameCore core) {
		super(core);
	}

	public void execute(int playerId, String betAction, int bet) {
		switch(getGameCore().getGameState()) {
			case PRE_FLOP:
			case FLOP:
			case TURN:
			case RIVER:
				Seat playerSeat = getGameCore().getPlayerSeat(playerId);
				if(playerSeat != null && playerSeat.getSeatNumber() == getGameCore().getLastAskBetPlayerIndex()) {
					if("raise".equals(betAction)) {
						if(bet < getGameCore().getBigBet()) {
							getGameCore().getGameServer().sendErrorMessage(playerId, "Bet is smaller than minimum bet.");
							getGameCore().getGameServer().askBet(playerId);
						} else if(bet < getGameCore().getLastRaise()) {
							getGameCore().getGameServer().sendErrorMessage(playerId, "Bet is smaller than last raise.");
							getGameCore().getGameServer().askBet(playerId);
						} else {
							bets(playerId, betAction, bet);
						}
					} else {
						bets(playerId, betAction, bet);
					}
				} else {
					String errorMsg = "You are not the betting user";
					getGameCore().getGameServer().sendErrorMessage(playerId, errorMsg);
				}
				break;
			default:
				//TODO: logar estado inválido do jogo
				break;
		}
	}

	private void bets(int playerId, String betAction, int bet) {
		Seat playerSeat = getGameCore().getPlayerSeat(playerId);

		if("raise".equals(betAction)) {
			int tableBet = getGameCore().getTableBet();
			tableBet += bet;
			getGameCore().setTableBet(tableBet);
			getGameCore().setLastRaise(bet);

			playerSeat.getPlayer().updateWallet(tableBet);
			playerSeat.getPlayer().setBet(tableBet);

			getGameCore().getGameServer().betsRaise(playerId, bet, playerSeat.getPlayer().getWallet());

			// count = 0
			getGameCore().setLastRaisePlayerIndex(playerSeat.getSeatNumber());
		} else if("call".equals(betAction)) {
			playerSeat.getPlayer().updateWallet(getGameCore().getTableBet());
			playerSeat.getPlayer().setBet(getGameCore().getTableBet());

			getGameCore().getGameServer().betsCall(playerId, playerSeat.getPlayer().getWallet());
		} else if("fold".equals(betAction)) {
			playerSeat.setState(SeatStates.FOLDED);
			getGameCore().getCounterFolded().increment();

			getGameCore().getGameServer().betsFold(playerId, playerSeat.getPlayer().getWallet());
		} else if("allin".equals(betAction)) {
			playerSeat.setState(SeatStates.ALL_IN);
			int allIntBet = playerSeat.getPlayer().getWallet() + playerSeat.getPlayer().getBet();
			getGameCore().getAllIns().put(allIntBet, playerSeat.getSeatNumber());

			if(allIntBet > getGameCore().getTableBet()) {
				int betTemp = allIntBet - getGameCore().getTableBet();
				getGameCore().setTableBet(allIntBet);

				playerSeat.getPlayer().updateWallet(getGameCore().getTableBet());
				playerSeat.getPlayer().setBet(getGameCore().getTableBet());

				getGameCore().setLastRaisePlayerIndex(playerSeat.getSeatNumber());
				if(allIntBet > getGameCore().getTableBet() + (getGameCore().getLastRaise() / 2)) {
					if (getGameCore().getLastRaise() < betTemp) {
						getGameCore().setLastRaise(betTemp);
					}

					getGameCore().getGameServer().betsAllInRaise(playerId, betTemp, playerSeat.getPlayer().getWallet());
				} else {
					getGameCore().getGameServer().betsAllInSpecialCall(playerId, betTemp, playerSeat.getPlayer().getWallet());
				}

			} else {
				playerSeat.getPlayer().updateWallet(allIntBet);
				playerSeat.getPlayer().setBet(allIntBet);

				getGameCore().getGameServer().betsAllInCall(playerId, allIntBet, playerSeat.getPlayer().getWallet());
			}
		}
		getGameCore().askBet();
	}
}