# Manual checklist

What cannot be tested automatically: the real TikTok page, the real WhatsApp and how
the screens look. Run it on the phone after installing the debug build, and write the
results in the "Result" column.

## Before you start

- Phone with USB debugging enabled, connected to the computer, with TikTok and WhatsApp installed.
- `./gradlew :app:installDebug` finished without errors.
- In a terminal, keep the diagnostic log open: `adb logcat -s StickerBridge`.
- Pick 5 TikTok videos whose comments contain stickers or photos.

## 1 — Share from TikTok and choose stickers

| # | Step | Expected | Result |
|---|---|---|---|
| 1 | In TikTok, open a video → **Share** → **Sticker Bridge** | The app opens on "Stickers del video" and starts searching; no TikTok login is asked | |
| 2 | Wait for the grid | Thumbnails appear in less than 15 s, ordered by likes (highest first), each with its like count | |
| 3 | Tap 4 or more stickers | Each one is marked; the counter shows "N elegidos" | |
| 4 | Tap a chosen sticker again | It is unmarked and the counter goes down | |
| 5 | Tap **Cargar más** | More thumbnails are added; the ones already chosen stay chosen | |
| 6 | Tap **Guardar stickers** | "Convirtiendo X de N…" and then "Listo" with how many went to each pack | |
| 7 | WhatsApp opens once per changed pack; accept each dialog | The stickers are in WhatsApp's picker, uncropped; animated ones move | |

## 2 — Paste a link

| # | Step | Expected | Result |
|---|---|---|---|
| 8 | Open Sticker Bridge, paste a video link, tap **Buscar stickers** | Same grid as in step 2 | |
| 9 | Type some text that is not a TikTok link and tap **Buscar stickers** | "No es un enlace de video de TikTok"; nothing is searched | |
| 10 | Start a search and tap **Cancelar** | Back to the first screen; no error | |
| 11 | Turn on airplane mode and search | "Sin conexión a internet." with **Reintentar** | |

## 3 — Packs that grow

| # | Step | Expected | Result |
|---|---|---|---|
| 12 | Save stickers from a second video | They are added to the same packs; WhatsApp offers **UPDATE** and shows the new ones | |
| 13 | Save fewer than 3 stickers of a kind into a new pack | "faltan N para poder agregarlo a WhatsApp"; WhatsApp is not opened for that pack | |
| 14 | Open **Mis paquetes** | Each pack shows its count, its kind (animados / estáticos) and its status | |

## 4 — Remove a sticker

| # | Step | Expected | Result |
|---|---|---|---|
| 15 | In **Mis paquetes**, tap a sticker → **Quitar** | It disappears from the pack and the count goes down | |
| 16 | WhatsApp opens by itself for a pack that was already added; accept **UPDATE** | The sticker is gone from WhatsApp's picker too | |

## 5 — Import from the gallery

| # | Step | Expected | Result |
|---|---|---|---|
| 17 | On the first screen, tap **Importar imagen** and pick one or more photos | "Convirtiendo…" and then "Listo"; they are added to the static pack | |
| 18 | Check it in WhatsApp | The whole image is visible, not cropped | |

## 6 — Exit criterion

| # | Measure | Target | Result |
|---|---|---|---|
| 19 | Videos completed from Share to WhatsApp | 5 of 5 | |
| 20 | Time per video, from Share to the stickers in WhatsApp | < 1 min | |
| 21 | Manual cropping or editing needed | None | |

If a step fails, copy the on-screen message and the matching `StickerBridge` log lines here.
