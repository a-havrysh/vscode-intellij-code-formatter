package com.intellij.formatter.bootstrap;

import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import lombok.experimental.UtilityClass;

import static com.intellij.formatter.bootstrap.BootstrapLogger.debug;

/**
 * Bootstrap class for initializing a minimal IntelliJ Platform environment for standalone formatting.
 *
 * <p>This class sets up all necessary services, extension points, and language support
 * required to use IntelliJ's code formatting engine without a full IDE installation.
 * Languages are loaded lazily based on file type to improve startup performance.</p>
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
     */
    static {
        Logger.setFactory(category -> new SilentLogger());
    }

    /**
     * Initializes the IntelliJ Platform environment for standalone formatting.
     *
     * <p>This method is thread-safe and idempotent. Languages are NOT loaded during
     * initialization - they are loaded lazily when first needed.</p>
     *
     * @throws RuntimeException if initialization fails
     */
    public static synchronized void initialize() {
        if (initialized) {
            debug(COMPONENT, "Already initialized, skipping");
            return;
        }

        debug(COMPONENT, "Starting initialization");

        try {
            setupSystemProperties();

            rootDisposable = Disposer.newDisposable("StandaloneFormatter");

            application = HeadlessMockApplication.create(rootDisposable);
            ExtensionPointsRegistrar.registerApplicationExtensionPoints(application.getExtensionArea());
            ServicesRegistrar.registerApplicationServices(application);

            project = new MockProject(application.getPicoContainer(), rootDisposable);
            ExtensionPointsRegistrar.registerProjectExtensionPoints(project);
            ServicesRegistrar.registerProjectServices(project);

            // NOTE: Languages are NOT registered here anymore.
            // They are loaded lazily via ensureLanguageRegistered() when formatting.

            initialized = true;
            debug(COMPONENT, "Initialization complete (languages will be loaded lazily)");
        } catch (Exception e) {
            debug(COMPONENT, "Initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize standalone formatter", e);
        }
    }

    /**
     * Ensures that the language for the given file is registered.
     * This method should be called before formatting a file.
     *
     * @param fileName the file name to determine which language to load
     */
    public static synchronized void ensureLanguageRegistered(String fileName) {
        ensureInitialized();

        var group = LanguageExtensionsRegistrar.getLanguageGroupForFile(fileName);
        if (group != null && !LanguageExtensionsRegistrar.isLanguageRegistered(group)) {
            debug(COMPONENT, "Loading language for: " + fileName);
            LanguageExtensionsRegistrar.registerLanguageGroup(
                    group,
                    application.getExtensionArea(),
                    rootDisposable
            );
        }
    }

    /**
     * Returns the extension area for language registration.
     * Used internally by LanguageExtensionsRegistrar.
     */
    public static ExtensionsAreaImpl getExtensionArea() {
        ensureInitialized();
        return application.getExtensionArea();
    }

    /**
     * Returns the root disposable.
     * Used internally for registering extensions with proper lifecycle.
     */
    public static Disposable getRootDisposable() {
        ensureInitialized();
        return rootDisposable;
    }

    /**
     * Returns the mock project instance used for formatting operations.
     *
     * @return the mock project
     * @throws IllegalStateException if not initialized
     */
    public static Project getProject() {
        ensureInitialized();
        return project;
    }

    /**
     * Returns the mock application instance.
     *
     * @return the mock application
     * @throws IllegalStateException if not initialized
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
     */
    public static synchronized void shutdown() {
        if (rootDisposable != null) {
            debug(COMPONENT, "Shutting down");
            Disposer.dispose(rootDisposable);
            rootDisposable = null;
            application = null;
            project = null;
            initialized = false;
            LanguageExtensionsRegistrar.reset();
            debug(COMPONENT, "Shutdown complete");
        }
    }

    /**
     * Configures system properties required for headless IntelliJ operation.
     */
    private static void setupSystemProperties() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("idea.is.internal", "false");
        System.setProperty("idea.is.unit.test", "false");
        System.setProperty("idea.use.native.fs.for.win", "false");
        System.setProperty("idea.ProcessCanceledException", "disabled");

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
