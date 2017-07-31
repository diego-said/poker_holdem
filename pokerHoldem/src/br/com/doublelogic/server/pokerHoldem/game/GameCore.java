package br.com.doublelogic.server.pokerHoldem.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import br.com.doublelogic.server.pokerHoldem.game.cards.Card;
import br.com.doublelogic.server.pokerHoldem.game.cards.DeckCard;
import br.com.doublelogic.server.pokerHoldem.game.player.Player;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;
import br.com.doublelogic.server.pokerHoldem.game.server.BlindTypes;
import br.com.doublelogic.server.pokerHoldem.game.server.GameServer;
import br.com.doublelogic.server.pokerHoldem.game.services.BetService;
import br.com.doublelogic.server.pokerHoldem.game.services.BlindService;
import br.com.doublelogic.server.pokerHoldem.game.services.PlayerDetachService;
import br.com.doublelogic.server.pokerHoldem.game.services.PlayerHandService;
import br.com.doublelogic.server.pokerHoldem.game.services.PlayerJoinService;
import br.com.doublelogic.server.pokerHoldem.game.services.SeatSelectService;
import br.com.doublelogic.server.pokerHoldem.game.services.ShowOrMuckService;
import br.com.doublelogic.server.pokerHoldem.game.services.SitOutService;
import br.com.doublelogic.server.pokerHoldem.game.services.SittingService;
import br.com.doublelogic.server.pokerHoldem.game.services.WaitingService;
import br.com.doublelogic.server.pokerHoldem.game.table.Table;
import br.com.doublelogic.server.pokerHoldem.game.util.Counter;

/**
 * Central class of the game, responsible for all the game controls and logic.
 * 
 * @author diego.said
 *
 */
public class GameCore {

	/**
	 * Maximum number of players
	 */
	public static final int MAX_PLAYERS = 9;

	private GameStates gameState;

	private GameServer gameServer;

	/**
	 * Map with all players in the game, with player's id as key
	 */
	private final Map<Integer, Player> players;

	/**
	 * Represents the game table with their seats
	 */
	private final Table gameTable;

	/**
	 * Map showing the relationship between player x seat, used as key the player's id and the value as seat's id
	 */
	private final Map<Integer, Integer> seatPlayers;

	/**
	 * Number of players who are playing
	 */
	private Counter counterPlaying;

	/**
	 * Number of players who are folding
	 */
	private Counter counterFolded;

	/**
	 * Number of players who are in their seats
	 */
	private Counter counterSeated;

	/**
	 * Number of players still playing the hand
	 */
	private Counter counterPlayersRemaining;

	/**
	 * Number of players waiting to enter the game
	 */
	private Counter counterPlayersWaiting;

	/**
	 * Controls the deck of cards
	 */
	private DeckCard deck;

	/**
	 * Table's cards that are common to all players in the game
	 */
	private CommunityCards communityCards;

	/**
	 * Identifier of the player who is the dealer
	 */
	private int buttonPlayerId;

	/**
	 * Index of the last player who made ​​a raise
	 */
	private int lastRaisePlayerIndex;

	/**
	 * Index of the last player who was asked to bet
	 */
	private int lastAskBetPlayerIndex;

	/**
	 * Controls if the small blind player has been asked to pay the blind
	 */
	private boolean smallBlind;

	/**
	 * Controls if the big blind player has been asked to pay the blind
	 */
	private boolean bigBlind;

	/**
	 * Value of big bet
	 */
	private int bigBet;

	/**
	 * Value of small bet
	 */
	private int smallBet;

	/**
	 * Value bet on the table
	 */
	private int tableBet;

	/**
	 * Last increase which was done on a bet
	 */
	private int lastRaise;

	/**
	 * List of seats that bet all the chips
	 */
	private LinkedList<Integer> allInSeats;
	
	/**
	 * Map of relationship between the amount bet of the all in and the player
	 */
	private TreeMap<Integer, Integer> allIns;
	
	/**
	 * Strength of the hand that is winning
	 */
	private int winningHandStrength;
	
	/**
	 * Winners list seats
	 */
	private List<Seat> winningSeats;
	
	/**
	 * Position of the pot in the current list of pots
	 */
	private int currentPot;
	
	/**
	 * List of the pots of the game
	 */
	private LinkedList<Integer> potList;

