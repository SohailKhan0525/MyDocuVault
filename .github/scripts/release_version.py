"""Validate version metadata before publishing an immutable Android release."""
import os
import re
import subprocess
from pathlib import Path


def git(*args):
    return subprocess.check_output(["git", *args], text=True).strip()


def versions(text):
    name = re.search(r'^\s*versionName\s*=\s*"([^"]+)"\s*$', text, re.M)[1]
    code = int(re.search(r'^\s*versionCode\s*=\s*(\d+)\s*$', text, re.M)[1])
    return name, code


def precedence(tag):
    core, _, suffix = tag.removeprefix("v").partition("-")
    # Numeric prerelease identifiers sort before nonnumeric ones.
    pre = tuple((0, int(i)) if i.isdecimal() else (1, i) for i in suffix.split("."))
    return tuple(map(int, core.split("."))), not bool(suffix), pre


gradle = "app/build.gradle.kts"
name, code = versions(Path(gradle).read_text())
component = r"(?:0|[1-9][0-9]*)"
identifier = rf"(?:{component}|[0-9A-Za-z-]*[A-Za-z-][0-9A-Za-z-]*)"
if not re.fullmatch(rf"{component}\.{component}\.{component}(?:-{identifier}(?:\.{identifier})*)?", name):
    raise SystemExit(f"Invalid release version: {name}")
tag = f"v{name}"
ref = os.environ["GITHUB_REF"]
if ref != "refs/heads/main" and ref != f"refs/tags/{tag}":
    raise SystemExit(f"Release ref {ref} does not match {tag} or main")

tags = git("tag", "--list", "v*").splitlines()
exists = tag in tags
if ref.startswith("refs/tags/") and git("rev-list", "-1", tag) != git("rev-parse", "HEAD"):
    raise SystemExit("Tag points to a different commit")

# An existing release is immutable. On later main pushes with unchanged
# metadata, skip. A tag created before this job may still need its release.
result = subprocess.run(["gh", "api", f"repos/{os.environ['GITHUB_REPOSITORY']}/releases/tags/{tag}"],
                        capture_output=True, text=True)
if result.returncode and "HTTP 404" not in result.stderr:
    raise SystemExit(result.stderr)
publish = result.returncode != 0
if publish:
    if exists and git("rev-list", "-1", tag) != git("rev-parse", "HEAD"):
        raise SystemExit("Refusing to publish an existing tag from another commit")
    if not Path(f"releases/{tag}.md").is_file():
        raise SystemExit("Missing release notes")
    prior = [old for old in tags if old != tag and re.fullmatch(r"v\d+\.\d+\.\d+(?:-[\w.-]+)?", old)]
    if prior and precedence(tag) <= max(map(precedence, prior)):
        raise SystemExit("Release version must exceed all prior release tags")
    if prior and code <= max(versions(git("show", f"{old}:{gradle}"))[1] for old in prior):
        raise SystemExit("versionCode must exceed all earlier tagged builds")
with open(os.environ["GITHUB_OUTPUT"], "a") as out:
    print(f"tag={tag}\npublish={str(publish).lower()}\nprerelease={str('-' in name).lower()}", file=out)
