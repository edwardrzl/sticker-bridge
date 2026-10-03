# ADR-006: WhatsApp es la fuente del estado "agregado" de un paquete

## Estado
Aceptada (por delegación de la persona; pendiente de su revisión)

## Fecha
2026-10-02

## Contexto
Tras guardar stickers, la app debe decidir por cada paquete afectado si basta
con actualizarlo o si hay que abrir la confirmación de alta de WhatsApp
(FR5.5). Para eso necesita saber si el paquete ya está agregado. El usuario
puede quitar un paquete desde WhatsApp, reinstalar WhatsApp o cancelar la
confirmación, sin que la app reciba aviso. WhatsApp ofrece a las apps de
stickers una consulta para saber si un paquete suyo está agregado.

## Decisión
La app no guarda si un paquete está agregado. Lo pregunta a WhatsApp cada vez
que lo necesita (tras guardar y al mostrar la lista de paquetes), mediante el
puerto `StickerPackPublisher.isAdded`. Si WhatsApp no está instalado o la
consulta falla, el paquete se trata como no agregado.

## Consecuencias

### Positivas
- El estado mostrado nunca queda desfasado respecto a WhatsApp.
- El índice de paquetes no guarda un dato que la app no controla.

### Negativas
- Una consulta entre procesos cada vez; debe hacerse fuera del hilo de la
  interfaz.
- Depende de una consulta de WhatsApp que podría cambiar; queda aislada en el
  adaptador CMP-08.

### Neutras
- La app debe declarar que consulta el paquete de WhatsApp para que Android le
  permita verlo.

## Alternativas consideradas

### Anotar el alta cuando WhatsApp confirma
- Pros: sin consultas.
- Contras: queda desfasado si el usuario quita el paquete desde WhatsApp; la
  app dejaría de ofrecer agregarlo.

## Referencias
- https://github.com/WhatsApp/stickers/blob/main/Android/README.md
- `requirements.md` (FR5.5, FR5.8), `domain-model.md` (BR-18, BR-20)
