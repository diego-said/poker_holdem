package br.com.doublelogic.server.pokerHoldem.game.seat;

import br.com.doublelogic.server.pokerHoldem.game.player.Player;

/**
 * Represents a place in the game that can be occupied by a particular player. Only one player is allowed to occupy the place at a time.
 * 
 * @author diego.said
 *
 */
public class Seat {

	/**
	 * The player who is occupying the seat.
	 */
	private Player player;

	/**
	 * The current state seat.
	 */
	private SeatStates state;

	/**
	 * The number of seat.
	 */
	private final int seatNumber;

	public Seat(int seatNumber) {
		this(seatNumber, null, SeatStates.EMPTY);
	}

	public Seat(int seatNumber, Player player, SeatStates state) {
		this.player = player;
		this.state = state;
		this.seatNumber = seatNumber;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public SeatStates getState() {
		return state;
	}

	public void setState(SeatStates state) {
		this.state = state;
	}

	public int getSeatNumber() {
		return seatNumber;
	}

	/**
	 * Changes the state of the seat for reserved.
	 */
	public void reserveSeat() {
		state = SeatStates.RESERVED;
	}

	/**
	 * Determines if the seat is reserved.
	 * @return <tt>true</tt> if the seat is reserved <tt>false</tt> otherwise
	 */
	public boolean isReserved() {
		return getState() == SeatStates.RESERVED;
	}

	/**
	 * Changes the state of the seat for folded
	 */
	public void setFolded() {
		state = SeatStates.FOLDED;
	}

	/**
	 * Remove player associated with this seat and put the seat in a state of emptiness.
	 */
	public void removePlayer() {
		player = null;
		state = SeatStates.EMPTY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + seatNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Seat other = (Seat) obj;
		if (seatNumber != other.seatNumber)
			return false;
		return true;
	}
	
}