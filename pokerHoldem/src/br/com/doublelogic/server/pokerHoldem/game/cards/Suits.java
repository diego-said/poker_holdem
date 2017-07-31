package br.com.doublelogic.server.pokerHoldem.game.cards;

/**
 * Represents the suits that a card can be. A suit is one of several categories into which the cards of a deck are divided.
 * Most often, each card bears one of several symbols showing to which suit it belongs.
 * 
 * @author diego.said
 *
 */
public enum Suits {

	SPADES(1),
	HEARTS(2),
	DIAMONDS(3),
	CLUBS(4),
	JOKER(0),
	;

	private final int value;

	private Suits(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static Suits getSuit(int value) {
		for(Suits suit : values()) {
			if(suit.getValue() == value) {
				return suit;
			}
		}
		return JOKER;
	}

}