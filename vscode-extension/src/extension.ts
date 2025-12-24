import * as vscode from 'vscode';
import * as cp from 'child_process';
import * as path from 'path';
import * as fs from 'fs';
import * as os from 'os';

const SUPPORTED_LANGUAGES = [
    'java',
    'kotlin',
    'groovy',
    'gradle',
    'gradle-kotlin-dsl',
    'xml',
    'html',
    'json',
    'yaml',
    'properties'
];

const LANGUAGE_TO_EXTENSION: Record<string, string> = {
    'java': '.java',
    'kotlin': '.kt',
    'groovy': '.groovy',
    'gradle': '.gradle',
    'gradle-kotlin-dsl': '.gradle.kts',
    'xml': '.xml',
    'html': '.html',
    'json': '.json',
    'yaml': '.yaml',
    'properties': '.properties'
};

let outputChannel: vscode.OutputChannel;

export function activate(context: vscode.ExtensionContext) {
    outputChannel = vscode.window.createOutputChannel('IntelliJ Formatter');

    const formattingProvider = new IntelliJFormattingProvider(context);

    for (const language of SUPPORTED_LANGUAGES) {
        context.subscriptions.push(
            vscode.languages.registerDocumentFormattingEditProvider(
                { language },
                formattingProvider
            )
        );

        context.subscriptions.push(
            vscode.languages.registerDocumentRangeFormattingEditProvider(
                { language },
                formattingProvider
            )
        );
    }

    context.subscriptions.push(
        vscode.commands.registerCommand('intellijFormatter.formatDocument', async () => {
            const editor = vscode.window.activeTextEditor;
            if (editor && isLanguageEnabled(editor.document.languageId)) {
                await vscode.commands.executeCommand('editor.action.formatDocument');
            }
        })
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('intellijFormatter.formatSelection', async () => {
            const editor = vscode.window.activeTextEditor;
            if (editor && isLanguageEnabled(editor.document.languageId) && !editor.selection.isEmpty) {
                await vscode.commands.executeCommand('editor.action.formatSelection');
            }
        })
    );

    outputChannel.appendLine('IntelliJ Formatter extension activated');
}

export function deactivate() {
    if (outputChannel) {
        outputChannel.dispose();
    }
}

function getConfig(): vscode.WorkspaceConfiguration {
    return vscode.workspace.getConfiguration('intellijFormatter');
}

function isLanguageEnabled(languageId: string): boolean {
    const config = getConfig();

    if (!config.get<boolean>('enabled')) {
        return false;
    }

    if (!SUPPORTED_LANGUAGES.includes(languageId)) {
        return false;
    }

    // Map gradle-kotlin-dsl to gradle setting
    const settingId = languageId === 'gradle-kotlin-dsl' ? 'gradle' : languageId;
    const languageSetting = `languages.${settingId}`;
    return config.get<boolean>(languageSetting, true);
}

