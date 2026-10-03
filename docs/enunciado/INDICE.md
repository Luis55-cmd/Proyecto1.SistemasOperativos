# Proyecto 1 — Por dónde entrar a los requisitos

Este es el mapa de navegación. El [checklist atómico](checklist-requerimientos-proyecto-1.md) conserva los requisitos y sus IDs; este documento los agrupa para poder trabajar sin recorrer sus 239 casillas cada vez. No duplica casillas ni registra un segundo estado de cumplimiento.

## Las seis cosas que hay que organizar

| Frente de trabajo | Pregunta que resuelve | Dónde mirar |
|---|---|---|
| **1. Acuerdos del enunciado** | ¿Qué debemos confirmar antes de decidir? | [Dudas explicadas](dudas-explicadas-proyecto-1.md) y [consulta lista para enviar](consulta-preparador-proyecto-1.md) |
| **2. Forma de trabajar en equipo y Git** | ¿Cómo distribuimos y dejamos evidencia del trabajo? | GIT-01–11; TEC-01 |
| **3. Reglas técnicas y diseño** | ¿Con qué herramientas y restricciones construimos? | ARC-01–23; TEC-02–08; OPT-01–03 |
| **4. Funcionamiento del simulador** | ¿Qué tiene que suceder dentro de ÁvilaOS? | PRO, EST, PCB, PLA, BUF, SEM, MEM, CIC |
| **5. Uso y resultados visibles** | ¿Qué configura el usuario y cómo observa lo que pasa? | GUI, CFG, MET |
| **6. Informe, entrega y defensa** | ¿Qué entregamos y qué debe demostrar cada integrante? | DOC-01–09; ENT-01–11 |

Todos los bloques del checklist están ubicados aquí. Los frentes son agrupaciones de trabajo, **no clases ni módulos de software propuestos**.

## 1. Acuerdos del enunciado

Primero separar tres cosas: una contradicción que debe corregir el preparador, una regla de evaluación que debe confirmar y una decisión que el equipo puede tomar si está autorizado a documentar supuestos.

Las dudas de mayor impacto en el comportamiento son cantidad de políticas, instrucciones y E/S, condición de terminación, deadline, latencia y métricas. UML y fecha son confirmaciones de entrega. No todas las 16 entradas D son contradicciones ni razones para paralizar todo.

- [Explicación con ejemplos de D-01 a D-16](dudas-explicadas-proyecto-1.md).
- [Mensaje para el preparador, con preguntas agrupadas](consulta-preparador-proyecto-1.md).
- [Enunciado transcrito](transcripcion.md), fuente de la consulta.

Las respuestas se registrarán aparte, con fecha y procedencia. El checklist que el estudiante pidió conservar queda intacto.

## 2. Forma de trabajar en equipo y Git

