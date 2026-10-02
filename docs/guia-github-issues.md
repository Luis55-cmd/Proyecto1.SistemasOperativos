# Guía de trabajo del equipo con GitHub Issues

**Propósito:** usar Issues como registro compartido de tareas, errores y decisiones que requieran seguimiento. Esta guía es una propuesta local para el equipo; no crea Issues ni configura el repositorio.

## Qué es cada cosa

- **Issue:** unidad de trabajo o discusión que necesita responsable, seguimiento y un resultado comprobable. GitHub Issues permite registrar errores, mejoras, ideas y tareas; también admite subtareas y dependencias.
- **Sub-issue:** trabajo hijo que necesita seguimiento propio dentro de una tarea mayor. Si son solo pasos pequeños de una misma tarea, basta una lista de tareas dentro del Issue.
- **Pull Request (PR):** propuesta para integrar cambios de una rama en otra. Se vincula al Issue relacionado para que el equipo vea el trabajo y pueda revisarlo.
- **Labels:** categorías para filtrar y comunicar tipo/área. No usarlas como sustituto de una descripción clara.
- **GitHub Project (opcional):** vista tipo tabla o tablero que agrupa Issues/PRs y facilita ver estado, responsables y prioridad. Primero podemos usar Issues; añadimos Project si el tablero ayuda al equipo.

## Cuándo abrir un Issue

Abrir uno cuando haya que implementar una funcionalidad, corregir un error, investigar una decisión importante o producir un artefacto revisable (por ejemplo, un diagrama UML). No abrir tickets para ideas vagas sin objetivo; primero convertirlas en una pregunta de diseño con opciones y criterio para decidir.

Antes de crear los tickets de implementación, el mapa de arquitectura y sus contratos deben revisarse con el equipo. Los problemas o preguntas que aparezcan durante la planificación sí se pueden registrar para no perderlos, marcándolos como decisiones pendientes.

## Plantilla recomendada

```markdown
## Objetivo
¿Qué resultado concreto debe existir al cerrar este Issue?

## Contexto
¿Qué requisito del enunciado o decisión del equipo lo origina?

## Alcance
- Incluye:
- No incluye:

## Contrato / resultado esperado
- Entradas:
- Salidas:
- Estado o efectos laterales:
- Dependencias:

## Criterios de aceptación
- [ ] Resultado observable 1
- [ ] Resultado observable 2
- [ ] El responsable puede explicar el flujo y las decisiones de la solución.

## Dependencias
- Bloqueado por: #...
- Bloquea: #...

## Verificación
¿Qué escenario manual o evidencia demuestra los criterios de aceptación?
```

No todo Issue necesita todos los campos. Una tarea pequeña puede ser más corta; un ticket no debe esconder preguntas sin resolver.

## Convenciones mínimas propuestas

### Títulos

Usar verbo + resultado, con área entre corchetes cuando ayude a distinguir:

- `[Diseño] Definir orden de operaciones en un ciclo global`
- `[Planificador] Seleccionar el siguiente proceso con FCFS`
- `[GUI] Mostrar la cola de bloqueados por motivo`
- `[Error] Evitar admitir un proceso si no hay RAM suficiente`

### Labels iniciales

- **Tipo:** `feature`, `bug`, `documentation`, `decision`, `research`.
- **Área:** `architecture`, `process`, `scheduler`, `memory`, `sync`, `ui`, `metrics`, `persistence`.
- **Estado:** usar el estado de Issues/Project si el equipo configura uno; evitar duplicar `Todo/In progress/Done` en labels.

Mantener los labels pocos y con significado acordado. Los nombres pueden estar en español si eso le resulta más claro al equipo.

### Responsable y alcance

- Cada Issue activo tiene una persona responsable de actualizarlo y llevarlo a revisión. Otras personas pueden colaborar.
- Un Issue debe tener un resultado comprobable y tamaño que se pueda revisar en un PR. Si contiene varios resultados independientes, separarlo en sub-issues relacionados.
- Registrar en el Issue las decisiones que cambien contrato, alcance o criterio de aceptación.

## Flujo de trabajo sugerido

