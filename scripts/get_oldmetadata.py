import requests

DEPOSITION_ID = "17088503"

import requests
import json
import os

ACCESS_TOKEN = os.getenv("ZENODO_ACCESS_TOKEN")

with open("zenodo_metadata.json", "r") as f:
    metadata = json.load(f)

response = requests.put(
    f"https://zenodo.org/api/deposit/depositions/{DEPOSITION_ID}",
    headers={
        "Authorization": f"Bearer {ACCESS_TOKEN}",
        "Content-Type": "application/json",
    },
    data=json.dumps({"metadata": metadata}),
)

print(response.status_code, response.text)
