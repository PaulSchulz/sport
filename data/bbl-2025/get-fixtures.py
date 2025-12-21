#!/usr/bin/env python3

# python -m venv venv

# pip install requests


FIXTURES      = "https://fixturedownload.com/feed/json/bbl-2025"
FIXTURES_FILE = "fixtures.json"

import json
from urllib.request import urlopen

with urlopen(FIXTURES) as response:
    data = json.load(response)

with open(FIXTURES_FILE, "w", encoding="utf-8") as f:
    json.dump(data, f, indent=2)

print("JSON file downloaded successfully")
