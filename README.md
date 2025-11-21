# Project & Task Management API

Una API RESTful robusta y escalable diseñada para la gestión colaborativa de proyectos y tareas. Este sistema permite a los usuarios crear espacios de trabajo, asignar tareas, gestionar estados mediante un flujo de trabajo definido, y colaborar a través de comentarios con soporte para menciones y archivos adjuntos multimedia.


## 🚀 Características Principales

### 🔐 Seguridad y Autenticación
* **Sistema Stateless:** Implementación de seguridad mediante **Spring Security** y **JWT (JSON Web Tokens)** para autenticación y autorización sin estado.
* **Gestión de Roles:** Control de acceso granular basado en roles (OWNER, ADMIN, MEMBER) para proyectos y tareas.
* **Password Hashing:** Encriptación segura de contraseñas utilizando BCrypt.

### 📂 Gestión de Proyectos y Tareas
* **Proyectos Colaborativos:** Creación de proyectos y gestión de miembros con diferentes niveles de permisos.
* **Tareas Avanzadas:** Gestión del ciclo de vida de tareas con Estados, Prioridades y Etiquetas (Tags).
* **Validaciones:** Uso estricto de Jakarta Validation para asegurar la integridad de los datos de entrada.

### 💬 Sistema de Comentarios y Social
* **Feedback en Tiempo Real:** Los usuarios pueden comentar en las tareas.
* **Menciones:** Sistema para mencionar a otros usuarios dentro de los comentarios.
* **Archivos Adjuntos:** Capacidad para adjuntar evidencia o archivos a los comentarios.

### ☁️ Cloud Storage & Optimización (AWS S3 / MinIO)
* **Almacenamiento de Medios:** Integración con **AWS S3 SDK** (compatible con MinIO para desarrollo local) para el almacenamiento de archivos.
* **Presigned URLs:** Implementación de **URLs pre-firmadas (PUT/GET)** para la subida y visualización segura de archivos, descargando el tráfico pesado del servidor principal y permitiendo la carga directa del cliente al bucket S3.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.5.4
* **Base de Datos:** MySQL 8.0 (JPA / Hibernate)
* **Seguridad:** Spring Security 6 + JJWT
* **Almacenamiento:** AWS SDK v2 (S3)
* **Herramientas:** Maven, Lombok, ModelMapper (manual/DTO pattern).

## 📐 Arquitectura

El proyecto sigue una arquitectura en capas clásica y limpia:
1.  **Controllers:** Manejan las peticiones HTTP y la validación de entrada.
2.  **Services:** Contienen toda la lógica de negocio y transaccionalidad (`@Transactional`).
3.  **DAO/Repositories:** Capa de acceso a datos utilizando Spring Data JPA y consultas nativas optimizadas cuando es necesario.
4.  **DTOs:** Uso extensivo de Data Transfer Objects para desacoplar las entidades de persistencia de la vista pública de la API.

## ⚙️ Configuración

El proyecto utiliza variables de entorno para proteger credenciales sensibles. Asegúrate de configurar las siguientes variables en tu IDE o servidor:

* `DB_URL`, `DB_USER`, `DB_PASSWORD` (MySQL)
* `JWT_SECRET` (Firma de tokens)
* `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET` (MinIO/AWS)