class IntelliJFormattingProvider implements
    vscode.DocumentFormattingEditProvider,
    vscode.DocumentRangeFormattingEditProvider {

    private extensionPath: string;

    constructor(context: vscode.ExtensionContext) {
        this.extensionPath = context.extensionPath;
    }

    async provideDocumentFormattingEdits(
        document: vscode.TextDocument,
        options: vscode.FormattingOptions,
        token: vscode.CancellationToken
    ): Promise<vscode.TextEdit[] | null> {
        if (!isLanguageEnabled(document.languageId)) {
            return null;
        }

        try {
            const formatted = await this.formatCode(document.getText(), document.languageId);
            if (formatted !== null && formatted !== document.getText()) {
                const fullRange = new vscode.Range(
                    document.positionAt(0),
                    document.positionAt(document.getText().length)
                );
                return [vscode.TextEdit.replace(fullRange, formatted)];
            }
        } catch (error) {
            this.handleError(error);
        }

        return null;
    }

    async provideDocumentRangeFormattingEdits(
        document: vscode.TextDocument,
        range: vscode.Range,
        options: vscode.FormattingOptions,
        token: vscode.CancellationToken
    ): Promise<vscode.TextEdit[] | null> {
        if (!isLanguageEnabled(document.languageId)) {
            return null;
        }

        try {
            const startLine = range.start.line + 1;
            const endLine = range.end.line + 1;

            const formatted = await this.formatCode(
                document.getText(),
                document.languageId,
                startLine,
                endLine
            );

            if (formatted !== null && formatted !== document.getText()) {
                const fullRange = new vscode.Range(
                    document.positionAt(0),
                    document.positionAt(document.getText().length)
                );
                return [vscode.TextEdit.replace(fullRange, formatted)];
            }
        } catch (error) {
            this.handleError(error);
        }

        return null;
    }

    private async formatCode(
        code: string,
        languageId: string,
        startLine?: number,
        endLine?: number
    ): Promise<string | null> {
        const config = getConfig();
        const javaPath = config.get<string>('javaPath') || 'java';
        const codeStylePath = config.get<string>('codeStylePath');

        const jarPath = this.getJarPath();
        if (!jarPath) {
            throw new Error('Formatter JAR not found. Please reinstall the extension.');
        }

        const tempFile = this.createTempFile(code, languageId);

        try {
            const args = this.buildJavaArgs(jarPath, tempFile, codeStylePath, startLine, endLine);

            const result = await this.executeJava(javaPath, args);

            if (result.exitCode === 0) {
                return fs.readFileSync(tempFile, 'utf8');
            } else {
                throw new Error(result.stderr || 'Formatting failed');
            }
        } finally {
            this.deleteTempFile(tempFile);
        }
    }

    private getJarPath(): string | null {
        const bundledJar = path.join(this.extensionPath, 'formatter', 'vscode-idea-code-formatter.jar');
        if (fs.existsSync(bundledJar)) {
            return bundledJar;
        }
        return null;
    }

    private buildJavaArgs(
        jarPath: string,
        filePath: string,
        codeStylePath?: string,
        startLine?: number,
        endLine?: number
    ): string[] {
        const args = [
            '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
            '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED',
            '--add-opens', 'java.base/java.io=ALL-UNNAMED',
            '--add-opens', 'java.base/java.util=ALL-UNNAMED',
            '-jar', jarPath
        ];

        if (codeStylePath && fs.existsSync(codeStylePath)) {
            args.push('--style', codeStylePath);
        }

        if (startLine !== undefined && endLine !== undefined) {
            args.push('--lines', `${startLine}:${endLine}`);
        }

        args.push(filePath);

        return args;
    }

    private createTempFile(content: string, languageId: string): string {
        const extension = LANGUAGE_TO_EXTENSION[languageId] || '.txt';
        const tempDir = os.tmpdir();
        const tempFile = path.join(tempDir, `intellij-format-${Date.now()}${extension}`);
        fs.writeFileSync(tempFile, content, 'utf8');
        return tempFile;
    }

    private deleteTempFile(filePath: string): void {
        try {
            if (fs.existsSync(filePath)) {
                fs.unlinkSync(filePath);
            }
        } catch {
            // ignore
        }
    }

    private executeJava(javaPath: string, args: string[]): Promise<{ exitCode: number; stderr: string }> {
        return new Promise((resolve) => {
            const proc = cp.spawn(javaPath, args);

            let stderr = '';

            proc.stderr.on('data', (data) => {
                stderr += data.toString();
            });

            proc.on('close', (code) => {
                resolve({
                    exitCode: code || 0,
                    stderr
                });
            });

            proc.on('error', (error) => {
                resolve({
                    exitCode: 1,
                    stderr: error.message
                });
            });
        });
    }

    private handleError(error: unknown): void {
        const message = error instanceof Error ? error.message : String(error);
        outputChannel.appendLine(`Error: ${message}`);
        vscode.window.showErrorMessage(`IntelliJ Formatter: ${message}`);
    }
}
