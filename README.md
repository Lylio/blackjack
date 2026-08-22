# Blackjack — Java Spring Boot + React

A fully working single-player Blackjack game with a Java Spring Boot REST backend and a React/Vite frontend.

## Requirements
- Java 17+
- Maven 3.9+
- Node.js 18+
- npm

|              |                                                                                                                                |
|--------------|--------------------------------------------------------------------------------------------------------------------------------|
| Demo Link    | [blackjack.lyle.app](https://sweet-nurturing-production-9c2d.up.railway.app/)                                                                           |
| Tech Stack   | Java 17+ | Maven 3.9+ | Node.js 18+ | npm                                                                                      |                                                                                                                         
| Cloud Deploy | ![Railway](https://img.shields.io/badge/Railway-000000?logo=railway&logoColor=white&style=for-the-badg) |
| Top Language | ![Github Language](https://img.shields.io/github/languages/top/lylio/blackjack)                                                |
| Last Commit  | ![Github Commit Activity](https://img.shields.io/github/last-commit/lylio/blackjack/main?style=for-the-badge)                  |

### Launch & Structure

## Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

## Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the URL printed by Vite (normally `http://localhost:5173`).

The Vite development server proxies `/api` requests to the Spring Boot server.

## Rules implemented
- Standard 52-card deck
- Dealer stands on 17, including soft 17
- Blackjack pays 3:2
- Player can Hit or Stand
- Dealer hole card is hidden until the player stands/busts
- Aces count as 1 or 11 automatically
- Pushes return the wager
- New shuffled deck is created when a game starts
- Basic wager validation
- Server owns the game state and determines outcomes
