#!/usr/bin/env python3
"""Verifica la integridad tras el escenario JMeter de HU-18.

Lee el JTL de JMeter y comprueba:
- exactamente 1x 201 y 499x 409 (cero otros códigos)
- p95 de latencia < 800 ms
- la API responde GET /reservas/recurso/{recursoId} con 1 sola reserva ACTIVA solapada en el rango

Uso:
    python3 verify-integrity.py perf/results.jtl localhost 8080 1 2030-01-01T10:00:00 2030-01-01T12:00:00
Sale con código 0 si todo pasa, 1 si algo falla.
"""
import csv
import json
import statistics
import sys
import urllib.request

def main():
    if len(sys.argv) < 5:
        print("Uso: verify-integrity.py <jtl> <host> <port> <recursoId> [fechaInicio] [fechaFin]")
        sys.exit(2)
    jtl_path = sys.argv[1]
    host, port, recurso_id = sys.argv[2], sys.argv[3], sys.argv[4]
    fecha_inicio = sys.argv[5] if len(sys.argv) > 5 else None
    fecha_fin = sys.argv[6] if len(sys.argv) > 6 else None

    codes = []
    latencies = []
    try:
        with open(jtl_path, newline="", encoding="utf-8") as fh:
            reader = csv.DictReader(fh)
            fieldnames = [f.strip() for f in (reader.fieldnames or [])]
            # normaliza encabezados con espacios
            reader.fieldnames = fieldnames
            for row in reader:
                try:
                    code = int(row.get("responseCode") or row.get("sc") or 0)
                except ValueError:
                    continue
                codes.append(code)
                try:
                    latencies.append(int(row.get("elapsed") or 0))
                except ValueError:
                    pass
    except FileNotFoundError:
        print(f"ERROR: no se encontró el JTL {jtl_path}")
        sys.exit(2)

    ok = True
    c201 = codes.count(201)
    c409 = codes.count(409)
    others = sorted({c for c in codes if c not in (201, 409)})
    print(f"Muestra: {len(codes)} respuestas")
    print(f"201: {c201}, 409: {c409}, otros: {others if others else 'ninguno'}")
    if c201 != 1 or c409 != 499 or others:
        print("FAIL: se esperaba exactamente 1x 201 y 499x 409 y ningún otro código")
        ok = False
    else:
        print("OK: 1x 201 y 499x 409")

    if latencies:
        latencies_sorted = sorted(latencies)
        p95 = latencies_sorted[int(len(latencies_sorted) * 0.95) - 1] if len(latencies_sorted) >= 1 else 0
        # percentil 95 con índice BIASado
        idx = max(0, min(len(latencies_sorted) - 1, int(0.95 * len(latencies_sorted))))
        p95 = latencies_sorted[idx]
        mean = statistics.mean(latencies_sorted)
        print(f"Latencia: min={min(latencies_sorted)}ms p50={statistics.median(latencies_sorted)}ms p95={p95}ms max={max(latencies_sorted)}ms mean={mean:.1f}ms")
        if p95 >= 800:
            print(f"FAIL: p95 {p95}ms >= 800ms")
            ok = False
        else:
            print(f"OK: p95 {p95}ms < 800ms")
    else:
        print("FAIL: sin latencias en el JTL")
        ok = False

    # Verificación en la API: una sola reserva solapada para el recurso
    try:
        url = f"http://{host}:{port}/reservas/recurso/{recurso_id}"
        if fecha_inicio and fecha_fin:
            # usa disponibilidad como segunda fuente
            pass
        with urllib.request.urlopen(url, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        activas = [r for r in data if r.get("estado") == "ACTIVA"]
        # filtra por solapamiento si se dio rango
        if fecha_inicio and fecha_fin:
            solapadas = [r for r in activas
                         if r.get("fechaInicio", "") < fecha_fin and r.get("fechaFin", "") > fecha_inicio]
        else:
            solapadas = activas
        print(f"Reservas ACTIVAS para recurso {recurso_id} según API: {len(solapadas)}")
        if len(solapadas) == 1:
            print("OK: exactamente 1 reserva activa solapada")
        else:
            print(f"FAIL: se esperaba 1 reserva activa solapada, hay {len(solapadas)}")
            ok = False
    except Exception as e:
        print(f"FAIL: no se pudo verificar la API: {e}")
        ok = False

    sys.exit(0 if ok else 1)

if __name__ == "__main__":
    main()
