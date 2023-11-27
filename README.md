# Java Sockets Project

Este proyecto implementa una aplicación cliente-servidor utilizando Java Sockets. Contiene dos componentes principales: un servidor y un cliente.

## Requisitos

- Java JDK 11 o superior

## Configuración y Ejecución

### Clonar el Repositorio

Para obtener el proyecto, clona el repositorio utilizando el siguiente comando:
```bash
git clone https://github.com/IvanSmir/JavaSockets.git
```

### Construir el Proyecto

Dentro de la raíz del proyecto, compila con el siguiente comando

```bash
javac -cp ".;lib/gson.jar" Server.java
javac -cp ".;lib/gson.jar" Client.java
```


### Ejecutar la Aplicación

#### Servidor

Para iniciar el servidor, ejecuta:

```
java -cp ".;lib/gson.jar" Server
```

#### Cliente

Para iniciar el cliente, ejecuta:

```
java -cp ".;lib/gson.jar" Client localhost:7777
```
