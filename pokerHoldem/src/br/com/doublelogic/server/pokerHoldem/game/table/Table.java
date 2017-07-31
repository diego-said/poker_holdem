package br.com.doublelogic.server.pokerHoldem.game.table;

import java.util.ArrayList;
import java.util.List;

import br.com.doublelogic.server.pokerHoldem.game.GameCore;
import br.com.doublelogic.server.pokerHoldem.game.seat.Seat;
import br.com.doublelogic.server.pokerHoldem.game.seat.SeatStates;

/**
 * Represents the game table with their seats, helping to control the seats and the dealer's player
 * @author diego.said
 *
 */
public class Table {

	/**
	 * Position of the player who is the big blind
	 */
	private int bigBlindIndex;

	/**
	 * Position of the player who is the dealer
	 */
	private int dealerIndex;

	/**
	 * List of all available seats
	 */
	private final List<Seat> seats;

	/**
	 * Position of the player who is the small blind
	 */
	private int smallBlindIndex;

	public Table() {
		seats = new ArrayList<Seat>(GameCore.MAX_PLAYERS);

		dealerIndex = 0;
		smallBlindIndex = 0;
		bigBlindIndex = 0;

		for(int i=0; i<GameCore.MAX_PLAYERS; i++) {
			seats.add(new Seat(i));
		}
	}

	/**
	 * Retrieves the seat of the big blind player
	 * @return seat of the player
	 */
	public Seat getBigBlindSeat() {
		return seats.get(bigBlindIndex);
	}

	/**
	 * Retrieves the seat of the dealer player
	 * @return seat of the player
	 */
	public Seat getDealerSeat() {
		return seats.get(dealerIndex);
	}

	/**
	 * Returns the seat of the player who will be the next big blind.
	 * @return returns the next big blind's seat or <tt>null</tt> if there is no seat
	 */
	public Seat getNextBigBlindSeat() {
		bigBlindIndex = smallBlindIndex + 1;
		for(int i=0; i<GameCore.MAX_PLAYERS; i++, bigBlindIndex++) {
			/*
			 *  If the position of the next seat to be equal to the number of seats at the table
			 *  back to the first seat
			 */
			if(smallBlindIndex == GameCore.MAX_PLAYERS) {
				smallBlindIndex = 0;
			}

			Seat bigBlindSeat = seats.get(bigBlindIndex);

			// if the big blind is equal to the dealer means that there are only two players in the game
			if(bigBlindIndex == dealerIndex) {
				bigBlindIndex = smallBlindIndex;
				smallBlindIndex = dealerIndex;
				return seats.get(bigBlindIndex);
			} else if(bigBlindSeat.getState() == SeatStates.PLAYING || bigBlindSeat.getState() == SeatStates.WAITING) {
				return bigBlindSeat;
			}
		}
		return null;
	}

	/**
	 * Returns the seat of the player who will be the next dealer. If no players have been
	 * the dealer, the next dealer will be the first seat that is playing.
	 * @return returns the next dealer's seat or <tt>null</tt> if there is no seat
	 */
	public Seat getNextDealerSeat() {
		for(int i=0; i<GameCore.MAX_PLAYERS; i++, dealerIndex++) {
			/*
			 *  If the position of the next seat to be equal to the number of seats at the table
			 *  back to the first seat
			 */
			if(dealerIndex == GameCore.MAX_PLAYERS) {
				dealerIndex = 0;
			}

			Seat dealerSeat = seats.get(dealerIndex);
			if(dealerSeat != null) {
				switch (dealerSeat.getState()) {
					case ALL_IN:
					case FOLDED:
					case PLAYING:
					case WAITING:
						return dealerSeat;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the seat of the player who will be the next small blind.
	 * @return returns the next small blind's seat or <tt>null</tt> if there is no seat
	 */
	public Seat getNextSmallBlindSeat() {
		smallBlindIndex = dealerIndex + 1;
		for(int i=0; i<GameCore.MAX_PLAYERS; i++, smallBlindIndex++) {
			/*
			 *  If the position of the next seat to be equal to the number of seats at the table
			 *  back to the first seat
			 */
			if(smallBlindIndex == GameCore.MAX_PLAYERS) {
				smallBlindIndex = 0;
			}

			Seat smallBlindSeat = seats.get(smallBlindIndex);
			if(smallBlindSeat.getState() == SeatStates.PLAYING) {
				return smallBlindSeat;
			} else if(smallBlindIndex == dealerIndex) {
				//TODO: talvez possa ser alterado para comunicar um fim de jogo, porque só existe um jogador na sala
				return null;
			}
		}
		return null;
	}

	/**
	 * Retrieves the seat of a particular place at the table
	 * @param index place at the table
	 * @return returns the {@link Seat} or <tt>null</tt> if there is no seat
	 */
	public Seat getSeat(int index) {
		return seats.get(index);
	}

	/**
	 * Returns the list of seats from the table, the seats being occupied or not
	 * @return list of seats
	 */
	public List<Seat> getSeats() {
		return seats;
	}

	/**
	 * Retrieves the seat of the small blind player
	 * @return seat of the player
	 */
	public Seat getSmallBlindSeat() {
		return seats.get(smallBlindIndex);
	}

	/**
	 * Retrieves the list of players waiting
	 * @return the list of players waiting or an empty list
	 */
	public List<Seat> getWaitingPlayers() {
		List<Seat> list = new ArrayList<Seat>();
		for(Seat seat : seats) {
			if(seat.getState() == SeatStates.WAITING) {
				list.add(seat);
			}
		}
		return list;
	}

	/**
	 * Returns the next assigned seat at the table. Starting with the informed position.
	 * @param index position to start the search
	 * @return returns the next {@link Seat} or <tt>null</tt> if there is no seat
	 */
	public Seat nextSeat(int index) {
		for(int i=0; i<GameCore.MAX_PLAYERS; i++, index++) {
			/*
			 *  If the position of the next seat to be equal to the number of seats at the table
			 *  back to the first seat
			 */
			if(index == GameCore.MAX_PLAYERS) {
				index = 0;
			}

			// checks whether the seat was assigned
			Seat seat = seats.get(index);
			if(seat != null && seat.getState() != SeatStates.EMPTY) {
				return seat;
			}
		}
		return null;
	}

	/**
	 * Assign the seat to a specific table position
	 * @param index place at the table
	 * @param seat seat that will be assigned
	 */
	public void setSeat(int index, Seat seat) {
		seats.set(index, seat);
	}

}