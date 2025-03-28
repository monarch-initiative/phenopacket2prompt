import os
import requests
import sys

ZENODO_API_BASE = "https://zenodo.org/api/deposit/depositions"
ACCESS_TOKEN = os.getenv("ZENODO_ACCESS_TOKEN")
DEPOSITION_ID = os.getenv("ZENODO_DEPOSITION_ID")

HEADERS = {"Authorization": f"Bearer {ACCESS_TOKEN}"}

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
    """Uploads a file to the existing deposition."""
    files = {"file": open(file_path, "rb")}
    params = {"name": os.path.basename(file_path)}

    upload_url = f"{ZENODO_API_BASE}/{deposition_id}/files"
    response = requests.post(upload_url, headers=HEADERS, files=files, params=params)

    if response.status_code != 201:
        print(f"Error uploading {file_path}: {response.text}")
        sys.exit(1)
    print(f"Uploaded {file_path} successfully.")

def publish_deposition(deposition_id):
    """Publishes the updated deposition."""
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

    delete_existing_files(DEPOSITION_ID)  # Delete old files

    for filename in os.listdir(directory):
        file_path = os.path.join(directory, filename)
        if os.path.isfile(file_path):
            upload_file(DEPOSITION_ID, file_path)

    #publish_deposition(DEPOSITION_ID)  # Re-publish

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: upload_to_zenodo.py <directory>")
        sys.exit(1)

    main(sys.argv[1])
