package com.example.blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackjackGame {
    private final List<Card> deck = new ArrayList<>();
    private final Hand player = new Hand();
    private final Hand dealer = new Hand();
    private double bankroll = 1000.00;
    private double wager = 0.00;
    private GameStatus status = GameStatus.PLAYER_TURN;
    private String message = "";

    public BlackjackGame() {
        resetDeck();
    }

    public void resetDeck() {
        deck.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(deck);
    }

    public Card draw() {
        if (deck.isEmpty()) {
            resetDeck();
        }
        return deck.remove(deck.size() - 1);
    }

    public Hand getPlayer() { return player; }
    public Hand getDealer() { return dealer; }
    public double getBankroll() { return bankroll; }
    public void setBankroll(double bankroll) { this.bankroll = bankroll; }
    public double getWager() { return wager; }
    public void setWager(double wager) { this.wager = wager; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
