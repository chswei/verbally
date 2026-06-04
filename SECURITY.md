# Security Policy

## Supported Versions

Verbally has not shipped a stable public release yet. Security fixes are currently
handled on the `main` branch and in the next release tag.

## Reporting A Vulnerability

Do not open a public issue for vulnerabilities that expose secrets, private
transcripts, audio, signing material, or exploitable device behavior.

Until a dedicated private security contact is published, open a minimal GitHub issue
that says you need to report a security issue without including exploit details or
private data. A maintainer will provide a private channel.

## Sensitive Areas

- AccessibilityService API behavior.
- Text insertion and clipboard fallback.
- Provider API key storage and request construction.
- Temporary audio file lifecycle.
- Local history retention and deletion.
- Release signing and store metadata.
