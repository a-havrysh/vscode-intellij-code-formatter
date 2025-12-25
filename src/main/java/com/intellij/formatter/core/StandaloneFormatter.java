package com.intellij.formatter.core;

import com.intellij.formatter.bootstrap.BootstrapLogger;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.testFramework.LightVirtualFile;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

import com.intellij.formatter.bootstrap.FormatterBootstrap;

import static com.intellij.formatter.bootstrap.FormatterBootstrap.getProject;
import static com.intellij.formatter.bootstrap.FormatterBootstrap.initialize;
import static com.intellij.openapi.application.ApplicationManager.getApplication;

/**
 * Utility class providing standalone code formatting capabilities using IntelliJ IDEA's formatting engine.
 *
 * <p>This formatter initializes a minimal IntelliJ Platform environment and leverages
 * the native {@link CodeStyleManager} to format source code. It supports multiple
 * file types including Java, XML, JSON, YAML, Groovy, Kotlin, and Properties.</p>
 *
 * <h2>Supported File Types</h2>
 * <table>
 *     <tr><th>Language</th><th>Extensions</th></tr>
 *     <tr><td>Java</td><td>.java</td></tr>
 *     <tr><td>Kotlin</td><td>.kt, .kts, .gradle.kts</td></tr>
 *     <tr><td>Groovy</td><td>.groovy, .gradle</td></tr>
 *     <tr><td>XML</td><td>.xml, .xsd, .xsl, .xslt, .wsdl, .fxml, .pom</td></tr>
 *     <tr><td>HTML</td><td>.html, .htm, .xhtml</td></tr>
 *     <tr><td>JSON</td><td>.json</td></tr>
 *     <tr><td>YAML</td><td>.yaml, .yml</td></tr>
 *     <tr><td>Properties</td><td>.properties</td></tr>
 * </table>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Format entire file
 * String javaCode = "public class Test{void method(){}}";
 * String formatted = StandaloneFormatter.formatCode(javaCode, "Test.java");
 *
 * // Format specific line range (1-based)
 * String rangeFormatted = StandaloneFormatter.formatCodeRange(javaCode, "Test.java", 1, 5);
 *
 * // Format XML
 * String xml = "<root><child>text</child></root>";
 * String formattedXml = StandaloneFormatter.formatCode(xml, "config.xml");
 * }</pre>
 *
 * <h2>Custom Code Styles</h2>
 * <p>Load custom IntelliJ code style settings before formatting:</p>
 * <pre>{@code
 * CodeStyleLoader.loadFromFile("/path/to/code-style.xml");
 * String formatted = StandaloneFormatter.formatCode(code, "MyClass.java");
 * }</pre>
 *
 * @see com.intellij.formatter.config.CodeStyleLoader
 * @see FormattingException
 */
@UtilityClass
public class StandaloneFormatter {

    private static final String COMPONENT = "Formatter";

    /**
     * Formats the given source code using IntelliJ IDEA's formatting engine.
     *
     * <p>The file type is automatically detected based on the filename extension.
     * The formatter applies all configured code style settings including indentation,
     * spacing, alignment, and wrapping rules.</p>
     *
     * @param code     the source code to format
     * @param fileName the filename used to detect file type (e.g., "MyClass.java")
     * @return the formatted source code
     * @throws FormattingException if formatting fails due to parsing errors or unsupported file type
     */
    public static String formatCode(@NotNull String code, @NotNull String fileName) throws FormattingException {
        initialize();
        FormatterBootstrap.ensureLanguageRegistered(fileName);
        var project = getProject();

        BootstrapLogger.debug(COMPONENT, "Formatting: " + fileName);

        try {
            var fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
            var psiFileFactory = PsiFileFactory.getInstance(project);

            // Create PsiFile from source code - this is the AST representation
            var psiFile = createPsiFile(psiFileFactory, fileName, code, fileType);

            if (psiFile == null) {
                throw new FormattingException("Failed to create PsiFile for: " + fileName +
                        ". The file type may not be supported.");
            }

            // Format the PsiFile using CodeStyleManager
            var formattedFile = formatPsiFile(psiFile);

            // Extract text from the formatted PsiFile
            // Use calcTreeElement() for PsiFileImpl to get the latest AST text
            var result = extractText(formattedFile);

            BootstrapLogger.debug(COMPONENT, "Formatting complete: " + fileName);
            return result;

        } catch (FormattingException e) {
            throw e;
        } catch (Exception e) {
            BootstrapLogger.warn(COMPONENT, "Formatting failed for " + fileName, e);
            throw new FormattingException("Formatting failed: " + e.getMessage(), e);
        }
    }

