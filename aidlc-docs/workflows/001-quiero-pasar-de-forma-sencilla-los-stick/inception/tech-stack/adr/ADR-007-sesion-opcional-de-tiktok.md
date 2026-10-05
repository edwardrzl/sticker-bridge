# ADR-007: Sesión de TikTok para ver los comentarios ocultos — descartada

## Estado
Descartada el 2026-10-05, el mismo día en que se propuso. Se construyó, se probó
en el teléfono con una sesión real y no resolvió el problema. La regla "nunca la
sesión de TikTok" sigue vigente.

## Fecha
2026-10-05

## Contexto
Al probar la interfaz, la persona vio que faltaban stickers, entre ellos el más
votado de cada video. La causa no es el formato de los que sí llegan: TikTok
entrega a su versión web solo una parte de los comentarios.

| Cómo se piden los comentarios | Video de prueba (21 comentarios) | ¿Trae las imágenes? |
|---|---|---|
| Web (`aid=1988`), lo que usa la app | 10 u 11 | Sí |
| Identificador de la app de TikTok (`aid=1180`, `1233`, `1340`) | 21 o 22 | No: solo el texto "[Sticker]" |

Los comentarios que faltan llevan un tipo de sticker que la web de TikTok no
muestra. En otro video, el comentario más votado (419 858 likes) es uno de ellos.

## Decisión
No usar la sesión de TikTok. La persona autorizó levantar la regla para probarlo;
se añadió un inicio de sesión opcional en la página de TikTok dentro de la app y
se comprobó, con la sesión activa y desde la propia página:

- La web con sesión devuelve los mismos comentarios que sin sesión.
- Los identificadores de la app devuelven la lista completa, también sin
  imágenes, con o sin sesión.
- Unas 25 combinaciones de parámetros y de identidad de navegador dan uno de
  esos dos resultados.

Como la sesión no aporta nada y expone la cuenta, el código se retiró.

## Consecuencias
- La app solo puede ofrecer los stickers que TikTok muestra en su versión web.
  Es una limitación conocida (ver FR2.1).
- La única fuente con esas imágenes es la API de la app móvil de TikTok, que
  exige registrar un dispositivo y firmar las peticiones con algoritmos propios.
  Reimplementarlos está descartado por ADR-002.
- Los stickers que faltan se pueden traer con una captura de pantalla e
  "Importar imagen" (FR7), sin animación.

## Detalle útil si se retoma
- Iniciar sesión con Google, Facebook o Apple no funciona en un navegador
  interno; Google lo bloquea a propósito.
- Un `WebView` creado para Compose necesita `LayoutParams` explícitos; sin ellos
  la página mide 0 de alto y no se ve.

## Referencias
- `requirements.md` (FR2.1, FR2.7, NFR4)
- ADR-002