**Abrir:** [GIT — Desarrollo y colaboración](checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github) y [TEC — Equipo](checklist-requerimientos-proyecto-1.md#tec--equipo-tecnología-y-restricciones).

Lo exigido es un equipo de hasta tres personas, repositorio GitHub, rama `develop`, ramas por funcionalidad, Issues para tareas y errores, integración por PR comentados y contribución equilibrada con commits descriptivos y limitados.

**Propuesta práctica de organización, no requisito adicional:**

1. Registrar en un Issue una tarea concreta, su responsable y qué demostraría que está terminada.
2. Trabajarla en una rama de funcionalidad creada desde `develop`.
3. Hacer commits pequeños que describan cambios reales.
4. Abrir un PR hacia `develop` con explicación y verificación; comentarlo y revisarlo con otro integrante.
5. Integrar mediante PR. Llevar a `main` una versión revisada también mediante PR cuando corresponda.

La dirección de integración y la revisión por otro integrante son propuestas para el equipo; el enunciado exige PR comentados pero no fija todo ese procedimiento. Participación equilibrada no equivale a igualar artificialmente el número de commits: debe existir trabajo real que cada integrante pueda defender.

## 3. Reglas técnicas y diseño

**Abrir:** [ARC](checklist-requerimientos-proyecto-1.md#arc--arquitectura-diseño-y-reloj), [TEC](checklist-requerimientos-proyecto-1.md#tec--equipo-tecnología-y-restricciones) y [obligaciones condicionales](checklist-requerimientos-proyecto-1.md#opciones-y-obligaciones-condicionales).

Aquí están Java posterior a 21, NetBeans, librerías permitidas, estructuras propias, threads y semáforos Java. También computador reutilizable, reloj global, enums e interfaces y separación de lógica y GUI.

Para los diagramas del equipo, estos requisitos sirven como restricciones que revisar. El enunciado pide **dos UML sin especificar tipos**; clases y secuencia son la propuesta conversada. Un dibujo adicional de arquitectura puede ayudar, pero no aparece como un tercer UML obligatorio.

Antes de programar una parte se conserva el mapa de función y la explicación del estudiante exigidos por [AGENTS.md](../regla-pedagogica.md). Este índice no aprueba automáticamente el [borrador de arquitectura](../arquitectura-borrador.md).

## 4. Funcionamiento del simulador

| Cuando estemos hablando de… | Consultar | Qué comprobar |
|---|---|---|
| Qué es un proceso y qué datos tiene | [PRO](checklist-requerimientos-proyecto-1.md#pro--creación-y-modelo-de-procesos), [PCB](checklist-requerimientos-proyecto-1.md#pcb--campos-mínimos-del-bloque-de-control) | Tipos, parámetros, identidad global, monohilo y contexto |
| Cómo nace, espera, corre y termina | [EST](checklist-requerimientos-proyecto-1.md#est--estados-y-terminación), [MEM](checklist-requerimientos-proyecto-1.md#mem--admisión-y-memoria-principal) | Estados, motivos, admisión por RAM, liberación y deadline |
| Quién recibe CPU | [PLA](checklist-requerimientos-proyecto-1.md#pla--políticas-y-colas-de-planificación) | Políticas, quantum, apropiación, cola única y cambio de política |
| Qué pasa en un ciclo | [CIC](checklist-requerimientos-proyecto-1.md#cic--reglas-de-ejecución-de-la-simulación), ARC-13–14 | Avance de instrucciones, PC/MAR, modo usuario/SO y sincronización del clúster |
| Cómo colaboran productor y consumidor | [BUF](checklist-requerimientos-proyecto-1.md#buf--buffers-distribución-y-latencia), [SEM](checklist-requerimientos-proyecto-1.md#sem--semáforos-y-comportamiento-bloqueante) | Capacidad, anfitrión, acceso remoto, orden de primitivas y bloqueos |

**Recorrido para la reunión:** tomar un proceso y seguirlo desde su creación hasta su terminación. Después repetir con un consumidor que encuentra vacío un buffer remoto. En cada paso señalar requisito, estado y dato que cambia. Es un ejercicio para descubrir decisiones, no una secuencia de implementación impuesta.

## 5. Uso y resultados visibles

**Abrir:** [GUI](checklist-requerimientos-proyecto-1.md#gui--interfaz-gráfica-y-observabilidad), [CFG](checklist-requerimientos-proyecto-1.md#cfg--configuración-y-persistencia) y [MET](checklist-requerimientos-proyecto-1.md#met--métricas-de-rendimiento).

Separar tres revisiones:

- **Introducir y guardar:** parámetros, carga inicial, CSV/JSON, cambios permitidos en ejecución y validaciones.
- **Observar:** CPU, PCB, colas, motivos, RAM, reloj, buffers, semáforos y log.
- **Medir:** métricas por computador y globales, indicadores de buffers y gráfico compartido de utilización.

Mostrar un dato no demuestra que su cálculo sea correcto. Por eso GUI y métricas conservan requisitos distintos.

## 6. Informe, entrega y defensa

**Abrir:** [DOC](checklist-requerimientos-proyecto-1.md#doc--informe-y-documentación), [ENT](checklist-requerimientos-proyecto-1.md#ent--entrega-y-defensa) y [consecuencias de evaluación](checklist-requerimientos-proyecto-1.md#consecuencias-de-evaluación-declaradas).

| Momento | Qué organizar |
|---|---|
| Mientras desarrollan | Guardar explicaciones, decisiones y evidencia de comparación de políticas y accesos locales/remotos |
| Preparación del informe | Clases/métodos principales, enums/interfaces, dos UML y conclusiones |
| Entrega | Programa funcional en NetBeans, informe PDF, enlace GitHub enviado a ambos destinatarios y registro en spreadsheet antes del límite |
| Defensa | Asistencia de todos y capacidad individual de explicar el funcionamiento general de todos los módulos |

La fecha literal es viernes de Semana 7 antes de las 7:00 AM. La fecha de calendario y el spreadsheet están por confirmar. No confundir la consulta al preparador con el envío de la entrega final a los dos destinatarios.

## Fuera de Proyecto 1

[Exclusiones y continuidad](checklist-requerimientos-proyecto-1.md#exclusiones-y-alcance-de-la-siguiente-entrega): disco, memoria virtual y suspensión quedan para Proyecto 2; en Proyecto 1 basta controlar RAM usada/libre. Tampoco se exige copiar el estilo de la GUI ni documentar todo el código.

## Dónde guardar cada cosa

- **Obligaciones y casillas:** checklist original, sin cambios en esta reorganización.
- **Navegación:** este índice.
- **Entender las dudas:** dudas explicadas.
- **Texto de consulta:** mensaje al preparador.
- **Respuestas futuras:** documento separado con pregunta, respuesta, procedencia y efecto; no dar un supuesto por confirmado.
