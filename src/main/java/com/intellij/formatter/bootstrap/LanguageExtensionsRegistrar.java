package com.intellij.formatter.bootstrap;

import com.intellij.formatting.service.CoreFormattingService;
import com.intellij.formatting.service.FormattingService;
import com.intellij.json.JsonLanguage;
import com.intellij.json.JsonParserDefinition;
import com.intellij.json.formatter.JsonFormattingBuilderModel;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.LanguageFormatting;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lang.html.HtmlFormattingModelBuilder;
import com.intellij.lang.java.JavaFormattingModelBuilder;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.JavaParserDefinition;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.formatting.PropertiesFormattingModelBuilder;
import com.intellij.lang.properties.parsing.PropertiesParserDefinition;
import com.intellij.lang.xhtml.XHTMLLanguage;
import com.intellij.lang.xhtml.XHTMLParserDefinition;
import com.intellij.lang.xhtml.XhtmlFormattingModelBuilder;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.lang.xml.XmlASTFactory;
import com.intellij.lang.xml.XmlFormattingModelBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl;
import com.intellij.psi.impl.source.tree.JavaASTFactory;
import lombok.experimental.UtilityClass;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.formatter.KotlinFormattingModelBuilder;
import org.jetbrains.kotlin.parsing.KotlinParserDefinition;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.formatter.GroovyFormattingModelBuilder;
import org.jetbrains.plugins.groovy.lang.parser.GroovyParserDefinition;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLParserDefinition;
import org.jetbrains.yaml.formatter.YAMLFormattingModelBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static com.intellij.formatter.bootstrap.BootstrapLogger.debug;
import static com.intellij.formatter.bootstrap.BootstrapLogger.skipped;
import static com.intellij.formatter.bootstrap.BootstrapLogger.warn;

/**
 * Registrar for language-specific parser definitions, AST factories, and formatting builders.
 *
 * <p>This class handles the registration of IntelliJ's language extensions required for
 * code formatting. The IntelliJ Platform uses a plugin system where language support
 * is provided via extensions. For standalone formatting, we manually register the
 * minimum required extensions.</p>
 *
 * <h2>Extension Types</h2>
 * <p>Each supported language requires several types of extensions:</p>
 *
 * <h3>Required Extensions</h3>
 * <ul>
 *     <li><b>ParserDefinition</b> - creates lexers and parsers for the language</li>
 *     <li><b>FormattingModelBuilder</b> - builds the formatting model used to format code</li>
 * </ul>
 *
 * <h3>Optional Extensions</h3>
 * <ul>
 *     <li><b>ASTFactory</b> - creates AST nodes (only needed for Java and XML)</li>
 *     <li><b>SyntaxDefinition</b> - IDEA 2025.x syntax-based parsing support</li>
 *     <li><b>ElementTypeConverter</b> - IDEA 2025.x element type conversion</li>
 * </ul>
 *
 * <h2>Supported Languages</h2>
 * <table>
 *     <tr><th>Language</th><th>Extensions</th></tr>
 *     <tr><td>Java</td><td>.java</td></tr>
 *     <tr><td>Kotlin</td><td>.kt, .kts, .gradle.kts</td></tr>
 *     <tr><td>Groovy</td><td>.groovy, .gradle</td></tr>
 *     <tr><td>XML</td><td>.xml, .xsd, .xsl, .xslt, .wsdl, .fxml, .pom</td></tr>
 *     <tr><td>HTML/XHTML</td><td>.html, .htm, .xhtml</td></tr>
 *     <tr><td>JSON</td><td>.json</td></tr>
 *     <tr><td>YAML</td><td>.yaml, .yml</td></tr>
 *     <tr><td>Properties</td><td>.properties</td></tr>
 * </table>
 *
 * @see FormatterBootstrap
 * @see ExtensionPointsRegistrar
 */
@UtilityClass
public class LanguageExtensionsRegistrar {

    private static final String COMPONENT = "Languages";

