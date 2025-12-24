package com.intellij.formatter.services.document;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Service for managing character encoding settings in standalone formatting mode.
 *
 * <p>This implementation defaults all encoding operations to UTF-8, which is
 * the recommended encoding for source code files.</p>
 *
 * @see EncodingManager
 */
public class EncodingManagerService extends EncodingManager {

    @Override
    public @NotNull Collection<Charset> getFavorites() {
        return List.of(UTF_8);
    }

    @Override
    public boolean isNative2Ascii(@Nullable VirtualFile virtualFile) {
        return false;
    }

    @Override
    public boolean isNative2AsciiForPropertiesFiles() {
        return false;
    }

    @Override
    public void setNative2AsciiForPropertiesFiles(@Nullable VirtualFile virtualFile, boolean value) {
    }

    @Override
    public @NotNull String getDefaultCharsetName() {
        return UTF_8.name();
    }

    @Override
    public @Nullable Charset getDefaultCharsetForPropertiesFiles(@Nullable VirtualFile virtualFile) {
        return UTF_8;
    }

    @Override
    public void setDefaultCharsetForPropertiesFiles(@Nullable VirtualFile virtualFile, @Nullable Charset charset) {
        // No-op in standalone mode
    }

    @Override
    public @Nullable Charset getDefaultConsoleEncoding() {
        return UTF_8;
    }

    @Override
    public @Nullable Charset getCachedCharsetFromContent(@NotNull Document document) {
        return UTF_8;
    }

    @Override
    public @Nullable Charset getEncoding(@Nullable VirtualFile virtualFile, boolean useParentDefaults) {
        return UTF_8;
    }

    @Override
    public void setEncoding(@Nullable VirtualFile virtualFileOrDir, @Nullable Charset charset) {
        // No-op in standalone mode
    }

    public boolean isUseUTFGuessing(@Nullable VirtualFile virtualFile) {
        return true;
    }

    @Override
    public @NotNull Charset getDefaultCharset() {
        return UTF_8;
    }
}
