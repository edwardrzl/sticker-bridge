# Mapa de historias por unidad

Qué unidad implementa cada historia. Cada historia tiene exactamente una
unidad responsable; cuando otra unidad aporta una parte, se indica como
contribución. U1 (esqueleto) no cierra ninguna historia: prueba la
viabilidad.

| Historia | Unidad responsable | Contribuyen | Nota |
|---|---|---|---|
| US1.1 Compartir desde TikTok | U5 | U2 (lector de enlaces) | Recepción del intent en la interfaz |
| US1.2 Pegar el enlace | U5 | U2 | |
| US1.3 Enlace que no sirve | U2 | U5 (mensaje) | La regla está en el lector de enlaces |
| US2.1 Ver las imágenes de los comentarios | U2 | U1, U5 (cuadrícula) | El núcleo es la extracción |
| US2.2 Elegir los que quiero | U5 | — | |
| US2.3 Cargar más comentarios | U2 | U5 (botón) | |
| US2.4 Video sin imágenes | U5 | U2 | |
| US3.1 Sticker listo sin recortar | U3 | U1 | |
| US3.2 Conservar la animación | U3 | — | |
| US3.3 Saber qué pasó al guardar | U4 | U5 (pantalla de resultado) | El resumen lo produce el caso de uso |
| US4.1 Agregar el paquete a WhatsApp | U4 | U1, U5 | |
| US4.2 Sumar stickers a un paquete ya agregado | U4 | U1 | U1 verifica la suposición de FR5.6 |
| US4.3 Paquete lleno | U4 | — | |
| US4.4 Ver mis paquetes | U5 | U4 (estado) | |
| US4.5 WhatsApp no está instalado | U4 | U5 (mensaje) | |
| US5.1 Entender por qué no funcionó | U2 | U5 (mensajes y acciones) | Los tipos de error están en la extracción |
| US5.2 Importar una imagen de la galería | U5 | U3, U4 | |
| US6.1 Diagnosticar un fallo de extracción | U2 | U1 (puerto de diagnóstico) | Transversal |

## Por unidad

| Unidad | Historias de las que es responsable |
|---|---|
| U1 | — (viabilidad de US2.1, US3.1, US4.1, US4.2) |
| U2 | US1.3, US2.1, US2.3, US5.1, US6.1 |
| U3 | US3.1, US3.2 |
| U4 | US3.3, US4.1, US4.2, US4.3, US4.5 |
| U5 | US1.1, US1.2, US2.2, US2.4, US4.4, US5.2 |

Las 17 historias tienen unidad responsable.
