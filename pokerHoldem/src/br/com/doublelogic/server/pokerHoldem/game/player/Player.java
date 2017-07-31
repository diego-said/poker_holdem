package br.com.doublelogic.server.pokerHoldem.game.player;

import br.com.doublelogic.server.pokerHoldem.game.cards.Card;

/**
 * Represents a player's game
 * 
 * @author diego.said
 *
 */
public class Player {

	/**
	 * Unique identification of the player
	 */
	private final int id;

	/**
	 * Number of chips user
	 */
	private int wallet;

	/**
	 * Last bet placed by the player
	 */
	private int bet;

	/**
	 * Unique cards from player
	 */
	private Card[] holeCards;

	/**
	 * Strength information of the player's hand
	 */
	private int strengthHand;

	/**
	 * Strength information of the player's cards
	 */
	private int strengthCards;


	public Player(int id) {
		this.id = id;

		holeCards = new Card[2];
	}

	public int getId() {
		return id;
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public int getStrengthHand() {
		return strengthHand;
	}

	public void setStrengthHand(int strengthHand) {
		this.strengthHand = strengthHand;
	}

	public int getStrengthCards() {
		return strengthCards;
	}

	public void setStrengthCards(int strengthCards) {
		this.strengthCards = strengthCards;
	}

	public int getWallet() {
		return wallet;
	}

	public void setWallet(int wallet) {
		this.wallet = wallet;
	}

	public void addWallet(int value) {
		wallet += value;
	}

	public Card[] getHoleCards() {
		return holeCards;
	}

	public void setHoleCards(Card[] holeCards) {
		this.holeCards = holeCards;
	}

	public void updateWallet(int tableBet) {
		wallet = wallet - tableBet + bet;
	}

}