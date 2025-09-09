import os
import requests
import sys
import json
from datetime import datetime

ZENODO_API_BASE = "https://zenodo.org/api/deposit/depositions"
ACCESS_TOKEN = os.getenv("ZENODO_ACCESS_TOKEN")
DEPOSITION_ID = os.getenv("ZENODO_DEPOSITION_ID")

HEADERS = {"Authorization": f"Bearer {ACCESS_TOKEN}"}
def create_new_version():
    """Creates a new version of the deposition and returns its ID."""
    response = requests.post(
        f"{ZENODO_API_BASE}/{DEPOSITION_ID}/actions/newversion",
        headers=HEADERS
    )

    if not (200 <= response.status_code < 300):
        print(f"Error creating new version: {response.text}")
        sys.exit(1)

    parent_data = response.json()

    # Follow the latest_draft link to get the actual new deposition
    draft_url = parent_data["links"]["latest_draft"]
    draft_response = requests.get(draft_url, headers=HEADERS)

    if not (200 <= draft_response.status_code < 300):
        print(f"Error retrieving draft deposition: {draft_response.text}")
        sys.exit(1)

    draft_data = draft_response.json()
    new_id = draft_data["id"]
    print(f"New Zenodo deposition draft created: {new_id}")

    # Update just the publication_date
    today_date = datetime.today().strftime('%Y-%m-%d')
    metadata_update = {
        "metadata": {
            "publication_date": today_date
        }
    }

    response = requests.put(
        f"{ZENODO_API_BASE}/{new_id}",
        headers={**HEADERS, "Content-Type": "application/json"},
        data=json.dumps(metadata_update)
    )

    if not (200 <= response.status_code < 300):
        print(f"Error updating publication_date: {response.text}")
        sys.exit(1)

    print(f"Updated publication_date for deposition {new_id} to {today_date}.")

    return new_id

       

def delete_existing_files(deposition_id):
    """Deletes all existing files in a draft deposition."""
    response = requests.get(f"{ZENODO_API_BASE}/{deposition_id}", headers=HEADERS)
    files = response.json().get("files", [])

    for file in files:
        file_id = file["id"]
        delete_url = f"{ZENODO_API_BASE}/{deposition_id}/files/{file_id}"
        requests.delete(delete_url, headers=HEADERS)
        print(f"Deleted {file['filename']}")

def upload_file(deposition_id, file_path, max_retries=5):
    """Uploads a file to the specified deposition with retries."""
    upload_url = f"{ZENODO_API_BASE}/{deposition_id}/files"
    params = {"name": os.path.basename(file_path)}

    for attempt in range(max_retries):
        with open(file_path, "rb") as fp:
            files = {"file": fp}
            response = requests.post(upload_url, headers=HEADERS, files=files, params=params)

        if 200 <= response.status_code < 300:
            print(f"Uploaded {file_path} successfully.")
            return

        if response.status_code == 403 and attempt < max_retries - 1:
            wait = 2 ** attempt
            print(f"Deposition locked, retrying in {wait}s...")
            time.sleep(wait)
            continue

        print(f"Error uploading {file_path}: {response.text}")
        sys.exit(1)

def publish_deposition(deposition_id):
    """Publishes the deposition on Zenodo."""
    publish_url = f"{ZENODO_API_BASE}/{deposition_id}/actions/publish"
    response = requests.post(publish_url, headers=HEADERS)

    if not (200 <= response.status_code < 300):
        print(f"Error publishing deposition: {response.text}")
        sys.exit(1)
    print(f"Deposition {deposition_id} published successfully.")

def main(directory):
    if not os.path.isdir(directory):
        print(f"Error: {directory} is not a valid directory.")
        sys.exit(1)

    new_dep_id = create_new_version()  # Create new version before uploading

    delete_existing_files(new_dep_id)  # Delete old files

    for filename in os.listdir(directory):
        file_path = os.path.join(directory, filename)
        if os.path.isfile(file_path):
            upload_file(new_dep_id, file_path)

    publish_deposition(new_dep_id)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: upload_to_zenodo.py <directory>")
        sys.exit(1)

    main(sys.argv[1])
