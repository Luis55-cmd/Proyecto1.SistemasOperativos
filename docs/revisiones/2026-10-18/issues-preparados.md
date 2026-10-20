# Issues preparados

Enlaces prellenados; no publicados. Revisados contra 034856d, no contra develop.

## 1. RR no desaloja al proceso al agotar su quantum

[Crear issue](https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/issues/new?title=RR%20no%20desaloja%20al%20proceso%20al%20agotar%20su%20quantum&body=Revision%20de%20origin%2Ffeat%2Fsimulation-controller%2C%20commit%20034856d.%20Fallo%20reproducido%20con%20avance%20manual%3B%20las%20ocho%20suites%20existentes%20pasan.%0A%0ACodigo%3A%20https%3A%2F%2Fgithub.com%2FLuis55-cmd%2FProyecto1.SistemasOperativos%2Fblob%2F034856d%2Fsrc%2Fmain%2Fjava%2Fcom%2Favilaos%2Fcore%2Fkernel%2FComputerKernel.java%23L353%0A%0AFuncion%3A%20ComputerKernel.executeCycle%20(lineas%20367-372)%2C%20en%20conjunto%20con%20dispatch%20y%20RoundRobinScheduler.scheduleNext.%0A%0AReproduccion%3A%20reset%20del%20controlador%3B%20crear%20computador%20RAM%3D128%20con%20ROUND_ROBIN%3B%20crear%20A%20y%20B%20CPU_BOUND%2C%20cada%20uno%20instructions%3D20%2C%20memory%3D10%2C%20priority%3D1%2C%20deadline%3D100.%20Avanzar%20ocho%20veces%20con%20stepSimulation().%0AActual%3A%20remainingTime%20de%20A%3D12%20y%20B%3D20%3B%20B%20no%20recibe%20CPU%20pese%20al%20quantum%3D3%20del%20kernel.%0AEsperado%3A%20rotacion%20al%20consumir%20el%20quantum%3B%20ambos%20procesos%20deben%20progresar.%0A%0ACausa%3A%20executeCycle%20reduce%20el%20quantum%2C%20pero%20solo%20llama%20dispatch%20cuando%20CPU%20esta%20libre%20o%20el%20proceso%20ya%20no%20esta%20EJECUTANDO.%20Nunca%20consulta%20scheduleNext%20para%20desalojar%20al%20ejecutando.%0A%0ASolucion%20sugerida%3A%20evaluar%20la%20politica%20tambien%20con%20un%20proceso%20ejecutando%3B%20conservarlo%20cuando%20corresponda%20o%20guardar%20contexto%20y%20reencolarlo%20al%20desalojar.%20No%20basta%20llamar%20dispatch%20siempre%3A%20su%20rama%20de%20cola%20vacia%20actualmente%20borra%20la%20CPU%20aunque%20pueda%20seguir%20ejecutando.%20Definir%20el%20momento%20del%20descuento%20para%20evitar%20desfase%20del%20quantum.%0A%0AAceptacion%3A%20prueba%20integrada%20de%20dos%20procesos%20RR%3B%20un%20proceso%20solo%20sigue%20ejecutando%20sin%20desaparecer%3B%20FCFS%20no%20es%20desalojado.%20Revisar%20tambien%20prioridad%20expropiativa.)

Revision de origin/feat/simulation-controller, commit 034856d. Fallo reproducido con avance manual; las ocho suites existentes pasan.

Codigo: https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/blob/034856d/src/main/java/com/avilaos/core/kernel/ComputerKernel.java#L353

Funcion: ComputerKernel.executeCycle (lineas 367-372), en conjunto con dispatch y RoundRobinScheduler.scheduleNext.

Reproduccion: reset del controlador; crear computador RAM=128 con ROUND_ROBIN; crear A y B CPU_BOUND, cada uno instructions=20, memory=10, priority=1, deadline=100. Avanzar ocho veces con stepSimulation().
Actual: remainingTime de A=12 y B=20; B no recibe CPU pese al quantum=3 del kernel.
Esperado: rotacion al consumir el quantum; ambos procesos deben progresar.

Causa: executeCycle reduce el quantum, pero solo llama dispatch cuando CPU esta libre o el proceso ya no esta EJECUTANDO. Nunca consulta scheduleNext para desalojar al ejecutando.

Solucion sugerida: evaluar la politica tambien con un proceso ejecutando; conservarlo cuando corresponda o guardar contexto y reencolarlo al desalojar. No basta llamar dispatch siempre: su rama de cola vacia actualmente borra la CPU aunque pueda seguir ejecutando. Definir el momento del descuento para evitar desfase del quantum.

