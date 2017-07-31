package br.com.doublelogic.server.pokerHoldem.game.server;

import java.util.List;

import br.com.doublelogic.server.pokerHoldem.game.GameStates;
import br.com.doublelogic.server.pokerHoldem.game.cards.Card;
import br.com.doublelogic.server.pokerHoldem.game.player.Player;

/**
 * Determines all the messages that the poker's server must execute.
 * 
 * @author diego.said
 *
 */
public interface GameServer {

	/**
	 * Asks each player in the game to make your bet
	 * @param lastBetPlayerId identification of the last player who made ​​the bet
	 */
	void askBet(int lastBetPlayerId);

	/**
	 * Asks each player in the game to make your blind
	 * @param blindType type of blind
	 * @param playersId one or more player's id
	 */
	void askBlind(BlindTypes blindType, int... playersId);

	/**
	 * Sends a message to every player in the game informing that the big blind was paid.
	 * @param bigBlind <tt>true</tt> if the big blind was paid <tt>false</tt> otherwise
	 */
	void sendBigBlindMessage(boolean bigBlind);

	/**
	 * Sends a message to all players in the game with the new state of the game
	 * @param state new state of the game
	 */
	void sendChangeStateMessage(GameStates state);

	/**
	 * Sends a message to all players in the game with the information of the community card of the river
	 * @param card the community card of the river
	 */
	void sendCommunityCardRiverMessage(Card card);

	/**
	 * Sends a message to all players in the game with the information of the community cards of the flop
	 * @param cards the three community cards of the flop
	 */
	void sendCommunityCardsFlopMessage(Card[] cards);

	/**
	 * Sends a message to all players in the game with the information of the community card of the turn
	 * @param card the community card of the turn
	 */
	void sendCommunityCardTurnMessage(Card card);

	/**
	 * Sends an error message for a certain player
	 * @param playerId identification of the player
	 * @param message error message
	 */
	void sendErrorMessage(int playerId, String message);

	/**
	 * Sends the state of the game for a certain player.
	 * @param state state of the game to be sent
	 * @param receiverPlayerId identification of the player who receives the message
	 */
	void sendGameState(GameStates state, int receiverPlayerId);

	/**
	 * Sends a message to a certain player who informs his cards.
	 * @param playerId identification of the player
	 * @param cards player's cards
	 */
	void sendHoleCardsMessage(int playerId, Card[] cards);

	/**
	 * Sends a message to all players in the game with the information of new dealer player
	 * @param playerId identification of the player
	 */
	void sendNewPlayerButtonMessage(int playerId);

	/**
	 * Send a message to all players that a certain player left the game.
	 * @param playerId identification of the player who was removed
	 */
	void sendPlayerDetach(int playerId);

	/**
	 * Sends a message to a player of the game with dealer's information and information of certain players.
	 * @param receiverPlayerId identification of the player who receives the message
	 * @param buttonPlayerId current dealer's id
	 * @param players list of players that the information will be sent
	 */
	void sendPlayersData(int receiverPlayerId, int buttonPlayerId, List<Player> players);

	/**
	 * Sends a message to every player in the game that a player is on the chosen seat.
	 * @param playerId identification of the player
	 * @param seatNumber selected seat number
	 */
	void sendSeatSelect(int playerId, int seatNumber);

	/**
	 * Sends a message to each player of the game with information about the player's cards
	 * @param playerId identification of the player
	 * @param show reports if the player displays the cards or not
	 * @param cards player's cards
	 */
	void sendShowOrMuck(int playerId, boolean show, Card[] cards);

	/**
	 * Sends a message to every player in the game that the player is not in his seat.
	 * @param playerId identification of the player
	 */
	void sendSitOut(int playerId);

	/**
	 * Sends a message to every player in the game that the player took his seat and his amount of chips.
	 * @param playerId identification of the player
	 * @param seatNumber selected seat number
	 * @param wallet amount of chips
	 */
	void sendSitting(int playerId, int seatNumber, int wallet);

	/**
	 * Sends a message to every player in the game informing that the small blind was paid.
	 * @param smallBlind <tt>true</tt> if the small blind was paid <tt>false</tt> otherwise
	 */
	void sendSmallBlindMessage(boolean smallBlind);

	/**
	 * Sends a message to all players in the game with the information about the pots in the table
	 * @param potList list of all pots in the game
	 * @param tableBet last table's bet done
	 */
	void sendUpdatePotMessage(List<Integer> potList, int tableBet);

	/**
	 * Sends a message to every player in the game to inform the player are waiting for the next hand.
	 * @param playerId identification of the player
	 */
	void sendWaiting(int playerId);

	/**
	 * Sends a message to all players in the game that a player on standby will pay the big blind.
	 * @param playerId identification of the player
	 * @param waitingPlayer <tt>true</tt> if the player wil pay the big blind <tt>false</tt> otherwise
	 */
	void sendWaitingPlayerBlindMessage(int playerId, boolean waitingPlayer);

	/**
	 * Sends a message to all players in the game with the information about the winner player
	 * @param playerId identification of the player
	 * @param wallet amount of chips of the player
	 */
	void sendWinnerPlayer(int playerId, int wallet);

	/**
	 * Sends a message to all players in the game with the player's raise bet.
	 * @param playerId identification of the player
	 * @param bet bet's value
	 * @param wallet amount of chips of the player
	 */
	void betsRaise(int playerId, int bet, int wallet);

	/**
	 * Sends a message to all players in the game that the player will pay the bet.
	 * @param playerId identification of the player
	 * @param wallet amount of chips of the player
	 */
	void betsCall(int playerId, int wallet);

	/**
	 * Sends a message to all players in the game that the player will not pay the bet.
	 * @param playerId identification of the player
	 * @param wallet amount of chips of the player
	 */
	void betsFold(int playerId, int wallet);

	/**
	 * Sends a message to all players in the game that the player will bet all the chips.
	 * Informs that the bet is being increased
	 * @param playerId identification of the player
	 * @param bet bet's value
	 * @param wallet amount of chips of the player
	 */
	void betsAllInRaise(int playerId, int bet, int wallet);

	/**
	 * Sends a message to all players in the game that the player will bet all the chips.
	 * @param playerId identification of the player
	 * @param bet bet's value
	 * @param wallet amount of chips of the player
	 */
	void betsAllInSpecialCall(int playerId, int bet, int wallet);

	/**
	 * Sends a message to all players in the game that the player will bet all the chips.
	 * Informs that the bet is equal to the table.
	 * @param playerId identification of the player
	 * @param bet bet's value
	 * @param wallet amount of chips of the player
	 */
	void betsAllInCall(int playerId, int bet, int wallet);

	/**
	 * Sends a message to all players in the game with the information about the cards of certain players.
	 * @param listPlayers players who were playing
	 * @param listWinnerPlayers players who have won
	 * @param listAllInPlayers players who bet all their chips
	 */
	void showdownCards(List<Player> listPlayers, List<Player> listWinnerPlayers, List<Player> listAllInPlayers);

}