# Prueba visual — monitor de computadores

## Alcance de esta entrega

Lectura real del controlador. Resumen global y pestañas inferiores por computador.
El refresco no arranca simulación ni genera procesos. Los botones globales sí controlan el reloj a petición del usuario. Actualiza cada
500 ms cuando está visible y al entrar en Simulación. El temporizador visual no es
el reloj simulado. Se muestran cantidades de las colas, no su contenido aún.

## Preparación

Reiniciar Main desde NetBeans. Crear tres computadores con nombres distintos,
RAM 128/256/512 y políticas diferentes. Ninguno tiene procesos por defecto.

## Checklist comprobable desde GUI ahora

- [ ] Antes de crear computadores, Simulación explica que no hay ninguno.
- [ ] El botón de ese estado lleva a Computadores.
- [ ] Tras crear los tres, Resumen muestra tres tarjetas y tres pestañas además de Resumen.
- [ ] Cada tarjeta permite reconocer nombre, ID, política y RAM del computador correcto.
- [ ] CPU muestra Libre y Sin proceso; no inventa un porcentaje de utilización.
- [ ] RAM muestra sólo la fracción usada/total junto a RAM. En el modelo actual recién creado: 0 usada. Aún no hay reserva de SO.
- [ ] Nuevos/Listos/Bloqueados/Terminados muestran cero sin procesos.
- [ ] Cabecera destaca Reloj global · ciclo 0, Detenida y cantidad correcta de computadores.
- [ ] Ver detalle abre la pestaña del computador correspondiente.
- [ ] En detalle, sin proceso, PC/prioridad/deadline muestran raya, no datos inventados.
- [ ] Puedo volver a Resumen mediante su pestaña inferior.
- [ ] Si creo otro computador y regreso, aparece su tarjeta y pestaña; se conserva el detalle antes seleccionado.
- [ ] Si hay muchas tarjetas/pestañas, puedo recorrerlas con scroll.
- [ ] Navegar por el monitor no crea computadores, modifica procesos ni avanza reloj.

## Lo que requiere procesos y ejecución — no marcar como validado visualmente todavía

- [ ] CPU ocupada muestra el nombre/ID del proceso, modo SO/usuario y PC.
- [ ] Prioridad y deadline restante corresponden al proceso ejecutando.
- [ ] RAM refleja reservas y liberaciones en la fracción usada/total.
- [ ] Cantidades de colas cambian al admitir, despachar, bloquear y terminar.
- [ ] La CPU vuelve a libre y desaparecen los datos anteriores al finalizar.
- [ ] Durante ejecución, datos se refrescan sin perder la pestaña seleccionada.

Una prueba Java aislada sí crea un proceso CPU_BOUND con el controlador y avanza dos
pasos: verifica RAM 32/128, CPU ocupada y terminación. No equivale a validar desde la
GUI un flujo de creación/ejecución que todavía no está conectado. No agrega fixtures
al arranque normal.

## Pendientes del enunciado

Contenido y orden de cada cola; motivos de bloqueo; inspector completo de PCB;
instrucción en PC; cambio efectivo de política/quantum; buffers y semáforos;
log de eventos; métricas históricas y gráfico comparativo. Los cinco issues del motor
siguen siendo limitaciones para pruebas completas de ejecución.

## Mapa de funciones

- `PanelPrincipal.mostrarPanel`: al destino SIMULACION invoca `monitor.actualizarVista()`.
- `MonitorSimulacion.actualizarVista`: consulta reloj/estado y computadores. Dentro del bloqueo del controlador lee CPU, RAM y colas. No modifica dominio. Recorre hasta el número de computadores. Lista vacía tiene vista propia.
- `reconstruirPestanas`: sólo al cambiar los IDs; elimina pestañas hasta dejar Resumen, recorre computadores y crea tarjetas/detalles. Conserva selección por ID; vuelve a Resumen si desaparece.
- `VistaComputador.actualizar`: recibe Computer, escribe rótulos sin ciclos. CPU vacía no presenta modo activo. RAM se muestra como fracción usada/total.
- `Timer` Swing: llama actualizarVista cada 500 ms sólo si la vista se muestra; arranca con addNotify y se detiene con removeNotify. Sus callbacks corren en EDT. No llama stepSimulation.

El bloqueo del controlador coordina con sus métodos sincronizados de creación y tick.
No protege modificaciones directas de objetos desde código que ignore ese controlador.
El rótulo del reloj es una lectura de observación, no una instantánea atómica conjunta
con todos los datos del computador.

## Controles del reloj

- [ ] Iniciar avanza incluso sin computadores; el botón queda deshabilitado mientras corre.
- [ ] Pausar conserva el contador y ofrece Reanudar. Un tick ya iniciado puede completar.
- [ ] +1 tick incrementa exactamente uno estando detenido o pausado; deshabilitado en automático.
- [ ] Elegir 100/250/500/1000 ms deja cambio pendiente, sin aplicarlo todavía.
- [ ] Aplicar actualiza el rótulo Aplicado; no reinicia contador ni arranca reloj.
- [ ] Reanudar sigue desde el contador actual; no vuelve a cero.
- [ ] Navegar a otras vistas no pausa el reloj: es global.

Funciones en MonitorSimulacion: controlesReloj conecta listeners; iniciarReloj,
pausarReloj, avanzarUnTick y aplicarIntervalo invocan el controlador.
actualizarControlesReloj sólo escribe habilitaciones/rótulos. Ninguna tiene ciclos.
SimulationController.getCycleDurationMs expone el intervalo existente del reloj.
No se agregó botón de reset ni se cambió el algoritmo de GlobalClock.
El intervalo nuevo no interrumpe un Thread.sleep que ya está en curso.
