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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.formatter.bootstrap.BootstrapLogger.debug;
import static com.intellij.formatter.bootstrap.BootstrapLogger.skipped;
import static com.intellij.formatter.bootstrap.BootstrapLogger.warn;

/**
 * Registrar for language-specific parser definitions, AST factories, and formatting builders.
 * Supports lazy loading of languages based on file extension.
 */
@UtilityClass
public class LanguageExtensionsRegistrar {

    private static final String COMPONENT = "Languages";

    /** Set of already registered language IDs for lazy loading */
    private static final Set<String> registeredLanguages = new HashSet<>();

    /** Flag indicating if core formatting service is registered */
    private static boolean formattingServiceRegistered = false;

    /**
     * Language groups for dependency management.
     * Some languages depend on others (e.g., HTML depends on XML).
     */
    public enum LanguageGroup {
        JAVA,
        KOTLIN,
        GROOVY,
        XML,      // Also registers HTML/XHTML
        JSON,
        YAML,
        PROPERTIES
    }

    /**
     * Registers all language extensions for supported file types.
     * @deprecated Use {@link #registerLanguageForFile(String, ExtensionsAreaImpl, Disposable)} for lazy loading
     */
    @Deprecated
    public static void registerAll(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        debug(COMPONENT, "Registering all language extensions");

        registerFormattingService(extensionArea, rootDisposable);

        registerJava();
        registerXml();
        registerHtml();
        registerJson();
        registerGroovy();
        registerProperties();
        registerYaml();
        registerKotlin(extensionArea, rootDisposable);

        debug(COMPONENT, "All language extensions registered");
    }

    /**
     * Registers only the language needed for the given file.
     * This is the preferred method for lazy loading.
     *
     * @param fileName the file name to determine language
     * @param extensionArea the application extension area
     * @param rootDisposable the root disposable
     * @return true if a language was registered, false if already registered or unsupported
     */
    public static synchronized boolean registerLanguageForFile(String fileName,
                                                               ExtensionsAreaImpl extensionArea,
                                                               Disposable rootDisposable) {
        var group = getLanguageGroupForFile(fileName);
        if (group == null) {
            debug(COMPONENT, "No language group for file: " + fileName);
            return false;
        }

        return registerLanguageGroup(group, extensionArea, rootDisposable);
    }

    /**
     * Registers a specific language group with its dependencies.
     */
    public static synchronized boolean registerLanguageGroup(LanguageGroup group,
                                                             ExtensionsAreaImpl extensionArea,
                                                             Disposable rootDisposable) {
        // Always ensure formatting service is registered first
        if (!formattingServiceRegistered) {
            registerFormattingService(extensionArea, rootDisposable);
        }

        String groupId = group.name();
        if (registeredLanguages.contains(groupId)) {
            debug(COMPONENT, "Language already registered: " + groupId);
            return false;
        }

        debug(COMPONENT, "Registering language group: " + groupId);

        switch (group) {
            case JAVA -> registerJava();
            case KOTLIN -> {
                // Kotlin may need Java for some type resolution
                if (!registeredLanguages.contains(LanguageGroup.JAVA.name())) {
                    registerJava();
                    registeredLanguages.add(LanguageGroup.JAVA.name());
                }
                registerKotlin(extensionArea, rootDisposable);
            }
            case GROOVY -> registerGroovy();
            case XML -> {
                registerXml();
                registerHtml(); // HTML depends on XML
            }
            case JSON -> registerJson();
            case YAML -> registerYaml();
            case PROPERTIES -> registerProperties();
        }

        registeredLanguages.add(groupId);
        debug(COMPONENT, "Language group registered: " + groupId);
        return true;
    }

    /**
     * Determines the language group for a given file name.
     *
     * @param fileName the file name
     * @return the language group, or null if unsupported
     */
    public static LanguageGroup getLanguageGroupForFile(String fileName) {
        if (fileName == null) return null;

        String lowerName = fileName.toLowerCase();

        // Check specific patterns first
        if (lowerName.endsWith(".gradle.kts")) {
            return LanguageGroup.KOTLIN;
        }

        // Extract extension
        int dotIndex = lowerName.lastIndexOf('.');
        if (dotIndex < 0) return null;

        String ext = lowerName.substring(dotIndex + 1);

        return switch (ext) {
            case "java" -> LanguageGroup.JAVA;
            case "kt", "kts" -> LanguageGroup.KOTLIN;
            case "groovy", "gradle" -> LanguageGroup.GROOVY;
            case "xml", "xsd", "xsl", "xslt", "wsdl", "fxml", "pom" -> LanguageGroup.XML;
            case "html", "htm", "xhtml" -> LanguageGroup.XML; // HTML needs XML
            case "json" -> LanguageGroup.JSON;
            case "yaml", "yml" -> LanguageGroup.YAML;
            case "properties" -> LanguageGroup.PROPERTIES;
            default -> null;
        };
    }

