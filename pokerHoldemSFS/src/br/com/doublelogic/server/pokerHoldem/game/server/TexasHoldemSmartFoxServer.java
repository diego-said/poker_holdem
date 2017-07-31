package br.com.doublelogic.server.pokerHoldem.game.server;

import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.events.InternalEventObject;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.GameStates;
import br.com.doublelogic.server.pokerHoldem.game.cards.Card;
import br.com.doublelogic.server.pokerHoldem.game.cards.Suits;
import br.com.doublelogic.server.pokerHoldem.game.player.Player;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.server.BlindTypes;
import br.com.doublelogic.server.pokerHoldem.game.server.GameServer;
import br.com.doublelogic.server.pokerHoldem.game.server.GameServerCommands;

public class TexasHoldemSmartFoxServer extends AbstractExtension implements GameServer {

	/**
	 * Class that helps in interaction with the server
	 */
	private ExtensionHelper extensionHelper;

	/**
	 * Central class of the game contains all the logic of the game
	 */
	private GameCore core;

	/**
	 * Saves the communication channel of each player using the player's id as key
	 */
	private Map<Integer, SocketChannel> playersChannel;

	/**
	 * Unique identification of the game room
	 */
	private int roomId;

	@Override
	public void init() {
		extensionHelper = ExtensionHelper.instance();

		core = new GameCore();
		playersChannel = new ConcurrentHashMap<Integer, SocketChannel>(GameCore.MAX_PLAYERS);

		Zone zone = extensionHelper.getZone(getOwnerZone());
		Room room = zone.getRoomByName(getOwnerRoom());
		if(room != null) {
			roomId = room.getId();
		}
	}

	@Override
	public void handleRequest(String command, ActionscriptObject ao, User user, int fromRoom) {
		GameServerCommands serveCommand = GameServerCommands.getGameServerCommand(command);
		switch (serveCommand) {
			case BLIND:
				String blindAction = ao.getString("action");
				boolean paid = ao.getBool("paid");
				core.blind(user.getUserId(), blindAction, paid);
				break;

			case BET:
				String betAction = ao.getString("action");
				int bet = (int) ao.getNumber("raisedAmount");
				core.bet(user.getUserId(), betAction, bet);
				break;

			case SEAT_SELECT:
				int seatNumber = (int) ao.getNumber("seat");
				core.seatSelect(user.getUserId(), seatNumber);
				break;

			case SITTING:
				int wallet = (int) ao.getNumber("wallet");
				core.sitting(user.getUserId(), wallet);
				break;

			case PLAYER_HAND:
				ActionscriptObject cards = ao.getObj("cards");
				Card[] playerHand = new Card[5];
				for (int i = 0; i < playerHand.length; i++) {
					playerHand[i] = new Card();

					String rank = cards.getString(i).substring(0, 1);
					String suit = cards.getString(i).substring(1, 2);

					if("A".equals(rank)) {
						playerHand[i].setValue(14);
					} else if("T".equals(rank)) {
						playerHand[i].setValue(10);
					} else if("J".equals(rank)) {
						playerHand[i].setValue(11);
					} else if("Q".equals(rank)) {
						playerHand[i].setValue(12);
					} else if("K".equals(rank)) {
						playerHand[i].setValue(13);
					} else {
						playerHand[i].setValue(Integer.parseInt(rank));
					}

					playerHand[i].setSuit(Suits.getSuit(Integer.parseInt(suit)));
				}
				core.playerHand(user.getUserId(), playerHand);
				break;

			case SHOW_OR_MUCK:
				boolean show = ao.getBool("show");
				core.showOrMuck(user.getUserId(), show);
				break;

			case SIT_OUT:
				core.sitOut(user.getUserId());
				break;

			case WAITING:
				core.waiting(user.getUserId());
				break;

			case UNKNOWN:
				//TODO: logar que o comando não é suportado
				break;
		}
	}

