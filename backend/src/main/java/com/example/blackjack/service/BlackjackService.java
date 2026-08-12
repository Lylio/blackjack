package com.example.blackjack.service;

import com.example.blackjack.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlackjackService {
    private final Map<String, BlackjackGame> games = new ConcurrentHashMap<>();

    public GameResponse start(double wager) {
        if (wager <= 0 || wager > 1000) {
            throw new IllegalArgumentException("Wager must be between £0.01 and £1,000.");
        }

        BlackjackGame game = new BlackjackGame();
        game.setWager(round(wager));
        game.setBankroll(round(1000.00 - wager));

        game.getPlayer().add(game.draw());
        game.getDealer().add(game.draw());
        game.getPlayer().add(game.draw());
        game.getDealer().add(game.draw());

        String id = UUID.randomUUID().toString();
        games.put(id, game);

        if (game.getPlayer().isBlackjack()) {
            if (game.getDealer().isBlackjack()) {
                game.setBankroll(round(1000.00));
                game.setStatus(GameStatus.PUSH);
                game.setMessage("Both have blackjack — push.");
            } else {
                game.setBankroll(round(1000.00 + wager * 1.5));
                game.setStatus(GameStatus.PLAYER_BLACKJACK);
                game.setMessage("Blackjack! You win 3:2.");
            }
        } else if (game.getDealer().isBlackjack()) {
            game.setBankroll(round(1000.00));
            game.setStatus(GameStatus.DEALER_WIN);
            game.setMessage("Dealer has blackjack.");
        } else {
            game.setStatus(GameStatus.PLAYER_TURN);
            game.setMessage("Your turn.");
        }

        return response(id, game);
    }

    public GameResponse hit(String id) {
        BlackjackGame game = get(id);
        requirePlayerTurn(game);

        game.getPlayer().add(game.draw());

        if (game.getPlayer().isBust()) {
            game.setStatus(GameStatus.PLAYER_BUST);
            game.setMessage("Bust! Dealer wins.");
        } else if (game.getPlayer().getValue() == 21) {
            dealerPlay(game);
        } else {
            game.setMessage("Hit or stand?");
        }

        return response(id, game);
    }

    public GameResponse stand(String id) {
        BlackjackGame game = get(id);
        requirePlayerTurn(game);
        dealerPlay(game);
        return response(id, game);
    }

    private void dealerPlay(BlackjackGame game) {
        game.setStatus(GameStatus.DEALER_TURN);

        while (game.getDealer().getValue() < 17) {
            game.getDealer().add(game.draw());
        }

        int playerValue = game.getPlayer().getValue();
        int dealerValue = game.getDealer().getValue();

        if (game.getDealer().isBust()) {
            game.setBankroll(round(1000.00 + game.getWager()));
            game.setStatus(GameStatus.DEALER_BUST);
            game.setMessage("Dealer busts — you win!");
        } else if (dealerValue > playerValue) {
            game.setBankroll(round(1000.00));
            game.setStatus(GameStatus.DEALER_WIN);
            game.setMessage("Dealer wins.");
        } else if (dealerValue < playerValue) {
            game.setBankroll(round(1000.00 + game.getWager()));
            game.setStatus(GameStatus.PLAYER_WIN);
            game.setMessage("You win!");
        } else {
            game.setBankroll(round(1000.00));
            game.setStatus(GameStatus.PUSH);
            game.setMessage("Push — your wager is returned.");
        }
    }

    private void requirePlayerTurn(BlackjackGame game) {
        if (game.getStatus() != GameStatus.PLAYER_TURN) {
            throw new IllegalStateException("The hand is already finished.");
        }
    }

    private BlackjackGame get(String id) {
        BlackjackGame game = games.get(id);
        if (game == null) {
            throw new IllegalArgumentException("Game not found.");
        }
        return game;
    }

    private GameResponse response(String id, BlackjackGame game) {
        List<CardView> dealerCards = game.getDealer().getCards().stream()
                .map(CardView::new)
                .toList();

        if (game.getStatus() == GameStatus.PLAYER_TURN && dealerCards.size() >= 2) {
            dealerCards = List.of(dealerCards.get(0), CardView.hiddenCard());
        }

        return new GameResponse(
                id,
                dealerCards,
                game.getPlayer().getCards().stream().map(CardView::new).toList(),
                game.getDealer().getValue(),
                game.getPlayer().getValue(),
                game.getBankroll(),
                game.getWager(),
                game.getStatus().name(),
                game.getMessage()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record CardView(String rank, String suit, boolean hidden) {
        public CardView(Card card) {
            this(card.getLabel(), card.getSuit(), false);
        }

        public static CardView hiddenCard() {
            return new CardView("", "", true);
        }
    }

    public record GameResponse(
            String gameId,
            List<CardView> dealerCards,
            List<CardView> playerCards,
            int dealerValue,
            int playerValue,
            double bankroll,
            double wager,
            String status,
            String message
    ) {}
}
