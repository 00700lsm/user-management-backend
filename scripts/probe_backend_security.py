#!/usr/bin/env python3
"""Loopback-only, read-only health and anonymous security smoke checks."""
from __future__ import annotations
import argparse
import json
import sys
from urllib.error import HTTPError, URLError
from urllib.parse import urlparse
from urllib.request import Request, urlopen

def get(url: str) -> tuple[int, dict | None]:
    req = Request(url, headers={"Accept": "application/json"}, method="GET")
    try:
        with urlopen(req, timeout=5) as resp:
            status, raw = resp.status, resp.read(65536)
    except HTTPError as err:
        status, raw = err.code, err.read(65536)
    try:
        data = json.loads(raw.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError):
        data = None
    return status, data if isinstance(data, dict) else None

def main() -> int:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--base-url", default="http://127.0.0.1:8080")
    args = p.parse_args()
    u = urlparse(args.base_url)
    if (u.scheme != "http" or u.hostname not in ("localhost", "127.0.0.1", "::1")
            or u.path not in ("", "/") or u.username or u.password or u.query or u.fragment):
        print("[FAIL] Only a loopback HTTP origin may be probed")
        return 2
    base = args.base_url.rstrip("/")
    failures = 0
    for path, expected in [
        ("/actuator/health", 200), ("/api/users/me", 401),
        ("/api/admin/users", 401), ("/api/admin/roles", 401),
    ]:
        try:
            status, payload = get(base + path)
        except (URLError, TimeoutError, OSError) as exc:
            print(f"[NOT_RUN] GET {path}: server unavailable: {exc}")
            return 2
        if status != expected:
            print(f"[FAIL] GET {path}: status {status} != {expected}")
            failures += 1
        elif expected == 401 and not (
                payload and isinstance(payload.get("code"), str)
                and isinstance(payload.get("message"), str)):
            print(f"[FAIL] GET {path}: 401 but missing ApiError code/message")
            failures += 1
        else:
            print(f"[PASS] GET {path}: HTTP {status}")
    print("[WARN] Security gateway only; no proof Controller/JWT/runtime OpenAPI/G3/G4 works")
    print(f"[{'FAIL' if failures else 'PASS'}] {failures} anonymous smoke failures")
    return 1 if failures else 0

if __name__ == "__main__":
    sys.exit(main())
