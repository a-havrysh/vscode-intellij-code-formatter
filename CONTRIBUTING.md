# Contributing

## Setup

```
git clone https://github.com/kern0x1b/vscode-intellij-code-formatter.git
cd vscode-intellij-code-formatter
./gradlew build
```

The first build downloads the IntelliJ IDEA JARs the formatter runs against — this
takes a while and needs a working internet connection.

## Making changes

- Standalone/CLI formatter (Java, Gradle): source under `src/`.
- VS Code extension (TypeScript): source under `vscode-extension/`.

Run `./gradlew test` before opening a pull request. For the VS Code extension,
`cd vscode-extension && npm install && npm run compile`.

## Pull requests

Keep changes focused — one fix or feature per PR. Describe what changed and why
in the PR description; update `CHANGELOG.md` (and `vscode-extension/CHANGELOG.md`
if the extension is affected) for user-facing changes.

## Reporting bugs

Open a [GitHub issue](https://github.com/kern0x1b/vscode-intellij-code-formatter/issues)
with the input file (or a minimal reproduction), the command/settings used, and
the actual vs. expected output.