    /**
     * Checks if a language group is already registered.
     */
    public static boolean isLanguageRegistered(LanguageGroup group) {
        return registeredLanguages.contains(group.name());
    }

    /**
     * Registers CoreFormattingService as the default formatting implementation.
     */
    private static void registerFormattingService(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        if (formattingServiceRegistered) return;

        try {
            ExtensionPoint<FormattingService> ep = extensionArea.getExtensionPoint("com.intellij.formattingService");
            ep.registerExtension(new CoreFormattingService(), rootDisposable);
            formattingServiceRegistered = true;
            debug(COMPONENT, "Registered CoreFormattingService");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register CoreFormattingService", e);
        }
    }

    /**
     * Registers Java language support.
     */
    @SuppressWarnings("deprecation")
    private static void registerJava() {
        if (registeredLanguages.contains("JAVA_INTERNAL")) return;

        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, new JavaParserDefinition());
            LanguageASTFactory.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, new JavaASTFactory());
            LanguageFormatting.INSTANCE.addExplicitExtension(JavaLanguage.INSTANCE, new JavaFormattingModelBuilder());

            tryRegisterSyntaxDefinition(JavaLanguage.INSTANCE,
                    "com.intellij.java.frontback.psi.impl.syntax.JavaSyntaxDefinitionExtension");
            tryRegisterElementTypeConverter(JavaLanguage.INSTANCE,
                    "com.intellij.lang.java.syntax.JavaElementTypeConverterExtension");

            registeredLanguages.add("JAVA_INTERNAL");
            debug(COMPONENT, "Registered Java");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Java", e);
        }
    }

    /**
     * Registers XML language support.
     */
    private static void registerXml() {
        if (registeredLanguages.contains("XML_INTERNAL")) return;

        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(XMLLanguage.INSTANCE, new XMLParserDefinition());
            LanguageASTFactory.INSTANCE.addExplicitExtension(XMLLanguage.INSTANCE, new XmlASTFactory());
            LanguageFormatting.INSTANCE.addExplicitExtension(XMLLanguage.INSTANCE, new XmlFormattingModelBuilder());

            tryRegisterSyntaxDefinition(XMLLanguage.INSTANCE,
                    "com.intellij.lang.xml.XmlSyntaxDefinitionExtension");
            tryRegisterElementTypeConverter(XMLLanguage.INSTANCE,
                    "com.intellij.psi.xml.XmlElementTypeConverterExtension");

            registeredLanguages.add("XML_INTERNAL");
            debug(COMPONENT, "Registered XML");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register XML", e);
        }
    }

    /**
     * Registers HTML and XHTML language support.
     */
    private static void registerHtml() {
        if (registeredLanguages.contains("HTML_INTERNAL")) return;

        // HTML depends on XML
        registerXml();

        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(HTMLLanguage.INSTANCE, new HTMLParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(HTMLLanguage.INSTANCE, new HtmlFormattingModelBuilder());

            LanguageParserDefinitions.INSTANCE.addExplicitExtension(XHTMLLanguage.INSTANCE, new XHTMLParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(XHTMLLanguage.INSTANCE, new XhtmlFormattingModelBuilder());

            registeredLanguages.add("HTML_INTERNAL");
            debug(COMPONENT, "Registered HTML/XHTML");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register HTML", e);
        }
    }

    /**
     * Registers JSON language support.
     */
    private static void registerJson() {
        if (registeredLanguages.contains("JSON_INTERNAL")) return;

        try {
            var jsonLanguage = JsonLanguage.INSTANCE;
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(jsonLanguage, new JsonParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(jsonLanguage, new JsonFormattingBuilderModel());

            tryRegisterSyntaxDefinition(jsonLanguage, "com.intellij.json.JsonLanguageDefinition");
            tryRegisterElementTypeConverter(jsonLanguage, "com.intellij.json.JsonFileTypeConverterFactory");
            tryRegisterElementTypeConverter(jsonLanguage, "com.intellij.json.psi.JsonElementTypeConverterFactory");

            registeredLanguages.add("JSON_INTERNAL");
            debug(COMPONENT, "Registered JSON");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register JSON", e);
        }
    }

    /**
     * Registers Groovy language support.
     */
    private static void registerGroovy() {
        if (registeredLanguages.contains("GROOVY_INTERNAL")) return;

        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(GroovyLanguage.INSTANCE, new GroovyParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(GroovyLanguage.INSTANCE, new GroovyFormattingModelBuilder());

            registeredLanguages.add("GROOVY_INTERNAL");
            debug(COMPONENT, "Registered Groovy");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Groovy", e);
        }
    }

    /**
     * Registers Properties file language support.
     */
    private static void registerProperties() {
        if (registeredLanguages.contains("PROPERTIES_INTERNAL")) return;

        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(PropertiesLanguage.INSTANCE, new PropertiesParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(PropertiesLanguage.INSTANCE, new PropertiesFormattingModelBuilder());

            registeredLanguages.add("PROPERTIES_INTERNAL");
            debug(COMPONENT, "Registered Properties");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Properties", e);
        }
    }

    /**
     * Registers YAML language support.
     */
    private static void registerYaml() {
        if (registeredLanguages.contains("YAML_INTERNAL")) return;

        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(YAMLLanguage.INSTANCE, new YAMLParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(YAMLLanguage.INSTANCE, new YAMLFormattingModelBuilder());

            registeredLanguages.add("YAML_INTERNAL");
            debug(COMPONENT, "Registered YAML");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register YAML", e);
        }
    }

    /**
     * Registers Kotlin language support.
     */
    private static void registerKotlin(ExtensionsAreaImpl extensionArea, Disposable rootDisposable) {
        if (registeredLanguages.contains("KOTLIN_INTERNAL")) return;

        try {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE, new KotlinParserDefinition());
            LanguageFormatting.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE, new KotlinFormattingModelBuilder());

            tryRegisterElementTypeConverterGeneric(KotlinLanguage.INSTANCE);
            tryRegisterKotlinCodeStyleProvider(extensionArea, rootDisposable);
            tryRegisterKotlinPreFormatProcessor(extensionArea, rootDisposable);

            registeredLanguages.add("KOTLIN_INTERNAL");
            debug(COMPONENT, "Registered Kotlin");
        } catch (Exception e) {
            warn(COMPONENT, "Failed to register Kotlin", e);
        }
    }

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
            skipped(COMPONENT, "ElementTypeConverter for " + language.getID(), "IDEA 2025.x API not available");
        } catch (Exception e) {
            skipped(COMPONENT, "ElementTypeConverter for " + language.getID(), e.getMessage());
        }
    }

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
            skipped(COMPONENT, "SyntaxDefinition for " + language.getID(), "IDEA 2025.x API not available");
        } catch (Exception e) {
            skipped(COMPONENT, "SyntaxDefinition for " + language.getID(), e.getMessage());
        }
    }

    private static void tryRegisterElementTypeConverter(Language language, String className) {
        try {
            var convertersClass = Class.forName("com.intellij.platform.syntax.psi.ElementTypeConverters");
            var getInstance = convertersClass.getDeclaredMethod("getInstance");
            var converters = getInstance.invoke(null);

            var converterFactoryClass = Class.forName(className);
            var languageFactory = converterFactoryClass.getDeclaredConstructor().newInstance();
            var languageGetConverterMethod = converterFactoryClass.getMethod("getElementTypeConverter");
            var languageConverter = languageGetConverterMethod.invoke(languageFactory);

            var commonFactoryClass = Class.forName("com.intellij.platform.syntax.psi.CommonElementTypeConverterFactory");
            var commonFactory = commonFactoryClass.getDeclaredConstructor().newInstance();
            var commonGetConverterMethod = commonFactoryClass.getMethod("getElementTypeConverter");
            var commonConverter = commonGetConverterMethod.invoke(commonFactory);

            var compositeClass = Class.forName("com.intellij.platform.syntax.psi.CompositeElementTypeConverter");
            var compositeConstructor = compositeClass.getConstructor(List.class);
            compositeConstructor.setAccessible(true);
            var compositeConverter = compositeConstructor.newInstance(List.of(languageConverter, commonConverter));

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
            skipped(COMPONENT, "ElementTypeConverter for " + language.getID(), "class not found");
        } catch (Exception e) {
            tryRegisterElementTypeConverterSimple(language, className);
        }
    }

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

    /**
     * Resets the registrar state. Used for testing.
     */
    public static synchronized void reset() {
        registeredLanguages.clear();
        formattingServiceRegistered = false;
    }
}
