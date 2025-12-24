package com.intellij.formatter.bootstrap;

import com.intellij.core.CoreJavaPsiImplementationHelper;
import com.intellij.formatter.services.SimpleTransferredWriteActionService;
import com.intellij.formatter.services.codestyle.CodeStyleSchemesService;
import com.intellij.formatter.services.codestyle.CodeStyleSettingsProvider;
import com.intellij.formatter.services.document.DocumentCommitService;
import com.intellij.formatter.services.document.DocumentManagerService;
import com.intellij.formatter.services.document.EncodingManagerService;
import com.intellij.formatter.services.filetype.FileTypeManagerService;
import com.intellij.formatter.services.formatting.FormattingProgressCallbackService;
import com.intellij.formatter.services.formatting.PomModelService;
import com.intellij.formatter.services.project.ProjectFileIndexService;
import com.intellij.formatter.services.project.ProjectRootManagerService;
import com.intellij.formatter.services.project.ResolveScopeService;
import com.intellij.formatter.services.psi.InjectedLanguageService;
import com.intellij.formatter.services.psi.JavaPsiFacadeService;
import com.intellij.formatter.services.psi.PsiDocumentService;
import com.intellij.formatting.Formatter;
import com.intellij.formatting.FormatterImpl;
import com.intellij.formatting.FormattingProgressCallbackFactory;
import com.intellij.lang.DefaultASTFactory;
import com.intellij.lang.DefaultASTFactoryImpl;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.impl.PsiBuilderFactoryImpl;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.mock.MockDumbService;
import com.intellij.mock.MockProject;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.application.TransactionGuardImpl;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.impl.CoreCommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.CoreProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.intellij.pom.PomModel;
import com.intellij.pom.tree.TreeAspect;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JvmPsiConversionHelper;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.codeStyle.AppCodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSchemes;
import com.intellij.psi.codeStyle.CodeStyleSettingsService;
import com.intellij.psi.codeStyle.ProjectCodeStyleSettingsManager;
import com.intellij.psi.impl.DocumentCommitProcessor;
import com.intellij.psi.impl.JavaPsiImplementationHelper;
import com.intellij.psi.impl.JvmPsiConversionHelperImpl;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.psi.impl.ResolveScopeManager;
import com.intellij.psi.impl.smartPointers.SmartPointerManagerImpl;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.search.ProjectScopeBuilder;
import com.intellij.psi.search.ProjectScopeBuilderImpl;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.CachedValuesManagerImpl;
import lombok.experimental.UtilityClass;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import static com.intellij.formatter.bootstrap.BootstrapLogger.debug;
import static com.intellij.formatter.bootstrap.BootstrapLogger.skipped;
import static com.intellij.formatter.bootstrap.BootstrapLogger.warn;

/**
 * Registrar for application and project level services required for standalone formatting.
 *
 * <p>IntelliJ Platform uses a service locator pattern where services are retrieved via
 * {@code ServiceManager.getService()}. For standalone formatting, we register minimal
 * implementations of these services that provide just enough functionality for the
 * formatting engine to work.</p>
 *
 * <h2>Service Categories</h2>
 *
 * <h3>Core Formatting Services</h3>
 * <ul>
 *     <li>{@link Formatter} - the actual formatting algorithm implementation</li>
 *     <li>{@link CodeStyleManager} - applies code style settings during formatting</li>
 *     <li>{@link FormattingProgressCallbackFactory} - progress reporting (no-op in standalone)</li>
 * </ul>
 *
 * <h3>PSI Infrastructure Services</h3>
 * <ul>
 *     <li>{@link PsiManager} - manages PSI trees for files</li>
 *     <li>{@link PsiBuilderFactory} - creates parsers for building PSI trees</li>
 *     <li>{@link PsiFileFactory} - creates PsiFile instances from text</li>
 *     <li>{@link PsiDocumentManager} - synchronizes PSI with documents</li>
 * </ul>
 *
 * <h3>Code Style Services</h3>
 * <ul>
 *     <li>{@link CodeStyleSettingsService} - provides code style settings factories</li>
 *     <li>{@link CodeStyleSchemes} - manages code style schemes</li>
 *     <li>{@link AppCodeStyleSettingsManager} - application-level style settings</li>
 *     <li>{@link ProjectCodeStyleSettingsManager} - project-level style settings</li>
 * </ul>
 *
 * <h2>Registration Order</h2>
 * <p>Application services must be registered before project services, as many project
 * services depend on application services during initialization.</p>
 *
 * @see FormatterBootstrap
 * @see ExtensionPointsRegistrar
 */
@UtilityClass
public class ServicesRegistrar {

    private static final String COMPONENT = "Services";

