# AI Trading Toolkit - Backend Service

Developer: **FX Signal Lab**

This backend service acts as a secure reverse proxy and provider manager for the **AI Trading Toolkit** Android app.

## Key Security Architecture
- **Zero In-App Keys**: API keys for external services (GoldAPI, AI / Gemini, Forex APIs) are stored exclusively in backend environment variables, never hardcoded inside Android client binaries.
- **Graceful No-Key Mode**: The server boots and functions without any API keys, supplying free public Binance market data, health status checks, and local technical indicator fallbacks.

## Setup Instructions

1. Install dependencies:
   ```bash
   npm install
   ```

2. Configure environment variables:
   ```bash
   cp .env.example .env
   # Edit .env and populate keys when ready (GOLDAPI_KEY, AI_API_KEY, etc.)
   ```

3. Start server:
   ```bash
   npm start
   ```

## Endpoints
- `GET /health` — Checks status of all external providers.
- `GET /api/markets` — Aggregated live market prices from public endpoints.
- `GET /api/candles?symbol=BTCUSDT&interval=1h&limit=60` — Historical candlestick data for charts.
- `POST /api/analyze-chart` — Multimodal AI chart inspection (when configured).
