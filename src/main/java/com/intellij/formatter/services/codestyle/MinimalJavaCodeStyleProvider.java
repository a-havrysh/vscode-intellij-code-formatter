package com.intellij.formatter.services.codestyle;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.psi.codeStyle.JavaCodeStyleSettings;

/**
 * Minimal code style provider for Java language.
 *
 * <p>Configures default formatting settings for Java source files:</p>
 * <ul>
 *     <li>Indent: 4 spaces</li>
 *     <li>Continuation indent: 8 spaces (for wrapped lines)</li>
 *     <li>Tab character: disabled (spaces only)</li>
 * </ul>
 *
 * <p>These defaults match standard Java conventions and IntelliJ's default settings.</p>
 *
 * @see AbstractCodeStyleProvider
 * @see JavaCodeStyleSettings
 */
public class MinimalJavaCodeStyleProvider extends AbstractCodeStyleProvider {

    /**
     * Creates a new Java code style provider with default settings.
     */
    public MinimalJavaCodeStyleProvider() {
        // Java uses 4-space indent and 8-space continuation indent
        super(JavaLanguage.INSTANCE, 4, 8);
    }

    @Override
    protected CustomCodeStyleSettings doCreateCustomSettings(CodeStyleSettings settings) {
        return new JavaCodeStyleSettings(settings);
    }
}