Aceptacion: prueba integrada de dos procesos RR; un proceso solo sigue ejecutando sin desaparecer; FCFS no es desalojado. Revisar tambien prioridad expropiativa.

## 2. Fin de productor duplica terminacion y libera RAM dos veces

[Crear issue](https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/issues/new?title=Fin%20de%20productor%20duplica%20terminacion%20y%20libera%20RAM%20dos%20veces&body=Revision%20de%20origin%2Ffeat%2Fsimulation-controller%2C%20commit%20034856d.%20Fallo%20reproducido%20con%20avance%20manual%3B%20las%20ocho%20suites%20existentes%20pasan.%0A%0ACodigo%3A%20https%3A%2F%2Fgithub.com%2FLuis55-cmd%2FProyecto1.SistemasOperativos%2Fblob%2F034856d%2Fsrc%2Fmain%2Fjava%2Fcom%2Favilaos%2Fcore%2Fkernel%2FComputerKernel.java%23L381%0A%0AFunciones%3A%20ComputerKernel.executeCycle%20(llamadas%20a%20terminateProcess%20en%20lineas%20386%20y%20406)%20y%20terminateProcess(Computer%2C%20ProcessControlBlock)%2C%20linea%20225.%0A%0AReproduccion%3A%20reset%3B%20computador%20FCFS%20RAM%3D128%3B%20buffer%20local%20de%205%3B%20productor%20instructions%3D10%2C%20memory%3D20%2C%20deadline%3D100%2C%20requiredElements%3D1%2C%20targetBufferId%3D%221%22%3B%20otro%20CPU_BOUND%20instructions%3D30%2C%20memory%3D30%2C%20deadline%3D100.%20Avanzar%205%20ticks.%0AActual%3A%20endedQueue.size()%3D2%20y%20RAM%20usada%3D15.%0AEsperado%3A%20productor%20registrado%20una%20vez%3B%20RAM%20usada%3D35%20(buffer%205%20%2B%20otro%20proceso%2030).%0A%0ACausa%3A%20el%20driver%20marca%20SALIENTE%20y%20executeCycle%20finaliza%20en%20su%20rama%3B%20luego%20la%20comprobacion%20final%20vuelve%20a%20finalizar%20el%20mismo%20PCB.%20terminateProcess%20no%20protege%20la%20liberacion%20repetida.%0A%0ASolucion%20sugerida%3A%20unificar%20la%20finalizacion%20o%20salir%20de%20executeCycle%20tras%20procesarla.%20Hacer%20la%20limpieza%20idempotente%20mediante%20registro%20explicito%20de%20finalizacion%2Freserva.%20No%20usar%20solamente%20state%3D%3DSALIENTE%20como%20guardia%3A%20el%20driver%20y%20executeOneCycle%20ya%20ponen%20ese%20estado%20ANTES%20de%20la%20primera%20limpieza.%0A%0AAceptacion%3A%20una%20entrada%20en%20terminados%3B%20una%20liberacion%20de%20reserva%3B%20repetir%20la%20solicitud%20de%20terminacion%20no%20cambia%20memoria%20ni%20colas%3B%20probar%20tambien%20CPU_BOUND%20y%20deadline.)

Revision de origin/feat/simulation-controller, commit 034856d. Fallo reproducido con avance manual; las ocho suites existentes pasan.

Codigo: https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/blob/034856d/src/main/java/com/avilaos/core/kernel/ComputerKernel.java#L381

Funciones: ComputerKernel.executeCycle (llamadas a terminateProcess en lineas 386 y 406) y terminateProcess(Computer, ProcessControlBlock), linea 225.

Reproduccion: reset; computador FCFS RAM=128; buffer local de 5; productor instructions=10, memory=20, deadline=100, requiredElements=1, targetBufferId="1"; otro CPU_BOUND instructions=30, memory=30, deadline=100. Avanzar 5 ticks.
Actual: endedQueue.size()=2 y RAM usada=15.
Esperado: productor registrado una vez; RAM usada=35 (buffer 5 + otro proceso 30).

Causa: el driver marca SALIENTE y executeCycle finaliza en su rama; luego la comprobacion final vuelve a finalizar el mismo PCB. terminateProcess no protege la liberacion repetida.

Solucion sugerida: unificar la finalizacion o salir de executeCycle tras procesarla. Hacer la limpieza idempotente mediante registro explicito de finalizacion/reserva. No usar solamente state==SALIENTE como guardia: el driver y executeOneCycle ya ponen ese estado ANTES de la primera limpieza.

