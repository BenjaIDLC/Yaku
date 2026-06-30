-- Esquema de la base de datos embebida H2 (se crea al iniciar la aplicacion).

CREATE TABLE IF NOT EXISTS estudiantes (
    id VARCHAR(10) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS suscripciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    estudiante_id VARCHAR(10) NOT NULL UNIQUE,
    clases_restantes INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVA',
    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id)
);

CREATE TABLE IF NOT EXISTS asistencias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    estudiante_id VARCHAR(10) NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id)
);
