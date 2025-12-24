# IntelliJ Code Formatter for VSCode

Format your code using IntelliJ IDEA's formatting engine directly in VSCode. Get the same high-quality formatting as IntelliJ IDEA without leaving your favorite editor.

## Features

- **Multiple Languages**: Format Java, Kotlin, Groovy, XML, HTML, JSON, YAML, and Properties files
- **Identical Results**: Uses IntelliJ IDEA's native formatting engine
- **Custom Code Styles**: Import your IntelliJ code style settings (XML export)
- **Flexible Formatting**: Format entire documents or just selections
- **Per-Language Control**: Enable/disable formatting for specific languages
- **Format on Save**: Optional automatic formatting when saving files

## Supported Languages

| Language   | File Extensions                              | Default |
|------------|----------------------------------------------|---------|
| Java       | `.java`                                      | Enabled |
| Kotlin     | `.kt`, `.kts`, `.gradle.kts`                 | Enabled |
| Groovy     | `.groovy`, `.gradle`                         | Enabled |
| XML        | `.xml`, `.xsd`, `.xsl`, `.wsdl`, `.fxml`     | Enabled |
| HTML       | `.html`, `.htm`, `.xhtml`                    | Enabled |
| JSON       | `.json`                                      | Enabled |
| YAML       | `.yaml`, `.yml`                              | Enabled |
| Properties | `.properties`                                | Enabled |

## Requirements

- **Java 21** or higher installed and available in PATH
- The formatter JAR is bundled with this extension

## Installation

### From VSIX File

```bash
code --install-extension intellij-formatter-0.1.0.vsix
```

### From Source

```bash
# Clone the repository
git clone <repository-url>
cd vscode-idea-code-formatter

# Build the extension
./gradlew build

# Install
code --install-extension vscode-extension/intellij-formatter-0.1.0.vsix
```

## Usage

### Format Document

1. Open a supported file (Java, Groovy, XML, JSON, YAML, etc.)
2. Use one of the following methods:
   - Press `Shift+Alt+F` (Windows/Linux) or `Shift+Option+F` (Mac)
   - Right-click and select "Format Document"
   - Open Command Palette (`Ctrl+Shift+P`) and run "Format Document"

### Format Selection

1. Select the code you want to format
2. Use one of the following methods:
   - Press `Ctrl+K Ctrl+F` (Windows/Linux) or `Cmd+K Cmd+F` (Mac)
   - Right-click and select "Format Selection"
   - Open Command Palette and run "Format Selection"

### Set as Default Formatter

Add to your `settings.json` to make IntelliJ Formatter the default for specific languages:

```json
{
  "[java]": {
    "editor.defaultFormatter": "idea-formatter.intellij-formatter"
  },
  "[kotlin]": {
    "editor.defaultFormatter": "idea-formatter.intellij-formatter"
  },
  "[groovy]": {
    "editor.defaultFormatter": "idea-formatter.intellij-formatter"
  },
  "[xml]": {
    "editor.defaultFormatter": "idea-formatter.intellij-formatter"
  }
}
```

## Configuration

### Global Settings

| Setting                           | Description                         | Default     |
|-----------------------------------|-------------------------------------|-------------|
| `intellijFormatter.enabled`       | Enable the formatter globally       | `true`      |
| `intellijFormatter.formatOnSave`  | Format files automatically on save  | `false`     |
| `intellijFormatter.javaPath`      | Path to Java executable             | `java`      |
| `intellijFormatter.codeStylePath` | Path to IntelliJ code style XML     | (none)      |

### Per-Language Settings

| Setting                               | Description          | Default |
|---------------------------------------|----------------------|---------|
| `intellijFormatter.languages.java`    | Enable for Java      | `true`  |
| `intellijFormatter.languages.kotlin`  | Enable for Kotlin    | `true`  |
| `intellijFormatter.languages.groovy`  | Enable for Groovy    | `true`  |
| `intellijFormatter.languages.gradle`  | Enable for Gradle    | `true`  |
| `intellijFormatter.languages.xml`     | Enable for XML       | `true`  |
| `intellijFormatter.languages.html`    | Enable for HTML      | `true`  |
| `intellijFormatter.languages.json`    | Enable for JSON      | `true`  |
| `intellijFormatter.languages.yaml`    | Enable for YAML      | `true`  |
| `intellijFormatter.languages.properties` | Enable for Properties | `true` |

### Example Configuration

```json
{
  // Enable the formatter
  "intellijFormatter.enabled": true,

  // Use custom IntelliJ code style
  "intellijFormatter.codeStylePath": "/path/to/your-code-style.xml",

  // Enable format on save
  "intellijFormatter.formatOnSave": true,

  // Language-specific settings
  "intellijFormatter.languages.java": true,
  "intellijFormatter.languages.kotlin": true,
  "intellijFormatter.languages.groovy": true,
  "intellijFormatter.languages.gradle": true,
  "intellijFormatter.languages.xml": true,
  "intellijFormatter.languages.html": true,
  "intellijFormatter.languages.json": false,  // Disable for JSON
  "intellijFormatter.languages.yaml": true,
  "intellijFormatter.languages.properties": true,

  // Set as default formatter for specific languages
  "[java]": {
    "editor.defaultFormatter": "idea-formatter.intellij-formatter",
    "editor.formatOnSave": true
  }
}
```

## Using Custom Code Style

### Export from IntelliJ IDEA

1. Open IntelliJ IDEA
2. Go to **Settings** (or **Preferences** on Mac)
3. Navigate to **Editor > Code Style**
4. Click the gear icon next to the scheme dropdown
5. Select **Export > IntelliJ IDEA code style XML**
6. Save the file

### Configure in VSCode

Add the path to your exported code style in settings:

```json
{
  "intellijFormatter.codeStylePath": "/path/to/your-code-style.xml"
}
```

Or use workspace-relative path:

```json
{
  "intellijFormatter.codeStylePath": "${workspaceFolder}/.idea/codeStyles/Project.xml"
}
```

## Commands

| Command                           | Description                    |
|-----------------------------------|--------------------------------|
| Format Document with IntelliJ     | Format the entire document     |
| Format Selection with IntelliJ    | Format selected code           |

## Troubleshooting

### "Formatter JAR not found"

The JAR is bundled with the extension. If you see this error:
1. Check that the extension installed correctly
2. Or manually set the JAR path:
   ```json
   {
     "intellijFormatter.jarPath": "/path/to/vscode-idea-code-formatter.jar"
   }
   ```

### "Java not found"

Ensure Java 21+ is installed and in your PATH, or set the path explicitly:

```json
{
  "intellijFormatter.javaPath": "/path/to/java"
}
```

### Formatting is Slow

The first format operation may take 2-3 seconds as the JVM starts. Subsequent operations are faster due to process caching.

### Incorrect Formatting

1. Ensure you're using a compatible IntelliJ code style XML
2. Check the Output panel (View > Output, select "IntelliJ Formatter") for errors
3. Try re-exporting your code style from IntelliJ IDEA

### View Logs

1. Open the Output panel: **View > Output**
2. Select "IntelliJ Formatter" from the dropdown
3. Review logs for errors or warnings

## Known Limitations

- First format operation has JVM startup latency (~2-3s)
- Some advanced IntelliJ-specific code style options may not be supported

## Feedback and Issues

Report issues and feature requests on [GitHub](https://github.com/your-repo/vscode-idea-code-formatter/issues).

## License

Apache License 2.0

## Acknowledgments

Built using [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/) by JetBrains.
