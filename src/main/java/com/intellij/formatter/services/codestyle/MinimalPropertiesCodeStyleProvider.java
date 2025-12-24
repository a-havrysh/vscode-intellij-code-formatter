package com.intellij.formatter.services.codestyle;

import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.codeStyle.PropertiesCodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * Minimal code style provider for Properties files.
 *
 * <p>Configures default formatting settings for .properties files:</p>
 * <ul>
 *     <li>Indent: 0 (no indentation - properties are flat key=value pairs)</li>
 *     <li>Tab size: 4 spaces (for occasional indentation needs)</li>
 *     <li>Tab character: disabled (spaces only)</li>
 * </ul>
 *
 * <p>Properties files are typically flat structures with no nesting,
 * so no indentation is applied by default.</p>
 *
 * @see AbstractCodeStyleProvider
 * @see PropertiesCodeStyleSettings
 */
public class MinimalPropertiesCodeStyleProvider extends AbstractCodeStyleProvider {

    /**
     * Creates a new Properties code style provider with default settings.
     */
    public MinimalPropertiesCodeStyleProvider() {
        // Properties files have no indentation (flat key=value structure)
        super(PropertiesLanguage.INSTANCE, 0, 0);
    }

    @Override
    protected CustomCodeStyleSettings doCreateCustomSettings(CodeStyleSettings settings) {
        return new PropertiesCodeStyleSettings(settings);
    }
}
