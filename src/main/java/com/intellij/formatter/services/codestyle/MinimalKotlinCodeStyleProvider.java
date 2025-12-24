package com.intellij.formatter.services.codestyle;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.core.formatter.KotlinCodeStyleSettings;
import org.jetbrains.kotlin.idea.formatter.KotlinCommonCodeStyleSettings;

/**
 * Minimal code style provider for Kotlin language.
 *
 * <p>Configures default formatting settings for Kotlin source files
 * including .kt, .kts, and .gradle.kts files:</p>
 * <ul>
 *     <li>Indent: 4 spaces</li>
 *     <li>Continuation indent: 4 spaces (Kotlin convention)</li>
 *     <li>Tab character: disabled (spaces only)</li>
 *     <li>Spaces around operators: enabled</li>
 * </ul>
 *
 * <p>Note: Kotlin uses 4-space continuation indent unlike Java's 8-space,
 * following official Kotlin coding conventions.</p>
 *
 * @see AbstractCodeStyleProvider
 * @see KotlinCodeStyleSettings
 */
public class MinimalKotlinCodeStyleProvider extends AbstractCodeStyleProvider {

    /**
     * Creates a new Kotlin code style provider with default settings.
     */
    public MinimalKotlinCodeStyleProvider() {
        // Kotlin uses 4-space continuation indent (unlike Java's 8)
        super(KotlinLanguage.INSTANCE, 4, 4);
    }

    @Override
    protected CommonCodeStyleSettings createCommonSettings() {
        // Kotlin has its own CommonCodeStyleSettings subclass
        return new KotlinCommonCodeStyleSettings();
    }

    @Override
    protected void configureAdditionalSettings(CommonCodeStyleSettings settings) {
        // Configure spacing rules that are common in Kotlin code
        settings.SPACE_AROUND_ASSIGNMENT_OPERATORS = true;
        settings.SPACE_AROUND_LOGICAL_OPERATORS = true;
        settings.SPACE_AROUND_EQUALITY_OPERATORS = true;
        settings.SPACE_AROUND_RELATIONAL_OPERATORS = true;
        settings.SPACE_AROUND_ADDITIVE_OPERATORS = true;
        settings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = true;
        settings.SPACE_AFTER_COMMA = true;
        settings.SPACE_AFTER_COLON = true;
        settings.SPACE_BEFORE_COLON = false;
        settings.SPACE_BEFORE_METHOD_PARENTHESES = false;
    }

    @Override
    protected CustomCodeStyleSettings doCreateCustomSettings(CodeStyleSettings settings) {
        return new KotlinCodeStyleSettings(settings);
    }
}
