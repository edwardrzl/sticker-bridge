# Declaración de intención

Qué se quiere construir, para quién y cómo sabremos que funcionó. Las etapas
siguientes (factibilidad, alcance, requisitos, diseño) se contrastan contra
este documento.

## Resumen

Una herramienta personal que toma el enlace de un video de TikTok, muestra los
stickers usados en sus comentarios y entrega los elegidos ya convertidos al
formato de WhatsApp, sin recortar nada a mano. Es una app Android propia, que
se instala una sola vez, y debe tardar menos de un minuto del enlace al sticker
usable.

> Actualizado en Feasibility (2026-10-02): la meta "sin instalar nada" se
> cambió por "instalar solo esta app, una vez", porque desde una web no se
> pueden entregar stickers animados ni paquetes a WhatsApp y la extracción a
> costo cero solo es viable en el propio teléfono.

## Problema

Hoy, cuando aparece un sticker en un comentario de TikTok, la única forma de
llevarlo a WhatsApp es hacer una captura de pantalla y recortarla a mano en un
creador de stickers. Es lento, pierde la animación y la calidad del archivo
original, y hay que repetirlo por cada sticker. La persona quiere que la
herramienta "la recorte y la guarde solita".

## Usuarios

| Tipo | Quién | Necesidad principal |
|---|---|---|
| Primario | El dueño del proyecto, usando su teléfono Android | Pasar stickers de comentarios de TikTok a WhatsApp sin trabajo manual |
| Futuro (no comprometido) | Otras personas, si más adelante se decide abrirlo | La misma; hoy no condiciona el diseño |

No hay quien pague ni quien administre aparte del propio usuario.

## Resultado esperado

- Pegar o compartir el enlace de un video de TikTok y ver los stickers de sus
  comentarios.
- Elegir los que se quieren y recibirlos ya ajustados al formato de WhatsApp
  (WebP de 512×512 dentro de los límites de peso), sin recorte manual.
- Conservar la animación cuando cabe en los límites de WhatsApp; si no cabe,
  entregar el sticker estático.
- Que el paso final de agregar el sticker a WhatsApp sea lo más corto posible.

## Métricas de éxito

| Métrica | Hoy | Objetivo | Cómo se mide |
|---|---|---|---|
| Tiempo del enlace del video al sticker usable en WhatsApp | Varios minutos por sticker (captura + recorte manual) [assumption] | Menos de 1 minuto | Cronometrado por el usuario en su teléfono Android |
| Instalaciones necesarias | Un creador de stickers aparte | Solo esta app, una vez | No hace falta ninguna otra app ni servicio para completar el flujo |
| Recorte o edición manual por sticker | Siempre | Nunca | Ningún paso del flujo pide recortar o ajustar |
| Stickers animados que siguen animados | Ninguno (la captura es estática) | Todos los que quepan en 500 KB y 10 s | Revisión de los stickers entregados |

## Restricciones

- **Plataforma:** teléfono Android.
- **Instalación:** solo la app propia, una vez; ninguna otra app ni servicio.
- **Extracción en el dispositivo:** sin servidor propio.
- **Costo:** cero o casi cero; no se contratan servicios de pago recurrentes.
- **Plazo:** sin fecha límite.
- **Equipo:** una sola persona.
- **Riesgo aceptado:** la extracción no usa una API oficial, va contra los
  términos de TikTok y puede romperse cuando TikTok cambie su firma de
  peticiones. Se acepta por ser de uso personal: si se rompe, se arregla.
- **Formato de destino:** los límites de stickers de WhatsApp (512×512; estático
  ≤ 100 KB; animado ≤ 500 KB y ≤ 10 s).

## No-objetivos

- Acceder a los stickers favoritos guardados en la cuenta de TikTok.
- Crear o editar stickers a mano (recortar a gusto, añadir texto). El ajuste
  automático a 512×512 sí forma parte del proyecto.
- Otros destinos además de WhatsApp (Telegram, Discord).
- Cuentas de usuario, historial o biblioteca de stickers en la nube.
- iPhone.

## Supuestos y preguntas abiertas

- [assumption] "Me la guarde solita" se interpreta como: la herramienta recorta
  y convierte sola; el último paso dentro de WhatsApp se reduce al mínimo
  posible. Confirmado por la persona como interpretación de trabajo.
- **Resuelta en Feasibility:** entre "sin instalar" y "guardado automático" se
  eligió el guardado automático, con una app Android propia que agrega el
  paquete a WhatsApp de un toque.
- **Resuelta en Feasibility:** la extracción a costo cero se hace en el propio
  teléfono, con un navegador interno de la app. Falta probarlo en un
  dispositivo real (ver `feasibility-assessment.md`, RSK-01).
- [assumption] Los datos de la investigación previa (campo `image_list`, WebP
  animado, firmas `X-Bogus`/`X-Gnarly`/`msToken`) vienen de búsquedas web y no
  de pruebas propias; factibilidad debe confirmarlos.
- [assumption] El tiempo actual por sticker ("varios minutos") no se midió.
- El alcance elegido es `feature` (ciclo completo, incluida la operación) para
  una herramienta de uso personal; puede reducirse en cualquier aprobación si
  resulta excesivo.
