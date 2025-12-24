package com.intellij.formatter.bootstrap;

import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;

/**
 * A headless mock application for running IntelliJ Platform services without a GUI.
 *
 * <p>This class extends {@link MockApplication} to provide a minimal application
 * environment suitable for headless operation. It's designed for CLI tools that
 * need IntelliJ's formatting engine but don't have a GUI.</p>
 *
 * <h2>Why a Mock Application?</h2>
 * <p>IntelliJ Platform requires an Application instance to be registered in
 * {@link ApplicationManager}. Many services use {@code ApplicationManager.getApplication()}
 * to access the application, and fail with NPE if it's not set.</p>
 *
 * <h2>Thread Model</h2>
 * <p>IntelliJ uses a complex threading model with:</p>
 * <ul>
 *     <li>EDT (Event Dispatch Thread) - for UI and write operations</li>
 *     <li>Read Actions - can run on any thread</li>
 *     <li>Write Actions - must run on EDT</li>
 * </ul>
 *
 * <p>In headless mode, we pretend all threads are EDT to avoid thread assertion
 * failures. This is safe because:</p>
 * <ul>
 *     <li>We control all code execution (no concurrent UI)</li>
 *     <li>Formatting is typically single-threaded</li>
 *     <li>We don't need EDT-specific behavior (like Swing repaint)</li>
 * </ul>
 *
 * @see FormatterBootstrap
 * @see MockApplication
 */
public final class HeadlessMockApplication extends MockApplication {

    /**
     * Private constructor - use {@link #create(Disposable)} factory method.
     *
     * @param parentDisposable the parent disposable for lifecycle management
     */
    private HeadlessMockApplication(Disposable parentDisposable) {
        super(parentDisposable);
    }

    /**
     * Always returns {@code true} since we're running in headless mode.
     *
     * <p>Many IntelliJ operations check this before proceeding with write actions.
     * By always returning true, we allow formatting to proceed on any thread.</p>
     *
     * @return always {@code true}
     */
    @Override
    public boolean isDispatchThread() {
        // In headless mode, treat all threads as EDT to allow write operations
        return true;
    }

    /**
     * No-op implementation since all threads are considered dispatch threads in headless mode.
     *
     * <p>This prevents {@code AssertionError} when IntelliJ code tries to verify
     * it's running on EDT before performing a write operation.</p>
     */
    @Override
    public void assertIsDispatchThread() {
        // Skip assertion in headless mode - all threads are treated as EDT
    }

    /**
     * No-op implementation for headless mode.
     *
     * <p>Some IntelliJ code asserts it's NOT on EDT for long-running operations.
     * In headless mode, we don't have this distinction.</p>
     */
    @Override
    public void assertIsNonDispatchThread() {
        // Skip assertion in headless mode - thread type distinction not needed
    }

    /**
     * Creates a new headless mock application and registers it with {@link ApplicationManager}.
     *
     * <p>This factory method ensures the application is properly registered as the
     * global application instance. The registration is tied to the disposable -
     * when disposed, the application is unregistered.</p>
     *
     * @param parentDisposable the parent disposable for lifecycle management;
     *                         disposing it will unregister the application
     * @return the created application instance
     */
    public static HeadlessMockApplication create(Disposable parentDisposable) {
        var app = new HeadlessMockApplication(parentDisposable);
        // Register as the global application - services will use this to look up other services
        ApplicationManager.setApplication(app, parentDisposable);
        return app;
    }
}
