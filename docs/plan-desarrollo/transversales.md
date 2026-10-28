# Requisitos transversales

[Volver al mapa](README.md). Se aplican durante el trabajo, no como un último paso después de desarrollar. Cada ID aparece aquí como ubicación principal y no se duplica como requisito propio de un paquete.

## TR-GIT — Forma de trabajo en GitHub

**Cuándo aplica:** Desde el primer cambio y en cada PR; revisar contribución a lo largo del proyecto.

**Evidencia:** Issue, rama, commits descriptivos y limitados, PR comentado e integración según el flujo acordado; trabajo real equilibrado.

- **[GIT-01](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Mantener el proyecto en un repositorio de GitHub.
- **[GIT-02](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** No desarrollar únicamente sobre la rama main.
- **[GIT-03](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Contar con una rama develop.
- **[GIT-04](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Evidenciar el uso de ramas por funcionalidad.
- **[GIT-05](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Registrar las tareas pendientes mediante Issues de GitHub.
- **[GIT-06](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Registrar los errores encontrados mediante Issues de GitHub.
- **[GIT-07](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Realizar las fusiones de código entre ramas mediante Pull Requests.
- **[GIT-08](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Comentar los Pull Requests utilizados para integrar código.
- **[GIT-09](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Mantener una participación equilibrada de los integrantes reflejada en el historial de commits.
- **[GIT-10](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Escribir mensajes descriptivos en los commits.
- **[GIT-11](../enunciado/checklist-requerimientos-proyecto-1.md#git--desarrollo-y-colaboración-en-github):** Mantener un tamaño limitado de los commits; el enunciado no fija un umbral numérico.

## TR-EQUIPO — Tamaño del equipo

**Cuándo aplica:** Organización inicial y si cambia la conformación.

**Evidencia:** Equipo de hasta tres integrantes.

- **[TEC-01](../enunciado/checklist-requerimientos-proyecto-1.md#tec--equipo-tecnología-y-restricciones):** Conformar un equipo de como máximo tres personas.

## TR-TEC — Librerías y colecciones permitidas

**Cuándo aplica:** Cada decisión de dependencia y cada PR con código.

**Evidencia:** Revisión de dependencias y uso de estructuras; no introducir colecciones prohibidas por comodidad.

- **[TEC-06](../enunciado/checklist-requerimientos-proyecto-1.md#tec--equipo-tecnología-y-restricciones):** Limitar las librerías externas a las categorías permitidas: gráficas, JSON/CSV, hilos y semáforos.
- **[TEC-07](../enunciado/checklist-requerimientos-proyecto-1.md#tec--equipo-tecnología-y-restricciones):** No utilizar colecciones del framework de Java, incluidas ArrayList, Queue, Stack y Vector.

## TR-GUI — Separación y calidad de interfaz

**Cuándo aplica:** Contratos desde P03; formularios y vistas en P22–P25; verificación final.

**Evidencia:** La GUI canaliza solicitudes y observa; validaciones de tipo/rango y entradas inválidas sin interrumpir el sistema.

- **[ARC-23](../enunciado/checklist-requerimientos-proyecto-1.md#arc--arquitectura-diseño-y-reloj):** Mantener la lógica del sistema fuera de las ventanas: la GUI observa y permite operar el simulador.
- **[GUI-02](../enunciado/checklist-requerimientos-proyecto-1.md#gui--interfaz-gráfica-y-observabilidad):** Proporcionar una interfaz intuitiva —criterio cualitativo del enunciado—.
- **[GUI-30](../enunciado/checklist-requerimientos-proyecto-1.md#gui--interfaz-gráfica-y-observabilidad):** Validar el tipo de dato en todos los campos de entrada.
- **[GUI-31](../enunciado/checklist-requerimientos-proyecto-1.md#gui--interfaz-gráfica-y-observabilidad):** Validar el rango en todos los campos de entrada.
- **[GUI-32](../enunciado/checklist-requerimientos-proyecto-1.md#gui--interfaz-gráfica-y-observabilidad):** Manejar entradas inválidas sin interrumpir el flujo del simulador.

## TR-OPC — Justificación de opciones elegidas

**Cuándo aplica:** Sólo si hay asignación automática o interfaces adicionales.

**Evidencia:** Investigación y justificación de balanceo; explicación de interfaces extra en la defensa. Si no aplica, registrar N/A, no incumplimiento.

- **[OPT-01](../enunciado/checklist-requerimientos-proyecto-1.md#opciones-y-obligaciones-condicionales):** Si se usa asignación automática: investigar el criterio de asignación o balanceo elegido. Fuente: RF §3.
- **[OPT-02](../enunciado/checklist-requerimientos-proyecto-1.md#opciones-y-obligaciones-condicionales):** Si se usa asignación automática: justificar ese criterio en la defensa. Fuente: RF §3.
- **[OPT-03](../enunciado/checklist-requerimientos-proyecto-1.md#opciones-y-obligaciones-condicionales):** Si se agregan otras interfaces: justificar cada interfaz adicional en la defensa. Fuente: RF §1.

## Condición pedagógica de trabajo

La regla de [aprendizaje por funciones](../regla-pedagogica.md) es adicional al enunciado y fue establecida por el estudiante. No recibe un ID inventado del checklist. Cada paquete de implementación se descompone con él en funciones defendibles antes de codificar; no es una orden de desarrollar todo el paquete de una vez.
