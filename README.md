# DataStock - Gestion de inventario

**Modulo:** Acceso a Datos  
**Curso:** 2.º DAM - 2026/2027  
**Centro:** IES Isidra de Guzman  
**Tecnologias:** Java 21, Maven, MySQL, SQLite, JDBC, JSON y XML

## 1. Descripcion del proyecto

DataStock es una aplicacion de consola desarrollada en Java para gestionar un inventario de productos informaticos.

El proyecto se desarrolla por fases. En la primera unidad se implementaron las operaciones con ficheros, importacion y exportacion de productos y almacenamiento en memoria. En la segunda unidad se ha ampliado la aplicacion para trabajar con una base de datos MySQL mediante JDBC.

Se mantiene el funcionamiento de la UT1 y se incorpora un menu independiente para gestionar categorias, proveedores, productos y movimientos de stock.

## 2. Requisitos para ejecutar

- JDK 21.
- Maven.
- MySQL Server 8.0.16 o posterior.
- MySQL Workbench o una herramienta equivalente.
- Visual Studio Code con soporte para Java, o un IDE compatible con Maven.

Las dependencias se gestionan desde `pom.xml`:

| Dependencia | Version | Utilidad |
|---|---|---|
| Gson | 2.11.0 | Lectura y escritura JSON |
| MySQL Connector/J | 8.4.0 | Conexion JDBC con MySQL |
| SQLite JDBC | 3.46.1.0 | Actividad independiente de SQLite |

## 3. Preparacion de MySQL

Antes de ejecutar las funcionalidades de UT2, es necesario tener MySQL Server iniciado.

1. Abrir MySQL Workbench y conectarse al servidor local.
2. Abrir el script `DataStock_BD_MySQL.sql` incluido en el proyecto.
3. Ejecutar el script completo.
4. Comprobar que existe la base de datos `datastock`.

El esquema contiene cuatro tablas:

- `categoria`: categorias de los productos.
- `proveedor`: empresas proveedoras.
- `producto`: informacion y existencias de los productos.
- `movimiento_stock`: historial de entradas y salidas.

El script tambien crea el procedimiento almacenado `registrar_movimiento`, encargado de actualizar el stock y registrar los movimientos dentro de una transaccion.

Se puede comprobar la instalacion con:

```sql
USE datastock;
SHOW TABLES;
SHOW PROCEDURE STATUS WHERE Db = 'datastock';
```

No se deben eliminar ni modificar las restricciones de integridad del script proporcionado.

## 4. Configuracion de la conexion

La conexion con MySQL se configura mediante un archivo local llamado `config.properties`, situado junto a `pom.xml`.

Se puede utilizar `config.example.properties` como referencia.

Ejemplo:

```properties
db.url=jdbc:mysql://localhost:3306/datastock
db.user=TU_USUARIO
db.password=TU_CONTRASENA
```

Hay que sustituir los datos de ejemplo por los de la instalacion local de MySQL.

El archivo `config.properties` esta excluido del repositorio mediante `.gitignore` para evitar publicar contrasenas.

La clase `ConexionBD` lee esta configuracion y abre las conexiones JDBC mediante `DriverManager`.

Si el servidor esta apagado, las credenciales son incorrectas o la base de datos no existe, la aplicacion muestra un mensaje de error y permite continuar utilizando el menu.

## 5. Compilar y ejecutar DataStock

Abrir en VS Code la carpeta del proyecto que contiene `pom.xml`.

Desde un terminal donde Maven este instalado y disponible:

```bash
mvn clean compile
mvn exec:java
```

Tambien se puede ejecutar directamente la clase principal:

`es.iesisidra.datastock.App`

Desde VS Code, abrir `App.java` y seleccionar **Run Java**.

El programa muestra un menu de consola con las funciones de las dos unidades.

## 6. Funcionalidades de UT1

La UT1 trabaja con un inventario almacenado en memoria Java.

| Opcion | Funcionalidad |
|---|---|
| 1 | Explorar ficheros y directorios |
| 2 | Importar productos desde JSON |
| 3 | Importar productos desde XML |
| 4 | Consultar productos en memoria |
| 5 | Exportar productos a JSON o XML |
| 6 | Guardar y recuperar el inventario mediante serializacion |

Los productos importados se validan antes de incorporarse al inventario. Se comprueban los campos obligatorios y se rechazan los registros incorrectos o duplicados.

En la importacion JSON tambien se controla el caso de un producto vacio, como `{}`, para que no se incorpore al inventario.

La funcionalidad de XML utiliza DOM y bloquea los documentos con DTD por seguridad.

### Actividades complementarias de UT1

**XPathActividad:** realiza consultas sobre un documento XML, incluyendo busquedas y filtros.

**AccesoAleatorioActividad:** trabaja con `RandomAccessFile` y registros binarios de longitud fija. Cada registro ocupa 36 bytes.

Ambas actividades tienen su propio metodo `main` y pueden ejecutarse desde VS Code sin utilizar el menu principal.

## 7. Funcionalidades de UT2

