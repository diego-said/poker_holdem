package br.com.doublelogic.server.pokerHoldem.game;

import java.util.Arrays;

import br.com.doublelogic.server.pokerHoldem.game.cards.Card;

/**
 * Structure with the five community cards
 * 
 * @author diego.said
 *
 */
public class CommunityCards {

	private Card[] cards;

	public CommunityCards() {
		// five is the number of community cards in the game
		cards = new Card[5];
	}

	/**
	 * Verifies if the card is a community card
	 * @param card to be verified
	 * @return <tt>true</tt> if is a community card <tt>false</tt> otherwise
	 */
	public boolean isCommunityCard(Card card) {
		return Arrays.binarySearch(cards, card) >= 0;
	}

	public void setFlop(Card[] flop) {
		for(int i = 0; i < flop.length; i++) {
			cards[i] = flop[i];
		}
	}

	public Card[] getFlop() {
		Card[] flop = new Card[3];
		for(int i = 0; i < flop.length; i++) {
			flop[i] = cards[i];
		}
		return flop;
	}

	public void setTurn(Card card) {
		cards[3] = card;
	}

	public Card getTurn() {
		return cards[3];
	}

	public void setRiver(Card card){
		cards[4] = card;
	}

	public Card getRiver(){
		return cards[4];
	}

}