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
import math
import statistics
import sys
from typing import Mapping, Sequence
import urllib.request


def calculate_summary(samples: Sequence[Mapping[str, str]]) -> dict[str, object]:
    if not samples:
        raise ValueError("El JTL no contiene muestras")

    normalized: list[tuple[int, int, int, bool]] = []
    for sample in samples:
        try:
            code = int(sample.get("responseCode") or sample.get("sc") or 0)
            elapsed = int(sample.get("elapsed") or 0)
            timestamp = int(sample.get("timeStamp") or 0)
        except (TypeError, ValueError):
            continue
        if elapsed < 0:
            continue
        success = str(sample.get("success", "")).lower() == "true"
        normalized.append((code, elapsed, timestamp, success))

    if not normalized:
        raise ValueError("El JTL no contiene muestras válidas")

    elapsed_values = sorted(sample[1] for sample in normalized)
    status_counts: dict[str, int] = {}
    raw_errors = 0
    unexpected_errors = 0
    for code, _, _, success in normalized:
        status_counts[str(code)] = status_counts.get(str(code), 0) + 1
        if not success:
            raw_errors += 1
        if code not in (201, 409):
            unexpected_errors += 1

    # El runner fija sampleresult.timestamp.start=true en el JTL.
    start_ms = min(sample[2] for sample in normalized)
    end_ms = max(sample[2] + sample[1] for sample in normalized)
    duration_ms = max(1, end_ms - start_ms)

    def percentile(percent: float) -> int:
        rank = max(1, math.ceil(percent * len(elapsed_values)))
        return elapsed_values[rank - 1]

    sample_count = len(normalized)
    return {
        "sampleCount": sample_count,
        "statusCodes": dict(sorted(status_counts.items())),
        "expectedConflictCount": status_counts.get("409", 0),
        "unexpectedErrorCount": unexpected_errors,
        "rawErrorCount": raw_errors,
        "rawErrorPercent": round(raw_errors * 100 / sample_count, 2),
        "unexpectedErrorPercent": round(unexpected_errors * 100 / sample_count, 2),
        "durationMs": duration_ms,
        "throughputPerSecond": round(sample_count * 1000 / duration_ms, 2),
        "averageMs": round(statistics.mean(elapsed_values), 2),
        "p50Ms": percentile(0.50),
        "p90Ms": percentile(0.90),
        "p95Ms": percentile(0.95),
        "p99Ms": percentile(0.99),
    }


def select_reservation_samples(samples: Sequence[Mapping[str, str]]) -> list[Mapping[str, str]]:
    return [sample for sample in samples if sample.get("label") in (None, "", "POST /reservas")]


def main():
    if len(sys.argv) < 5:
        print("Uso: verify-integrity.py <jtl> <host> <port> <recursoId> [fechaInicio] [fechaFin] [summary.json]")
        sys.exit(2)
    jtl_path = sys.argv[1]
    host, port, recurso_id = sys.argv[2], sys.argv[3], sys.argv[4]
    fecha_inicio = sys.argv[5] if len(sys.argv) > 5 else None
    fecha_fin = sys.argv[6] if len(sys.argv) > 6 else None

    samples = []
    try:
        with open(jtl_path, newline="", encoding="utf-8") as fh:
            reader = csv.DictReader(fh)
            fieldnames = [f.strip() for f in (reader.fieldnames or [])]
            # normaliza encabezados con espacios
            reader.fieldnames = fieldnames
            for row in reader:
                samples.append(row)
    except FileNotFoundError:
        print(f"ERROR: no se encontró el JTL {jtl_path}")
        sys.exit(2)

    reservation_samples = select_reservation_samples(samples)
    csrf_samples = [sample for sample in samples if sample.get("label") == "GET /auth/csrf"]
    try:
        summary = calculate_summary(reservation_samples)
    except ValueError as error:
        print(f"ERROR: {error}")
        sys.exit(2)

    ok = True
    codes = [int(sample.get("responseCode") or sample.get("sc") or 0) for sample in reservation_samples]
    c201 = codes.count(201)
    c409 = codes.count(409)
    others = sorted({code for code in codes if code not in (201, 409)})
    print(f"Muestra: {summary['sampleCount']} respuestas")
    print(f"201: {c201}, 409: {c409}, otros: {others if others else 'ninguno'}")
    if c201 != 1 or c409 != 499 or others:
        print("FAIL: se esperaba exactamente 1x 201 y 499x 409 y ningún otro código")
        ok = False
    else:
        print("OK: 1x 201 y 499x 409")

    csrf_codes = [int(sample.get("responseCode") or sample.get("sc") or 0) for sample in csrf_samples]
    if len(csrf_samples) != 500 or any(code != 200 for code in csrf_codes):
        print(f"FAIL: se esperaban 500 tokens CSRF HTTP 200; muestras={len(csrf_samples)}, códigos={sorted(set(csrf_codes))}")
        ok = False
    else:
        print("OK: 500 tokens CSRF HTTP 200")

    if summary["sampleCount"]:
        latencies_sorted = sorted(int(sample.get("elapsed") or 0) for sample in reservation_samples)
        p95 = summary["p95Ms"]
        print(f"Latencia: min={min(latencies_sorted)}ms p50={summary['p50Ms']}ms p90={summary['p90Ms']}ms p95={p95}ms p99={summary['p99Ms']}ms max={max(latencies_sorted)}ms mean={summary['averageMs']:.1f}ms")
        print(f"Throughput: {summary['throughputPerSecond']} muestras/s; error HTTP bruto: {summary['rawErrorPercent']}%; error inesperado: {summary['unexpectedErrorPercent']}%")
        if p95 >= 800:
            print(f"FAIL: p95 {p95}ms >= 800ms")
            ok = False
        else:
            print(f"OK: p95 {p95}ms < 800ms")
    else:
        print("FAIL: sin latencias en el JTL")
        ok = False

    if len(sys.argv) > 7:
        with open(sys.argv[7], "w", encoding="utf-8") as summary_file:
            json.dump(summary, summary_file, indent=2, ensure_ascii=False)
            summary_file.write("\n")

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
