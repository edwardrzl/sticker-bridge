# Preguntas — Practices Discovery

> Responde cada pregunta escribiendo la letra (y, si quieres, un comentario)
> después de `[Answer]:`. Para varias opciones: `[Answer]: A, C`.
> Si ninguna opción encaja, usa `X` y explica.

El proyecto es nuevo y no tiene historial: no hay evidencia de la que inferir
prácticas, así que propongo valores por defecto para un desarrollador solo.

## Q1 — Ramas y mensajes de commit
Define cómo se guarda el historial del código.
¿Cómo quieres trabajar con git?

A. Todo directo en `main`, con commits pequeños y mensajes en inglés tipo Conventional Commits (`feat: ...`, `fix: ...`) (recomendada — eres una sola persona, las ramas y revisiones añaden pasos sin revisor)
B. Una rama por funcionalidad que se une a `main` al terminar, con el mismo estilo de mensajes
C. Directo en `main`, con mensajes libres
X. Otra (especifica)

[Answer]: B — Una rama por funcionalidad unida a `main` con pull request en GitHub; mensajes en inglés tipo Conventional Commits. Comentario inicial: "quiero que esto me sirva también para mostrar en github, entonces sé ordenado y buena arquitectura por favor".

## Q2 — Quién hace los commits
Yo puedo hacer commits, pero solo si me autorizas de forma permanente.
¿Cuándo quieres que se haga commit?

A. Yo (Claude) hago commit al terminar cada paso aprobado del plan, cuando sus pruebas pasan (recomendada — deja un historial ordenado sin que tengas que acordarte)
B. Yo preparo los cambios y tú haces el commit
C. Yo hago commit solo cuando me lo pidas
X. Otra (especifica)

[Answer]: A — Claude hace commit al terminar cada paso aprobado del plan, cuando sus pruebas pasan

## Q3 — Esqueleto funcional
Un esqueleto funcional es una versión mínima que recorre todo el sistema, hecha primero para probar que las piezas conectan antes de meter las funcionalidades reales.
¿Construimos primero una rebanada mínima de punta a punta?

A. Sí: un enlace fijo → extraer un sticker en el teléfono → convertirlo → verlo en WhatsApp, sin interfaz cuidada (recomendada — incluye la prueba de extracción que factibilidad exige y además comprueba la entrega a WhatsApp)
B. Solo la prueba de extracción primero; el resto se construye después por partes
C. No: construir cada capacidad completa en el orden del backlog
X. Otra (especifica)

[Answer]: A — Sí: enlace fijo → extraer un sticker → convertirlo → verlo en WhatsApp

## Q4 — Cuándo se escriben las pruebas
Determina el orden de trabajo en cada paso de código.
¿Qué método prefieres?

A. Mixto: prueba primero (TDD) para la lógica pura — conversión de imágenes, reglas del paquete, lectura de enlaces — y prueba después para pantallas y navegador interno (recomendada — la lógica es fácil de probar primero; la interfaz y TikTok no)
B. Prueba primero (TDD) en todo
C. Prueba después en todo
X. Otra (especifica)

[Answer]: A — Mixto: prueba primero para la lógica pura, prueba después para pantallas y navegador interno

## Q5 — Qué tipos de prueba y cuánta cobertura
La extracción depende de TikTok real y no se puede probar de forma automática y estable.
¿Qué nivel de pruebas quieres?

A. Pruebas unitarias de la lógica, sin objetivo numérico de cobertura, más una lista de comprobación manual en tu teléfono para extracción y WhatsApp (recomendada — proporcional a una herramienta personal)
B. Lo anterior con un objetivo de 80 % de cobertura en la lógica
C. Lo anterior y además pruebas automáticas de interfaz en emulador o dispositivo
X. Otra (especifica)

[Answer]: A — Pruebas unitarias de la lógica sin objetivo numérico, más lista de comprobación manual en el teléfono

## Q6 — Cómo llega la app a tu teléfono
No hay servidor que desplegar; "desplegar" aquí es instalar la app.
¿Cómo quieres instalarla?

A. Se compila en tu computadora y se instala en el teléfono por cable USB o copiando el archivo de instalación (recomendada — costo cero y sin servicios externos)
B. Un servicio de integración continua gratuito (GitHub Actions) compila el archivo de instalación en cada cambio y lo descargas al teléfono
C. Distribución por Google Play en prueba interna (cuota única de registro de desarrollador)
X. Otra (especifica)

[Answer]: A — Se compila en la computadora y se instala por USB o copiando el archivo de instalación

## Q7 — Estilo y organización del código
El lenguaje se decide en la etapa de stack; aquí solo la convención general.
¿Cómo organizamos el código?

A. Formateador y analizador estándar del lenguaje elegido, con carpetas por funcionalidad (extracción, conversión, paquete, pantallas) (recomendada — la extracción queda aislada, como exige factibilidad)
B. Formateador y analizador estándar, con carpetas por capa (interfaz, dominio, datos)
C. Sin herramientas de formato; organización libre
X. Otra (especifica)

[Answer]: A — Formateador y analizador estándar, carpetas por funcionalidad. (Delegada: la persona pidió avanzar con las opciones recomendadas mientras no puede responder; pendiente de que la revise en la aprobación.)

## Q8 — Reglas fijas (elige todas las que apliquen)
Estas reglas se aplicarán siempre, en todas las sesiones futuras.
¿Cuáles quieres fijar?

A. NUNCA pedir, usar ni guardar la sesión o las credenciales de TikTok
B. NUNCA enviar datos a servidores propios o de terceros; la app solo habla con TikTok y WhatsApp
C. NUNCA subir al repositorio la clave de firma de la app ni otros secretos
D. SIEMPRE mantener la extracción de TikTok aislada detrás de una interfaz propia, reemplazable sin tocar el resto
X. Otra (especifica)

[Answer]: A, B, C, D — Respuesta literal: "lo que recomiendes". Se recomiendan las cuatro. (Delegada; pendiente de que la persona las revise en la aprobación, porque pasan a ser reglas permanentes.)

## Q9 (seguimiento) — Repositorio en GitHub
En la pregunta sobre git (Q1) respondiste: "quiero que esto me sirva también para mostrar en github, entonces sé ordenado y buena arquitectura". Hasta ahora el riesgo con los términos de TikTok se aceptó por ser de uso personal y privado; publicar el código lo hace visible, y GitHub retira a veces repositorios de este tipo a petición de la plataforma afectada (riesgo bajo).
¿Cómo quieres publicar el repositorio?

A. Público, con un README que explique que es un proyecto personal y educativo que usa una interfaz no oficial de TikTok, e incluyendo los documentos de proceso de `aidlc-docs/` (recomendada — muestra tanto el código como el método de trabajo)
B. Público con el mismo README, pero sin los documentos de proceso
C. Privado por ahora; se decide si publicarlo cuando esté terminado
X. Otra (especifica)

[Answer]: A — Público, con README que lo declara proyecto personal y educativo con interfaz no oficial de TikTok, incluyendo `aidlc-docs/`
