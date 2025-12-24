package com.intellij.formatter.services.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.ResolveScopeManager;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal implementation of {@link ResolveScopeManager} for standalone formatting.
 *
 * <p>This implementation returns global scope for all resolve operations,
 * which is sufficient for code formatting without actual symbol resolution.</p>
 *
 * @see ResolveScopeManager
 */
@RequiredArgsConstructor
public class ResolveScopeService extends ResolveScopeManager {

    private final Project project;

    @Override
    public @NotNull GlobalSearchScope getResolveScope(@NotNull PsiElement element) {
        return GlobalSearchScope.allScope(project);
    }

    @Override
    public @NotNull GlobalSearchScope getDefaultResolveScope(@NotNull VirtualFile vFile) {
        return GlobalSearchScope.allScope(project);
    }

    @Override
    public @NotNull GlobalSearchScope getUseScope(@NotNull PsiElement element) {
        return GlobalSearchScope.allScope(project);
    }
}