    /**
     * Registers all language extensions for supported file types.
     *
     * <p>The registration order matters for some languages. XML must be registered
     * before HTML since HTML extends XMLLanguage. Java must be registered with
     * special IDEA 2025.x compatibility extensions.</p>
     *
     * @param extensionArea  the application extension area
     * @param rootDisposable the root disposable for extension lifecycle management
     */
    public static void registerAll(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        debug(COMPONENT, "Registering language extensions");

        // CoreFormattingService provides the actual formatting algorithm
        registerFormattingService(extensionArea, rootDisposable);

        // Register languages in dependency order (base languages first)
        registerJava();        // Required by Kotlin for some type resolution
        registerXml();         // Required by HTML (HTML extends XML)
        registerHtml();        // Depends on XML
        registerJson();
        registerGroovy();      // Uses some Java infrastructure
        registerProperties();
        registerYaml();
        registerKotlin(extensionArea, rootDisposable);

        debug(COMPONENT, "Language extensions registered");
    }

    /**
     * Registers CoreFormattingService as the default formatting implementation.
     *
     * <p>CoreFormattingService is IntelliJ's built-in formatter that uses
     * FormattingModelBuilder to create formatting models for each language.</p>
     */
    private static void registerFormattingService(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        try {
            ExtensionPoint<FormattingService> ep = extensionArea.getExtensionPoint("com.intellij.formattingService");
            ep.registerExtension(new CoreFormattingService(), rootDisposable);
            debug(COMPONENT, "Registered CoreFormattingService");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register CoreFormattingService", e);
        }
    }

