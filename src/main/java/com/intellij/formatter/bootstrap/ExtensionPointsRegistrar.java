package com.intellij.formatter.bootstrap;

import com.intellij.mock.MockProject;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl;
import lombok.experimental.UtilityClass;

import static com.intellij.formatter.bootstrap.BootstrapLogger.debug;
import static com.intellij.formatter.bootstrap.BootstrapLogger.skipped;

/**
 * Registrar for IntelliJ Platform extension points required for standalone formatting.
 *
 * <p>Extension points are the plugin infrastructure of IntelliJ IDEA - they allow
 * plugins to extend IDE functionality. For standalone formatting, we need to
 * manually register the extension points that would normally be loaded from
 * plugin.xml descriptors.</p>
 *
 * <h2>Why Extension Points Are Required</h2>
 * <p>The formatting engine queries extension points to find:</p>
 * <ul>
 *     <li><b>langCodeStyleSettingsProvider</b> - language-specific formatting settings</li>
 *     <li><b>formattingService</b> - the actual formatting implementation</li>
 *     <li><b>preFormatProcessor/postFormatProcessor</b> - pre/post processing hooks</li>
 *     <li><b>lang.formatter</b> - FormattingModelBuilder for each language</li>
 * </ul>
 *
 * <h2>Registration Order</h2>
 * <p>Application-level extension points must be registered before project-level ones,
 * as project extension areas inherit from the application area.</p>
 *
 * @see FormatterBootstrap
 * @see LanguageExtensionsRegistrar
 */
@UtilityClass
public class ExtensionPointsRegistrar {

    private static final String COMPONENT = "ExtensionPoints";

    /**
     * Registers all required application-level extension points.
     *
     * <p>Application-level extension points are shared across all projects.
     * These include language-agnostic services like file types and code style providers.</p>
     *
     * @param area the application extension area obtained from {@link HeadlessMockApplication#getExtensionArea()}
     */
    public static void registerApplicationExtensionPoints(ExtensionsAreaImpl area) {
        debug(COMPONENT, "Registering application extension points");

        // Code style extension points - required for formatting settings management
        registerSafe(area, "com.intellij.fileIndentOptionsProvider",
                "com.intellij.psi.codeStyle.FileIndentOptionsProvider");
        registerSafe(area, "com.intellij.langCodeStyleSettingsProvider",
                "com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider");
        registerSafe(area, "com.intellij.codeStyleSettingsProvider",
                "com.intellij.psi.codeStyle.CodeStyleSettingsProvider");

        // Formatting pipeline extension points
        registerSafe(area, "com.intellij.lang.formatter",
                "com.intellij.formatting.FormattingModelBuilder");
        registerSafe(area, "com.intellij.formattingService",
                "com.intellij.formatting.service.FormattingService");
        registerSafe(area, "com.intellij.lang.formatter.restriction",
                "com.intellij.lang.LanguageFormattingRestriction");

        // Pre/post processors - modify code before/after main formatting pass
        registerSafe(area, "com.intellij.externalFormatProcessor",
                "com.intellij.psi.codeStyle.ExternalFormatProcessor");
        registerSafe(area, "com.intellij.postFormatProcessor",
                "com.intellij.psi.impl.source.codeStyle.PostFormatProcessor");
        registerSafe(area, "com.intellij.preFormatProcessor",
                "com.intellij.psi.impl.source.codeStyle.PreFormatProcessor");

        // PSI infrastructure extension points
        registerSafe(area, "com.intellij.lang.parserDefinition",
                "com.intellij.lang.ParserDefinition");
        registerSafe(area, "com.intellij.fileType",
                "com.intellij.openapi.fileTypes.FileType");
        registerSafe(area, "com.intellij.lang.psiAugmentProvider",
                "com.intellij.psi.augment.PsiAugmentProvider");
        registerSafe(area, "com.intellij.psi.treeChangePreprocessor",
                "com.intellij.psi.impl.PsiTreeChangePreprocessor");

        // Document and smart pointer extension points
        registerSafe(area, "com.intellij.documentWriteAccessGuard",
                "com.intellij.openapi.editor.impl.DocumentWriteAccessGuard");
        registerSafe(area, "com.intellij.smartPointer.anchorProvider",
                "com.intellij.psi.impl.smartPointers.SmartPointerAnchorProvider");

        // Groovy-specific: inline AST transformations for proper Groovy formatting
        registerSafe(area, "org.intellij.groovy.inlineASTTransformationSupport",
                "org.jetbrains.plugins.groovy.transformations.inline.GroovyInlineASTTransformationSupport");

        // IDEA 2025.x multiverse support (for multi-context code insight)
        registerSafe(area, "com.intellij.multiverseEnabler",
                "com.intellij.codeInsight.multiverse.MultiverseEnabler");

        // Meta language and injection support
        registerSafe(area, "com.intellij.metaLanguage",
                "com.intellij.lang.MetaLanguage");
        registerSafe(area, "com.intellij.languageInjector",
                "com.intellij.psi.LanguageInjector");

        debug(COMPONENT, "Application extension points registered");
    }

    /**
     * Registers all required project-level extension points.
     *
     * <p>Project-level extension points are scoped to individual projects.
     * For standalone formatting we only have one mock project, but the
     * infrastructure still requires these extension points to be present.</p>
     *
     * @param project the mock project instance
     */
    public static void registerProjectExtensionPoints(MockProject project) {
        debug(COMPONENT, "Registering project extension points");

        var projectArea = project.getExtensionArea();

        // PSI tree change listeners - notified when PSI structure changes during formatting
        registerSafe(projectArea, "com.intellij.psi.treeChangePreprocessor",
                "com.intellij.psi.impl.PsiTreeChangePreprocessor");
        registerSafe(projectArea, "com.intellij.psi.treeChangeListener",
                "com.intellij.psi.PsiTreeChangeListener");

        // Multi-host injection - for languages embedded in other languages (e.g., SQL in Java strings)
        registerSafe(projectArea, "com.intellij.multiHostInjector",
                "com.intellij.lang.injection.MultiHostInjector");

        debug(COMPONENT, "Project extension points registered");
    }

    /**
     * Safely registers an extension point, ignoring failures.
     *
     * <p>Some extension points may already be registered by IntelliJ core classes,
     * or may not exist in certain IntelliJ versions. We use INTERFACE kind which
     * allows multiple extensions to be registered for the same point.</p>
     *
     * @param area           the extension area (application or project level)
     * @param name           the extension point name (e.g., "com.intellij.lang.formatter")
     * @param interfaceClass the interface class that extensions must implement
     */
    private static void registerSafe(ExtensionsAreaImpl area, String name, String interfaceClass) {
        try {
            if (area.hasExtensionPoint(name)) {
                skipped(COMPONENT, "Extension point " + name, "already registered");
                return;
            }
            area.registerExtensionPoint(name, interfaceClass, ExtensionPoint.Kind.INTERFACE, true);
        } catch (Exception e) {
            // Expected for some extension points in older IDEA versions or when interface class not found
            skipped(COMPONENT, "Extension point " + name, e.getClass().getSimpleName());
        }
    }
}
