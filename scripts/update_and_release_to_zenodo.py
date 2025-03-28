import os
import requests
import sys
from datetime import datetime

ZENODO_API_BASE = "https://zenodo.org/api/deposit/depositions"
ACCESS_TOKEN = os.getenv("ZENODO_ACCESS_TOKEN")
DEPOSITION_ID = os.getenv("ZENODO_DEPOSITION_ID")

HEADERS = {"Authorization": f"Bearer {ACCESS_TOKEN}"}

def create_new_version():
    """Creates a new version of the deposition and returns its ID."""
    today_date = datetime.today().strftime('%Y-%m-%d')

    # Create a new version with publication date
    data = {
        "metadata": {
            "publication_date": today_date
        }
    }
    
    response = requests.post(f"{ZENODO_API_BASE}/{DEPOSITION_ID}/actions/newversion", headers=HEADERS, json=data)

    if response.status_code != 201:
        print(f"Error creating new version: {response.text}")
        sys.exit(1)

    new_deposition = response.json()
    new_id = new_deposition["id"]
    print(f"New Zenodo deposition created: {new_id}")
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

def upload_file(deposition_id, file_path):
    """Uploads a file to the specified deposition."""
    files = {"file": open(file_path, "rb")}
    params = {"name": os.path.basename(file_path)}

    upload_url = f"{ZENODO_API_BASE}/{deposition_id}/files"
    response = requests.post(upload_url, headers=HEADERS, files=files, params=params)

    if response.status_code != 201:
        print(f"Error uploading {file_path}: {response.text}")
        sys.exit(1)
    print(f"Uploaded {file_path} successfully.")

def publish_deposition(deposition_id):
    """Publishes the deposition on Zenodo."""
    publish_url = f"{ZENODO_API_BASE}/{deposition_id}/actions/publish"
    response = requests.post(publish_url, headers=HEADERS)

    if response.status_code != 202:
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