Aceptacion: una entrada en terminados; una liberacion de reserva; repetir la solicitud de terminacion no cambia memoria ni colas; probar tambien CPU_BOUND y deadline.

## 3. Procesos bloqueados por buffer no despiertan al liberar semaforos

[Crear issue](https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/issues/new?title=Procesos%20bloqueados%20por%20buffer%20no%20despiertan%20al%20liberar%20semaforos&body=Revision%20de%20origin%2Ffeat%2Fsimulation-controller%2C%20commit%20034856d.%20Fallo%20reproducido%20con%20avance%20manual%3B%20las%20ocho%20suites%20existentes%20pasan.%0A%0ACodigo%3A%20https%3A%2F%2Fgithub.com%2FLuis55-cmd%2FProyecto1.SistemasOperativos%2Fblob%2F034856d%2Fsrc%2Fmain%2Fjava%2Fcom%2Favilaos%2Fsync%2FProducerConsumerDriver.java%23L55%0A%0AFunciones%3A%20ProducerConsumerDriver.executeCycle%20(tryAcquire%20y%20release)%2C%20ComputerKernel.blockProcess%20y%20blockedToReady.%0A%0AReproduccion%3A%20reset%3B%20computador%20FCFS%20RAM%3D128%3B%20buffer%20local%20capacidad%3D5%3B%20crear%20primero%20consumidor%20y%20luego%20productor%2C%20ambos%20instructions%3D10%2C%20memory%3D10%2C%20deadline%3D100%2C%20requiredElements%3D1%2C%20targetBufferId%3D%221%22.%20Avanzar%2015%20ticks.%0AActual%3A%20consumidor%20BLOQUEADO%3B%20buffer%20contiene%201%20elemento%20y%20consumed%3D0.%0AEsperado%3A%20tras%20producir%20y%20liberar%20llenos%2C%20el%20consumidor%20vuelve%20a%20LISTO%20y%20puede%20consumir.%0A%0ACausa%3A%20tryAcquire%20fallido%20mueve%20el%20PCB%20a%20bloqueados.%20Semaphore.release()%20aumenta%20permisos%20Java%2C%20pero%20no%20despierta%20automaticamente%20los%20PCB%20simulados.%20El%20ciclo%20solo%20atiende%20al%20proceso%20ejecutando.%0A%0ASolucion%20sugerida%3A%20registrar%20esperas%20por%20buffer%2Frecurso%3B%20al%20liberar%2C%20notificar%20al%20kernel%20correspondiente%20para%20pasar%20al%20candidato%20a%20LISTO%20exactamente%20una%20vez.%20Acordar%20si%20al%20despertar%20se%20reserva%20el%20permiso%20o%20se%20reintenta%20tryAcquire%3A%20no%20mezclar%20ambos%20enfoques%20porque%20duplicaria%20adquisiciones.%20Conservar%20syncStage%20y%20distinguir%20espacios%2C%20elementos%20y%20mutex.%0A%0AAceptacion%3A%20consumidor%20sobre%20vacio%2C%20productor%20sobre%20lleno%20y%20espera%20de%20mutex%20progresan%20al%20liberarse%20su%20recurso%3B%20sin%20duplicados%20en%20colas%2C%20permisos%20perdidos%20ni%20acceso%20a%20otro%20buffer.%20Probar%20desbloqueo%20entre%20computadores.)

Revision de origin/feat/simulation-controller, commit 034856d. Fallo reproducido con avance manual; las ocho suites existentes pasan.

Codigo: https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/blob/034856d/src/main/java/com/avilaos/sync/ProducerConsumerDriver.java#L55

Funciones: ProducerConsumerDriver.executeCycle (tryAcquire y release), ComputerKernel.blockProcess y blockedToReady.

Reproduccion: reset; computador FCFS RAM=128; buffer local capacidad=5; crear primero consumidor y luego productor, ambos instructions=10, memory=10, deadline=100, requiredElements=1, targetBufferId="1". Avanzar 15 ticks.
Actual: consumidor BLOQUEADO; buffer contiene 1 elemento y consumed=0.
Esperado: tras producir y liberar llenos, el consumidor vuelve a LISTO y puede consumir.

Causa: tryAcquire fallido mueve el PCB a bloqueados. Semaphore.release() aumenta permisos Java, pero no despierta automaticamente los PCB simulados. El ciclo solo atiende al proceso ejecutando.

Solucion sugerida: registrar esperas por buffer/recurso; al liberar, notificar al kernel correspondiente para pasar al candidato a LISTO exactamente una vez. Acordar si al despertar se reserva el permiso o se reintenta tryAcquire: no mezclar ambos enfoques porque duplicaria adquisiciones. Conservar syncStage y distinguir espacios, elementos y mutex.