    /**
     * Registers all required application-level services.
     *
     * <p>Application services are singletons shared across all projects. They provide
     * infrastructure that doesn't depend on project context.</p>
     *
     * @param application the mock application instance
     */
    public static void registerApplicationServices(HeadlessMockApplication application) {
        debug(COMPONENT, "Registering application services");

        // Code style settings - factories for language-specific style configurations
        application.registerService(CodeStyleSettingsService.class, new CodeStyleSettingsProvider());
        tryRegisterAppCodeStyleSettingsManager(application);
        application.registerService(CodeStyleSchemes.class, new CodeStyleSchemesService());

        // Core formatting - the actual formatting engine
        application.registerService(Formatter.class, new FormatterImpl());
        application.registerService(FormattingProgressCallbackFactory.class, new FormattingProgressCallbackService());

        // File and document management
        application.registerService(FileTypeManager.class, new FileTypeManagerService());
        application.registerService(FileDocumentManager.class, new DocumentManagerService());
        application.registerService(EncodingManager.class, new EncodingManagerService());

        // PSI building infrastructure - required for parsing source code
        application.registerService(PsiBuilderFactory.class, new PsiBuilderFactoryImpl());
        application.registerService(DefaultASTFactory.class, new DefaultASTFactoryImpl());
        tryRegisterPsiSyntaxBuilderFactory(application);

        // Document commit - synchronizes document changes to PSI (no-op in standalone)
        application.registerService(DocumentCommitProcessor.class, new DocumentCommitService());

        // Command and transaction infrastructure - for write actions
        application.registerService(CommandProcessor.class, new CoreCommandProcessor());
        application.registerService(TransactionGuard.class, new TransactionGuardImpl());
        application.registerService(ProgressManager.class, new CoreProgressManager());

        // IDEA 2025.x compatibility services (may not exist in older versions)
        tryRegisterTransferredWriteActionService(application);
        tryRegisterReadActionCache(application);
        tryRegisterPluginProblemReporter(application);

        debug(COMPONENT, "Application services registered");
    }

    /**
     * Registers all required project-level services.
     *
     * <p>Project services are scoped to individual projects. They handle project-specific
     * functionality like resolve scopes and project file indexing.</p>
     *
     * @param project the mock project instance
     */
    public static void registerProjectServices(MockProject project) {
        debug(COMPONENT, "Registering project services");

        // PSI modification tracking - required for incremental updates
        project.registerService(PsiModificationTracker.class, new PsiModificationTrackerImpl(project));
        project.registerService(CachedValuesManager.class, new CachedValuesManagerImpl(project, null));

        // Core PSI services - manage PSI trees
        project.registerService(PsiManager.class, new PsiManagerImpl(project));
        project.registerService(PsiDocumentManager.class, new PsiDocumentService(project));
        project.registerService(SmartPointerManager.class, new SmartPointerManagerImpl(project));

        // Code style for this project
        project.registerService(CodeStyleManager.class, new CodeStyleManagerImpl(project));
        project.registerService(ProjectCodeStyleSettingsManager.class, new ProjectCodeStyleSettingsManager(project));

        // POM (Program Object Model) - for AST modifications during formatting
        project.registerService(TreeAspect.class, new TreeAspect());
        project.registerService(PomModel.class, new PomModelService(project));

        // Resolve infrastructure - not used for formatting but required by some services
        project.registerService(ResolveCache.class, new ResolveCache(project));
        project.registerService(ResolveScopeManager.class, new ResolveScopeService(project));
        project.registerService(ProjectScopeBuilder.class, new ProjectScopeBuilderImpl(project));

        // Project structure services (minimal stubs)
        project.registerService(ProjectRootManager.class, new ProjectRootManagerService());
        project.registerService(ProjectFileIndex.class, new ProjectFileIndexService());

        // Java-specific services
        project.registerService(JavaPsiImplementationHelper.class, new CoreJavaPsiImplementationHelper(project));
        project.registerService(JvmPsiConversionHelper.class, new JvmPsiConversionHelperImpl());
        project.registerService(JavaPsiFacade.class, new JavaPsiFacadeService(project));
        project.registerService(PsiElementFactory.class, new PsiElementFactoryImpl(project));
        project.registerService(PsiFileFactory.class, new PsiFileFactoryImpl(project));

        // Dumb mode service - we're never in "dumb" mode (indexing)
        project.registerService(DumbService.class, new MockDumbService(project));

        // Language injection support
        registerInjectedLanguageManager(project);

        // IDEA 2025.x multiverse support
        registerCodeInsightContextManager(project);

        debug(COMPONENT, "Project services registered");
    }

    private static void tryRegisterAppCodeStyleSettingsManager(HeadlessMockApplication application) {
        try {
            application.registerService(AppCodeStyleSettingsManager.class, new AppCodeStyleSettingsManager());
            debug(COMPONENT, "Registered AppCodeStyleSettingsManager");
        } catch (Exception e) {
            // May fail in some IDEA versions where this class has different constructor
            skipped(COMPONENT, "AppCodeStyleSettingsManager", e.getMessage());
        }
    }

    /**
     * Registers PsiSyntaxBuilderFactory for IDEA 2025.x compatibility.
     *
     * <p>IDEA 2025.x introduced a new syntax-based parsing infrastructure that works
     * alongside the traditional PSI parsing. This service bridges the two systems.</p>
     */
    private static void tryRegisterPsiSyntaxBuilderFactory(HeadlessMockApplication application) {
        tryRegisterService(application, "com.intellij.platform.syntax.psi.PsiSyntaxBuilderFactory",
                "com.intellij.platform.syntax.psi.PsiSyntaxBuilderFactoryImpl",
                "IDEA 2025.x syntax builder support");
    }