	public GameCore() {
		gameState = GameStates.GAME_STOP;

		gameTable = new Table();

		players = new ConcurrentHashMap<Integer, Player>(MAX_PLAYERS);
		seatPlayers = new ConcurrentHashMap<Integer, Integer>(MAX_PLAYERS);

		allIns = new TreeMap<Integer, Integer>();
		allInSeats = new LinkedList<Integer>();
		winningSeats = new ArrayList<Seat>();
		
		potList = new LinkedList<Integer>();

		counterPlaying = new Counter();
		counterFolded = new Counter();
		counterSeated = new Counter();
		counterPlayersRemaining = new Counter();
		counterPlayersWaiting = new Counter();

		deck = new DeckCard();
		communityCards = new CommunityCards();

		lastRaisePlayerIndex = -1;
		lastAskBetPlayerIndex = -1;

		lastRaise = -1;
	}

	/**
	 * Adds a the new player to the game
	 * @param newPlayer reference to the new player
	 */
	public void addPlayer(Player newPlayer) {
		players.put(newPlayer.getId(), newPlayer);
	}

	/**
	 * Reports that the player are in seat informed
	 * @param playerId identification of the player
	 * @param seatNumber identification of the seat
	 */
	public void addPlayerSeat(int playerId, int seatNumber) {
		seatPlayers.put(playerId, seatNumber);
	}

	/**
	 * Retrieves the player at the moment is the dealer
	 * @return dealer's player
	 */
	public Player getButtonPlayer() {
		return players.get(buttonPlayerId);
	}

	public GameServer getGameServer() {
		return gameServer;
	}

	public GameStates getGameState() {
		return gameState;
	}

	public Table getGameTable() {
		return gameTable;
	}

	public Counter getCounterPlaying() {
		return counterPlaying;
	}

	public Counter getCounterFolded() {
		return counterFolded;
	}

	public Counter getCounterSeated() {
		return counterSeated;
	}

	public Counter getCounterPlayersRemaining() {
		return counterPlayersRemaining;
	}

	public int getLastAskBetPlayerIndex() {
		return lastAskBetPlayerIndex;
	}

	public Map<Integer, Player> getPlayers() {
		return players;
	}

	/**
	 * Gets the seat in which the player sits
	 * @param playerId unique identification of the player
	 * @return Returns the seat or <tt>null</tt> if the player is not in any seat
	 */
	public Seat getPlayerSeat(int playerId) {
		Integer seatNumber = seatPlayers.get(playerId);
		if(seatNumber != null) {
			return gameTable.getSeat(seatNumber);
		}
		return null;
	}

	/**
	 * Gets all the players who are in a seat.
	 * @return a list of all the players or an empty list
	 */
	public List<Player> getSeatPlayers() {
		List<Player> seatPlayers = new ArrayList<Player>();
		for(Seat seat : gameTable.getSeats()) {
			if(seat.getState() != SeatStates.EMPTY) {
				seatPlayers.add(seat.getPlayer());
			}
		}
		return seatPlayers;
	}

	/**
	 * Method that removes a player from the game
	 * @param playerId unique identification of the player
	 * @return <tt>true</tt> if the player is removed <tt>false</tt> otherwise
	 */
	public boolean playerDetach(int playerId) {
		PlayerDetachService playerDetach = new PlayerDetachService(this);
		return playerDetach.execute(playerId);
	}

	/**
	 * Method that registers a new player in the game
	 * @param newPlayerId unique identification of the new player
	 */
	public void playerJoin(int newPlayerId) {
		PlayerJoinService playerJoin = new PlayerJoinService(this);
		playerJoin.execute(newPlayerId);
	}