Se ha añadido la opcion `7. Gestionar base de datos MySQL`.

Dentro de este menu estan disponibles las siguientes operaciones:

| Opcion | Requisito | Funcion |
|---|---|---|
| 1 | RF-JDBC-01 | Comprobar conexion y esquema MySQL |
| 2 | RF-JDBC-02 | Gestionar categorias |
| 3 | RF-JDBC-02 | Gestionar proveedores |
| 4 | RF-JDBC-03 | Crear, consultar, modificar y eliminar productos |
| 5 | RF-JDBC-04 | Consultar y buscar productos con SQL |
| 6 | RF-JDBC-05 | Registrar entradas y salidas de stock |
| 7 | RF-JDBC-06 | Consultar el historial de movimientos |
| 8 | RF-JDBC-07 | Importar productos desde memoria a MySQL |
| 9 | RF-JDBC-08 | Demostrar commit y rollback |

### Categorias y proveedores

Se pueden dar de alta, listar, modificar y eliminar categorias y proveedores.

La aplicacion comprueba identificadores, campos obligatorios y valores duplicados. Las categorias y proveedores asociados a productos no pueden eliminarse mientras exista esa relacion.

### Productos

El mantenimiento de productos permite realizar las operaciones CRUD.

Se validan el codigo, nombre, precio, stock inicial y los identificadores de categoria y proveedor.

Los precios se trabajan con `BigDecimal`. Una vez creado un producto, su stock no se modifica desde la opcion de edicion, sino mediante los movimientos.

### Consultas

Las consultas utilizan `PreparedStatement` y `ResultSet`.

Se pueden listar productos, buscar por codigo o por una parte del nombre y consultar los productos con stock inferior a un valor.

Mediante `JOIN` se muestran tambien los nombres de las categorias y proveedores.

### Entradas y salidas de stock

Las operaciones de stock llaman al procedimiento `registrar_movimiento` mediante `CallableStatement`.

El procedimiento actualiza las existencias y registra el movimiento. Si se intenta retirar una cantidad superior al stock disponible, la operacion se rechaza.

El historial conserva el stock anterior y posterior de cada movimiento.

### Importacion desde memoria

La opcion 8 permite copiar productos del inventario Java de UT1 a MySQL.

Antes de insertar, se comprueban los datos del producto, su codigo, duplicados y la existencia de categoria y proveedor.

Se informa de los productos insertados, omitidos y rechazados. La importacion utiliza una transaccion JDBC con `commit()` y `rollback()` para evitar inserciones parciales ante errores SQL.

Esta operacion no elimina los productos almacenados en memoria.

### Demostracion de transacciones

La opcion 9 realiza dos pruebas con categorias temporales:

- **COMMIT:** inserta dos categorias y confirma los cambios.
- **ROLLBACK:** provoca un error de clave duplicada y deshace la insercion temporal.

Despues comprueba el resultado mediante consultas SQL y explica como limpiar los datos utilizados para la demostracion.

## 8. Diferencia entre memoria y MySQL

La aplicacion utiliza dos almacenes independientes.

**Memoria Java (UT1):** contiene los productos importados durante la ejecucion. Se pueden guardar en un fichero mediante la opcion de serializacion. Si se cierra el programa sin guardarlos, se pierde ese contenido de memoria.

**MySQL (UT2):** conserva los productos almacenados en la base de datos aunque se cierre y vuelva a ejecutar DataStock.

Los datos no se sincronizan automaticamente. La copia desde memoria a MySQL se realiza solamente cuando el usuario selecciona la opcion `7 → 8`.

Por ejemplo, importar un JSON desde la opcion 2 no significa que sus productos se hayan guardado directamente en MySQL.

## 9. Actividad independiente SQLite

Como actividad complementaria de UT2 se ha desarrollado `DemoSQLite`.

Clase principal:

`es.iesisidra.datastock.complementarias.DemoSQLite`

Para ejecutarla, abrir `DemoSQLite.java` en VS Code y pulsar **Run Java** sobre su metodo `main`.

La demostracion:

1. Crea el directorio `datos/sqlite`, si no existe.
2. Abre una conexion SQLite.
3. Crea la tabla `producto_demo`.
4. Inserta dos productos de ejemplo.
5. Recupera y muestra los registros mediante `SELECT`.
6. Controla los duplicados cuando se ejecuta de nuevo.
7. Cierra los recursos JDBC.

La base de datos se guarda en:

`datos/sqlite/productos_demo.db`

Esta actividad no modifica la base MySQL ni el inventario principal de DataStock.

## 10. Comparativa MySQL y SQLite

| Caracteristica | MySQL | SQLite |
|---|---|---|
| Servidor | Necesita un servidor | No necesita servidor independiente |
| Almacenamiento | Datos gestionados por MySQL Server | Fichero local `.db` |
| Conexion JDBC | `jdbc:mysql://localhost:3306/datastock` | `jdbc:sqlite:datos/sqlite/productos_demo.db` |
| Dependencia | `mysql-connector-j` | `sqlite-jdbc` |
| Ventaja principal | Adecuado para varios usuarios y aplicaciones conectadas | Facil de instalar y utilizar |
| Inconveniente principal | Necesita configurar y mantener un servidor | Menos adecuado para muchas escrituras concurrentes |
| Uso recomendado | Aplicaciones empresariales o sistemas compartidos | Aplicaciones locales, pruebas y programas pequenos |

