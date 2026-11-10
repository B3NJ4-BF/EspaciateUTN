INSERT INTO espacios
(nombre, tipo_espacio, capacidad_max, tiene_proyector, es_laboratorio, ubicacion, estado)
SELECT 'Aula 101', 'AULA_COMUN', 30, TRUE, FALSE, 'Piso 1 - Ala Norte', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM espacios WHERE nombre = 'Aula 101');

INSERT INTO espacios
(nombre, tipo_espacio, capacidad_max, tiene_proyector, es_laboratorio, ubicacion, estado)
SELECT 'Laboratorio de Redes', 'LABORATORIO', 24, TRUE, TRUE, 'Piso 2 - Ala Este', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM espacios WHERE nombre = 'Laboratorio de Redes');

INSERT INTO espacios
(nombre, tipo_espacio, capacidad_max, tiene_proyector, es_laboratorio, ubicacion, estado)
SELECT 'Box 2', 'BOX_ESTUDIO', 4, FALSE, FALSE, 'Biblioteca - Planta baja', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM espacios WHERE nombre = 'Box 2');

INSERT INTO espacios
(nombre, tipo_espacio, capacidad_max, tiene_proyector, es_laboratorio, ubicacion, estado)
SELECT 'Aula 204', 'AULA_COMUN', 40, FALSE, FALSE, 'Piso 2 - Ala Oeste', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM espacios WHERE nombre = 'Aula 204');

INSERT INTO espacios
(nombre, tipo_espacio, capacidad_max, tiene_proyector, es_laboratorio, ubicacion, estado)
SELECT 'Box 5', 'BOX_ESTUDIO', 3, TRUE, FALSE, 'Biblioteca - Primer piso', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM espacios WHERE nombre = 'Box 5');

INSERT INTO espacios
(nombre, tipo_espacio, capacidad_max, tiene_proyector, es_laboratorio, ubicacion, estado)
SELECT 'Laboratorio de Electrónica', 'LABORATORIO', 18, FALSE, TRUE, 'Piso 3 - Ala Sur', 'EN_MANTENIMIENTO'
WHERE NOT EXISTS (SELECT 1 FROM espacios WHERE nombre = 'Laboratorio de Electrónica');
