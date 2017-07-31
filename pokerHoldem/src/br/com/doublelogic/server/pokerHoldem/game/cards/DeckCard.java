package br.com.doublelogic.server.pokerHoldem.game.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a deck of cards in the game. The deck consists of 52 cards, 13 of each suit.
 * 
 * @author diego.said
 *
 */
public class DeckCard {

	/**
	 * Deck of cards with 52 cards
	 */
	private List<Card> deck;

	/**
	 * List of burned cards
	 */
	private List<Card> burnedList;

	/**
	 * List of cards used
	 */
	private List<Card> usedList;

	public DeckCard() {
		deck = new ArrayList<Card>(52);

		burnedList = new ArrayList<Card>();
		usedList = new ArrayList<Card>();

		// generates the 13 cards, and the ace is worth 14
		for(int value = 2; value < 15; value++) {
			deck.add(new Card(value, Suits.SPADES));
			deck.add(new Card(value, Suits.HEARTS));
			deck.add(new Card(value, Suits.DIAMONDS));
			deck.add(new Card(value, Suits.CLUBS));
		}
	}

	/**
	 * Shuffles the cards in the deck
	 */
	public void shuffle() {
		Collections.shuffle(deck, new Random(System.currentTimeMillis()));
	}

	/**
	 * It burns the next card in the deck
	 */
	public void burnCard() {
		if(deck.size() >= 1) {
			burnedList.add(deck.remove(0));
		}
	}

	/**
	 * Grab the next card in the deck if it exists
	 * @return the next card or <tt>null</tt> if there are no more cards in the deck
	 */
	public Card getCard() {
		if(deck.size() >= 1) {
			Card card = deck.remove(0);
			usedList.add(card);
			return card;
		} else
			return null;
	}

}