**Eleccion para DataStock**

MySQL es adecuado para la gestion principal del inventario porque permite almacenar y relacionar categorias, proveedores, productos y movimientos en una base de datos independiente.

SQLite resulta util para la demostracion porque permite practicar JDBC utilizando un fichero local, sin necesidad de configurar otro servidor.

### Pool de conexiones

Un pool de conexiones es un conjunto de conexiones a base de datos que una aplicacion mantiene disponibles para reutilizarlas.

Es util cuando hay muchos usuarios o consultas frecuentes, porque abrir una conexion nueva constantemente puede consumir tiempo y recursos.

En DataStock no hemos utilizado un pool de conexiones porque es una aplicacion de consola con operaciones sencillas y puntuales. Cada operacion abre la conexion que necesita y la cierra correctamente mediante `try-with-resources`.

## 11. Buenas practicas y seguridad

Durante el desarrollo se han aplicado las siguientes medidas:

- Uso de `PreparedStatement` para los datos recibidos del usuario.
- Uso de `CallableStatement` para el procedimiento de movimientos.
- Cierre automatico de recursos JDBC con `try-with-resources`.
- Comprobacion de datos antes de insertar o actualizar registros.
- Respeto de las claves primarias, foraneas y restricciones del esquema SQL.
- Confirmacion antes de eliminar registros.
- Control de errores sin finalizar bruscamente la aplicacion.
- Mensajes de operaciones correctas y errores diferenciados mediante colores ANSI.
- Archivo de configuracion local excluido de Git.

No se utilizan Hibernate ni JPA. El acceso a datos se realiza mediante JDBC.

## 12. Estructura principal del proyecto

```text
DataStock_UT1_Proyecto_Inicial/
|
|-- pom.xml
|-- README.md
|-- .gitignore
|-- config.example.properties
|-- datos/
|   |-- ejemplos/
|   |-- sqlite/
|   |-- salidas/
|
|-- src/main/java/es/iesisidra/datastock/
|   |-- App.java
|   |-- config/
|   |   |-- ConexionBD.java
|   |-- modelo/
|   |-- servicio/
|   |   |-- Inventario.java
|   |   |-- ImportadorMemoriaBD.java
|   |-- ficheros/
|   |-- ui/
|   |   |-- MenuPrincipal.java
|   |   |-- MenuMySQL.java
|   |   |-- MenuCatalogos.java
|   |   |-- MenuProductosBD.java
|   |   |-- MenuConsultasBD.java
|   |   |-- MenuMovimientosBD.java
|   |   |-- MenuHistorialBD.java
|   |   |-- DemoTransaccionesBD.java
|   |-- util/
|   |-- complementarias/
|       |-- XPathActividad.java
|       |-- AccesoAleatorioActividad.java
|       |-- DemoSQLite.java
```

El proyecto mantiene las clases de UT1 y añade las necesarias para gestionar las operaciones JDBC de UT2.

## 13. Pruebas realizadas

Las pruebas de aceptacion se documentan en el registro de pruebas de la entrega.

Incluyen:

- **CP-JDBC-01 a CP-JDBC-20:** conexion, categorias, proveedores, productos, consultas, movimientos, historial, importacion y transacciones.
- **CP-SQLITE-01 a CP-SQLITE-03:** primera ejecucion, control de duplicados y persistencia del fichero SQLite.

Se han comprobado tanto situaciones correctas como errores, incluyendo duplicados, referencias inexistentes, stock insuficiente y ejecucion de rollback.

El documento de pruebas incluye los datos introducidos, resultados esperados, resultados obtenidos y las capturas correspondientes.

## 14. Ejemplo de uso

Un recorrido completo de DataStock seria:

1. Ejecutar la aplicacion.
2. Importar un fichero JSON desde la opcion 2.
3. Consultar los productos en memoria con la opcion 4.
4. Entrar en la opcion 7 y comprobar la conexion MySQL.
5. Crear las categorias y proveedores necesarios.
6. Copiar los productos de memoria a MySQL mediante la opcion 8 del menu MySQL.
7. Consultar los productos guardados.
8. Registrar una entrada y una salida de stock.
9. Consultar el historial de movimientos.
10. Cerrar DataStock y volver a ejecutarlo para comprobar que los datos siguen almacenados en MySQL.

## 15. Entrega

La segunda fase se entrega como continuacion del repositorio individual de UT1.

Se incluyen el codigo Java, `pom.xml`, el script SQL facilitado, el README y los ficheros de ejemplo necesarios.

Las pruebas se presentan en el documento correspondiente, entregado en formato PDF.

**Etiqueta de Git de esta entrega:** `UT2-v1.0`.