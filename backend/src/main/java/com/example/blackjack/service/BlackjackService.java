```java
package com.example.blackjack.service;

import com.example.blackjack.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlackjackService {

    private static final double STARTING_BANKROLL = 1000.00;

    private final Map<String, BlackjackGame> games = new ConcurrentHashMap<>();

    // Current player's bankroll.
    // This persists between hands while the backend is running.
    private double bankroll = STARTING_BANKROLL;

    public synchronized GameResponse start(double wager) {

        if (wager <= 0 || wager > bankroll) {
            throw new IllegalArgumentException(
                    String.format(
                            "Wager must be between £0.01 and your current bankroll of £%.2f.",
                            bankroll
                    )
            );
        }

        BlackjackGame game = new BlackjackGame();

        game.setWager(round(wager));

        // Remove the wager from the bankroll when the hand starts.
        bankroll = round(bankroll - wager);
        game.setBankroll(bankroll);

        game.getPlayer().add(game.draw());
        game.getDealer().add(game.draw());
        game.getPlayer().add(game.draw());
        game.getDealer().add(game.draw());

        String id = UUID.randomUUID().toString();

        games.put(id, game);

        // Player blackjack
        if (game.getPlayer().isBlackjack()) {

            // Both player and dealer have blackjack = push
            if (game.getDealer().isBlackjack()) {

                bankroll = round(bankroll + wager);
                game.setBankroll(bankroll);

                game.setStatus(GameStatus.PUSH);
                game.setMessage("Both have blackjack — push.");

            } else {

                // Blackjack pays 3:2
                bankroll = round(bankroll + (wager * 2.5));
                game.setBankroll(bankroll);

                game.setStatus(GameStatus.PLAYER_BLACKJACK);
                game.setMessage("Blackjack! You win 3:2.");
            }

        // Dealer blackjack
        } else if (game.getDealer().isBlackjack()) {

            // Player loses the wager, which has already been removed.
            game.setBankroll(bankroll);

            game.setStatus(GameStatus.DEALER_WIN);
            game.setMessage("Dealer has blackjack.");

        } else {

            game.setStatus(GameStatus.PLAYER_TURN);
            game.setMessage("Your turn.");
        }

        return response(id, game);
    }

    public synchronized GameResponse hit(String id) {

        BlackjackGame game = get(id);

        requirePlayerTurn(game);

        game.getPlayer().add(game.draw());

        if (game.getPlayer().isBust()) {

            // Player loses the wager.
            game.setBankroll(bankroll);

            game.setStatus(GameStatus.PLAYER_BUST);
            game.setMessage("Bust! Dealer wins.");

        } else if (game.getPlayer().getValue() == 21) {

            dealerPlay(game);

        } else {

            game.setBankroll(bankroll);
            game.setMessage("Hit or stand?");
        }

        return response(id, game);
    }

    public synchronized GameResponse stand(String id) {

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

        // Dealer busts - player wins 1:1
        if (game.getDealer().isBust()) {

            bankroll = round(bankroll + (game.getWager() * 2));
            game.setBankroll(bankroll);

            game.setStatus(GameStatus.DEALER_BUST);
            game.setMessage("Dealer busts — you win!");

        // Dealer wins - wager already removed from bankroll
        } else if (dealerValue > playerValue) {

            game.setBankroll(bankroll);

            game.setStatus(GameStatus.DEALER_WIN);
            game.setMessage("Dealer wins.");

        // Player wins - return wager plus equal winnings
        } else if (dealerValue < playerValue) {

            bankroll = round(bankroll + (game.getWager() * 2));
            game.setBankroll(bankroll);

            game.setStatus(GameStatus.PLAYER_WIN);
            game.setMessage("You win!");

        // Push - return the original wager
        } else {

            bankroll = round(bankroll + game.getWager());
            game.setBankroll(bankroll);

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

        List<CardView> dealerCards = game.getDealer()
                .getCards()
                .stream()
                .map(CardView::new)
                .toList();

        if (game.getStatus() == GameStatus.PLAYER_TURN
                && dealerCards.size() >= 2) {

            dealerCards = List.of(
                    dealerCards.get(0),
                    CardView.hiddenCard()
            );
        }

        return new GameResponse(
                id,
                dealerCards,
                game.getPlayer().getCards()
                        .stream()
                        .map(CardView::new)
                        .toList(),
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

    public record CardView(
            String rank,
            String suit,
            boolean hidden
    ) {

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
    ) {
    }
}
```