	@Override
	public void handleRequest(String arg0, String[] arg1, User arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleInternalEvent(InternalEventObject evt) {
		if(InternalEventObject.EVENT_JOIN.equals(evt.getEventName())) {
			User user = (User) evt.getObject("user");
			playersChannel.put( user.getUserId(), user.getChannel() );

			core.playerJoin(user.getUserId());
		} else if (
				InternalEventObject.EVENT_USER_EXIT.equals(evt.getEventName()) ||
				InternalEventObject.EVENT_USER_LOST.equals(evt.getEventName()) ) {
			User user = (User) evt.getObject("user");

			if(core.playerDetach(user.getUserId())) {
				playersChannel.remove(user.getUserId());
			}
		} else {
			// TODO: informar que o comando não é tratado
		}
	}

	@Override
	public void askBet(int lastBetPlayerId) {
		ActionscriptObject betMsg = new ActionscriptObject();
		betMsg.put("_cmd", "askBet");
		betMsg.putNumber("userId", lastBetPlayerId);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(betMsg, roomId, null, receiverList);
	}

	@Override
	public void askBlind(BlindTypes blindType, int... playersId) {
		ActionscriptObject blindMsg = new ActionscriptObject();
		blindMsg.put("_cmd", "askBlind");

		switch (blindType) {
			case SMALL_BLIND:
				blindMsg.putBool("small", true);
				blindMsg.putNumber("userId", playersId[0]);
				break;
			case BIG_BLIND:
				blindMsg.putBool("big", true);
				blindMsg.putNumber("userId", playersId[0]);
				break;
			case WAITING_LIST:
				LinkedList<ActionscriptObject> playerWaitingList = new LinkedList<ActionscriptObject>();
				for(int i=0; i<playersId.length; i++) {
					ActionscriptObject playerWaiting = new ActionscriptObject();
					playerWaiting.putNumber("userId", playersId[i]);
					playerWaitingList.add(playerWaiting);
				}
				blindMsg.putBool("waitingList", true);
				blindMsg.putCollection("playerWaitingList", playerWaitingList);
				break;
		}

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(blindMsg, roomId, null, receiverList);
	}

	@Override
	public void sendPlayersData(int receiverPlayerId, int buttonPlayerId, List<Player> players) {
		LinkedList<ActionscriptObject> playersList = new LinkedList<ActionscriptObject>();
		ActionscriptObject playerListAO = new ActionscriptObject();

		for(Player player : players) {
			ActionscriptObject playerContainer = new ActionscriptObject();

			Seat seat = core.getPlayerSeat(player.getId());

			playerContainer.putNumber("userId", player.getId());
			playerContainer.putNumber("seat",  seat.getSeatNumber());
			playerContainer.put("state", seat.getState());
			playerContainer.putNumber("wallet", player.getWallet());

			playersList.add(playerContainer);
		}
		playerListAO.put("_cmd", "playerList");
		playerListAO.putCollection("players", playersList);

		playerListAO.putNumber("playerButton", buttonPlayerId);

		sendResponse(playerListAO, roomId, null, getReceiverChannel(receiverPlayerId));
	}

	@Override
	public void sendPlayerDetach(int playerId) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "userExit");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendGameState(GameStates state, int receiverPlayerId) {
		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "changeState");
		message.put("gameState", state);
		sendResponse(message, roomId, null, getReceiverChannel(receiverPlayerId));
	}

	@Override
	public void sendErrorMessage(int playerId, String message) {
		ActionscriptObject errorContent = new ActionscriptObject();
		errorContent.put("errorMsg", message);

		ActionscriptObject errorMessage = new ActionscriptObject();
		errorMessage.put("_cmd", "message");
		errorMessage.put("message", "error");
		errorMessage.put("content", errorContent);

		LinkedList<SocketChannel> receiver = new LinkedList<SocketChannel>();
		receiver.add(playersChannel.get(playerId));

		sendResponse(errorMessage, roomId, null, receiver);
	}

	@Override
	public void sendSeatSelect(int playerId, int seatNumber) {
		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "seatReserved");
		message.putNumber("userId", playerId);
		message.putNumber("seat", seatNumber);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendSitting(int playerId, int seatNumber, int wallet) {
		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "playerWaiting");
		message.putNumber("userId", playerId);
		message.putNumber("seat", seatNumber);
		message.putNumber("wallet", wallet);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendShowOrMuck(int playerId, boolean show, Card[] cards) {
		ActionscriptObject message = new ActionscriptObject();

		message.put("_cmd", "cards");
		message.put("dealing", "muckPlayer");

		if (show) {
			message.putBool("show", true);

			ActionscriptObject holeCards = new ActionscriptObject();

			ActionscriptObject card0 = new ActionscriptObject();
			ActionscriptObject card1 = new ActionscriptObject();

			card0.putNumber("valor", cards[0].getValue());
			card0.put("naipe", cards[0].getSuit());

			card1.putNumber("valor", cards[1].getValue());
			card1.put("naipe", cards[0].getSuit());

			holeCards.put(0, card0);
			holeCards.put(1, card1);

			message.put("cards", holeCards);
			message.putNumber("userId", playerId);
		}
		else {
			message.putBool("show", false);
			message.putNumber("userId", playerId);
		}

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendSitOut(int playerId) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "playerSitOut");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendWaiting(int playerId) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "playerWaiting");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendSmallBlindMessage(boolean smallBlind) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putBool("small", smallBlind);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "blind");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendBigBlindMessage(boolean bigBlind) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putBool("big", bigBlind);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "blind");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendWaitingPlayerBlindMessage(int playerId, boolean waitingPlayer) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putBool("waitingPlayer", waitingPlayer);
		messageContent.putNumber("userId", playerId);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "blind");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendCommunityCardsFlopMessage(Card[] cards) {
		ActionscriptObject message = new ActionscriptObject();
		ActionscriptObject flopCards = new ActionscriptObject();
		ActionscriptObject card0 = new ActionscriptObject();
		ActionscriptObject card1 = new ActionscriptObject();
		ActionscriptObject card2 = new ActionscriptObject();

		message.put("_cmd", "cards");
		message.put("dealing", "flop");

		card0.putNumber("valor", cards[0].getValue());
		card0.put("naipe", cards[0].getSuit());

		card1.putNumber("valor", cards[1].getValue());
		card1.put("naipe", cards[1].getSuit());

		card2.putNumber("valor", cards[2].getValue());
		card2.put("naipe", cards[2].getSuit());

		flopCards.put(0, card0);
		flopCards.put(1, card1);
		flopCards.put(2, card2);

		message.put("cards", flopCards);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendCommunityCardTurnMessage(Card card) {
		ActionscriptObject message = new ActionscriptObject();
		ActionscriptObject turnCards = new ActionscriptObject();
		ActionscriptObject card0 = new ActionscriptObject();


		message.put("_cmd", "cards");
		message.put("dealing", "turn");

		card0.putNumber("valor", card.getValue());
		card0.put("naipe", card.getSuit());

		turnCards.put(0, card0);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendCommunityCardRiverMessage(Card card) {
		ActionscriptObject message = new ActionscriptObject();
		ActionscriptObject turnCards = new ActionscriptObject();
		ActionscriptObject card0 = new ActionscriptObject();


		message.put("_cmd", "cards");
		message.put("dealing", "river");

		card0.putNumber("valor", card.getValue());
		card0.put("naipe", card.getSuit());

		turnCards.put(0, card0);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendNewPlayerButtonMessage(int playerId) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "newButtonPlayer");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendHoleCardsMessage(int playerId, Card[] cards) {
		ActionscriptObject message = new ActionscriptObject();
		ActionscriptObject card0 = new ActionscriptObject();
		ActionscriptObject card1 = new ActionscriptObject();
		ActionscriptObject playerCards = new ActionscriptObject();

		message.put("_cmd", "cards");
		message.put("dealing", "holeCards");

		card0.putNumber("valor", cards[0].getValue());
		card0.put("naipe", cards[0].getSuit());

		card1.putNumber("valor", cards[1].getValue());
		card1.put("naipe", cards[1].getSuit());

		playerCards.put(0, card0);
		playerCards.put(1, card1);

		message.put("cards", cards);

		LinkedList<SocketChannel> receiver = new LinkedList<SocketChannel>();
		receiver.add(playersChannel.get(playerId));

		sendResponse(message, roomId, null, receiver);
	}

	@Override
	public void sendChangeStateMessage(GameStates state) {
		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "changeState");
		message.put("gameState", state);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendWinnerPlayer(int playerId, int wallet) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);
		messageContent.putNumber("wallet", wallet);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "winner");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void sendUpdatePotMessage(List<Integer> potList, int tableBet) {
		ActionscriptObject aoPotList = new ActionscriptObject();
		for (int i = 0; i < potList.size(); i++) {
			aoPotList.putNumber(i, potList.get(i));
		}

		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.put("potList", aoPotList);
		messageContent.putNumber("tableBet", tableBet);

		ActionscriptObject message = new ActionscriptObject();
		message.put("_cmd", "message");
		message.put("message", "potUpdate");
		message.put("content", messageContent);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	@Override
	public void betsRaise(int playerId, int bet, int wallet) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);
		messageContent.put("action", "raise");
		messageContent.putNumber("bet", bet);
		messageContent.putNumber("wallet", wallet);

		sendMessageAllPlayers("userBet", messageContent);
	}

	@Override
	public void betsCall(int playerId, int wallet) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);
		messageContent.put("action", "call");
		messageContent.putNumber("wallet", wallet);

		sendMessageAllPlayers("userBet", messageContent);
	}

	@Override
	public void betsFold(int playerId, int wallet) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);
		messageContent.put("action", "fold");
		messageContent.putNumber("wallet", wallet);

		sendMessageAllPlayers("userBet", messageContent);
	}

	@Override
	public void betsAllInRaise(int playerId, int bet, int wallet) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);
		messageContent.put("action", "allin-raise");
		messageContent.putNumber("bet", bet);
		messageContent.putNumber("wallet", wallet);

		sendMessageAllPlayers("userBet", messageContent);
	}

	@Override
	public void betsAllInSpecialCall(int playerId, int bet, int wallet) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);
		messageContent.put("action", "allin-specialcall");
		messageContent.putNumber("bet", bet);
		messageContent.putNumber("wallet", wallet);

		sendMessageAllPlayers("userBet", messageContent);
	}

	@Override
	public void betsAllInCall(int playerId, int bet, int wallet) {
		ActionscriptObject messageContent = new ActionscriptObject();
		messageContent.putNumber("userId", playerId);
		messageContent.put("action", "allin-call");
		messageContent.putNumber("bet", bet);
		messageContent.putNumber("wallet", wallet);

		sendMessageAllPlayers("userBet", messageContent);
	}

	@Override
	public void showdownCards(List<Player> listPlayers, List<Player> listWinnerPlayers, List<Player> listAllInPlayers) {
		ActionscriptObject message = new ActionscriptObject();

		LinkedList<ActionscriptObject> playersList = new LinkedList<ActionscriptObject>();

		message.put("_cmd", "cards");
		message.put("dealing", "showdown");

		for(Player player : listPlayers) {
			playersList.add(getPlayerCards(player));
		}

		for(Player player : listWinnerPlayers) {
			ActionscriptObject playerContainer = getPlayerCards(player);
			playerContainer.putBool("winner", true);
			playersList.add(playerContainer);
		}

		for(Player player : listAllInPlayers) {
			ActionscriptObject playerContainer = getPlayerCards(player);
			playerContainer.putBool("allin", true);
			playersList.add(playerContainer);
		}

		message.putCollection("players", playersList);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(message, roomId, null, receiverList);
	}

	/**
	 * Returns information about the player's cards into a structure ready to be sent to the client
	 * @param player  player who will be sent to the client
	 * @return the structure with the identification of the player and his cards
	 */
	private ActionscriptObject getPlayerCards(Player player) {
		ActionscriptObject playerContainer = new ActionscriptObject();

		ActionscriptObject cards = new ActionscriptObject();
		ActionscriptObject card0 = new ActionscriptObject();
		ActionscriptObject card1 = new ActionscriptObject();

		card0.putNumber("valor", player.getHoleCards()[0].getValue());
		card0.put("naipe",  player.getHoleCards()[0].getSuit());

		card1.putNumber("valor", player.getHoleCards()[1].getValue());
		card1.put("naipe",  player.getHoleCards()[1].getSuit());

		cards.put(0, card0);
		cards.put(1, card1);

		playerContainer.put("cards", cards);
		playerContainer.putNumber("userId", player.getId());

		return playerContainer;
	}

	/**
	 * Sends a message to all players in the game
	 * @param message message type
	 * @param content message content
	 */
	private void sendMessageAllPlayers(String message, ActionscriptObject content) {
		ActionscriptObject ao = new ActionscriptObject();
		ao.put("_cmd", "message");
		ao.put("message", ao);
		ao.put("content", content);

		// sends the message to all players
		LinkedList<SocketChannel> receiverList = new LinkedList<SocketChannel>(playersChannel.values());

		sendResponse(ao, roomId, null, receiverList);
	}

	/**
	 * Method that retrieves the communication channel of a particular player
	 * @param receiverPlayerId player's id
	 * @return A list of the communication channel of the player or an empty list
	 */
	private LinkedList<SocketChannel> getReceiverChannel(int receiverPlayerId) {
		LinkedList<SocketChannel> receiverChannel = new LinkedList<SocketChannel>();

		// getting the player's socket
		SocketChannel playerChannel = playersChannel.get(receiverPlayerId);
		if(playerChannel != null) {
			receiverChannel.add(playerChannel);
		}

		return receiverChannel;
	}

}