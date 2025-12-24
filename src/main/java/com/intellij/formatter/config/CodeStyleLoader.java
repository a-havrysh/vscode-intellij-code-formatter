package com.intellij.formatter.config;

import com.intellij.formatter.core.CodeStyleLoadException;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.psi.codeStyle.ProjectCodeStyleSettingsManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.formatter.bootstrap.FormatterBootstrap.getProject;
import static com.intellij.formatter.bootstrap.FormatterBootstrap.initialize;

/**
 * Utility class for loading IntelliJ IDEA code style settings from XML files.
 *
 * <p>This loader supports various code style XML formats exported from IntelliJ IDEA:</p>
 * <ul>
 *     <li>Direct code_scheme exports</li>
 *     <li>Project-level code style configurations</li>
 *     <li>Component-wrapped configurations</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Load custom code style before formatting
 * CodeStyleLoader.loadFromFile("/path/to/my-code-style.xml");
 *
 * // Now format code with the loaded style
 * String formatted = StandaloneFormatter.formatCode(code, "MyClass.java");
 * }</pre>
 *
 * @see CodeStyleLoadException
 * @see com.intellij.formatter.core.StandaloneFormatter
 */
public final class CodeStyleLoader {

    private CodeStyleLoader() {
        // Utility class - prevent instantiation
    }

    /**
     * Loads code style settings from the specified XML file and applies them to the project.
     *
     * <p>The file must be a valid IntelliJ IDEA code style export. The loader automatically
     * detects and handles different XML structures including direct code_scheme elements,
     * project configurations, and component wrappers.</p>
     *
     * @param filePath the absolute path to the code style XML file
     * @throws CodeStyleLoadException if the file cannot be read, parsed, or applied
     */
    public static void loadFromFile(@NotNull String filePath) throws CodeStyleLoadException {
        var path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new CodeStyleLoadException("Code style file not found: " + filePath);
        }

        initialize();

        try {
            var content = Files.readString(path);
            var rootElement = JDOMUtil.load(content);
            applySettings(rootElement);
        } catch (IOException e) {
            throw new CodeStyleLoadException("Failed to read code style file: " + e.getMessage(), e);
        } catch (CodeStyleLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeStyleLoadException("Failed to parse code style file: " + e.getMessage(), e);
        }
    }

    private static void applySettings(Element rootElement) throws CodeStyleLoadException {
        var project = getProject();
        var settingsManager = project.getService(ProjectCodeStyleSettingsManager.class);

        if (settingsManager == null) {
            throw new CodeStyleLoadException("ProjectCodeStyleSettingsManager not available");
        }

        var codeStyleElement = findCodeStyleElement(rootElement);
        if (codeStyleElement == null) {
            throw new CodeStyleLoadException("No code style settings found in file");
        }

        try {
            var settings = settingsManager.getMainProjectCodeStyle();
            if (settings == null) {
                settings = settingsManager.createSettings();
            }
            settings.readExternal(codeStyleElement);
            settingsManager.setMainProjectCodeStyle(settings);
        } catch (Exception e) {
            throw new CodeStyleLoadException("Failed to apply code style settings: " + e.getMessage(), e);
        }
    }

    private static Element findCodeStyleElement(Element root) {
        var rootName = root.getName();

        if ("code_scheme".equals(rootName)) {
            return root;
        }

        if ("component".equals(rootName) && "ProjectCodeStyleConfiguration".equals(root.getAttributeValue("name"))) {
            var stateElement = root.getChild("state");
            return stateElement != null ? stateElement.getChild("code_scheme") : root.getChild("code_scheme");
        }

        if ("project".equals(rootName)) {
            for (var child : root.getChildren("component")) {
                if ("ProjectCodeStyleConfiguration".equals(child.getAttributeValue("name"))) {
                    return findCodeStyleElement(child);
                }
            }
        }

        var codeScheme = root.getChild("code_scheme");
        return codeScheme != null ? codeScheme : root;
    }
}