Aceptacion: consumidor sobre vacio, productor sobre lleno y espera de mutex progresan al liberarse su recurso; sin duplicados en colas, permisos perdidos ni acceso a otro buffer. Probar desbloqueo entre computadores.

## 4. Latencia de red queda congelada cuando el proceso se bloquea

[Crear issue](https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/issues/new?title=Latencia%20de%20red%20queda%20congelada%20cuando%20el%20proceso%20se%20bloquea&body=Revision%20de%20origin%2Ffeat%2Fsimulation-controller%2C%20commit%20034856d.%20Fallo%20reproducido%20con%20avance%20manual%3B%20las%20ocho%20suites%20existentes%20pasan.%0A%0ACodigo%3A%20https%3A%2F%2Fgithub.com%2FLuis55-cmd%2FProyecto1.SistemasOperativos%2Fblob%2F034856d%2Fsrc%2Fmain%2Fjava%2Fcom%2Favilaos%2Fsync%2FNetworkSimulator.java%23L40%0A%0AFunciones%3A%20NetworkSimulator.handleDelay%2C%20ProducerConsumerDriver.executeCycle%20y%20ComputerKernel.executeCycle.%0A%0AReproduccion%3A%20reset%3B%20dos%20computadores%20FCFS%20RAM%3D128%3B%20buffer%20capacidad%3D5%20en%20computador%201%3B%20productor%20en%20computador%202%20con%20instructions%3D10%2C%20memory%3D10%2C%20deadline%3D100%2C%20requiredElements%3D1%2C%20targetBufferId%3D%221%22%3B%20latencia%20por%20defecto%3D2.%20Avanzar%2010%20ticks.%0AActual%3A%20PCB%20sigue%20BLOQUEADO%20y%20networkDelayRemaining%3D2.%0AEsperado%3A%20la%20demora%20avanza%20con%20el%20reloj%20aunque%20el%20proceso%20no%20tenga%20CPU%2C%20y%20al%20completarse%20retorna%20a%20LISTO.%0A%0ACausa%3A%20handleDelay%20inicia%20bloqueo%3B%20el%20decremento%20solo%20ocurre%20al%20volver%20a%20ejecutar%20handleDelay%20desde%20el%20proceso%20en%20CPU.%20Al%20bloquearse%20ya%20no%20entra%20en%20ese%20camino.%20Ademas%20la%20rama%20de%20demora%20positiva%20devuelve%20true%20incluso%20cuando%20aun%20quedan%20ciclos.%0A%0ASolucion%20sugerida%3A%20separar%20inicio%20de%20solicitud%20y%20avance%20temporal%20de%20solicitudes%20pendientes.%20Procesar%20red%20una%20vez%20por%20tick%20global%3B%20al%20llegar%20a%20cero%20notificar%20al%20kernel%20propietario%2C%20retirar%20de%20bloqueados%20y%20encolar%20en%20listos%20sin%20duplicar.%20No%20ejecutar%20la%20operacion%20de%20buffer%20antes%20de%20terminar%20la%20espera.%0A%0AAceptacion%3A%20latencia%200%2C%201%20y%202%3B%20multiples%20solicitudes%20simultaneas%3B%20no%20decrementar%20varias%20veces%20por%20tick%20por%20tener%20varios%20computadores%3B%20cancelacion%20por%20deadline%20sin%20reactivar%20procesos%20terminados.)

Revision de origin/feat/simulation-controller, commit 034856d. Fallo reproducido con avance manual; las ocho suites existentes pasan.

Codigo: https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/blob/034856d/src/main/java/com/avilaos/sync/NetworkSimulator.java#L40

Funciones: NetworkSimulator.handleDelay, ProducerConsumerDriver.executeCycle y ComputerKernel.executeCycle.

Reproduccion: reset; dos computadores FCFS RAM=128; buffer capacidad=5 en computador 1; productor en computador 2 con instructions=10, memory=10, deadline=100, requiredElements=1, targetBufferId="1"; latencia por defecto=2. Avanzar 10 ticks.
Actual: PCB sigue BLOQUEADO y networkDelayRemaining=2.
Esperado: la demora avanza con el reloj aunque el proceso no tenga CPU, y al completarse retorna a LISTO.

Causa: handleDelay inicia bloqueo; el decremento solo ocurre al volver a ejecutar handleDelay desde el proceso en CPU. Al bloquearse ya no entra en ese camino. Ademas la rama de demora positiva devuelve true incluso cuando aun quedan ciclos.

