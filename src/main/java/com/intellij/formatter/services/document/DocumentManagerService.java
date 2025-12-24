package com.intellij.formatter.services.document;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Service for managing document-to-file associations in standalone formatting mode.
 *
 * <p>This implementation provides document creation and caching for virtual files,
 * allowing the formatting engine to work with in-memory documents without
 * requiring actual file system operations.</p>
 *
 * @see FileDocumentManager
 */
public class DocumentManagerService extends FileDocumentManager {

    private static final Key<Document> DOC_KEY = Key.create("DocumentManagerService.DOC");
    private static final Key<VirtualFile> FILE_KEY = Key.create("DocumentManagerService.FILE");

    @Override
    public @Nullable Document getDocument(@NotNull VirtualFile file) {
        var existing = file.getUserData(DOC_KEY);
        if (existing != null) {
            return existing;
        }

        try {
            var content = LoadTextUtil.loadText(file);
            var doc = new DocumentImpl(content);
            doc.putUserData(FILE_KEY, file);
            file.putUserData(DOC_KEY, doc);
            return doc;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public @Nullable Document getCachedDocument(@NotNull VirtualFile file) {
        return file.getUserData(DOC_KEY);
    }

    @Override
    public @Nullable VirtualFile getFile(@NotNull Document document) {
        return document.getUserData(FILE_KEY);
    }

    @Override
    public void saveAllDocuments() {
    }

    @Override
    public void saveDocuments(@NotNull Predicate<? super Document> filter) {
    }

    @Override
    public void saveDocument(@NotNull Document document) {
    }

    @Override
    public void saveDocumentAsIs(@NotNull Document document) {
    }

    @Override
    public Document @NotNull [] getUnsavedDocuments() {
        return Document.EMPTY_ARRAY;
    }

    @Override
    public boolean isDocumentUnsaved(@NotNull Document document) {
        return false;
    }

    @Override
    public boolean isFileModified(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isPartialPreviewOfALargeFile(@NotNull Document document) {
        return false;
    }

    @Override
    public void reloadFromDisk(@NotNull Document document) {
    }

    @Override
    public void reloadFromDisk(@NotNull Document document, @Nullable Project project) {
    }

    @Override
    public void reloadFiles(VirtualFile @NotNull ... files) {
    }

    @Override
    public @NotNull String getLineSeparator(@Nullable VirtualFile file, @Nullable Project project) {
        return "\n";
    }

    @Override
    public boolean requestWriting(@NotNull Document document, @Nullable Project project) {
        return true;
    }
}
