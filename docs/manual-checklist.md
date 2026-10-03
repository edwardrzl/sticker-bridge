# Manual checklist — walking skeleton (U1)

What cannot be tested automatically: the real TikTok page and the real WhatsApp.
Run it on the phone after installing the debug build, and write the results in the
"Result" column.

## Before you start

- Phone with USB debugging enabled, connected to the computer, with TikTok and WhatsApp installed.
- `./gradlew :app:installDebug` finished without errors.
- In a terminal, keep the diagnostic log open: `adb logcat -s StickerBridge`.
- Pick 3 TikTok videos whose comments contain stickers or photos. Copy each link from
  TikTok's Share button.

## Q-SK1 — Extraction without logging in

| # | Step | Expected | Result |
|---|---|---|---|
| 1 | Open Sticker Bridge, paste the first link, tap **Probar** | "Imágenes encontradas: N" with N ≥ 1, in less than 30 s | |
| 2 | Repeat with the second and third links | Same for each | |
| 3 | Check the log line `blocked hosts: [...]` | Lists the blocked domains; none of them prevented step 1 | |
| 4 | At no point did a TikTok login screen appear | No login asked | |

If step 1 fails: copy the error line and the log here. The build stops and the person
decides on the alternative (home computer or paid scraper), as agreed in feasibility.

## Q-SK2 — WhatsApp sees new stickers without re-adding the pack

| # | Step | Expected | Result |
|---|---|---|---|
| 5 | After step 1, WhatsApp shows its own "add sticker pack" dialog; accept it | "TikTok estáticos 1" appears in WhatsApp's sticker picker with 3 stickers | |
| 6 | Back in the app, tap **Añadir otro** | "El paquete ya está en WhatsApp…" | |
| 7 | Open WhatsApp's sticker picker (close and reopen WhatsApp if needed) | The pack shows 4 stickers | |

## Q-SK3 — Timings

| # | Measure | Target | Result |
|---|---|---|---|
| 8 | "Imágenes encontradas" time (link to first batch with images) | ≤ 15 s | |
| 9 | "Sticker convertido" time | < 2 s | |
| 10 | Sticker size | ≤ 100 KB | |

## Notes

- The "Probar" button repeats the same sticker 3 times only to reach WhatsApp's minimum
  pack size; that is skeleton behaviour, not the final app.
