import os, requests, sys

ppktstore_repo = "monarch-initiative/phenopacket-store"
this_repo = os.environ["GITHUB_REPOSITORY"]
token  = os.environ["GITHUB_TOKEN"]
var_name = "LAST_RUN_RELEASE"

# Get phenopacket-store latest version
latest = requests.get(
    f"https://api.github.com/repos/{ppktstore_repo}/releases/latest",
    headers={"Accept": "application/vnd.github+json"}
).json().get("tag_name")

if not latest:
    print("Error: Could not fetch latest release tag!")
    sys.exit(1)
    
# Get last version of phenopacket-store that ppkt2prompt ran
r = requests.get(
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
        json=payload
    )
    if res.status_code == 404:
        requests.post(
            f"https://api.github.com/repos/{this_repo}/actions/variables",
            headers={"Authorization": f"token {token}",
                     "Accept": "application/vnd.github+json"},
            json=payload
        )

    # You can also send mail here via SMTP if you prefer Python's smtplib
    print(f"Detected new release {latest} from {ppktstore_repo}")
else:
    print("No new release found.")
