#!/usr/bin/env python3
"""
Script to check and update phenopacket-store version.
Fetches the latest release from phenopacket-store repository and updates
the repository variable if needed.
"""

import os
import sys
import requests


def main():
    """Main function to check and update phenopacket-store version."""
    github_token = os.environ.get("GITHUB_TOKEN")
    if not github_token:
        print("Error: GITHUB_TOKEN environment variable not set")
        sys.exit(1)

    headers = {
        "Authorization": f"token {github_token}",
        "Accept": "application/vnd.github.v3+json"
    }

    # Fetch latest release from phenopacket-store
    print("Fetching latest release from phenopacket-store...")
    release_url = "https://api.github.com/repos/phenopackets/phenopacket-store/releases/latest"
    release_response = requests.get(release_url, headers=headers)
    release_response.raise_for_status()
    
    latest_version = release_response.json()["tag_name"]
    print(f"Latest phenopacket-store version: {latest_version}")

    # Fetch current repository variable
    print("Fetching current repository variable...")
    repo_owner = os.environ.get("GITHUB_REPOSITORY_OWNER", "P2GX")
    repo_name = os.environ.get("GITHUB_REPOSITORY", "P2GX/phenopacket2prompt").split("/")[-1]
    
    var_url = f"https://api.github.com/repos/{repo_owner}/{repo_name}/actions/variables/PPKTSTORE_VERSION"
    var_response = requests.get(var_url, headers=headers)
    
    if var_response.status_code == 404:
        # Variable doesn't exist, create it
        print("Repository variable not found, creating...")
        create_url = f"https://api.github.com/repos/{repo_owner}/{repo_name}/actions/variables"
        create_data = {
            "name": "PPKTSTORE_VERSION",
            "value": latest_version
        }
        create_response = requests.post(create_url, headers=headers, json=create_data)
        create_response.raise_for_status()
        print(f"Created PPKTSTORE_VERSION variable with value: {latest_version}")
    else:
        var_response.raise_for_status()
        current_version = var_response.json()["value"]
        print(f"Current PPKTSTORE_VERSION: {current_version}")
        
        if current_version != latest_version:
            # Update the variable
            print(f"Updating version from {current_version} to {latest_version}...")
            update_url = f"https://api.github.com/repos/{repo_owner}/{repo_name}/actions/variables/PPKTSTORE_VERSION"
            update_data = {"value": latest_version}
            update_response = requests.patch(update_url, headers=headers, json=update_data)
            update_response.raise_for_status()
            print(f"Successfully updated PPKTSTORE_VERSION to {latest_version}")
        else:
            print("Version is already up to date")


if __name__ == "__main__":
    main()
