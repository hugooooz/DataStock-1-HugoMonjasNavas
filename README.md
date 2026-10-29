# DataStock - UT1

**Modulo:** Acceso a Datos - 2 DAM  
**Curso:** 2026-2027  
**Tecnologia:** Java 21 y Maven  
**Aplicacion:** Consola

## Descripcion

DataStock es una aplicacion de consola para trabajar con un inventario de productos informaticos. Permite importar catalogos JSON y XML, consultar productos, exportar datos y guardar o recuperar el inventario. Durante la UT1 los productos se mantienen en memoria y no se utiliza MySQL.

## Requisitos para ejecutar

- JDK 21.
- Maven.
- VS Code con Extension Pack for Java, u otro IDE compatible con Maven.

Abrir en VS Code la carpeta del proyecto que contiene `pom.xml`. En la terminal de esa carpeta ejecutar:

```bash
mvn clean compile
mvn exec:java
```

Tambien se puede ejecutar la clase `es.iesisidra.datastock.App` desde VS Code.

## Funciones del menu principal

- **1. Explorar ficheros y directorios (RF-FIC-01):** comprueba una ruta, muestra archivos, carpetas, tamanos y totales.
- **2. Importar JSON (RF-FIC-02):** lee productos con Gson, valida sus datos y rechaza duplicados o registros incorrectos.
- **3. Importar XML (RF-FIC-03):** lee productos con DOM, aplica validaciones y rechaza XML con DTD.
- **4. Consultar productos (RF-FIC-04):** lista productos ordenados por codigo y busca por codigo o nombre.
- **5. Exportar productos (RF-FIC-05):** crea archivos JSON o XML con los productos disponibles.
- **6. Guardar y recuperar inventario (RF-FIC-06):** utiliza serializacion Java para almacenar y recuperar una copia en un fichero `.dat`.

El menu comienza con el inventario vacio. Hay que importar productos antes de consultarlos, exportarlos o guardarlos. Las importaciones JSON y XML anaden productos; recuperar un `.dat` sustituye la lista actual, con confirmacion cuando hay productos cargados.

## Actividades independientes

**RF-FIC-07 - XPath:** clase `es.iesisidra.datastock.complementarias.XPathActividad`. Permite buscar en un XML por codigo, categoria, precio superior y contar productos. Se ejecuta desde VS Code abriendo la clase y pulsando **Run Java** en su `main`.

**RF-FIC-08 - RandomAccessFile:** clase `es.iesisidra.datastock.complementarias.AccesoAleatorioActividad`. Permite generar `stock.bin`, consultar posiciones, modificar el stock y mostrar todos los registros. Cada registro ocupa 36 bytes. Tambien se ejecuta desde su propio `main` en VS Code.

El fichero `stock.bin` es independiente del inventario en memoria: modificar un registro binario no modifica los objetos `Producto`.

## Carpetas principales

```text
DataStock_UT1_Proyecto_Inicial/
  pom.xml
  README.md
  .gitignore
  datos/
    ejemplos/             JSON y XML de entrada y pruebas
    salidas/              Resultados generados por el programa
  material_referencia/    Material facilitado para la futura UT2
  src/main/java/es/iesisidra/datastock/
    App.java
    ficheros/             ExploradorFicheros.java
    modelo/               Producto y otras clases del modelo
    servicio/             Inventario, importadores, consultas, exportacion y serializacion
    ui/                   MenuPrincipal.java
    util/                 Lectura de consola
    complementarias/      XPathActividad.java y AccesoAleatorioActividad.java
```

## Archivos para realizar pruebas

Algunos ejemplos de entrada estan en `datos/ejemplos/`, entre ellos `catalogo_valido.json`, `catalogo_valido.xml` y ficheros con errores. Al probar las exportaciones se pueden utilizar rutas como `datos/salidas/inventario.json` y `datos/salidas/inventario.xml`. La carpeta de destino debe existir.

Las pruebas de aceptacion de cada requisito se documentan en el registro de pruebas de la UT1 con entrada, resultado esperado, resultado obtenido y estado. Los archivos generados se pueden volver a crear ejecutando las opciones correspondientes.

## Notas

- Esta entrega corresponde solo a la UT1. No se conecta a MySQL.
- Gson se gestiona desde el `pom.xml`; XML y XPath utilizan bibliotecas de Java.
- El codigo separa el menu de las operaciones con ficheros.
- No se deben subir archivos compilados, credenciales ni datos personales al repositorio.
