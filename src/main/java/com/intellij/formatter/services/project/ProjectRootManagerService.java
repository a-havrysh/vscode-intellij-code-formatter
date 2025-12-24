package com.intellij.formatter.services.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Minimal implementation of {@link ProjectRootManager} for standalone formatting.
 *
 * <p>This implementation provides a minimal project structure view suitable
 * for formatting operations without full project management capabilities.</p>
 *
 * @see ProjectRootManager
 * @see ProjectFileIndexService
 */
public class ProjectRootManagerService extends ProjectRootManager {

    private final ProjectFileIndex fileIndex = new ProjectFileIndexService();

    @Override
    public @NotNull ProjectFileIndex getFileIndex() {
        return fileIndex;
    }

    @Override
    public @NotNull OrderEnumerator orderEntries() {
        throw new UnsupportedOperationException("Order enumeration not supported in standalone mode");
    }

    @Override
    public @NotNull OrderEnumerator orderEntries(@NotNull Collection<? extends Module> modules) {
        throw new UnsupportedOperationException("Order enumeration not supported in standalone mode");
    }

    @Override
    public VirtualFile @NotNull [] getContentRootsFromAllModules() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public @NotNull List<String> getContentRootUrls() {
        return List.of();
    }

    @Override
    public VirtualFile @NotNull [] getContentRoots() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public VirtualFile @NotNull [] getContentSourceRoots() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public @NotNull List<VirtualFile> getModuleSourceRoots(@NotNull Set<? extends JpsModuleSourceRootType<?>> rootTypes) {
        return List.of();
    }

    @Override
    public @Nullable Sdk getProjectSdk() {
        return null;
    }

    @Override
    public @Nullable String getProjectSdkName() {
        return null;
    }

    @Override
    public @Nullable String getProjectSdkTypeName() {
        return null;
    }

    @Override
    public void setProjectSdk(@Nullable Sdk sdk) {
    }

    @Override
    public void setProjectSdkName(@NotNull String name, @NotNull String sdkTypeName) {
    }

    @Override
    public @NotNull ModuleRootManager getModuleRootManager(@NotNull Module module) {
        throw new UnsupportedOperationException("ModuleRootManager not supported in standalone mode");
    }
}
