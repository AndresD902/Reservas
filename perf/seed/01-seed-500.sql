-- Semilla para el escenario de 500 usuarios simultáneos (HU-18).
-- Crea 500 usuarios activos (documento CARGA-0001..CARGA-0500) y un recurso
-- disponible (Recurso de carga) para que la prueba de carga pueda reservar
-- sin depender de los datos de ejemplo.

INSERT INTO usuarios (nombre, documento, correo, estado)
SELECT 'Carga ' || n,
       'CARGA-' || lpad(to_char(n, 'FM9999'), 4, '0'),
       'carga-' || lpad(to_char(n, 'FM9999'), 4, '0') || '@example.com',
       'ACTIVO'
FROM generate_series(1, 500) AS n
ON CONFLICT (documento) DO NOTHING;

INSERT INTO recursos (nombre, descripcion, tipo, capacidad, estado)
VALUES ('Recurso de carga', 'Recurso compartido para el escenario de 500 usuarios', 'Sala', 500, 'DISPONIBLE')
ON CONFLICT DO NOTHING;
