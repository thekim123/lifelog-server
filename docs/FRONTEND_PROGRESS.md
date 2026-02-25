# Frontend Progress (MVP)

## Added
- `src/main/resources/static/index.html`

## What it supports
1. Receipt upload (`POST /api/receipts/upload`)
2. Receipt detail (`GET /api/receipts/{id}`)
3. Receipt confirm (`PUT /api/receipts/{id}/confirm`)
4. Recipe create (`POST /api/recipes`)
5. Monthly expense summary (`GET /api/expenses/summary/monthly`)
6. Quick fetch:
   - expenses list
   - inventory stocks
   - recommendations today

## UI characteristics
- Single-page, no build step (vanilla HTML/CSS/JS)
- Mobile-friendly card layout
- API response JSON rendered in `pre` blocks for debugging

## Next frontend steps
- Add ReceiptItem editable form before confirm
- Better chart view (monthly trend/category pie)
- Inventory consume button (cook/adjust)
- Recipe list + detail panel
- Korean font/theme polish