    /**
     * Registers Java language support.
     *
     * <p>Java requires special handling for IDEA 2025.x which introduced a new
     * syntax-based parsing system. We register both the traditional PSI components
     * and the new syntax definition/element type converters.</p>
     */
    @SuppressWarnings("deprecation")
    private static void registerJava() {
        try {
            // Core Java PSI support
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, new JavaParserDefinition());
            LanguageASTFactory.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, new JavaASTFactory());
            LanguageFormatting.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, new JavaFormattingModelBuilder());

            // IDEA 2025.x syntax-based parsing support
            // SyntaxDefinition defines how source code is tokenized
            tryRegisterSyntaxDefinition(JavaLanguage.INSTANCE,
                    "com.intellij.java.frontback.psi.impl.syntax.JavaSyntaxDefinitionExtension");
            // ElementTypeConverter converts between old IElementType and new SyntaxElementType
            tryRegisterElementTypeConverter(JavaLanguage.INSTANCE,
                    "com.intellij.lang.java.syntax.JavaElementTypeConverterExtension");

            debug(COMPONENT, "Registered Java");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Java", e);
        }
    }

    /**
     * Registers XML language support.
     *
     * <p>XML is a base language for HTML, XHTML, and other markup languages.
     * It must be registered before languages that extend it.</p>
     */
    private static void registerXml() {
        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(XMLLanguage.INSTANCE, new XMLParserDefinition());
            // XmlASTFactory creates XML-specific AST nodes (XmlTag, XmlAttribute, etc.)
            LanguageASTFactory.INSTANCE.addExplicitExtension(XMLLanguage.INSTANCE, new XmlASTFactory());
            LanguageFormatting.INSTANCE.addExplicitExtension(XMLLanguage.INSTANCE, new XmlFormattingModelBuilder());

            // IDEA 2025.x compatibility
            tryRegisterSyntaxDefinition(XMLLanguage.INSTANCE,
                    "com.intellij.lang.xml.XmlSyntaxDefinitionExtension");
            tryRegisterElementTypeConverter(XMLLanguage.INSTANCE,
                    "com.intellij.psi.xml.XmlElementTypeConverterExtension");

            debug(COMPONENT, "Registered XML");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register XML", e);
        }
    }

    /**
     * Registers HTML and XHTML language support.
     *
     * <p>HTML extends XMLLanguage, so XML must be registered first. XHTML is
     * a variant of HTML that follows stricter XML rules.</p>
     */
    private static void registerHtml() {
        try {
            // HTML uses XML's AST factory but has its own parser and formatter
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(HTMLLanguage.INSTANCE, new HTMLParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(HTMLLanguage.INSTANCE, new HtmlFormattingModelBuilder());

            // XHTML follows HTML structure but with XML-strict parsing
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(XHTMLLanguage.INSTANCE, new XHTMLParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(XHTMLLanguage.INSTANCE, new XhtmlFormattingModelBuilder());

            debug(COMPONENT, "Registered HTML/XHTML");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register HTML", e);
        }
    }

    /**
     * Registers JSON language support.
     *
     * <p>JSON requires multiple element type converters in IDEA 2025.x because
     * it has both file-level and element-level type conversions.</p>
     */
    private static void registerJson() {
        try {
            var jsonLanguage = JsonLanguage.INSTANCE;
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(jsonLanguage, new JsonParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(jsonLanguage, new JsonFormattingBuilderModel());

            // JSON needs syntax definition for IDEA 2025.x
            tryRegisterSyntaxDefinition(jsonLanguage, "com.intellij.json.JsonLanguageDefinition");
            // Try both file type and element type converters
            tryRegisterElementTypeConverter(jsonLanguage, "com.intellij.json.JsonFileTypeConverterFactory");
            tryRegisterElementTypeConverter(jsonLanguage, "com.intellij.json.psi.JsonElementTypeConverterFactory");

            debug(COMPONENT, "Registered JSON");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register JSON", e);
        }
    }

    /**
     * Registers Groovy language support.
     *
     * <p>Groovy is used for both .groovy files and Gradle build scripts (.gradle).
     * It doesn't need special IDEA 2025.x converters as it uses traditional parsing.</p>
     */
    private static void registerGroovy() {
        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(GroovyLanguage.INSTANCE, new GroovyParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(GroovyLanguage.INSTANCE, new GroovyFormattingModelBuilder());
            debug(COMPONENT, "Registered Groovy");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Groovy", e);
        }
    }

    /**
     * Registers Properties file language support.
     *
     * <p>Properties files (.properties) are simple key=value configuration files
     * commonly used in Java applications.</p>
     */
    private static void registerProperties() {
        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(PropertiesLanguage.INSTANCE, new PropertiesParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(PropertiesLanguage.INSTANCE, new PropertiesFormattingModelBuilder());
            debug(COMPONENT, "Registered Properties");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Properties", e);
        }
    }

    /**
     * Registers YAML language support.
     *
     * <p>YAML is used for configuration files and data serialization.
     * It has strict indentation rules that the formatter enforces.</p>
     */
    private static void registerYaml() {
        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(YAMLLanguage.INSTANCE, new YAMLParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(YAMLLanguage.INSTANCE, new YAMLFormattingModelBuilder());
            debug(COMPONENT, "Registered YAML");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register YAML", e);
        }
    }

    /**
     * Registers Kotlin language support.
     *
     * <p>Kotlin requires additional extension point registrations beyond the basic
     * parser and formatter. It needs:</p>
     * <ul>
     *     <li>KotlinPreFormatProcessor - prepares code before formatting</li>
     *     <li>KotlinLanguageCodeStyleSettingsProvider - provides Kotlin-specific settings</li>
     *     <li>ElementTypeConverter - for IDEA 2025.x compatibility</li>
     * </ul>
     */
    private static void registerKotlin(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE, new KotlinParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE, new KotlinFormattingModelBuilder());

            // Register Kotlin-specific extensions via extension points
            tryRegisterElementTypeConverterGeneric(KotlinLanguage.INSTANCE);
            tryRegisterKotlinCodeStyleProvider(extensionArea, rootDisposable);
            tryRegisterKotlinPreFormatProcessor(extensionArea, rootDisposable);

            debug(COMPONENT, "Registered Kotlin");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Kotlin", e);
        }
    }

    /**
     * Registers Kotlin's pre-format processor for import optimization and other preprocessing.
     */
    private static void tryRegisterKotlinPreFormatProcessor(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        try {
            var processorClass = Class.forName("org.jetbrains.kotlin.idea.formatter.KotlinPreFormatProcessor");
            var processor = processorClass.getDeclaredConstructor().newInstance();

            var getEpMethod = extensionArea.getClass().getMethod("getExtensionPointIfRegistered", String.class);
            var ep = getEpMethod.invoke(extensionArea, "com.intellij.preFormatProcessor");
            if (ep != null) {
                var registerMethod = ep.getClass().getMethod("registerExtension", Object.class, Disposable.class);
                registerMethod.invoke(ep, processor, rootDisposable);
                debug(COMPONENT, "Registered KotlinPreFormatProcessor");
            }
        } catch (ClassNotFoundException e) {
            skipped(COMPONENT, "KotlinPreFormatProcessor", "class not found");
        } catch (Exception e) {
            skipped(COMPONENT, "KotlinPreFormatProcessor", e.getMessage());
        }
    }

    /**
     * Registers Kotlin's code style settings provider for language-specific formatting options.
     */
    private static void tryRegisterKotlinCodeStyleProvider(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        try {
            var providerClass = Class.forName("org.jetbrains.kotlin.idea.formatter.KotlinLanguageCodeStyleSettingsProvider");
            var provider = providerClass.getDeclaredConstructor().newInstance();

            var getEpMethod = extensionArea.getClass().getMethod("getExtensionPointIfRegistered", String.class);
            var ep = getEpMethod.invoke(extensionArea, "com.intellij.langCodeStyleSettingsProvider");
            if (ep != null) {
                var registerMethod = ep.getClass().getMethod("registerExtension", Object.class, Disposable.class);
                registerMethod.invoke(ep, provider, rootDisposable);
                debug(COMPONENT, "Registered KotlinLanguageCodeStyleSettingsProvider");
            }
        } catch (ClassNotFoundException e) {
            skipped(COMPONENT, "KotlinLanguageCodeStyleSettingsProvider", "class not found");
        } catch (Exception e) {
            skipped(COMPONENT, "KotlinLanguageCodeStyleSettingsProvider", e.getMessage());
        }
    }

    /**
     * Registers a generic ElementTypeConverter for languages without specialized converters.
     *
     * <p>Uses CommonElementTypeConverterFactory which handles common element types
     * like WHITE_SPACE and ERROR_ELEMENT that are shared across languages.</p>
     */
    private static void tryRegisterElementTypeConverterGeneric(Language language) {
        try {
            var convertersClass = Class.forName("com.intellij.platform.syntax.psi.ElementTypeConverters");
            var getInstance = convertersClass.getDeclaredMethod("getInstance");
            var languageExtension = getInstance.invoke(null);
            var addMethod = languageExtension.getClass().getMethod("addExplicitExtension", Language.class, Object.class);

            var factoryClass = Class.forName("com.intellij.platform.syntax.psi.CommonElementTypeConverterFactory");
            var factoryInstance = factoryClass.getDeclaredConstructor().newInstance();
            addMethod.invoke(languageExtension, language, factoryInstance);
        } catch (ClassNotFoundException e) {
            // Expected in pre-2025.x IDEA versions
            skipped(COMPONENT, "ElementTypeConverter for " + language.getID(), "IDEA 2025.x API not available");
        } catch (Exception e) {
            skipped(COMPONENT, "ElementTypeConverter for " + language.getID(), e.getMessage());
        }
    }

    /**
     * Registers a SyntaxDefinition for IDEA 2025.x syntax-based parsing.
     *
     * <p>SyntaxDefinition defines the lexer and token types for a language in the
     * new syntax-based parsing system introduced in IDEA 2025.x.</p>
     */
    private static void tryRegisterSyntaxDefinition(Language language, String className) {
        try {
            var definitionsClass = Class.forName("com.intellij.platform.syntax.psi.LanguageSyntaxDefinitions");
            var getInstance = definitionsClass.getDeclaredMethod("getINSTANCE");
            var syntaxDefinitions = getInstance.invoke(null);

            var syntaxDefClass = Class.forName(className);
            var syntaxDefInstance = syntaxDefClass.getDeclaredConstructor().newInstance();

            var addMethod = syntaxDefinitions.getClass().getMethod("addExplicitExtension", Language.class, Object.class);
            addMethod.invoke(syntaxDefinitions, language, syntaxDefInstance);
        } catch (ClassNotFoundException e) {
            // Expected in pre-2025.x IDEA versions
            skipped(COMPONENT, "SyntaxDefinition for " + language.getID(), "IDEA 2025.x API not available");
        } catch (Exception e) {
            skipped(COMPONENT, "SyntaxDefinition for " + language.getID(), e.getMessage());
        }
    }

    /**
     * Registers an ElementTypeConverter for IDEA 2025.x element type conversion.
     *
     * <p>ElementTypeConverter bridges between the traditional IElementType system
     * and the new SyntaxElementType system. We create a composite converter that
     * combines the language-specific converter with CommonElementTypeConverter
     * for fallback handling of common types.</p>
     */
    private static void tryRegisterElementTypeConverter(Language language, String className) {
        try {
            var convertersClass = Class.forName("com.intellij.platform.syntax.psi.ElementTypeConverters");
            var getInstance = convertersClass.getDeclaredMethod("getInstance");
            var converters = getInstance.invoke(null);

            // Get language-specific converter
            var converterFactoryClass = Class.forName(className);
            var languageFactory = converterFactoryClass.getDeclaredConstructor().newInstance();
            var languageGetConverterMethod = converterFactoryClass.getMethod("getElementTypeConverter");
            var languageConverter = languageGetConverterMethod.invoke(languageFactory);

            // Get common converter for fallback (handles WHITE_SPACE, ERROR_ELEMENT, etc.)
            var commonFactoryClass = Class.forName("com.intellij.platform.syntax.psi.CommonElementTypeConverterFactory");
            var commonFactory = commonFactoryClass.getDeclaredConstructor().newInstance();
            var commonGetConverterMethod = commonFactoryClass.getMethod("getElementTypeConverter");
            var commonConverter = commonGetConverterMethod.invoke(commonFactory);

            // Combine converters: try language-specific first, then fall back to common
            var compositeClass = Class.forName("com.intellij.platform.syntax.psi.CompositeElementTypeConverter");
            var compositeConstructor = compositeClass.getConstructor(List.class);
            compositeConstructor.setAccessible(true);
            var compositeConverter = compositeConstructor.newInstance(List.of(languageConverter, commonConverter));

            // Wrap in a factory interface using dynamic proxy
            var factoryClass = Class.forName("com.intellij.platform.syntax.psi.ElementTypeConverterFactory");
            InvocationHandler handler = (proxy, method, args) -> {
                if ("getElementTypeConverter".equals(method.getName())) {
                    return compositeConverter;
                }
                return null;
            };
            var factoryProxy = Proxy.newProxyInstance(factoryClass.getClassLoader(), new Class<?>[]{factoryClass}, handler);

            var addMethod = converters.getClass().getMethod("addExplicitExtension", Language.class, Object.class);
            addMethod.invoke(converters, language, factoryProxy);
        } catch (ClassNotFoundException e) {
            // Expected in pre-2025.x IDEA versions or when the specific converter doesn't exist
            skipped(COMPONENT, "ElementTypeConverter for " + language.getID(), "class not found");
        } catch (Exception e) {
            // Fall back to simple registration without composite
            tryRegisterElementTypeConverterSimple(language, className);
        }
    }

    /**
     * Fallback registration of ElementTypeConverter without composite wrapping.
     */
    private static void tryRegisterElementTypeConverterSimple(Language language, String className) {
        try {
            var convertersClass = Class.forName("com.intellij.platform.syntax.psi.ElementTypeConverters");
            var getInstance = convertersClass.getDeclaredMethod("getInstance");
            var converters = getInstance.invoke(null);

            var converterFactoryClass = Class.forName(className);
            var languageFactory = converterFactoryClass.getDeclaredConstructor().newInstance();

            var addMethod = converters.getClass().getMethod("addExplicitExtension", Language.class, Object.class);
            addMethod.invoke(converters, language, languageFactory);
        } catch (Exception e) {
            skipped(COMPONENT, "ElementTypeConverter (simple) for " + language.getID(), e.getMessage());
        }
    }
}
