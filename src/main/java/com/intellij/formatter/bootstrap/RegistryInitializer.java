package com.intellij.formatter.bootstrap;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.formatter.bootstrap.BootstrapLogger.debug;
import static com.intellij.formatter.bootstrap.BootstrapLogger.skipped;

/**
 * Initializer for IntelliJ Platform registry keys required for formatting.
 *
 * <p>The IntelliJ Platform uses a Registry as a lightweight configuration store
 * for experimental features and internal settings. Unlike application settings,
 * registry keys are meant for developer use and can be modified at runtime.</p>
 *
 * <h2>Why Registry Keys Are Needed</h2>
 * <p>Some formatting behaviors depend on registry values:</p>
 * <ul>
 *     <li>Java formatter checks compatibility flags for method chain formatting</li>
 *     <li>Groovy formatter checks whether to use document-based or PSI-based formatting</li>
 *     <li>Kotlin formatter checks trailing comma preferences</li>
 * </ul>
 *
 * <h2>Configured Registry Keys</h2>
 * <table>
 *     <tr><th>Key</th><th>Value</th><th>Purpose</th></tr>
 *     <tr>
 *         <td>{@code java.formatter.chained.calls.pre212.compatibility}</td>
 *         <td>false</td>
 *         <td>Use new method chain formatting (post IDEA 2021.2)</td>
 *     </tr>
 *     <tr>
 *         <td>{@code groovy.document.based.formatting}</td>
 *         <td>false</td>
 *         <td>Use PSI-based formatting for better accuracy</td>
 *     </tr>
 *     <tr>
 *         <td>{@code kotlin.formatter.allowTrailingCommaOnCallSite}</td>
 *         <td>false</td>
 *         <td>Don't add trailing commas automatically</td>
 *     </tr>
 * </table>
 *
 * @see FormatterBootstrap
 */
@UtilityClass
public class RegistryInitializer {

    private static final String COMPONENT = "Registry";

    /**
     * Initializes the IntelliJ Platform registry with required keys.
     *
     * <p>Uses reflection because:</p>
     * <ol>
     *     <li>Registry API changed between IDEA versions (Kotlin rewrite)</li>
     *     <li>Direct dependency would require version-specific code</li>
     *     <li>Registry may not exist in all IntelliJ builds</li>
     * </ol>
     *
     * <p>If initialization fails, formatting still works but some edge cases
     * may behave differently than in the IDE.</p>
     */
    public static void initialize() {
        try {
            debug(COMPONENT, "Initializing registry keys");

            // Registry class was rewritten in Kotlin, access via Companion object
            var registryClass = Class.forName("com.intellij.openapi.util.registry.Registry");
            var companionField = registryClass.getDeclaredField("Companion");
            var companion = companionField.get(null);
            var companionClass = companion.getClass();

            // RegistryKeyDescriptor(name, defaultValue, description, restartRequired, pluginId, overriddenValue)
            var descriptorClass = Class.forName("com.intellij.openapi.util.registry.RegistryKeyDescriptor");
            var descriptorConstructor = descriptorClass.getConstructor(
                    String.class, String.class, String.class, boolean.class, boolean.class, String.class);

            Map<String, Object> keys = new HashMap<>();

            // Java: Use modern method chain formatting (IDEA 2021.2+)
            keys.put("java.formatter.chained.calls.pre212.compatibility",
                    descriptorConstructor.newInstance(
                            "java.formatter.chained.calls.pre212.compatibility", "false",
                            "Java formatter backward compatibility", false, false, null));

            // Groovy: PSI-based formatting is more accurate than document-based
            keys.put("groovy.document.based.formatting",
                    descriptorConstructor.newInstance(
                            "groovy.document.based.formatting", "false",
                            "Groovy document based formatting", false, false, null));

            // Kotlin: Don't auto-add trailing commas (matches default IDE behavior)
            keys.put("kotlin.formatter.allowTrailingCommaOnCallSite",
                    descriptorConstructor.newInstance(
                            "kotlin.formatter.allowTrailingCommaOnCallSite", "false",
                            "Allow a trailing comma on call-site", false, false, null));

            // Register keys and mark registry as loaded
            var setContributedKeysMethod = companionClass.getMethod("setContributedKeys", Map.class);
            setContributedKeysMethod.invoke(companion, keys);

            var markAsLoadedMethod = companionClass.getMethod("markAsLoaded");
            markAsLoadedMethod.invoke(companion);

            debug(COMPONENT, "Registry initialized with " + keys.size() + " keys");
        } catch (ClassNotFoundException e) {
            // Registry class not found - older IDEA version or different build
            skipped(COMPONENT, "Registry initialization", "Registry class not found");
        } catch (NoSuchFieldException e) {
            // Kotlin Companion object not found - pre-Kotlin rewrite version
            skipped(COMPONENT, "Registry initialization", "pre-Kotlin Registry API");
        } catch (Exception e) {
            // Other errors - log but continue
            skipped(COMPONENT, "Registry initialization", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
