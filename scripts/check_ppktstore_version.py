import os, requests, sys, time


def fetch_with_retry(url, headers, max_retries=3, timeout=10):
    """
    Fetch data from URL with retry logic and exponential backoff.
    
    Args:
        url: The URL to fetch
        headers: HTTP headers to include in the request
        max_retries: Maximum number of retry attempts (default: 3)
        timeout: Request timeout in seconds (default: 10)
    
    Returns:
        Response object if successful
    
    Raises:
        Exception on final failed attempt
    """
    for attempt in range(max_retries):
        try:
            response = requests.get(url, headers=headers, timeout=timeout)
            response.raise_for_status()
            return response
        except Exception as e:
            if attempt < max_retries - 1:
                wait_time = 2 ** attempt  # Exponential backoff: 1s, 2s, 4s
                print(f"Request failed (attempt {attempt + 1}/{max_retries}): {e}")
                print(f"Retrying in {wait_time} seconds...")
                time.sleep(wait_time)
            else:
                # Final attempt failed, raise the exception
                raise


ppktstore_repo = "monarch-initiative/phenopacket-store"
hpo_repo = "obophenotype/human-phenotype-ontology"
this_repo = os.environ["GITHUB_REPOSITORY"]
token  = os.environ["GH_TOKEN"]
ppkt_release_name = "LAST_RUN_RELEASE"

# Get phenopacket-store latest version
latest = fetch_with_retry(
    f"https://api.github.com/repos/{ppktstore_repo}/releases/latest",
    headers={"Accept": "application/vnd.github+json"}
).json().get("tag_name")

if not latest:
    print("Error: Could not fetch latest phenopacket-store release tag!")
    sys.exit(1)

# Get HPO latest version
latest_hpo = fetch_with_retry(
    f"https://api.github.com/repos/{hpo_repo}/releases/latest",
    headers={"Accept": "application/vnd.github+json"}
).json().get("tag_name")

if not latest_hpo:
    print("Error: Could not fetch latest HPO release tag!")
    sys.exit(1)

# Get last version of phenopacket-store that ppkt2prompt ran
r = fetch_with_retry(
    f"https://api.github.com/repos/{this_repo}/actions/variables/{ppkt_release_name}",
    headers={
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github+json"
        }
)

if r.status_code != 200:
    print(f"Error: Could not fetch repository variable '{ppkt_release_name}'! Status code: {r.status_code}")
    sys.exit(1)

stored_ppkt_value = r.json().get("value")
if stored_ppkt_value is None:
    print(f"Error: Repository variable '{ppkt_release_name}' returned no value.")
    sys.exit(1)

latest = latest.strip()
stored_ppkt_value = stored_ppkt_value.strip()
latest_hpo = latest_hpo.strip()

new_release = (latest != stored_ppkt_value)

with open(os.environ["GITHUB_OUTPUT"], "a") as gh_out:
    gh_out.write(f"new_release={str(new_release).lower()}\n")
    gh_out.write(f"latest_store_tag={latest}\n")
    gh_out.write(f"latest_hpo_tag={latest_hpo}\n")


# Update variable if needed
if new_release:
    payload = {"name": ppkt_release_name, "value": latest}
    res = requests.patch(
        f"https://api.github.com/repos/{this_repo}/actions/variables/{ppkt_release_name}",
        headers={"Authorization": f"token {token}",
                 "Accept": "application/vnd.github+json"},
        json=payload,
        timeout=10
    )
    if res.status_code == 404:
        requests.post(
            f"https://api.github.com/repos/{this_repo}/actions/variables",
            headers={"Authorization": f"token {token}",
                     "Accept": "application/vnd.github+json"},
            json=payload,
            timeout=10
        )

    # You can also send mail here via SMTP if you prefer Python's smtplib
    print(f"Detected new release {latest} from {ppktstore_repo}")
else:
    print(f"The latest phenopacket-store release {stored_ppkt_value} was already run.")
