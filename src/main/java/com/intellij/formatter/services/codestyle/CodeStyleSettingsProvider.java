package com.intellij.formatter.services.codestyle;

import com.intellij.json.formatter.JsonCodeStyleSettings;
import com.intellij.lang.properties.psi.codeStyle.PropertiesCodeStyleSettings;
import com.intellij.openapi.Disposable;
import com.intellij.psi.codeStyle.CodeStyleSettingsService;
import com.intellij.psi.codeStyle.CodeStyleSettingsServiceListener;
import com.intellij.psi.codeStyle.CustomCodeStyleSettingsFactory;
import com.intellij.psi.codeStyle.FileTypeIndentOptionsFactory;
import com.intellij.psi.codeStyle.JavaCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleProvider;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.core.formatter.KotlinCodeStyleSettings;
import org.jetbrains.plugins.groovy.codeStyle.GroovyCodeStyleSettings;
import org.jetbrains.yaml.formatter.YAMLCodeStyleSettings;

import java.util.List;

/**
 * Service provider for code style settings in standalone formatting mode.
 *
 * <p>This implementation registers code style settings factories and
 * language providers for all supported file types.</p>
 *
 * @see CodeStyleSettingsService
 * @see LanguageCodeStyleProvider
 */
public class CodeStyleSettingsProvider implements CodeStyleSettingsService {

    private final List<LanguageCodeStyleProvider> languageProviders = List.of(
            new MinimalJavaCodeStyleProvider(),
            new MinimalGroovyCodeStyleProvider(),
            new MinimalPropertiesCodeStyleProvider(),
            new MinimalKotlinCodeStyleProvider()
    );

    private final List<CustomCodeStyleSettingsFactory> customFactories = List.of(
            JavaCodeStyleSettings::new,
            JsonCodeStyleSettings::new,
            XmlCodeStyleSettings::new,
            GroovyCodeStyleSettings::new,
            PropertiesCodeStyleSettings::new,
            YAMLCodeStyleSettings::new,
            KotlinCodeStyleSettings::new
    );

    @Override
    public @NotNull List<CustomCodeStyleSettingsFactory> getCustomCodeStyleSettingsFactories() {
        return customFactories;
    }

    @Override
    public void addListener(@NotNull CodeStyleSettingsServiceListener listener,
                            @Nullable Disposable disposable) {
    }

    @Override
    public @NotNull List<? extends FileTypeIndentOptionsFactory> getFileTypeIndentOptionsFactories() {
        return List.of();
    }

    @Override
    public @NotNull List<? extends LanguageCodeStyleProvider> getLanguageCodeStyleProviders() {
        return languageProviders;
    }
}
