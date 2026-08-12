package com.example.blackjack.model;

public record Card(Rank rank, Suit suit) {
    public String getLabel() {
        return rank.getLabel();
    }

    public String getSuit() {
        return suit.getSymbol();
    }

    public int getValue() {
        return rank.getValue();
    }

    public boolean isAce() {
        return rank == Rank.ACE;
    }
}
