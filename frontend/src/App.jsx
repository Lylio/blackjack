import { useState } from 'react'

const initialGame = null

function Card({ card }) {
  if (card.hidden) {
    return <div className="card hidden-card"><span>?</span></div>
  }

  const red = card.suit === '♥' || card.suit === '♦'

  return (
    <div className={`card ${red ? 'red' : ''}`}>
      <div className="rank">{card.rank}</div>
      <div className="suit">{card.suit}</div>
    </div>
  )
}

function App() {
  const [game, setGame] = useState(initialGame)
  const [wager, setWager] = useState('25')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const canPlay = game?.status === 'PLAYER_TURN'

  async function request(url, options = {}) {
    setLoading(true)
    setError('')
    try {
      const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json' },
        ...options
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.error || 'Something went wrong.')
      setGame(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function newGame() {
    const amount = Number(wager)
    if (!Number.isFinite(amount) || amount <= 0) {
      setError('Enter a valid wager.')
      return
    }

    request('/api/blackjack/games', {
      method: 'POST',
      body: JSON.stringify({ wager: amount })
    })
  }

  function hit() {
    request(`/api/blackjack/games/${game.gameId}/hit`, { method: 'POST' })
  }

  function stand() {
    request(`/api/blackjack/games/${game.gameId}/stand`, { method: 'POST' })
  }

  const gameOver = game && !canPlay

  return (
    <main className="app">
      <section className="table">
        <header className="topbar">
          <div>
            <div className="eyebrow">ROYAL TABLE</div>
            <h1>BLACKJACK</h1>
          </div>
          <div className="bankroll">
            <span>Bankroll</span>
            <strong>£{game?.bankroll?.toFixed(2) ?? '1,000.00'}</strong>
          </div>
        </header>

        {!game ? (
          <section className="welcome">
            <div className="chip">♠</div>
            <h2>Beat the dealer.</h2>
            <p>Get as close to 21 as possible without going over.</p>

            <label>
              Wager
              <div className="wager">
                <span>£</span>
                <input
                  type="number"
                  min="0.01"
                  max="1000"
                  step="0.01"
                  value={wager}
                  onChange={e => setWager(e.target.value)}
                />
              </div>
            </label>

            <button className="primary" onClick={newGame} disabled={loading}>
              {loading ? 'DEALING…' : 'DEAL HAND'}
            </button>

            <div className="rules">
              <span>BLACKJACK <b>3:2</b></span>
              <span>DEALER STANDS <b>17</b></span>
            </div>
          </section>
        ) : (
          <>
            <section className="hand-area">
              <div className="hand-header">
                <span>DEALER</span>
                <strong>
                  {game.status === 'PLAYER_TURN' ? '?' : game.dealerValue}
                </strong>
              </div>
              <div className="cards">
                {game.dealerCards.map((card, i) => <Card key={i} card={card} />)}
              </div>
            </section>

            <section className="message">
              <div className={`status ${gameOver ? 'finished' : ''}`}>
                {game.message}
              </div>
              <div className="wager-display">Wager £{game.wager.toFixed(2)}</div>
            </section>

            <section className="hand-area player">
              <div className="hand-header">
                <span>YOU</span>
                <strong>{game.playerValue}</strong>
              </div>
              <div className="cards">
                {game.playerCards.map((card, i) => <Card key={i} card={card} />)}
              </div>
            </section>

            <section className="controls">
              {canPlay ? (
                <>
                  <button className="secondary" onClick={hit} disabled={loading}>HIT</button>
                  <button className="primary" onClick={stand} disabled={loading}>STAND</button>
                </>
              ) : (
                <button className="primary" onClick={() => setGame(null)}>
                  NEW HAND
                </button>
              )}
            </section>

            {error && <div className="error">{error}</div>}
          </>
        )}

        <footer>
          <span>52 CARD DECK</span>
          <span>♣ ♦ ♥ ♠</span>
          <span>GOOD LUCK</span>
        </footer>
      </section>
    </main>
  )
}

export default App
