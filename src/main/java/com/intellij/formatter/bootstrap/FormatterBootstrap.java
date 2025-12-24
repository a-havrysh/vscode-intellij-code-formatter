package com.intellij.formatter.bootstrap;

import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import lombok.experimental.UtilityClass;

import static com.intellij.formatter.bootstrap.BootstrapLogger.debug;

/**
 * Bootstrap class for initializing a minimal IntelliJ Platform environment for standalone formatting.
 *
 * <p>This class sets up all necessary services, extension points, and language support
 * required to use IntelliJ's code formatting engine without a full IDE installation.
 * It creates a headless mock application with the essential services for PSI parsing
 * and code style management.</p>
 *
 * <h2>Architecture Overview</h2>
 * <p>The IntelliJ Platform is designed as a monolithic IDE, but code formatting
 * only needs a subset of its functionality:</p>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    FormatterBootstrap                        │
 * │  ┌──────────────────┐  ┌──────────────────────────────────┐ │
 * │  │ MockApplication  │  │          MockProject             │ │
 * │  │                  │  │                                  │ │
 * │  │ - FileTypeManager│  │ - PsiManager                    │ │
 * │  │ - PsiBuilderFact │  │ - CodeStyleManager              │ │
 * │  │ - CodeStyleSett..│  │ - PsiDocumentManager            │ │
 * │  └──────────────────┘  └──────────────────────────────────┘ │
 * │                                                             │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │              LanguageExtensionsRegistrar              │  │
 * │  │  Java | Kotlin | Groovy | XML | HTML | JSON | YAML   │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Initialize once at application startup
 * FormatterBootstrap.initialize();
 *
 * // Get the project for service access
 * Project project = FormatterBootstrap.getProject();
 *
 * // Format code using StandaloneFormatter
 * String formatted = StandaloneFormatter.formatCode(code, "MyClass.java");
 *
 * // Clean up on application shutdown (optional)
 * FormatterBootstrap.shutdown();
 * }</pre>
 *
 * <h2>Debugging</h2>
 * <p>Enable debug logging with {@code -Dformatter.debug=true} to see detailed
 * information about service registration and language extension loading.</p>
 *
 * @see com.intellij.formatter.core.StandaloneFormatter
 * @see HeadlessMockApplication
 * @see ServicesRegistrar
 * @see LanguageExtensionsRegistrar
 */
@UtilityClass
public class FormatterBootstrap {

    private static final String COMPONENT = "Bootstrap";

    /** Flag indicating whether the bootstrap has been initialized. */
    private static volatile boolean initialized = false;

    /** Root disposable for managing lifecycle of all created resources. */
    private static Disposable rootDisposable;

    /** The mock application instance. */
    private static HeadlessMockApplication application;

    /** The mock project instance. */
    private static MockProject project;

    /*
     * Static initializer: Configure IntelliJ's internal logger to use our silent implementation.
     * This prevents log spam from IntelliJ internals during formatting operations.
     */
    static {
        Logger.setFactory(category -> new SilentLogger());
    }

    /**
     * Initializes the IntelliJ Platform environment for standalone formatting.
     *
     * <p>This method is thread-safe and idempotent. Multiple calls have no effect
     * once initialization is complete.</p>
     *
     * <h3>Initialization Steps</h3>
     * <ol>
     *     <li>Set system properties for headless operation</li>
     *     <li>Create root disposable for resource management</li>
     *     <li>Create mock application with required services</li>
     *     <li>Create mock project with required services</li>
     *     <li>Register language extensions (parsers, formatters)</li>
     * </ol>
     *
     * @throws RuntimeException if initialization fails due to missing dependencies or configuration errors
     */
    public static synchronized void initialize() {
        if (initialized) {
            debug(COMPONENT, "Already initialized, skipping");
            return;
        }

        debug(COMPONENT, "Starting initialization");

        try {
            // System properties must be set before creating application
            setupSystemProperties();

            // Create root disposable - disposing it will clean up everything
            rootDisposable = Disposer.newDisposable("StandaloneFormatter");

            // Create application and register application-level services
            application = HeadlessMockApplication.create(rootDisposable);
            ExtensionPointsRegistrar.registerApplicationExtensionPoints(application.getExtensionArea());
            ServicesRegistrar.registerApplicationServices(application);

            // Create project and register project-level services
            project = new MockProject(application.getPicoContainer(), rootDisposable);
            ExtensionPointsRegistrar.registerProjectExtensionPoints(project);
            ServicesRegistrar.registerProjectServices(project);

            // Register language support (parsers, formatters, etc.)
            LanguageExtensionsRegistrar.registerAll(application.getExtensionArea(), rootDisposable);

            initialized = true;
            debug(COMPONENT, "Initialization complete");
        } catch (Exception e) {
            debug(COMPONENT, "Initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize standalone formatter", e);
        }
    }

    /**
     * Returns the mock project instance used for formatting operations.
     *
     * <p>The project provides access to project-level services like
     * {@code CodeStyleManager} and {@code PsiManager}.</p>
     *
     * @return the mock project
     * @throws IllegalStateException if {@link #initialize()} has not been called
     */
    public static Project getProject() {
        ensureInitialized();
        return project;
    }

    /**
     * Returns the mock application instance.
     *
     * <p>The application provides access to application-level services like
     * {@code FileTypeManager} and {@code PsiBuilderFactory}.</p>
     *
     * @return the mock application
     * @throws IllegalStateException if {@link #initialize()} has not been called
     */
    public static HeadlessMockApplication getApplication() {
        ensureInitialized();
        return application;
    }

    /**
     * Checks whether the bootstrap has been initialized.
     *
     * @return {@code true} if initialized, {@code false} otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Shuts down the IntelliJ Platform environment and releases all resources.
     *
     * <p>This method disposes all services and clears references. After calling
     * this method, {@link #initialize()} must be called again before using
     * any formatting functionality.</p>
     *
     * <p>Note: In most CLI applications, calling shutdown is optional as the
     * JVM exit will clean up resources anyway. It's useful for long-running
     * applications that want to release memory.</p>
     */
    public static synchronized void shutdown() {
        if (rootDisposable != null) {
            debug(COMPONENT, "Shutting down");
            Disposer.dispose(rootDisposable);
            rootDisposable = null;
            application = null;
            project = null;
            initialized = false;
            debug(COMPONENT, "Shutdown complete");
        }
    }

    /**
     * Configures system properties required for headless IntelliJ operation.
     *
     * <p>These properties disable GUI-related features and configure IntelliJ
     * to run in a non-interactive mode.</p>
     */
    private static void setupSystemProperties() {
        // Run without GUI (no Swing/AWT windows)
        System.setProperty("java.awt.headless", "true");

        // Disable internal features that require IDE infrastructure
        System.setProperty("idea.is.internal", "false");
        System.setProperty("idea.is.unit.test", "false");

        // Disable Windows-specific native filesystem (not needed for formatting)
        System.setProperty("idea.use.native.fs.for.win", "false");

        // Disable ProcessCanceledException - we don't have cancellation UI
        System.setProperty("idea.ProcessCanceledException", "disabled");

        // Initialize registry keys for formatting behavior
        RegistryInitializer.initialize();
    }

    /**
     * Ensures that the bootstrap has been initialized.
     *
     * @throws IllegalStateException if not initialized
     */
    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("FormatterBootstrap not initialized. Call initialize() first.");
        }
    }
}
