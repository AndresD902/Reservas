#!/usr/bin/env python3
"""Valida los reportes de OWASP ZAP y aplica el umbral de fallo configurado."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

RISK_BY_CODE = {
    "0": "Informational",
    "1": "Low",
    "2": "Medium",
    "3": "High",
}
RISK_ORDER = {
    "informational": 0,
    "low": 1,
    "medium": 2,
    "high": 3,
}
THRESHOLDS = {
    "info": 0,
    "low": 1,
    "medium": 2,
    "high": 3,
    "none": 99,
}
REQUIRED_REPORTS = ("api-zap.json", "ui-zap.json")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Valida los reportes JSON de OWASP ZAP contra un umbral de riesgo."
    )
    parser.add_argument(
        "--reports",
        type=Path,
        default=Path(__file__).resolve().parent / "reports",
        help="Directorio que contiene los reportes JSON de ZAP.",
    )
    parser.add_argument(
        "--fail-on",
        choices=sorted(THRESHOLDS),
        default="medium",
        help="Riesgo mínimo que hace fallar la verificación.",
    )
    parser.add_argument(
        "--summary",
        type=Path,
        help="Archivo Markdown opcional donde se escribe el resumen.",
    )
    return parser.parse_args()


def risk_name(alert: dict) -> str:
    return RISK_BY_CODE.get(str(alert.get("riskcode", "0")), "Informational")


def alert_urls(alert: dict, limit: int = 3) -> list[str]:
    urls = [instance.get("uri", "") for instance in alert.get("instances", [])]
    return [url for url in urls if url][:limit]


def load_alerts(report_path: Path):
    data = json.loads(report_path.read_text(encoding="utf-8"))
    for site in data.get("site", []):
        site_name = site.get("@name", report_path.stem)
        for alert in site.get("alerts", []):
            yield site_name, alert


def build_rows(report_paths: list[Path]):
    rows = []
    for report_path in report_paths:
        for site_name, alert in load_alerts(report_path):
            rows.append(
                {
                    "report": report_path.name,
                    "site": site_name,
                    "plugin": str(alert.get("pluginid", "?")),
                    "name": alert.get("alert", "Sin nombre"),
                    "risk": risk_name(alert),
                    "confidence": str(alert.get("confidence", "?")),
                    "instances": len(alert.get("instances", [])),
                    "urls": alert_urls(alert),
                }
            )
    return rows


def render_summary(rows: list[dict], fail_on: str, violations: list[dict]) -> str:
    lines = [
        "# Resumen del escaneo OWASP ZAP",
        "",
        f"Umbral de fallo: **{fail_on}**.",
        "",
        "| Reporte | Sitio | ID | Hallazgo | Riesgo | Confianza | Instancias |",
        "|---|---|---|---|---|---|---|",
    ]
    for row in rows:
        lines.append(
            f"| {row['report']} | {row['site']} | {row['plugin']} | {row['name']} | "
            f"{row['risk']} | {row['confidence']} | {row['instances']} |"
        )
    if not rows:
        lines.append("| — | — | — | Sin alertas | — | — | 0 |")
    lines.append("")
    if violations:
        lines.append("## Alertas que superan el umbral")
        lines.append("")
        for violation in violations:
            lines.append(
                f"- `{violation['report']}` · [{violation['plugin']}] "
                f"**{violation['name']}** ({violation['risk']})"
            )
    else:
        lines.append("No hay alertas que superen el umbral configurado.")
    lines.append("")
    return "\n".join(lines)


def main() -> int:
    args = parse_args()
    reports_dir = args.reports.resolve()
    report_paths = [reports_dir / name for name in REQUIRED_REPORTS]
    missing = [path for path in report_paths if not path.exists()]
    if missing:
        for path in missing:
            print(f"Falta el reporte: {path}", file=sys.stderr)
        return 2

    rows = build_rows(report_paths)
    threshold = THRESHOLDS[args.fail_on]
    violations = [
        row for row in rows if RISK_ORDER[row["risk"].lower()] >= threshold
    ]
    summary = render_summary(rows, args.fail_on, violations)
    print(summary)
    if args.summary:
        args.summary.write_text(summary, encoding="utf-8")

    if violations:
        print(
            f"El escaneo supera el umbral '{args.fail_on}': "
            f"{len(violations)} alerta(s).",
            file=sys.stderr,
        )
        return 1

    print(f"El escaneo no supera el umbral '{args.fail_on}'.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
