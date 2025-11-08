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
this_repo = os.environ["GITHUB_REPOSITORY"]
token  = os.environ["GITHUB_TOKEN"]
var_name = "LAST_RUN_RELEASE"

# Get phenopacket-store latest version
latest = fetch_with_retry(
    f"https://api.github.com/repos/{ppktstore_repo}/releases/latest",
    headers={"Accept": "application/vnd.github+json"}
).json().get("tag_name")

if not latest:
    print("Error: Could not fetch latest release tag!")
    sys.exit(1)
    
# Get last version of phenopacket-store that ppkt2prompt ran
r = fetch_with_retry(
    f"https://api.github.com/repos/{this_repo}/actions/variables/{var_name}",
    headers={
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github+json"
        }
)

if r.status_code != 200:
    print(f"Error: Could not fetch repository variable '{var_name}'! Status code: {r.status_code}")
    sys.exit(1)

stored = r.json().get("value")
if stored is None:
    print(f"Error: Repository variable '{var_name}' returned no value.")
    sys.exit(1)

latest = latest.strip()
stored = stored.strip()
new_release = (latest != stored)

#---------DEBUG---------
print(f"new_release is {new_release}")
print(f"latest is {latest}")
print(f"stored is {stored}")
sys.exit(1)
#---------DEBUG---------

with open(os.environ["GITHUB_OUTPUT"], "a") as gh_out:
    gh_out.write(f"latest_tag={latest}\n")
    gh_out.write(f"new_release={str(new_release).lower()}\n")


# Update variable if needed
if new_release:
    payload = {"name": var_name, "value": latest}
    res = requests.patch(
        f"https://api.github.com/repos/{this_repo}/actions/variables/{var_name}",
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
    print("No new release found.")
