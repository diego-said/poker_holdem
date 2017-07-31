package br.com.doublelogic.server.pokerHoldem.game.cards;

/**
 * Represents a card with its number and suit, from a deck of 52 cards.
 * 
 * @author diego.said
 *
 */
public class Card implements Comparable<Card> {

	/**
	 * The value of card may be from 1 to 14 (1 = Ace or 14 = Ace, 11 = Jack, 12 = Queen and 13 = King) and 0 for the joker.
	 */
	private int value;

	private Suits suit;

	public Card() {
		value = 0;
		suit = Suits.JOKER;
	}

	public Card(int value, Suits suit) {
		this.value = value;
		this.suit = suit;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		if(value >= 0 && value <= 14) {
			this.value = value;
		}
	}

	public Suits getSuit() {
		return suit;
	}

	public void setSuit(Suits suit) {
		this.suit = suit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((suit == null) ? 0 : suit.hashCode());
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Card other = (Card) obj;
		if (suit == null) {
			if (other.suit != null) {
				return false;
			}
		} else if (!suit.equals(other.suit)) {
			return false;
		}
		if (value != other.value) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Card o) {
		if(getValue() < o.getValue()){
			return -1;
		} else if(getValue() > o.getValue()) {
			return 1;
		}
		return 0;
	}

}