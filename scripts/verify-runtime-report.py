"""Validate the opt-in game probe's evidence without treating a stale location as a visit."""
import json
import sys
from pathlib import Path

report = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
assert report["finished"], "Probe has not finished"
assert len(report["results"]) == len(report["targets"]), "Missing visit result"
for target, result in zip(report["targets"], report["results"]):
    assert result.get("status") == "visited", result
    assert result["dimensionId"] == target, result
    assert result["attributes"]["dimensionId"] == str(target), "Stale world location"
    assert result["title"] and result["biomeTitle"], "Missing fallback title"
checks = report["resourceChecks"]
assert len(checks) == 10 and all(value is True for value in checks.values()), checks
print(f"PASS: {len(report['results'])} visits, {len(set(report['targets']))} dimensions, {len(checks)} resource checks")