    private static void tryRegisterTransferredWriteActionService(HeadlessMockApplication application) {
        tryRegisterService(application, "com.intellij.util.concurrency.TransferredWriteActionService",
                SimpleTransferredWriteActionService.class,
                "Write action transfer support");
    }

    /**
     * Registers ReadActionCache for IDEA 2025.x.
     *
     * <p>ReadActionCache optimizes repeated read actions by caching results.
     * Not critical for formatting but prevents NPE in some code paths.</p>
     */
    private static void tryRegisterReadActionCache(HeadlessMockApplication application) {
        tryRegisterService(application, "com.intellij.psi.util.ReadActionCache",
                "com.intellij.openapi.application.impl.ReadActionCacheImpl",
                "Read action cache");
    }

    private static void tryRegisterPluginProblemReporter(HeadlessMockApplication application) {
        tryRegisterService(application, "com.intellij.diagnostic.PluginProblemReporter",
                "com.intellij.diagnostic.PluginProblemReporterImpl",
                "Plugin problem reporter");
    }

    @SuppressWarnings("unchecked")
    private static void tryRegisterService(HeadlessMockApplication application,
                                           String serviceClassName,
                                           String implClassName,
                                           String description) {
        try {
            var serviceClass = (Class<Object>) Class.forName(serviceClassName);
            var implClass = Class.forName(implClassName);
            var constructor = implClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            application.registerService(serviceClass, constructor.newInstance());
            debug(COMPONENT, "Registered " + description);
        } catch (ClassNotFoundException e) {
            // Expected - class doesn't exist in this IDEA version
            skipped(COMPONENT, description, "class not found in this IDEA version");
        } catch (Exception e) {
            skipped(COMPONENT, description, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void tryRegisterService(HeadlessMockApplication application,
                                           String serviceClassName,
                                           Class<?> implClass,
                                           String description) {
        try {
            var serviceClass = (Class<Object>) Class.forName(serviceClassName);
            application.registerService(serviceClass, implClass.getDeclaredConstructor().newInstance());
            debug(COMPONENT, "Registered " + description);
        } catch (ClassNotFoundException e) {
            skipped(COMPONENT, description, "class not found in this IDEA version");
        } catch (Exception e) {
            skipped(COMPONENT, description, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Registers InjectedLanguageManager for language injection support.
     *
     * <p>Language injection allows one language to be embedded in another
     * (e.g., SQL in Java strings). The real implementation is preferred,
     * but we fall back to a stub if it fails.</p>
     */
    @SuppressWarnings("unchecked")
    private static void registerInjectedLanguageManager(MockProject project) {
        try {
            // Try to use the real implementation for proper injection support
            var implClass = Class.forName("com.intellij.psi.impl.source.tree.injected.InjectedLanguageManagerImpl");
            var constructor = implClass.getDeclaredConstructor(Project.class);
            var instance = constructor.newInstance(project);
            project.registerService(InjectedLanguageManager.class, (InjectedLanguageManager) instance);
            // Also register under implementation class for direct lookups
            project.registerService((Class<Object>) implClass, instance);
            debug(COMPONENT, "Registered InjectedLanguageManagerImpl");
        } catch (Exception e) {
            // Fall back to stub implementation
            project.registerService(InjectedLanguageManager.class, new InjectedLanguageService());
            warn(COMPONENT, "Using stub InjectedLanguageService", e);
        }
    }

    /**
     * Registers CodeInsightContextManager for IDEA 2025.x multiverse support.
     *
     * <p>IDEA 2025.x introduced "multiverse" for handling multiple contexts
     * (e.g., different Kotlin/JVM targets). We create a proxy that returns
     * empty/null for all methods since we don't need this for formatting.</p>
     */
    @SuppressWarnings("unchecked")
    private static void registerCodeInsightContextManager(MockProject project) {
        try {
            var serviceClass = (Class<Object>) Class.forName("com.intellij.codeInsight.multiverse.CodeInsightContextManager");

            // Create a dynamic proxy that returns sensible defaults for all methods
            InvocationHandler handler = (proxyObj, method, args) -> {
                var returnType = method.getReturnType();

                // Return empty list for collection-returning methods
                if (returnType == List.class) {
                    return Collections.emptyList();
                }

                // Kotlin Flow is used for reactive streams - return null (no updates)
                if (returnType.getName().equals("kotlinx.coroutines.flow.Flow")) {
                    return null;
                }

                // All other methods return null (getCodeInsightContext, getPreferredContext, etc.)
                return null;
            };

            var proxy = Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{serviceClass}, handler);
            project.registerService(serviceClass, proxy);
            debug(COMPONENT, "Registered CodeInsightContextManager proxy");
        } catch (ClassNotFoundException e) {
            // Expected in IDEA versions before 2025.x
            skipped(COMPONENT, "CodeInsightContextManager", "class not found (pre-2025.x IDEA)");
        } catch (Exception e) {
            skipped(COMPONENT, "CodeInsightContextManager", e.getMessage());
        }
    }
}
