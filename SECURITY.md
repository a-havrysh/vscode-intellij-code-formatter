# Security Policy

## Supported Versions

Only the latest released version (standalone JAR and VS Code extension) receives
security fixes.

## Reporting a Vulnerability

Please do not open a public issue for security vulnerabilities. Instead, use
[GitHub Security Advisories](https://github.com/kern0x1b/vscode-intellij-code-formatter/security/advisories/new)
to report privately.

Include: affected version, a description of the issue, and steps to reproduce
if possible. Expect an initial response within a few days.

## Scope notes

This project downloads and bundles IntelliJ IDEA Community Edition JARs to run
its formatting engine headlessly. It does not send formatted source code
anywhere over the network — formatting runs entirely locally via a subprocess.
