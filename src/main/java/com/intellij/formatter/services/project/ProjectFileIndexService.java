package com.intellij.formatter.services.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.platform.workspace.jps.entities.LibraryEntity;
import com.intellij.platform.workspace.jps.entities.SdkEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Minimal implementation of {@link ProjectFileIndex} for standalone formatting.
 *
 * <p>This implementation treats all files as being in the project and in source,
 * which is appropriate for standalone formatting operations.</p>
 *
 * @see ProjectFileIndex
 */
public class ProjectFileIndexService implements ProjectFileIndex {

    @Override
    public boolean isInProject(@NotNull VirtualFile file) {
        return true;
    }

    @Override
    public boolean isInProjectOrExcluded(@NotNull VirtualFile file) {
        return true;
    }

    @Override
    public @Nullable Module getModuleForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public @Nullable Module getModuleForFile(@NotNull VirtualFile file, boolean honorExclusion) {
        return null;
    }

    @Override
    public @NotNull Set<Module> getModulesForFile(@NotNull VirtualFile file, boolean honorExclusion) {
        return Set.of();
    }

    @Override
    public @NotNull List<OrderEntry> getOrderEntriesForFile(@NotNull VirtualFile file) {
        return List.of();
    }

    @Override
    public @Nullable VirtualFile getClassRootForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public @Nullable VirtualFile getSourceRootForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public @Nullable VirtualFile getContentRootForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public @Nullable VirtualFile getContentRootForFile(@NotNull VirtualFile file, boolean honorExclusion) {
        return null;
    }

    @Override
    public @Nullable String getPackageNameByDirectory(@NotNull VirtualFile dir) {
        return null;
    }

    @Override
    public boolean isLibraryClassFile(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isInSource(@NotNull VirtualFile file) {
        return true;
    }

    @Override
    public boolean isInLibraryClasses(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isInLibrary(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isInLibrarySource(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isExcluded(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public @NotNull Collection<LibraryEntity> findContainingLibraries(@NotNull VirtualFile file) {
        return List.of();
    }

    @Override
    public @NotNull Collection<SdkEntity> findContainingSdks(@NotNull VirtualFile file) {
        return List.of();
    }

    @Override
    public boolean isUnderIgnored(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public @Nullable JpsModuleSourceRootType<?> getContainingSourceRootType(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public boolean isInGeneratedSources(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public @Nullable String getUnloadedModuleNameForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public boolean iterateContent(@NotNull ContentIterator processor) {
        return true;
    }

    @Override
    public boolean iterateContent(@NotNull ContentIterator processor, @Nullable VirtualFileFilter filter) {
        return true;
    }

    @Override
    public boolean iterateContentUnderDirectory(@NotNull VirtualFile dir, @NotNull ContentIterator processor) {
        return true;
    }

    @Override
    public boolean iterateContentUnderDirectory(@NotNull VirtualFile dir,
                                                @NotNull ContentIterator processor,
                                                @Nullable VirtualFileFilter filter) {
        return true;
    }

    @Override
    public boolean isInContent(@NotNull VirtualFile file) {
        return true;
    }

    @Override
    public boolean isInSourceContent(@NotNull VirtualFile file) {
        return true;
    }

    @Override
    public boolean isInTestSourceContent(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isUnderSourceRootOfType(@NotNull VirtualFile file,
                                           @NotNull Set<? extends JpsModuleSourceRootType<?>> rootTypes) {
        return false;
    }
}
