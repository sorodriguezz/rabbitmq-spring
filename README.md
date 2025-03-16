# Microservicios Spring Boot 3 con RabbitMQ

Proyecto que demuestra cómo implementar dos microservicios que se comunican de forma asíncrona utilizando RabbitMQ. Uno de los servicios actúa como **Productor** (producer-service) y el otro como **Consumidor** (consumer-service).

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Requisitos Previos](#requisitos-previos)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Configuración de RabbitMQ con Docker Compose](#configuración-de-rabbitmq-con-docker-compose)
- [Microservicio Productor (producer-service)](#microservicio-productor-producer-service)
  - [Estructura y Clases](#estructura-y-clases-del-productor)
  - [Configuración (application.properties)](#configuración-del-productor-applicationproperties)
- [Microservicio Consumidor (consumer-service)](#microservicio-consumidor-consumer-service)
  - [Estructura y Clases](#estructura-y-clases-del-consumidor)
  - [Configuración (application.properties)](#configuración-del-consumidor-applicationproperties)
- [Ejecución y Prueba](#ejecución-y-prueba)
- [Posibles Extensiones](#posibles-extensiones)

---

## Descripción General

El proyecto está compuesto por dos microservicios:

- **producer-service:** Expone un endpoint REST para enviar mensajes a una cola de RabbitMQ. Utiliza `RabbitTemplate` para la comunicación.
- **consumer-service:** Se suscribe a la cola de RabbitMQ y procesa los mensajes recibidos utilizando `@RabbitListener`.

Esta arquitectura permite desacoplar los servicios y procesar los mensajes de forma asíncrona, mejorando la escalabilidad y resiliencia del sistema.

## Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalados y configurados:

- **JDK 17 o superior**
- **Maven** o **Gradle**
- **IDE** (IntelliJ IDEA, Eclipse, etc.)
- **Docker** (para ejecutar RabbitMQ)
- **Docker Compose** (para levantar RabbitMQ fácilmente)

## Arquitectura del Proyecto

Cada microservicio es una aplicación Spring Boot independiente:
- **producer-service:** Envía mensajes a RabbitMQ.
- **consumer-service:** Escucha y procesa los mensajes enviados a RabbitMQ.

Ambos microservicios se conectan a la misma cola denominada `exampleQueue` para garantizar la comunicación.

## Configuración de RabbitMQ con Docker Compose

Crea un archivo llamado **docker-compose.yml** en la raíz del proyecto (o en un directorio específico para infraestructura) con el siguiente contenido:

```yaml
services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    restart: unless-stopped
```

### Para levantar el servicio, en una terminal ejecuta:

```bash
docker-compose up -d
```

> Este comando iniciará RabbitMQ en segundo plano, con la interfaz de administración accesible en http://localhost:15672 (usuario/contraseña: guest/guest).

## Microservicio Productor (producer-service)

### Estructura y Clases del Productor
- **RabbitMQConfig.java**
    - Ubicación: com.sorz.rabbitmqproducerspring.config
    - Función: Configura la conexión a RabbitMQ y declara la cola exampleQueue.
    - Código clave:
        ```java
        @Configuration
        public class RabbitMQConfig {
            public static final String QUEUE_NAME = "exampleQueue";

            @Bean
            public Queue exampleQueue() {
                return new Queue(QUEUE_NAME, false);
            }
        }
        ```
        > Notas: La propiedad false indica que la cola no es durable.

- **MessageProducer.java**
    - Ubicación: com.sorz.rabbitmqproducerspring.service
    - Función: Servicio encargado de enviar mensajes a la cola mediante RabbitTemplate.
    - Código clave:
        ```java
        @Service
        public class MessageProducer {
            private final RabbitTemplate rabbitTemplate;

            public MessageProducer(RabbitTemplate rabbitTemplate) {
                this.rabbitTemplate = rabbitTemplate;
            }

            public void sendMessage(String message) {
                rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
            }
        }
        ```

- **MessageController.java**
    - Ubicación: com.sorz.rabbitmqproducerspring.controller
    - Función: Exponer un endpoint REST (/send) para recibir mensajes y delegar el envío al MessageProducer.
    - Código clave:
        ```java
        @RestController
        public class MessageController {
            private final MessageProducer messageProducer;

            public MessageController(MessageProducer messageProducer) {
                this.messageProducer = messageProducer;
            }

            @GetMapping("/send")
            public String sendMessage(@RequestParam String message) {
                messageProducer.sendMessage(message);
                return "Mensaje enviado: " + message;
            }
        }
        ```


### Configuración del Productor (application.properties)

En el archivo `src/main/resources/application.properties`, configura los siguientes parámetros:
```properties
spring.application.name=rabbitmq-producer-spring
server.port=8081

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

Estas propiedades definen el nombre de la aplicación y los detalles de conexión a RabbitMQ.

## Microservicio Consumidor (consumer-service)

### Estructura y Clases del Consumidor
- **RabbitMQConfig.java**
    - Ubicación: com.sorz.rabbitmqproducerspring.config
    - Función: Declara la cola exampleQueue y configura un método con @RabbitListener para procesar los mensajes recibidos.
    - Código clave:
        ```java
        @Configuration
        public class RabbitMQConfig {
            public static final String QUEUE_NAME = "exampleQueue";

            @Bean
            public Queue exampleQueue() {
                return new Queue(QUEUE_NAME, false);
            }

            @RabbitListener(queues = QUEUE_NAME)
            public void listen(String message) {
                System.out.println("Mensaje recibido: " + message);
            }
        }
        ```
        > Notas: El método listen se ejecuta automáticamente cada vez que se recibe un mensaje en la cola.

### Configuración del Consumidor (application.properties)

En el archivo `src/main/resources/application.properties` de consumer-service, añade:

```properties
spring.application.name=consumer-service
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```
Esta configuración es similar a la del productor y asegura que ambos servicios se conecten al mismo RabbitMQ.

## Ejecución y Prueba

- **Levantar RabbitMQ**
    - Ejecuta el archivo docker-compose.yml:
        ```bash
        docker-compose up -d
        ```
    - Verifica en http://localhost:15672 que RabbitMQ esté en funcionamiento.

- **Ejecutar los Microservicios**
    - Productor (producer-service):
        - Ejecuta la clase principal (por ejemplo, ProducerserviceApplication.java).
    - Consumidor (consumer-service):
        - Ejecuta la clase principal (por ejemplo, ConsumerserviceApplication.java).

- **Probar la Comunicación**
    - Abre un navegador o utiliza Postman para enviar un mensaje:
        ```curl
        http://localhost:8081/send?message=Hola
        ```
        > (Asegúrate de que el puerto corresponda al configurado para producer-service, en este ejemplo se asume el 8081).

	- Revisa la consola de consumer-service: deberías ver la salida Mensaje recibido: Hola.


## Posibles Extensiones
- Manejo de Errores: Implementar mecanismos para gestionar fallos en la conexión o en el procesamiento de mensajes.
- Persistencia: Guardar en base de datos los mensajes enviados y recibidos.
- Seguridad: Configurar autenticación y autorización para los endpoints.
- Escalabilidad: Añadir más colas o microservicios para procesar diferentes tipos de mensajes.

## Conclusión

Este proyecto es un ejemplo práctico para comprender la comunicación asíncrona entre microservicios utilizando Spring Boot 3 y RabbitMQ. La separación entre productor y consumidor permite un diseño desacoplado, lo que facilita el escalado y mejora la resiliencia del sistema.
