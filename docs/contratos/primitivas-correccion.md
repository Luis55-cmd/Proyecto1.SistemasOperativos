# Contratos de las primitivas corregidas

Revisión base: `c1686da`. Corrección autorizada por César tras el visto bueno de Ricardo, 04-10-2026. Alcance: errores secuenciales y contratos discutidos; no cambios al diseño del SO. Aún requiere revisión del autor y prueba de integración.

## Acuerdos que aplica este cambio

- Extraer/consultar vacío o borrar un extremo de lista vacía lanza `NoSuchElementException` antes de modificarla. Esta clase de excepción no es una colección Java.
- Insertar/borrar con índice inválido lanza `IndexOutOfBoundsException` sin mutación. Un borrado por posición sobre lista vacía tiene índice inválido.
- `dequeue()` y `pop()` devuelven el elemento retirado. Un llamador puede ignorar ese retorno.
- Se conservan firmas restantes y nombres del autor. `null` como dato conserva su comportamiento previo, sin convertirlo en contrato definitivo. No se añaden candados ni getters/setters.

## Mapa por función afectada

| Función | Responsabilidad / entradas → salida | Estado, conexiones y condiciones | Iteraciones y terminación | Normal / error |
|---|---|---|---|---|
| deleteFirst | Borrar primer nodo; sin entrada → void | Cambia primero/tamaño y último si queda vacía; usa isEmpty | Sin ciclos | Borra uno / vacío: excepción sin cambio |
| deleteLast | Borrar último; sin entrada → void | Vacío, único o varios; conecta con deleteFirst sólo en único y retorna | En varios recorre hasta predecesor del último en cadena válida | Deja n−1 / vacío: excepción |
| insertPos | pos y dato T → void; insertar una vez | Rango 0…n; delega inicio/final y retorna; interior modifica enlaces y tamaño | Interior: pos−1 enlaces, después inserta | Conserva orden/extremos / índice inválido: excepción sin cambio |
| deletePos | pos → void; borrar exactamente uno | Rango 0…n−1; delega inicio/final y retorna; no sigue por otro camino | Interior: pos−1 enlaces, después elimina | Conserva resto / índice inválido: excepción sin cambio |
| dequeue | Retirar frente → T | Usa isEmpty; cambia primero/tamaño y último al vaciar | Sin ciclos | Devuelve primero / vacío: excepción |
| peek | Consultar frente → T | Usa isEmpty; no cambia enlaces/tamaño | Sin ciclos | Devuelve primero / vacío: excepción |
| push | Dato T → void; agregar al tope | Nuevo Node<T>, enlace al tope anterior, incrementa tamaño | Sin ciclos | Agrega uno; tratamiento null preservado |
| pop | Retirar tope → T | Usa isEmpty; conserva dato antes de mover tope y decrementar tamaño | Sin ciclos | Devuelve último insertado / vacío: excepción |

Node.pNext y los nodos de pila conservan ahora T, evitando operaciones genéricas no verificadas. Ninguna operación modifica estados de PCB ni aplica políticas de CPU.

## Verificación reproducible

Las pruebas están escritas en Java en `src/test/java/com/avilaos/core/structures/PrimitivasTest.java`. Su `main` ejecuta las comprobaciones y lanza `AssertionError` si alguna falla. No requieren Python ni librerías de pruebas externas. El antiguo script Python sólo lanzaba la compilación y ejecución; se retiró para depender únicamente del JDK.

Desde la raíz del repositorio, con el JDK en el PATH (terminal macOS/Linux):

```sh
javac -Xlint:unchecked -Werror -d target/primitivas-test src/main/java/com/avilaos/core/structures/*.java src/test/java/com/avilaos/core/structures/PrimitivasTest.java
```

Sólo si la compilación termina correctamente:

```sh
java -cp target/primitivas-test com.avilaos.core.structures.PrimitivasTest
```

En el Mac de César se pueden sustituir `javac` y `java` por `"/Applications/Apache NetBeans.app/Contents/Home/bin/javac"` y `"/Applications/Apache NetBeans.app/Contents/Home/bin/java"`, respectivamente. En otros equipos, usar los ejecutables de su propio JDK. Las clases compiladas quedan en `target/primitivas-test`; no deben versionarse.

Incluye tamaños 0…5, todas sus posiciones válidas, índices inválidos, vacío, duplicados, retornos de extracción y vaciar/reutilizar. Comprueba contenido, enlaces/extremos, tamaño y tipo de excepción.

No prueba concurrencia ni NetBeans. Compila sólo estructuras porque el esqueleto heredado tiene SchedulingPolicyType vacío; no presenta este resultado como compilación completa del proyecto. POM Java 17 y ese enum deben resolverse en su ámbito, no se modifican aquí.

Resultado del 04-10-2026 con JDK 25.0.2: **110 PASS, 0 FAIL y 3 observaciones de null**; compilación de estructuras y sonda sin avisos unchecked, con `-Werror`. No se declara validado un contrato de null ni acceso concurrente.

Trazabilidad: P04 / TEC-08; concurrencia ARC-12 pendiente por uso e instancia. Las pruebas no sustituyen la explicación del autor ni cierran P04 completo.