    /**
     * Formats a specific line range within the given source code.
     *
     * <p>Only the specified line range is reformatted, leaving the rest of the code unchanged.
     * This is useful for formatting recently edited code without affecting the entire file.</p>
     *
     * @param code      the source code to format
     * @param fileName  the filename used to detect file type (e.g., "MyClass.java")
     * @param startLine the starting line number (1-based, inclusive)
     * @param endLine   the ending line number (1-based, inclusive)
     * @return the formatted source code
     * @throws FormattingException if formatting fails due to parsing errors or invalid line range
     */
    public static String formatCodeRange(@NotNull String code,
                                         @NotNull String fileName,
                                         int startLine,
                                         int endLine) throws FormattingException {
        initialize();
        FormatterBootstrap.ensureLanguageRegistered(fileName);
        var project = getProject();

        BootstrapLogger.debug(COMPONENT, "Formatting range: " + fileName + " [" + startLine + ":" + endLine + "]");

        try {
            var fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
            var psiFileFactory = PsiFileFactory.getInstance(project);

            // Create PsiFile using Language for better parsing
            PsiFile psiFile;
            if (fileType instanceof LanguageFileType languageFileType) {
                var language = languageFileType.getLanguage();
                psiFile = psiFileFactory.createFileFromText(fileName, language, code);
            } else {
                psiFile = psiFileFactory.createFileFromText(fileName, fileType, code);
            }

            if (psiFile == null) {
                throw new FormattingException("Failed to create PsiFile for: " + fileName);
            }

            // Convert line numbers to character offsets
            var text = psiFile.getText();
            var startOffset = getLineStartOffset(text, startLine);
            var endOffset = getLineEndOffset(text, endLine);

            // Format the specified range
            var formattedFile = formatPsiFileRange(psiFile, startOffset, endOffset);
            var result = extractText(formattedFile);

            BootstrapLogger.debug(COMPONENT, "Range formatting complete: " + fileName);
            return result;

        } catch (FormattingException e) {
            throw e;
        } catch (Exception e) {
            BootstrapLogger.warn(COMPONENT, "Range formatting failed for " + fileName, e);
            throw new FormattingException("Range formatting failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a PsiFile from source code with automatic language detection.
     */
    private static PsiFile createPsiFile(PsiFileFactory factory, String fileName, String code, Object fileType) {
        PsiFile psiFile = null;

        // Try Language-based creation first (more reliable for language plugins)
        if (fileType instanceof LanguageFileType languageFileType) {
            Language language = languageFileType.getLanguage();

            // Method 1: Use PsiFileFactory with Language
            psiFile = factory.createFileFromText(fileName, language, code);

            // Method 2: If factory fails, try direct ParserDefinition approach
            // This is a fallback for languages with custom file creation logic
            if (psiFile == null) {
                var parserDef = LanguageParserDefinitions.INSTANCE.forLanguage(language);
                if (parserDef != null) {
                    try {
                        var virtualFile = new LightVirtualFile(fileName, language, code);
                        var viewProvider = new SingleRootFileViewProvider(
                                PsiManager.getInstance(getProject()), virtualFile, true);
                        psiFile = parserDef.createFile(viewProvider);
                    } catch (Exception e) {
                        BootstrapLogger.skipped(COMPONENT, "ParserDefinition file creation",
                                e.getClass().getSimpleName());
                    }
                }
            }
        }

        // Fallback to FileType-based creation
        if (psiFile == null) {
            psiFile = factory.createFileFromText(fileName, (com.intellij.openapi.fileTypes.FileType) fileType, code);
        }

        return psiFile;
    }

    /**
     * Formats the entire PsiFile using CodeStyleManager.
     */
    private static PsiFile formatPsiFile(PsiFile psiFile) throws Exception {
        var project = getProject();
        var codeStyleManager = CodeStyleManager.getInstance(project);

        final PsiFile[] result = new PsiFile[1];
        final Exception[] error = new Exception[1];

        // Formatting must run in a write action within a command
        Runnable formatTask = () -> CommandProcessor.getInstance().executeCommand(
                project,
                () -> getApplication().runWriteAction(() -> {
                    try {
                        result[0] = (PsiFile) codeStyleManager.reformat(psiFile);
                    } catch (Exception e) {
                        error[0] = e;
                    }
                }),
                "Format Code",
                null
        );

        executeOnEdt(formatTask);

        if (error[0] != null) {
            throw error[0];
        }

        return result[0] != null ? result[0] : psiFile;
    }

    /**
     * Formats a range within the PsiFile using CodeStyleManager.
     */
    private static PsiFile formatPsiFileRange(PsiFile psiFile, int startOffset, int endOffset) throws Exception {
        var project = getProject();
        var codeStyleManager = CodeStyleManager.getInstance(project);

        final PsiFile[] result = new PsiFile[1];
        final Exception[] error = new Exception[1];

        Runnable formatTask = () -> CommandProcessor.getInstance().executeCommand(
                project,
                () -> getApplication().runWriteAction(() -> {
                    try {
                        result[0] = (PsiFile) codeStyleManager.reformatRange(psiFile, startOffset, endOffset);
                    } catch (Exception e) {
                        error[0] = e;
                    }
                }),
                "Format Range",
                null
        );

        executeOnEdt(formatTask);

        if (error[0] != null) {
            throw error[0];
        }

        return result[0] != null ? result[0] : psiFile;
    }

    /**
     * Extracts text from a PsiFile.
     *
     * <p>For PsiFileImpl, we use calcTreeElement() to get the AST and extract text
     * from it. This ensures we get the latest formatted text, not a cached version.</p>
     */
    private static String extractText(PsiFile psiFile) {
        if (psiFile instanceof PsiFileImpl psiFileImpl) {
            // calcTreeElement() returns the actual AST, ensuring fresh text
            return psiFileImpl.calcTreeElement().getText();
        }
        return psiFile.getText();
    }

    /**
     * Executes a task on the Event Dispatch Thread (EDT).
     *
     * <p>IntelliJ's write actions must run on EDT. If we're already on EDT,
     * run directly. Otherwise, use invokeAndWait to synchronously run on EDT.</p>
     */
    private static void executeOnEdt(Runnable task) throws InvocationTargetException, InterruptedException {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeAndWait(task);
        }
    }

    /**
     * Converts a 1-based line number to a character offset (start of line).
     */
    private static int getLineStartOffset(String text, int line) {
        var currentLine = 1;
        var offset = 0;

        while (currentLine < line && offset < text.length()) {
            if (text.charAt(offset) == '\n') {
                currentLine++;
            }
            offset++;
        }
        return offset;
    }

    /**
     * Converts a 1-based line number to a character offset (end of line).
     */
    private static int getLineEndOffset(String text, int line) {
        var currentLine = 1;
        var offset = 0;

        while (offset < text.length()) {
            if (text.charAt(offset) == '\n') {
                if (currentLine == line) {
                    return offset;
                }
                currentLine++;
            }
            offset++;
        }
        return text.length();
    }
}
