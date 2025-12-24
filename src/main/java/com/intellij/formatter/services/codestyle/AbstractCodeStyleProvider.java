package com.intellij.formatter.services.codestyle;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.psi.codeStyle.DocCommentSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Abstract base class for minimal language-specific code style providers.
 *
 * <p>This class provides common implementation for {@link LanguageCodeStyleProvider}
 * that sets up default indentation settings and delegates language-specific
 * configuration to subclasses.</p>
 *
 * <h2>Default Settings</h2>
 * <ul>
 *     <li>Indent size: 4 spaces (configurable)</li>
 *     <li>Tab size: 4 spaces</li>
 *     <li>Use tab character: false (spaces only)</li>
 *     <li>Continuation indent: configurable (default: 8 for Java-style, 4 for Kotlin-style)</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * public class MinimalJavaCodeStyleProvider extends AbstractCodeStyleProvider {
 *     public MinimalJavaCodeStyleProvider() {
 *         super(JavaLanguage.INSTANCE, 4, 8);
 *     }
 *
 *     @Override
 *     protected CustomCodeStyleSettings doCreateCustomSettings(CodeStyleSettings settings) {
 *         return new JavaCodeStyleSettings(settings);
 *     }
 * }
 * }</pre>
 *
 * @see LanguageCodeStyleProvider
 * @see CommonCodeStyleSettings
 */
@RequiredArgsConstructor
public abstract class AbstractCodeStyleProvider implements LanguageCodeStyleProvider {

    /** The language this provider handles. */
    @Getter
    @NotNull
    private final Language language;

    /** Indentation size in spaces. */
    private final int indentSize;

    /** Continuation indent size in spaces (for wrapped lines). */
    private final int continuationIndentSize;

    /**
     * Creates a provider with default indentation (4 spaces, 8 continuation).
     *
     * @param language the language this provider handles
     */
    protected AbstractCodeStyleProvider(@NotNull Language language) {
        this(language, 4, 8);
    }

    /**
     * Returns default common code style settings with configured indentation.
     *
     * @return common code style settings with indentation configured
     */
    @Override
    public @NotNull CommonCodeStyleSettings getDefaultCommonSettings() {
        var settings = createCommonSettings();
        configureIndentOptions(settings);
        configureAdditionalSettings(settings);
        return settings;
    }

    /**
     * Creates the language-specific CommonCodeStyleSettings instance.
     *
     * <p>Override this method to return a subclass of CommonCodeStyleSettings
     * if your language has specific settings (e.g., KotlinCommonCodeStyleSettings).</p>
     *
     * @return a new CommonCodeStyleSettings instance
     */
    protected CommonCodeStyleSettings createCommonSettings() {
        return new CommonCodeStyleSettings(language);
    }

    /**
     * Configures indent options on the given settings.
     *
     * @param settings the settings to configure
     */
    private void configureIndentOptions(CommonCodeStyleSettings settings) {
        var indentOptions = settings.initIndentOptions();
        indentOptions.INDENT_SIZE = indentSize;
        indentOptions.TAB_SIZE = 4;
        indentOptions.USE_TAB_CHARACTER = false;
        indentOptions.CONTINUATION_INDENT_SIZE = continuationIndentSize;
    }

    /**
     * Override to configure additional language-specific settings.
     *
     * <p>This method is called after indent options are configured.
     * The default implementation does nothing.</p>
     *
     * @param settings the settings to configure
     */
    protected void configureAdditionalSettings(CommonCodeStyleSettings settings) {
        // Default: no additional configuration
    }

    /**
     * Creates custom code style settings for the language.
     *
     * @param settings the parent code style settings
     * @return custom settings, or null if not needed
     */
    @Override
    public CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
        return doCreateCustomSettings(settings);
    }

    /**
     * Template method for creating language-specific custom settings.
     *
     * @param settings the parent code style settings
     * @return custom settings instance
     */
    protected abstract CustomCodeStyleSettings doCreateCustomSettings(@NotNull CodeStyleSettings settings);

    /**
     * Returns doc comment settings. Default implementation returns null.
     *
     * @param settings the code style settings
     * @return doc comment settings, or null if not applicable
     */
    @Override
    public DocCommentSettings getDocCommentSettings(@NotNull CodeStyleSettings settings) {
        return null;
    }

    /**
     * Returns supported fields for UI. Default implementation returns empty set.
     *
     * @return set of supported field names
     */
    @Override
    public @NotNull Set<String> getSupportedFields() {
        return Set.of();
    }
}
