package com.intellij.formatter.services.codestyle;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.codeStyle.GroovyCodeStyleSettings;

/**
 * Minimal code style provider for Groovy language.
 *
 * <p>Configures default formatting settings for Groovy source files
 * including .groovy and .gradle files:</p>
 * <ul>
 *     <li>Indent: 4 spaces</li>
 *     <li>Continuation indent: 8 spaces</li>
 *     <li>Tab character: disabled (spaces only)</li>
 * </ul>
 *
 * <p>These defaults are consistent with Java conventions since Groovy
 * is often used alongside Java code.</p>
 *
 * @see AbstractCodeStyleProvider
 * @see GroovyCodeStyleSettings
 */
public class MinimalGroovyCodeStyleProvider extends AbstractCodeStyleProvider {

    /**
     * Creates a new Groovy code style provider with default settings.
     */
    public MinimalGroovyCodeStyleProvider() {
        // Groovy uses same indentation as Java for consistency
        super(GroovyLanguage.INSTANCE, 4, 8);
    }

    @Override
    protected CustomCodeStyleSettings doCreateCustomSettings(CodeStyleSettings settings) {
        return new GroovyCodeStyleSettings(settings);
    }
}
