package br.com.doublelogic.server.pokerHoldem.game.cards;

import java.util.Arrays;

/**
 * Class which calculates the strength of the player's hand
 * 
 * @author diego.said
 *
 */
public class CardHandRanking {

	/**
	 * Calculates the strength of the player's hand considering the ranking of poker hands
	 * @param cards the five player's cards
	 * @return a value that represents the strength of the player's hand
	 */
	public static int calculateStrengthHand(Card[] cards) {

		boolean isFlush = true;
		boolean isStraight = true;

		int[] pairs = {0, 0};
		int threeOfAKind = 0;
		int fourOfAKind = 0;

		Arrays.sort(cards);

		for (int i = 0; i < cards.length-1; i++) {
			// verifies if is a flush
			if(cards[i].getSuit() != cards[i+1].getSuit()) {
				isFlush = false;
			}

			// verifies if is a straigth
			if (cards[i+1].getValue() - cards[1].getValue() != 1) {
				isStraight = false;
			}

			if (!isFlush && !isStraight) {
				break;
			}
		}

		// verifies if is a straigth for change the value of ace
		if (
				cards[0].getValue() == 2 &&
				cards[1].getValue() == 3 &&
				cards[2].getValue() == 4 &&
				cards[3].getValue() == 5 &&
				cards[4].getValue() == 14 ) {
			isStraight = true;
			cards[4].setValue(1);
		}

		if (!isStraight && !isFlush) {
			// pair, three and four of a kind
			for (int i = 0; i < cards.length; i++) {
				int found = 0;
				for (int j = i + 1; j < cards.length; j++) {
					if (cards[i].getValue() == cards[j].getValue()) {
						found++;
					}
				}
				switch (found) {
				case 1:
					if (fourOfAKind != cards[i].getValue() && threeOfAKind != cards[i].getValue()) {
						if (pairs[0] == 0) {
							pairs[0] = cards[i].getValue();
						} else {
							if (pairs[0] > cards[i].getValue()) {
								pairs[1] = pairs[0];
								pairs[0] = cards[i].getValue();
							} else {
								pairs[1] = cards[i].getValue();
							}
						}
					}
					break;

				case 2:
					if (fourOfAKind != cards[i].getValue()) {
						threeOfAKind = cards[i].getValue();
					}
					break;

				case 3:
					fourOfAKind = cards[i].getValue();
					break;
				default:
					break;
				}
			}
		}

		int strength = 0;

		if (pairs[0] != 0) {
			strength += 1000 + pairs[0];
		}
		if (pairs[1] != 0) {
			strength += 1000 + (pairs[1] * 30);
		}
		if (threeOfAKind != 0) {
			strength += 3000 + (threeOfAKind * 30);
		}
		if (strength > 4000) {
			strength += 1000;
		}
		if (isStraight) {
			strength += 4000;
		}
		if (isFlush) {
			strength += 4500;
		}
		if (fourOfAKind != 0) {
			strength += 6000 + fourOfAKind;
		}

		return strength;
	}

	/**
	 * Calculates the strength of the player's cards.
	 * @param handPlayer the five player's cards
	 * @return a value that represents the strength of the player's cards
	 */
	public static int calculateStrengthCards(Card[] handPlayer) {
		int strength = 0;

		strength += handPlayer[0].getValue() * 1;
		strength += handPlayer[1].getValue() * 10;
		strength += handPlayer[2].getValue() * 100;
		strength += handPlayer[3].getValue() * 1000;
		strength += handPlayer[4].getValue() * 10000;

		return strength;
	}

}