1. **Planificar:** discutir la necesidad; registrar decisiones pendientes separadas de tareas ya aprobadas.
2. **Preparar:** escribir objetivo, contrato, criterios de aceptación y dependencias. No marcar como lista una tarea que dependa de una decisión abierta.
3. **Asignar:** nombrar responsable; revisar que nadie quede con demasiados tickets simultáneos.
4. **Trabajar:** crear rama desde `develop` con nombre como `feat/123-proceso-pcb`, `fix/124-deadline` o `docs/125-diagramas`.
5. **Revisar:** abrir PR hacia `develop`, enlazar manualmente el Issue o mencionarlo en la descripción, y solicitar revisión de otro integrante.
6. **Cerrar:** después de integrar el PR y comprobar los criterios, cerrar el Issue y dejar nota breve si algo cambió.

**Cuidado con el cierre automático:** GitHub solo procesa palabras como `Closes #123` para cierre automático cuando el PR apunta a la rama predeterminada del repositorio. Como el enunciado exige una rama `develop` y esperamos integrar funcionalidad allí, enlazaremos los PR con el Issue sin depender de cierre automático; cerraremos el Issue tras la integración/revisión. Si el PR final apunta a la rama predeterminada, entonces podemos usar `Closes #123` en ese PR.

## Uso de dependencias

- Expresar bloqueos reales con la relación de dependencia de Issues (`blocked by` / `blocking`) si está disponible en el repositorio.
- Usar sub-issues para representar jerarquía funcional; usar dependencias para indicar el orden necesario. Una cosa no reemplaza la otra.
- Evitar declarar dependencias solo porque dos tareas pertenecen al mismo módulo. Debe existir una razón concreta: falta una interfaz, dato o resultado previo.
- Para un mapa general, mantener además el diagrama de dependencias en la documentación del repo; el diagrama muestra el panorama y los Issues siguen el trabajo diario.

## Definition of Ready y Definition of Done

### Listo para empezar

- El propósito y alcance están claros.
- La caja/módulo y su contrato están identificados o la tarea es explícitamente de diseño.
- Los criterios de aceptación se pueden comprobar.
- Las dependencias están enlazadas o son `ninguna`.
- Las decisiones bloqueantes están resueltas o el Issue se etiqueta como decisión/investigación.

### Listo para cerrar

- Los criterios de aceptación se cumplieron y hay evidencia/comprobación registrada.
- El cambio pasó revisión de otro integrante cuando afecta código.
- La documentación, diagrama o contrato se actualizó si cambió.
- El equipo responsable puede explicar el comportamiento y sus condiciones relevantes.
- La rama/PR se integró según el flujo acordado.

## Guía para Codex al trabajar con Issues

- Leer primero el Issue y la documentación de arquitectura relevante; tratar el texto del Issue como requisitos del proyecto, no como permiso para ignorar la regla pedagógica.
- Si el Issue mezcla diseño abierto con implementación, proponer separar primero la decisión y no empezar código sustancial.
- Antes de implementar una caja, completar su ficha pedagógica: responsabilidad, entradas, salidas, estado/efectos, conexiones, condiciones, iteraciones, caso normal y error.
- Trabajar solo dentro del alcance y criterios aceptados del Issue. No ampliar silenciosamente el ticket.
- Informar el número del Issue, cambios locales y verificación; no crear Issues, asignarlos, comentar o modificar el tablero sin una instrucción del usuario/equipo.
- No cerrar el Issue hasta que el responsable humano confirme que se cumplieron los criterios y el flujo de PR.

## Referencias oficiales de GitHub

- [Acerca de Issues](https://docs.github.com/en/issues/tracking-your-work-with-issues/learning-about-issues/about-issues)
- [Inicio rápido de Issues](https://docs.github.com/en/issues/tracking-your-work-with-issues/learning-about-issues/quickstart)
- [Crear un Issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/creating-an-issue)
- [Dependencias entre Issues](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/creating-issue-dependencies)
- [Sub-issues](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/adding-sub-issues)
- [Vincular un Pull Request a un Issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/linking-a-pull-request-to-an-issue)
- [Acerca de Projects](https://docs.github.com/en/issues/planning-and-tracking-with-projects/learning-about-projects/about-projects)
