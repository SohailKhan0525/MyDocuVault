"""Wake subscribed clients. Clients verify the release against GitHub before displaying it."""
import json
import os

from google.auth.transport.requests import AuthorizedSession
from google.oauth2.service_account import Credentials

config = json.loads(os.environ["FIREBASE_ANDROID_CONFIG"])
project = config["project_info"]["project_id"]
credentials = Credentials.from_service_account_info(
    json.loads(os.environ["FCM_CREDENTIALS"]),
    scopes=["https://www.googleapis.com/auth/firebase.messaging"],
)
response = AuthorizedSession(credentials).post(
    f"https://fcm.googleapis.com/v1/projects/{project}/messages:send",
    json={"message": {"topic": "stable-releases", "data": {"version": os.environ["TAG"]},
                      "android": {"priority": "NORMAL", "ttl": "86400s", "collapse_key": "vault-release"}}},
    timeout=30,
)
response.raise_for_status()
