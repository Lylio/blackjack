package com.example.blackjack.controller;

import com.example.blackjack.service.BlackjackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blackjack")
@CrossOrigin(origins = "http://localhost:5173")
public class BlackjackController {
    private final BlackjackService service;

    public BlackjackController(BlackjackService service) {
        this.service = service;
    }

    @PostMapping("/games")
    public ResponseEntity<?> start(@Valid @RequestBody StartGameRequest request) {
        try {
            return ResponseEntity.ok(service.start(request.wager()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/games/{id}/hit")
    public ResponseEntity<?> hit(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.hit(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/games/{id}/stand")
    public ResponseEntity<?> stand(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.stand(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    public record StartGameRequest(
            @DecimalMin(value = "0.01", message = "Wager must be at least £0.01.")
            double wager
    ) {}

    public record ErrorResponse(String error) {}
}