Solucion sugerida: separar inicio de solicitud y avance temporal de solicitudes pendientes. Procesar red una vez por tick global; al llegar a cero notificar al kernel propietario, retirar de bloqueados y encolar en listos sin duplicar. No ejecutar la operacion de buffer antes de terminar la espera.

Aceptacion: latencia 0, 1 y 2; multiples solicitudes simultaneas; no decrementar varias veces por tick por tener varios computadores; cancelacion por deadline sin reactivar procesos terminados.

## 5. createBuffer registra un buffer aunque la reserva de RAM falle

[Crear issue](https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/issues/new?title=createBuffer%20registra%20un%20buffer%20aunque%20la%20reserva%20de%20RAM%20falle&body=Revision%20de%20origin%2Ffeat%2Fsimulation-controller%2C%20commit%20034856d.%20Fallo%20reproducido%20con%20avance%20manual%3B%20las%20ocho%20suites%20existentes%20pasan.%0A%0ACodigo%3A%20https%3A%2F%2Fgithub.com%2FLuis55-cmd%2FProyecto1.SistemasOperativos%2Fblob%2F034856d%2Fsrc%2Fmain%2Fjava%2Fcom%2Favilaos%2Fsimulation%2FSimulationController.java%23L107%0A%0AFunciones%3A%20SimulationController.createBuffer%20y%20ComputerKernel.placeBufferRAM.%0A%0AReproduccion%3A%20reset%20del%20controlador%3B%20createComputer(32%2C%20FCFS)%3B%20createBuffer(1%2C64).%0AActual%3A%20devuelve%20Buffer%20y%20queda%20registrado%20en%20getBuffers()%2C%20aunque%20RAM%20usada%20sigue%20en%200.%0AEsperado%3A%20resultado%20explicito%20de%20fallo%3B%20sin%20buffer%20registrado%20ni%20reserva%20parcial.%0A%0ACausa%3A%20createBuffer%20ignora%20el%20String%20devuelto%20por%20placeBufferRAM%20y%20siempre%20agrega%20el%20buffer.%20Tambien%20hay%20que%20revisar%20la%20reserva%20contable%20previa%20a%20la%20busqueda%20contigua%20de%20placeBufferRAM.%0A%0ASolucion%20sugerida%3A%20usar%20un%20resultado%20explicito%20de%20reserva%20(boolean%2Fresultado%20estructurado%20o%20excepcion%20acordada)%3B%20registrar%20y%20devolver%20exito%20solo%20despues%20de%20reservar.%20Si%20falla%20alguna%20etapa%2C%20deshacer%20reservas%20parciales.%20Evitar%20interpretar%20textos%20de%20mensajes%20como%20contrato.%20Validar%20capacidad%20positiva%20y%20computador%20existente.%0A%0AAceptacion%3A%20RAM%20insuficiente%20no%20altera%20lista%20ni%20memoria%3B%20ajuste%20exacto%20funciona%3B%20multiples%20buffers%20respetan%20capacidad%3B%20con%20modelo%20contiguo%2C%20falta%20de%20rango%20contiguo%20no%20se%20presenta%20como%20exito%20contable.)

Revision de origin/feat/simulation-controller, commit 034856d. Fallo reproducido con avance manual; las ocho suites existentes pasan.

Codigo: https://github.com/Luis55-cmd/Proyecto1.SistemasOperativos/blob/034856d/src/main/java/com/avilaos/simulation/SimulationController.java#L107

Funciones: SimulationController.createBuffer y ComputerKernel.placeBufferRAM.

Reproduccion: reset del controlador; createComputer(32, FCFS); createBuffer(1,64).
Actual: devuelve Buffer y queda registrado en getBuffers(), aunque RAM usada sigue en 0.
Esperado: resultado explicito de fallo; sin buffer registrado ni reserva parcial.

Causa: createBuffer ignora el String devuelto por placeBufferRAM y siempre agrega el buffer. Tambien hay que revisar la reserva contable previa a la busqueda contigua de placeBufferRAM.

Solucion sugerida: usar un resultado explicito de reserva (boolean/resultado estructurado o excepcion acordada); registrar y devolver exito solo despues de reservar. Si falla alguna etapa, deshacer reservas parciales. Evitar interpretar textos de mensajes como contrato. Validar capacidad positiva y computador existente.

Aceptacion: RAM insuficiente no altera lista ni memoria; ajuste exacto funciona; multiples buffers respetan capacidad; con modelo contiguo, falta de rango contiguo no se presenta como exito contable.