	/**
	 * Removes a player from the game
	 * @param playerId identification of the player
	 * @return <tt>true</tt> if the player is removed <tt>false</tt> otherwise
	 */
	public boolean removePlayer(int playerId) {
		Player player = players.remove(playerId);
		if(player != null) {
			// determine if the player is in a seat
			if(seatPlayers.containsKey(player.getId())) {
				Seat playerSeat = gameTable.getSeat(seatPlayers.get(player.getId()));

				// checks if the player is playing a hand
				if(playerSeat.getState() == SeatStates.PLAYING || playerSeat.getState() == SeatStates.FOLDED) {
					counterPlaying.decrement();

					// verifies that the player was the last to bet
					if(player.getId() == lastAskBetPlayerIndex) {
						askBet();
					}
				} else if (playerSeat.getState() == SeatStates.RESERVED) {
					counterSeated.decrement();
				}

				// removing the player from the seat
				playerSeat.removePlayer();
				seatPlayers.remove(player.getId());
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes a player from the game
	 * @param player player will be removed
	 * @return <tt>true</tt> if the player is removed <tt>false</tt> otherwise
	 */
	public boolean removePlayer(Player player) {
		return removePlayer(player.getId());
	}

	/**
	 * Removes the information that the player is in the seat
	 * @param playerId identification of the player
	 * @param seatNumber identification of the seat
	 */
	public void removePlayerSeat(int playerId, int seatNumber) {
		seatPlayers.remove(playerId);
	}

	/**
	 * The player informs if will pay the blind or not
	 * @param playerId identification of the player
	 * @param action can be small, big or waitingPlayer
	 * @param paid <tt>true</tt> if the player will pay <tt>false</tt> otherwise
	 */
	public void blind(int playerId, String action, boolean paid) {
		BlindService blind = new BlindService(this);
		blind.execute(playerId, action, paid);
	}

	public void bet(int playerId, String betAction, int bet) {
		BetService betService = new BetService(this);
		betService.execute(playerId, betAction, bet);
	}

	/**
	 * Associates a particular player to sit at the table, if this seat is vacant.
	 * Otherwise the player is not associated with to the seat.
	 * @param playerId identification of the player
	 * @param seatNumber seat number desired
	 * @return <tt>true</tt> if the player is associated with to the seat or <tt>false</tt> otherwise
	 */
	public boolean seatSelect(int playerId, int seatNumber) {
		SeatSelectService seatSelect = new SeatSelectService(this);
		return seatSelect.execute(playerId, seatNumber);
	}

	/**
	 * The player says it is taking its seat and the amount of chips his entering the table.
	 * @param playerId identification of the player
	 * @param wallet amount of chips
	 */
	public void sitting(int playerId, int wallet) {
		SittingService sitting = new SittingService(this);
		sitting.execute(playerId, wallet);
	}

	/**
	 * Validates the cards of particular player
	 * @param playerId identification of the player
	 * @param cards player's cards
	 */
	public void playerHand(int playerId, Card[] cards) {
		PlayerHandService playerHand = new PlayerHandService(this);
		playerHand.execute(playerId, cards);
	}

	/**
	 * The player informs if shows his cards or not
	 * @param playerId identification of the player
	 * @param show <tt>true</tt> if the player shows his cards and <tt>false</tt> otherwise
	 */
	public void showOrMuck(int playerId, boolean show) {
		ShowOrMuckService showOrMuck = new ShowOrMuckService(this);
		showOrMuck.execute(playerId, show);
	}

	/**
	 * Reports that the player is not in his seat
	 * @param playerId identification of the player
	 */
	public void sitOut(int playerId) {
		SitOutService sitOut = new SitOutService(this);
		sitOut.execute(playerId);
	}

	/**
	 * Reports that the player are waiting for the next hand to play
	 * @param playerId identification of the player
	 */
	public void waiting(int playerId) {
		WaitingService waiting = new WaitingService(this);
		waiting.execute(playerId);
	}

	public void setGameState(GameStates gameState) {
		switch (gameState) {
			case GAME_START:
				gameStart();
				break;

			case BLINDS:
				blinds();
				break;

			case PRE_FLOP:
				preFlop();
				break;

			case FLOP:
				flop();
				break;
				
			case TURN:
				turn();
				break;
				
			case RIVER:
				river();
				break;
				
			case SHOWDOWN:
				showdown();
				break;
				
			case SHOWDOWN_CARDS:
				showdownCards();
				break;

		}
		this.gameState = gameState;
		gameServer.sendChangeStateMessage(gameState);
	}

	private void gameStart() {
		// going through all the players who were playing and cleaning your status
		for (Seat seat : gameTable.getSeats()) {
			if(seat.getState() == SeatStates.FOLDED) {
				seat.setState(SeatStates.PLAYING);
			} else if(seat.getState() == SeatStates.PLAYING) {
				seat.getPlayer().setBet(0);
			}
		}

		deck = new DeckCard();

		counterFolded = new Counter();
		
		smallBet = 10;
		bigBet = 20;

		winningHandStrength = -1;
		winningSeats.clear();
		
		allIns.clear();
		allInSeats.clear();
		
		currentPot = 0;
		potList.clear();
	}

	private void blinds() {
		smallBlind = false;
		bigBlind = false;
		counterPlayersWaiting = new Counter();

		askBlinds(BlindTypes.SMALL_BLIND);
	}

	private void preFlop() {
		dealCards();
		dealHoleCards();

		tableBet = bigBet;
		lastRaise = bigBet;

		lastRaisePlayerIndex = gameTable.getBigBlindSeat().getSeatNumber();
		lastAskBetPlayerIndex = gameTable.getBigBlindSeat().getSeatNumber();

		askBet();
	}

	private void flop() {
		updatePot();
		
		dealCommunityCards();

		lastRaisePlayerIndex = gameTable.getSmallBlindSeat().getSeatNumber();
		lastAskBetPlayerIndex = gameTable.getSmallBlindSeat().getSeatNumber();
		
		for(Seat seat : gameTable.getSeats()) {
			if(seat.getState() == SeatStates.PLAYING) {
				seat.getPlayer().setBet(0);
			}
		}

		tableBet = 0;
		lastRaise = 0;

		askBet();
	}

	private void turn() {
		updatePot();

		dealCommunityCards();

		lastRaisePlayerIndex = gameTable.getSmallBlindSeat().getSeatNumber();
		lastAskBetPlayerIndex = gameTable.getSmallBlindSeat().getSeatNumber();
		
		for(Seat seat : gameTable.getSeats()) {
			if(seat.getState() == SeatStates.PLAYING) {
				seat.getPlayer().setBet(0);
			}
		}
		
		tableBet = 0;
		lastRaise = 0;

		askBet();
	}
	
	private void river() {
		updatePot();

		dealCommunityCards();
		
		lastRaisePlayerIndex = gameTable.getSmallBlindSeat().getSeatNumber();
		lastAskBetPlayerIndex = gameTable.getSmallBlindSeat().getSeatNumber();

		for(Seat seat : gameTable.getSeats()) {
			if(seat.getState() == SeatStates.PLAYING) {
				seat.getPlayer().setBet(0);
			}
		}
		
		tableBet = 0;
		lastRaise = 0;

		askBet();
	}
	
	private void showdown() {
		updatePot();

		counterPlayersRemaining = new Counter(counterPlaying.value() - counterFolded.value());
	}
	
	private void showdownCards() {
		counterPlayersRemaining = new Counter(counterPlaying.value() - counterFolded.value());

		gameWinner();
		
		List<Player> listPlayers = new ArrayList<Player>(MAX_PLAYERS);
		List<Player> listWinnerPlayers = new ArrayList<Player>(MAX_PLAYERS);
		List<Player> listAllInPlayers = new ArrayList<Player>(MAX_PLAYERS);
		
		for(Seat seat : gameTable.getSeats()) {
			if(seat.getState() == SeatStates.PLAYING) {
				if(winningSeats.contains(seat)) {
					listWinnerPlayers.add(seat.getPlayer());
				} else {
					listPlayers.add(seat.getPlayer());
				}
			} else if(seat.getState() == SeatStates.ALL_IN) {
				listAllInPlayers.add(seat.getPlayer());
			}
		}

		gameServer.showdownCards(listPlayers, listWinnerPlayers, listAllInPlayers);
		
		endGame();
	}

	/**
	 * Determines which player won the game or if there was a tie between players
	 */
	private void gameWinner() {
		for(Seat seat : gameTable.getSeats()) {
			if(seat.getState() == SeatStates.PLAYING) {
				if(seat.getPlayer().getStrengthHand() > winningHandStrength) {
					winningHandStrength = seat.getPlayer().getStrengthHand();
					winningSeats.clear();
					winningSeats.add(seat);
				} else if(seat.getPlayer().getStrengthHand() == winningHandStrength) {
					int tieResult = seat.getPlayer().getStrengthCards() - winningSeats.get(0).getPlayer().getStrengthCards();
					if (tieResult == 0) {
						winningSeats.add(seat);
					} else if (tieResult > 0) {
						winningSeats.clear();
						winningSeats.add(seat);
					}
				}
			}
		}
	}

	public void askBet() {
		if(counterPlaying.value() <= counterFolded.value() + 1 || counterPlaying.value() < 2) {
			updatePot();
			endGame();
		} else {
			Seat seat = null;
			do {
				seat = gameTable.nextSeat(lastAskBetPlayerIndex);
				if(seat != null) {
					lastAskBetPlayerIndex = seat.getSeatNumber();
				}
			} while(seat != null && seat.getState() == SeatStates.PLAYING);

			if(seat != null) {
				if(lastRaisePlayerIndex == seat.getSeatNumber()) {
					if(gameState == GameStates.PRE_FLOP) {
						setGameState(GameStates.FLOP);
					} else if (gameState == GameStates.FLOP) {
						setGameState(GameStates.TURN);
					}  else if (gameState == GameStates.TURN) {
						setGameState(GameStates.RIVER);
					}
				} else {
					gameServer.askBet(seat.getPlayer().getId());
				}
			} else {
				//TODO: logar que não achou nenhum jogador
			}
		}
	}

	private void updatePot() {
		int pot = 0;
		
		for(Integer seatNumber : allIns.values()) {
			Seat seat = gameTable.getSeat(seatNumber.intValue());
			if(seat != null && seat.getPlayer().getBet() != 0) {
				int allInBet = seat.getPlayer().getBet();
				pot = potList.get(currentPot);
				for(Seat playerSeat : gameTable.getSeats()) {
					if(playerSeat.getState() != SeatStates.EMPTY && playerSeat.getPlayer().getBet() != 0) {
						pot += allInBet;
						playerSeat.getPlayer().addWallet(-allInBet);
					}
				}
				potList.set(currentPot, pot);
				potList.add(0);
				currentPot = potList.size() - 1;
				allInSeats.add(seatNumber);
			}
		}

		allIns.clear();

		pot = potList.get(currentPot);
		for (Seat seat : gameTable.getSeats()) {
			if (seat.getState() != SeatStates.EMPTY && seat.getPlayer().getBet() != 0) {
				pot += seat.getPlayer().getBet();
				seat.getPlayer().setBet(0);
			}
		}
		potList.set(currentPot, pot);
		
		gameServer.sendUpdatePotMessage(potList, tableBet);
	}

	public void endGame() {
		while(!potList.isEmpty()) {
			int pot = potList.removeLast();
			
			if (counterPlaying.value() <= counterFolded.value() + 1) {
				if (counterPlaying.value() == counterFolded.value() + 1) {
					for (Seat seat : gameTable.getSeats()) {
						if (seat.getState() == SeatStates.PLAYING) {
							winningSeats.add(seat);
							break;
						}
					}
				}
				//TODO: não faz sentido, perguntar!
				/*else {
					winningSeats.add(buttonSeat);
				}*/
			} else {
				pot = pot / winningSeats.size();
			}
			
			for (Seat seat : winningSeats) {
				seat.getPlayer().addWallet(pot);

				gameServer.sendWinnerPlayer(seat.getPlayer().getId(), seat.getPlayer().getWallet());
			}
			
			if(!allInSeats.isEmpty()) {
				int seatNumber = allInSeats.removeLast();
				Seat seat = gameTable.getSeat(seatNumber);
				
				if(seat.getPlayer().getStrengthHand() > winningHandStrength) {
					winningHandStrength = seat.getPlayer().getStrengthHand();
					winningSeats.clear();
					winningSeats.add(seat);
				} else if(seat.getPlayer().getStrengthHand() == winningHandStrength) {
					int tieResult = seat.getPlayer().getStrengthCards() - winningSeats.get(0).getPlayer().getStrengthCards();
					if (tieResult == 0) {
						winningSeats.add(seat);
					} else if (tieResult > 0) {
						winningSeats.clear();
						winningSeats.add(seat);
					}
				}
			}
		}
		
		setGameState(GameStates.GAME_STOP);
	}

	public void askBlinds(BlindTypes blind) {
		switch (blind) {
			case SMALL_BLIND:
				// seeking the small blind player
				Seat smallBlindSeat = gameTable.getNextSmallBlindSeat();
				gameServer.askBlind(BlindTypes.SMALL_BLIND, smallBlindSeat.getPlayer().getId());
				break;

			case BIG_BLIND:
				// seeking the big blind player
				Seat bigBlindSeat = gameTable.getNextBigBlindSeat();
				gameServer.askBlind(BlindTypes.BIG_BLIND, bigBlindSeat.getPlayer().getId());
				break;

			case WAITING_LIST:
				List<Seat> listSeats = gameTable.getWaitingPlayers();
				int[] list = new int[listSeats.size()];
				for(int i=0; i< listSeats.size(); i++) {
					list[i] = listSeats.get(i).getPlayer().getId();
					counterPlayersWaiting.increment();
				}
				gameServer.askBlind(BlindTypes.WAITING_LIST, list);
				break;
		}
	}

	/**
	 * Validates the cards informed by the player is in the game
	 * @param playerId identification of the player
	 * @param cards player's cards
	 * @return <tt>true</tt> if the cards are in play <tt>false</tt> otherwise
	 */
	public boolean validateCards(int playerId, Card[] cards) {
		Player player = players.get(playerId);
		if(player != null) {
			for(int i=0; i<cards.length; i++) {
				if(Arrays.binarySearch(player.getHoleCards(), cards[i]) < 0 && !communityCards.isCommunityCard(cards[i])) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean isSmallBlind() {
		return smallBlind;
	}

	public void setSmallBlind(boolean smallBlind) {
		this.smallBlind = smallBlind;
	}

	public boolean isBigBlind() {
		return bigBlind;
	}

	public void setBigBlind(boolean bigBlind) {
		this.bigBlind = bigBlind;
	}

	public int getBigBet() {
		return bigBet;
	}

	public int getSmallBet() {
		return smallBet;
	}

	public Counter getCounterPlayersWaiting() {
		return counterPlayersWaiting;
	}

	/**
	 * Distribute cards to all players and also the community cards
	 */
	private void dealCards() {
		deck.shuffle();

		// distributing the cards to the players
		Seat seat = gameTable.nextSeat(gameTable.getDealerSeat().getSeatNumber());
		while(seat != null && seat.getSeatNumber() != gameTable.getDealerSeat().getSeatNumber()) {
			if(seat.getState() == SeatStates.PLAYING) {
				giveHoleCards(seat);
			}
		}

		// distributing the cards to the dealer player
		giveHoleCards(gameTable.getDealerSeat());

		// distributing the community cards
		Card[] flopCards = new Card[3];
		flopCards[0] = deck.getCard();
		flopCards[1] = deck.getCard();
		flopCards[2] = deck.getCard();

		communityCards.setFlop(flopCards);
		communityCards.setTurn(deck.getCard());
		communityCards.setRiver(deck.getCard());
	}

	/**
	 * Distributes the cards to particular player
	 * @param seat seat where the player is
	 */
	private void giveHoleCards(Seat seat) {
		Card[] holeCards =  new Card[2];
		holeCards[0] = deck.getCard();
		holeCards[1] = deck.getCard();
		seat.getPlayer().setHoleCards(holeCards);
	}

	/**
	 * Informs each player in the game what are their cards
	 */
	private void dealHoleCards() {
		for(Seat seat : gameTable.getSeats()) {
			if(seat.getState() == SeatStates.PLAYING) {
				getGameServer().sendHoleCardsMessage(seat.getPlayer().getId(), seat.getPlayer().getHoleCards());
			}
		}
	}
	
	/**
	 * Informs each player in the game what are the community cards
	 */
	private void dealCommunityCards() {
		switch (gameState) {
		case FLOP:
			gameServer.sendCommunityCardsFlopMessage(communityCards.getFlop());
			break;

		case TURN:
			gameServer.sendCommunityCardTurnMessage(communityCards.getTurn());
			break;

		case RIVER:
			gameServer.sendCommunityCardRiverMessage(communityCards.getRiver());
			break;
		}
	}

	public int getTableBet() {
		return tableBet;
	}

	public void setTableBet(int tableBet) {
		this.tableBet = tableBet;
	}

	public int getLastRaise() {
		return lastRaise;
	}

	public void setLastRaise(int lastRaise) {
		this.lastRaise = lastRaise;
	}

	public int getLastRaisePlayerIndex() {
		return lastRaisePlayerIndex;
	}

	public void setLastRaisePlayerIndex(int lastRaisePlayerIndex) {
		this.lastRaisePlayerIndex = lastRaisePlayerIndex;
	}

	public TreeMap<Integer, Integer> getAllIns() {
		return allIns;
	}

}