import importlib.util
import pathlib
import unittest


SCRIPT_PATH = pathlib.Path(__file__).parents[1] / "scripts" / "verify-integrity.py"
SPEC = importlib.util.spec_from_file_location("verify_integrity", SCRIPT_PATH)
VERIFY_INTEGRITY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERIFY_INTEGRITY)


class CalculateSummaryTests(unittest.TestCase):
    def test_throughput_covers_first_start_through_last_completion(self) -> None:
        samples = [
            {"timeStamp": "1000", "elapsed": "1000", "responseCode": "201", "success": "true"},
            {"timeStamp": "1500", "elapsed": "10", "responseCode": "409", "success": "false"},
        ]

        summary = VERIFY_INTEGRITY.calculate_summary(samples)

        self.assertEqual(summary["durationMs"], 1000)
        self.assertEqual(summary["throughputPerSecond"], 2.0)

    def test_reservation_summary_excludes_csrf_get_samples(self) -> None:
        samples = [
            {"label": "GET /auth/csrf", "responseCode": "200"},
            {"label": "POST /reservas", "responseCode": "201"},
            {"label": "POST /reservas", "responseCode": "409"},
        ]

        selected = VERIFY_INTEGRITY.select_reservation_samples(samples)

        self.assertEqual(len(selected), 2)
        self.assertTrue(all(sample["label"] == "POST /reservas" for sample in selected))

    def test_calculates_throughput_percentiles_and_expected_conflicts(self) -> None:
        samples = [
            {"timeStamp": "2000", "elapsed": "1000", "responseCode": "201", "success": "true"},
            {"timeStamp": "2000", "elapsed": "1000", "responseCode": "409", "success": "false"},
            {"timeStamp": "4000", "elapsed": "2000", "responseCode": "409", "success": "false"},
            {"timeStamp": "5000", "elapsed": "1000", "responseCode": "500", "success": "false"},
        ]

        summary = VERIFY_INTEGRITY.calculate_summary(samples)

        self.assertEqual(summary["sampleCount"], 4)
        self.assertEqual(summary["statusCodes"], {"201": 1, "409": 2, "500": 1})
        self.assertEqual(summary["expectedConflictCount"], 2)
        self.assertEqual(summary["unexpectedErrorCount"], 1)
        self.assertEqual(summary["rawErrorPercent"], 75.0)
        self.assertEqual(summary["unexpectedErrorPercent"], 25.0)
        self.assertEqual(summary["averageMs"], 1250.0)
        self.assertEqual(summary["throughputPerSecond"], 1.0)
        self.assertEqual(summary["p50Ms"], 1000)
        self.assertEqual(summary["p90Ms"], 2000)
        self.assertEqual(summary["p95Ms"], 2000)
        self.assertEqual(summary["p99Ms"], 2000)

    def test_rejects_empty_samples(self) -> None:
        with self.assertRaises(ValueError):
            VERIFY_INTEGRITY.calculate_summary([])


if __name__ == "__main__":
    unittest.main()
