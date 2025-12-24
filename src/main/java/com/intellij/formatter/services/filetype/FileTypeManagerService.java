package com.intellij.formatter.services.filetype;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.yaml.YAMLFileType;

import java.util.List;

/**
 * Service for managing file type detection in standalone formatting mode.
 *
 * <p>This implementation provides file type detection based on file extensions
 * for all supported languages:</p>
 * <ul>
 *     <li>Java (.java)</li>
 *     <li>Kotlin (.kt, .kts, .gradle.kts)</li>
 *     <li>Groovy (.groovy, .gradle)</li>
 *     <li>XML (.xml, .xsd, .xsl, .xslt, .wsdl, .fxml, .pom)</li>
 *     <li>JSON (.json)</li>
 *     <li>YAML (.yaml, .yml)</li>
 *     <li>Properties (.properties)</li>
 * </ul>
 *
 * @see FileTypeManager
 */
public class FileTypeManagerService extends FileTypeManager {

    @Override
    public @NotNull FileType getFileTypeByFileName(@NotNull @NonNls String fileName) {
        return getFileTypeByExtension(getExtension(fileName));
    }

    @Override
    public @NotNull FileType getFileTypeByFile(@NotNull VirtualFile file) {
        return getFileTypeByFileName(file.getName());
    }

    @Override
    public @NotNull FileType getFileTypeByExtension(@NotNull @NonNls String extension) {
        return switch (extension.toLowerCase()) {
            case "java" -> JavaFileType.INSTANCE;
            case "xml", "xsd", "xsl", "xslt", "wsdl", "fxml", "pom" -> XmlFileType.INSTANCE;
            case "html", "htm", "xhtml" -> XmlFileType.INSTANCE;
            case "json" -> JsonFileType.INSTANCE;
            case "groovy", "gradle" -> GroovyFileType.GROOVY_FILE_TYPE;
            case "kt", "kts" -> KotlinFileType.INSTANCE;
            case "properties" -> PropertiesFileType.INSTANCE;
            case "yaml", "yml" -> YAMLFileType.YML;
            default -> PlainTextFileType.INSTANCE;
        };
    }

    @Override
    public @NotNull FileType getStdFileType(@NotNull @NonNls String fileTypeName) {
        return switch (fileTypeName) {
            case "JAVA" -> JavaFileType.INSTANCE;
            case "XML", "HTML", "XHTML" -> XmlFileType.INSTANCE;
            case "JSON" -> JsonFileType.INSTANCE;
            case "Groovy" -> GroovyFileType.GROOVY_FILE_TYPE;
            case "Kotlin" -> KotlinFileType.INSTANCE;
            case "Properties" -> PropertiesFileType.INSTANCE;
            case "YAML" -> YAMLFileType.YML;
            default -> PlainTextFileType.INSTANCE;
        };
    }

    @Override
    public FileType @NotNull [] getRegisteredFileTypes() {
        return new FileType[]{
                JavaFileType.INSTANCE,
                XmlFileType.INSTANCE,
                JsonFileType.INSTANCE,
                GroovyFileType.GROOVY_FILE_TYPE,
                KotlinFileType.INSTANCE,
                PropertiesFileType.INSTANCE,
                YAMLFileType.YML,
                PlainTextFileType.INSTANCE
        };
    }

    @Override
    public boolean isFileIgnored(@NotNull @NonNls String name) {
        return false;
    }

    @Override
    public boolean isFileIgnored(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public @NotNull List<FileNameMatcher> getAssociations(@NotNull FileType type) {
        return List.of();
    }

    public void registerFileType(@NotNull FileType type, @NotNull List<? extends FileNameMatcher> defaultAssociations) {
    }

    public void registerFileType(@NotNull FileType type, @NotNull @NonNls String... defaultAssociatedExtensions) {
    }

    public String @NotNull [] getAssociatedExtensions(@NotNull FileType type) {
        if (type == JavaFileType.INSTANCE) {
            return new String[]{"java"};
        }
        if (type == XmlFileType.INSTANCE) {
            return new String[]{"xml", "xsd", "xsl", "xslt", "wsdl", "fxml", "pom", "html", "htm", "xhtml"};
        }
        if (type == JsonFileType.INSTANCE) {
            return new String[]{"json"};
        }
        if (type == GroovyFileType.GROOVY_FILE_TYPE) {
            return new String[]{"groovy", "gradle"};
        }
        if (type == KotlinFileType.INSTANCE) {
            return new String[]{"kt", "kts"};
        }
        if (type == PropertiesFileType.INSTANCE) {
            return new String[]{"properties"};
        }
        if (type == YAMLFileType.YML) {
            return new String[]{"yaml", "yml"};
        }
        return new String[0];
    }

    @Override
    public @NotNull String getIgnoredFilesList() {
        return "";
    }

    @Override
    public void setIgnoredFilesList(@NotNull String list) {
    }

    public @NotNull FileType getKnownFileTypeOrAssociate(@NotNull VirtualFile file) {
        return getFileTypeByFile(file);
    }

    public @NotNull FileType getKnownFileTypeOrAssociate(@NotNull VirtualFile file, @NotNull Project project) {
        return getFileTypeByFile(file);
    }

    @Override
    public @Nullable FileType findFileTypeByName(@NotNull String fileTypeName) {
        return getStdFileType(fileTypeName);
    }

    @Override
    public void associate(@NotNull FileType type, @NotNull FileNameMatcher matcher) {
    }

    @Override
    public void removeAssociation(@NotNull FileType type, @NotNull FileNameMatcher matcher) {
    }

    @Override
    public @NotNull FileType getFileTypeByFile(@NotNull VirtualFile file, @Nullable byte[] content) {
        return getFileTypeByFile(file);
    }

    private String getExtension(String fileName) {
        var lastDot = fileName.lastIndexOf('.');
        return (lastDot >= 0 && lastDot < fileName.length() - 1)
                ? fileName.substring(lastDot + 1)
                : "";
    }
}
