-- Datos semilla del entorno local.
-- Solo se cargan desde el perfil `local`; producción no incluye `db/seed`.
-- Las inserciones son idempotentes para que la migración repeatable pueda
-- ejecutarse de nuevo cuando cambie su checksum.

INSERT INTO usuarios (nombre, documento, correo, estado)
VALUES
    ('Ana Torres', '100000001', 'ana.torres@example.com', 'ACTIVO'),
    ('Carlos Ruiz', '100000002', 'carlos.ruiz@example.com', 'ACTIVO'),
    ('Laura Mendez', '100000003', 'laura.mendez@example.com', 'INACTIVO')
ON CONFLICT (documento) DO NOTHING;

INSERT INTO recursos (nombre, descripcion, tipo, capacidad, estado)
SELECT 'Sala 101', 'Sala de reuniones con videoconferencia', 'SALA', 8, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM recursos WHERE nombre = 'Sala 101');

INSERT INTO recursos (nombre, descripcion, tipo, capacidad, estado)
SELECT 'Auditorio principal', 'Auditorio para eventos y capacitaciones', 'AUDITORIO', 50, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM recursos WHERE nombre = 'Auditorio principal');

INSERT INTO recursos (nombre, descripcion, tipo, capacidad, estado)
SELECT 'Equipo audiovisual', 'Proyector y sistema de sonido movil', 'EQUIPO', 1, 'MANTENIMIENTO'
WHERE NOT EXISTS (SELECT 1 FROM recursos WHERE nombre = 'Equipo audiovisual');
