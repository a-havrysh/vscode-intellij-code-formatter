import de.undercouch.gradle.tasks.download.Download
import java.util.zip.ZipFile

plugins {
    java
    application
    id("de.undercouch.download") version "5.5.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val ideVersion = "idea-2025.3.1"
val ideDownloadUrl = "https://download-cdn.jetbrains.com/idea/$ideVersion.win.zip"
val ideDownloadDir = layout.projectDirectory.dir(".idea-downloads")
val ideExtractDir = layout.projectDirectory.dir("ide")

val ideLibPath = "${ideExtractDir.asFile.path}/lib"
val idePluginsPath = "${ideExtractDir.asFile.path}/plugins"

dependencies {
    implementation(files("$ideLibPath/app.jar"))
    implementation(files("$ideLibPath/util.jar"))
    implementation(files("$ideLibPath/util_rt.jar"))
    implementation(files("$ideLibPath/util-8.jar"))
    implementation(files("$ideLibPath/lib.jar"))
    implementation(files("$ideLibPath/jps-model.jar"))
    implementation(files("$ideLibPath/module-intellij.libraries.fastutil.jar"))
    implementation(files("$ideLibPath/module-intellij.libraries.kotlinx.collections.immutable.jar"))
    implementation(files("$ideLibPath/module-intellij.libraries.guava.jar"))
    implementation(files("$ideLibPath/opentelemetry.jar"))
    implementation(files("$ideLibPath/module-intellij.regexp.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.parser.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.psi.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.psi.impl.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.analysis.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.analysis.impl.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.impl.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.ui.common.jar"))
    implementation(files("$ideLibPath/module-intellij.xml.syntax.jar"))
    implementation(files("$idePluginsPath/java/lib/java-impl.jar"))
    implementation(files("$idePluginsPath/java/lib/java-impl-frontend.jar"))
    implementation(files("$idePluginsPath/Groovy/lib/Groovy.jar"))
    implementation(files("$idePluginsPath/properties/lib/properties.jar"))
    implementation(files("$idePluginsPath/yaml/lib/yaml.jar"))
    implementation(files("$idePluginsPath/json/lib/json.jar"))
    // Kotlin
    implementation(files("$idePluginsPath/Kotlin/lib/kotlin-plugin.jar"))
    implementation(files("$idePluginsPath/Kotlin/lib/kotlin-plugin-shared.jar"))
    implementation(files("$idePluginsPath/Kotlin/lib/kotlinc.kotlin-compiler-common.jar"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.jetbrains.intellij.deps:trove4j:1.0.20200330")
    implementation("org.yaml:snakeyaml:2.0")
    implementation("org.apache.logging.log4j:log4j-1.2-api:2.20.0")
    implementation("com.fasterxml:aalto-xml:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

application {
    mainClass.set("com.intellij.formatter.JetbrainsFormatterApplication")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "--add-exports", "java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED"
    ))
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED",
        "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens", "java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens", "java.desktop/javax.swing=ALL-UNNAMED",
        "-Djava.awt.headless=true"
    )
}

tasks.named("startScripts") {
    dependsOn("fatJar")
}

tasks.named("distTar") {
    dependsOn("fatJar")
}

tasks.named("distZip") {
    dependsOn("fatJar")
}

tasks.jar {
    enabled = false
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    isZip64 = true

    manifest {
        attributes(
            "Main-Class" to "com.intellij.formatter.JetbrainsFormatterApplication",
            "Multi-Release" to "true"
        )
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
    }) {
        exclude("**/*.png")
        exclude("**/*.gif")
        exclude("**/*.jpg")
        exclude("**/*.svg")
        exclude("**/*.ico")
        exclude("**/*.ttf")
        exclude("**/*.otf")
        exclude("**/*.woff")
        exclude("**/*.woff2")
        exclude("images/**")
        exclude("expui/**")
        exclude("fonts/**")
        exclude("tips/**")
        exclude("darwin/**")
        exclude("win32/**")
        exclude("linux/**")
        exclude("native/**")
        exclude("com/sun/jna/**")
        exclude("com/intellij/debugger/**")
        exclude("com/intellij/coverage/**")
        exclude("com/intellij/execution/**")
        exclude("org/apache/xmlgraphics/**")
        exclude("org/apache/batik/**")
        exclude("com/intellij/diff/**")
        exclude("com/intellij/vcs/**")
        exclude("com/intellij/openapi/vcs/**")
        exclude("org/intellij/plugins/markdown/**")
        exclude("org/toml/**")
        exclude("dictionary/**")
        exclude("com/intellij/documentation/mdn/**")
        exclude("META-INF/native/**")
        exclude("org/apache/pdfbox/**")
        exclude("org/apache/fontbox/**")
        exclude("org/bouncycastle/**")
        exclude("io/grpc/**")
        exclude("search/**")
        exclude("org/apache/lucene/**")
        exclude("net/jpountz/**")
        exclude("tables/**")
        exclude("mozilla/**")
        exclude("io/ktor/**")
        exclude("kotlinx/html/**")
        exclude("openhtmltopdf/**")
        exclude("EmojiReference.txt")
        exclude("java_model/**")
        exclude("io/netty/**")
        exclude("ai/grazie/**")
        exclude("org/eclipse/**")
        exclude("com/amazon/**")
        exclude("org/jdesktop/**")
        exclude("resources/event-log-metadata/**")
        exclude("win32-x86/**")
        exclude("win32-x86-64/**")
        exclude("com/intellij/xdebugger/**")
        exclude("com/intellij/collaboration/**")
        exclude("com/intellij/webSymbols/**")
        exclude("com/intellij/compiler/**")
        exclude("com/openhtmltopdf/**")
        exclude("com/thoughtworks/**")
        exclude("com/vladsch/**")
        exclude("com/intellij/workspaceModel/**")
        exclude("org/apache/http/**")
        exclude("org/apache/maven/**")
        exclude("org/apache/velocity/**")
        exclude("org/mozilla/**")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("com/intellij/ide/actions/**")
        exclude("com/intellij/ide/ui/**")
        exclude("com/intellij/ide/wizard/**")
        exclude("com/intellij/ide/browsers/**")
        exclude("com/intellij/ide/todo/**")
        exclude("com/intellij/ide/projectView/**")
        exclude("com/intellij/ide/favoritesTreeView/**")
        exclude("com/intellij/ide/hierarchy/**")
        exclude("com/intellij/ide/dnd/**")
        exclude("com/intellij/ui/treeStructure/**")
        exclude("com/intellij/ui/speedSearch/**")
        exclude("com/intellij/ui/tabs/**")
        exclude("com/intellij/ui/popup/**")
        exclude("com/intellij/ui/tree/**")
        exclude("com/intellij/util/ui/tree/**")
        exclude("com/intellij/notification/**")
        exclude("com/intellij/refactoring/**")
        exclude("com/intellij/find/**")
        exclude("com/intellij/usages/**")
        exclude("org/jetbrains/kotlin/idea/inspections/**")
        exclude("org/jetbrains/kotlin/idea/quickfix/**")
        exclude("org/jetbrains/kotlin/idea/refactoring/**")
        exclude("org/jetbrains/kotlin/idea/completion/**")
        exclude("org/jetbrains/kotlin/idea/navigation/**")
        exclude("org/jetbrains/kotlin/nj2k/**")
        exclude("org/jetbrains/kotlin/tools/**")
        exclude("inspectionDescriptions/**")
        exclude("intentionDescriptions/**")
        exclude("postfixTemplates/**")
        exclude("liveTemplates/**")
        exclude("fileTemplates/**")
        exclude("META-INF/services/org.codehaus.**")
        exclude("META-INF/services/javax.**")
        exclude("META-INF/*.RSA")
        exclude("com/siyeh/**")
        exclude("fleet/**")
        exclude("andel/**")
        exclude("jetbrains/buildServer/**")
        exclude("standardSchemas/**")
        exclude("com/jgoodies/**")
        exclude("org/freedesktop/**")
        exclude("kotlin/script/**")
        exclude("kotlin/metadata/**")
        exclude("META-INF/native-image/**")
        exclude("META-INF/maven/**")
        exclude("okhttp3/**")
        exclude("okio/**")
        exclude("org/jetbrains/kotlin/j2k/**")
        exclude("org/jetbrains/kotlin/konan/**")
        exclude("org/jetbrains/kotlin/cli/**")
        exclude("org/jetbrains/kotlin/daemon/**")
        exclude("org/jetbrains/kotlin/gradle/**")
        exclude("org/jetbrains/kotlin/scripting/**")
        exclude("org/jetbrains/kotlin/buildtools/**")
        exclude("org/jetbrains/kotlin/build/**")
        exclude("org/jetbrains/kotlin/statistics/**")
        exclude("org/jetbrains/kotlin/onboarding/**")
        exclude("org/jetbrains/kotlin/incremental/**")
        exclude("org/jetbrains/builtInWebServer/**")
        exclude("org/jetbrains/eval4j/**")
        exclude("org/jetbrains/zip/**")
        exclude("org/jetbrains/io/**")
        exclude("com/intellij/internal/**")
        exclude("com/intellij/diagnostic/logging/**")
        exclude("com/intellij/diagnostic/report/**")
        exclude("com/intellij/microservices/**")
        exclude("com/intellij/remoteDev/**")
        exclude("com/intellij/remoteServer/**")
        exclude("com/intellij/designer/**")
        exclude("com/intellij/build/**")
        exclude("com/intellij/toolWindow/**")
        exclude("com/intellij/packaging/**")
        exclude("com/intellij/jarRepository/**")
        exclude("com/intellij/facet/**")
        exclude("com/intellij/credentialStore/**")
        exclude("com/intellij/framework/**")
        exclude("com/intellij/packageDependencies/**")
        exclude("com/intellij/largeFilesEditor/**")
        exclude("com/intellij/remote/**")
        exclude("com/intellij/testIntegration/**")
        exclude("com/intellij/terminal/**")
        exclude("com/intellij/slicer/**")
        exclude("com/intellij/tools/**")
        exclude("com/intellij/codeInspection/dataFlow/**")
        exclude("com/intellij/codeInspection/bytecodeAnalysis/**")
        exclude("com/intellij/codeInspection/streamMigration/**")
        exclude("com/intellij/codeInspection/streamToLoop/**")
        exclude("com/intellij/codeInspection/optionalToIf/**")
        exclude("com/intellij/codeInspection/deadCode/**")
        exclude("com/intellij/codeInspection/classCanBeRecord/**")
        exclude("com/intellij/codeInspection/java19api/**")
        exclude("com/intellij/codeInspection/java18api/**")
        exclude("com/intellij/platform/eel/**")
        exclude("com/intellij/platform/ijent/**")
        exclude("com/intellij/platform/feedback/**")
        exclude("com/intellij/platform/pasta/**")
        exclude("com/intellij/platform/templates/**")
        exclude("com/intellij/platform/externalSystem/**")
        exclude("com/intellij/ui/jcef/**")
        exclude("com/intellij/ui/mac/**")
        exclude("com/intellij/ui/icons/**")
        exclude("com/intellij/ui/colorpicker/**")
        exclude("com/intellij/ui/charts/**")
        exclude("com/intellij/ui/svg/**")
        exclude("com/intellij/ui/debugger/**")
        exclude("com/intellij/ui/codeFloatingToolbar/**")
        exclude("org/jetbrains/kotlin/idea/debugger/**")
        exclude("org/jetbrains/kotlin/idea/j2k/**")
        exclude("org/jetbrains/kotlin/idea/gradleJava/**")
        exclude("org/jetbrains/kotlin/idea/gradle/**")
        exclude("org/jetbrains/kotlin/idea/compilerPlugin/**")
        exclude("org/jetbrains/kotlin/idea/ultimate/**")
        exclude("org/jetbrains/kotlin/idea/search/**")
        exclude("org/jetbrains/kotlin/idea/actions/**")
        exclude("com/intellij/openapi/externalSystem/**")
        exclude("com/intellij/openapi/wm/impl/**")
        exclude("com/intellij/openapi/updateSettings/**")
        exclude("com/intellij/openapi/keymap/**")
        exclude("com/intellij/openapi/fileChooser/**")
        exclude("org/jetbrains/kotlin/analysis/decompiler/**")
        exclude("org/jetbrains/kotlin/analysis/decompiled/**")
        exclude("org/jetbrains/plugins/groovy/mvc/**")
        exclude("org/jetbrains/plugins/groovy/compiler/**")
        exclude("org/jetbrains/plugins/groovy/shell/**")
        exclude("org/jetbrains/plugins/groovy/debugger/**")
        exclude("org/jetbrains/plugins/groovy/refactoring/**")
        exclude("org/jetbrains/plugins/groovy/template/**")
        exclude("org/jetbrains/plugins/groovy/actions/**")
        exclude("com/intellij/java/debugger/**")
        exclude("com/intellij/java/performancePlugin/**")
        exclude("com/intellij/java/terminal/**")
        exclude("com/intellij/java/ultimate/**")
        exclude("com/intellij/java/execution/**")
    }
}

tasks.register<Download>("downloadIdea") {
    group = "ide"
    description = "Downloads IntelliJ IDEA Community Edition"

    src(ideDownloadUrl)
    dest(ideDownloadDir.file("$ideVersion.zip").asFile)
    overwrite(false)

    onlyIf {
        !file("$ideLibPath/app.jar").exists()
    }
}

tasks.register("extractIdea") {
    group = "ide"
    description = "Extracts required JARs from IntelliJ IDEA"
    dependsOn("downloadIdea")

    val zipFile = ideDownloadDir.file("$ideVersion.zip").asFile
    val targetDir = ideExtractDir.asFile

    val requiredEntries = listOf(
        "lib/app.jar",
        "lib/util.jar",
        "lib/util_rt.jar",
        "lib/util-8.jar",
        "lib/lib.jar",
        "lib/jps-model.jar",
        "lib/module-intellij.libraries.fastutil.jar",
        "lib/module-intellij.libraries.kotlinx.collections.immutable.jar",
        "lib/module-intellij.libraries.guava.jar",
        "lib/opentelemetry.jar",
        "lib/module-intellij.regexp.jar",
        "lib/module-intellij.xml.parser.jar",
        "lib/module-intellij.xml.psi.jar",
        "lib/module-intellij.xml.psi.impl.jar",
        "lib/module-intellij.xml.analysis.jar",
        "lib/module-intellij.xml.analysis.impl.jar",
        "lib/module-intellij.xml.impl.jar",
        "lib/module-intellij.xml.ui.common.jar",
        "lib/module-intellij.xml.syntax.jar",
        "plugins/java/lib/java-impl.jar",
        "plugins/java/lib/java-impl-frontend.jar",
        "plugins/Groovy/lib/Groovy.jar",
        "plugins/properties/lib/properties.jar",
        "plugins/yaml/lib/yaml.jar",
        "plugins/json/lib/json.jar",
        // Kotlin
        "plugins/Kotlin/lib/kotlin-plugin.jar",
        "plugins/Kotlin/lib/kotlin-plugin-shared.jar",
        "plugins/Kotlin/lib/kotlinc.kotlin-compiler-common.jar"
    )

    inputs.file(zipFile).optional()
    outputs.files(requiredEntries.map { File(targetDir, it) })

    onlyIf {
        !file("$ideLibPath/app.jar").exists()
    }

    doLast {
        if (!zipFile.exists()) {
            logger.warn("ZIP file not found: $zipFile")
            return@doLast
        }

        targetDir.mkdirs()

        ZipFile(zipFile).use { zip: java.util.zip.ZipFile ->
            for (entryPath in requiredEntries) {
                val entry = zip.entries().asSequence()
                    .find { e -> e.name.endsWith(entryPath) || e.name.contains(entryPath) }

                if (entry != null) {
                    val targetFile = File(targetDir, entryPath)
                    targetFile.parentFile.mkdirs()

                    zip.getInputStream(entry).use { input: java.io.InputStream ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    logger.lifecycle("Extracted: $entryPath")
                } else {
                    logger.warn("Entry not found: $entryPath")
                }
            }
        }
    }
}

tasks.register("setupIde") {
    group = "ide"
    description = "Downloads and extracts IntelliJ IDEA (run this first)"
    dependsOn("extractIdea")
}

tasks.named("compileJava") {
    dependsOn("extractIdea")
}

tasks.build {
    dependsOn("fatJar")
    dependsOn("buildVscode")
}

tasks.register<Delete>("cleanIde") {
    group = "ide"
    description = "Removes downloaded IntelliJ IDEA JARs"
    delete(ideExtractDir)
}

tasks.named<Delete>("clean") {
    delete(ideExtractDir)
    delete("vscode-extension/node_modules")
    delete("vscode-extension/out")
    delete("vscode-extension/formatter")
    delete(fileTree("vscode-extension") { include("*.vsix") })
}

val vscodeDir = layout.projectDirectory.dir("vscode-extension")

tasks.register<Exec>("npmInstall") {
    group = "vscode"
    description = "Install npm dependencies for VSCode extension"
    workingDir = vscodeDir.asFile
    commandLine = listOf("npm", "install")

    inputs.file(vscodeDir.file("package.json"))
    outputs.dir(vscodeDir.dir("node_modules"))

    onlyIf {
        !vscodeDir.dir("node_modules").asFile.exists()
    }
}

tasks.register<Exec>("npmCompile") {
    group = "vscode"
    description = "Compile TypeScript for VSCode extension"
    workingDir = vscodeDir.asFile
    commandLine = listOf("npm", "run", "compile")
    dependsOn("npmInstall")

    inputs.dir(vscodeDir.dir("src"))
    inputs.file(vscodeDir.file("tsconfig.json"))
    outputs.dir(vscodeDir.dir("out"))
}

tasks.register<Copy>("bundleJar") {
    group = "vscode"
    description = "Bundle formatter JAR into VSCode extension"
    dependsOn("fatJar")

    from(layout.buildDirectory.file("libs/vscode-idea-code-formatter.jar"))
    into(vscodeDir.dir("formatter"))
}

tasks.register<Exec>("vscodePackage") {
    group = "vscode"
    description = "Package VSCode extension as .vsix"
    workingDir = vscodeDir.asFile
    commandLine = listOf("npm", "run", "package")
    dependsOn("npmCompile", "bundleJar")

    inputs.dir(vscodeDir.dir("out"))
    inputs.dir(vscodeDir.dir("formatter"))
    inputs.file(vscodeDir.file("package.json"))
    outputs.files(fileTree(vscodeDir) { include("*.vsix") })
}

tasks.register("buildVscode") {
    group = "vscode"
    description = "Build VSCode extension (compile + package)"
    dependsOn("vscodePackage")
}

tasks.register<Copy>("copyVsix") {
    group = "vscode"
    description = "Copy .vsix to build/libs"
    dependsOn("vscodePackage")

    from(fileTree(vscodeDir) { include("*.vsix") })
    into(layout.buildDirectory.dir("libs"))
}
