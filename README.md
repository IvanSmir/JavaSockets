# Java Sockets Project

Este proyecto implementa una aplicación cliente-servidor utilizando Java Sockets. Contiene tres componentes principales: un servidor, un cliente y la cantidad de esclavos que se levanten.

## Requisitos

- Java JDK 11 o superior
- Maven

## Configuración y Ejecución

### Clonar el Repositorio

Para obtener el proyecto, clona el repositorio utilizando el siguiente comando:
```bash
git clone https://github.com/IvanSmir/JavaSockets.git
cd JavaSockets
git checkout parte3
```

### Construir el Proyecto

Dentro de la raíz del proyecto, ejecuta el siguiente comando para construir el proyecto y generar los archivos JAR para el servidor y el cliente:

```bash
mvn clean package
```


Esto generará dos archivos JAR en el directorio `target`: uno para el servidor y otro para el cliente.

### Ejecutar la Aplicación

#### Servidor

Para iniciar el servidor, ejecuta:

```
java -jar target/server-jar-with-dependencies.jar 
```

Al conectarse el servidor dara un puerto en el cual deberas conectar los clientes y esclavos.

#### Cliente

Para iniciar el cliente, ejecuta:

```
java -jar target/client-jar-with-dependencies.jar localhost:port
```
#### Slave

Para iniciar cada esclavo, ejecuta:

```
java -jar target/slave-jar-with-dependencies.jar localhost:port
```
