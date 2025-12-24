package com.intellij.formatter.services.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiConstantEvaluationHelper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiJavaModule;
import com.intellij.psi.PsiJavaParserFacade;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiResolveHelper;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Minimal implementation of {@link JavaPsiFacade} for standalone formatting.
 *
 * <p>This implementation provides stub behavior for Java PSI operations
 * such as class lookup and package resolution, which are not required
 * for code formatting.</p>
 *
 * @see JavaPsiFacade
 * @see MinimalPsiResolveHelper
 */
@RequiredArgsConstructor
public class JavaPsiFacadeService extends JavaPsiFacade {

    @Getter
    private final Project project;
    
    @Getter
    private final PsiResolveHelper resolveHelper = new MinimalPsiResolveHelper();

    @Override
    public @Nullable PsiClass findClass(@NotNull String qualifiedName, @NotNull GlobalSearchScope scope) {
        return null;
    }

    @Override
    public PsiClass @NotNull [] findClasses(@NotNull String qualifiedName, @NotNull GlobalSearchScope scope) {
        return PsiClass.EMPTY_ARRAY;
    }

    @Override
    public boolean hasClass(@NotNull String qualifiedName, @NotNull GlobalSearchScope scope) {
        return false;
    }

    @Override
    public @Nullable PsiPackage findPackage(@NotNull String qualifiedName) {
        return null;
    }

    @Override
    public @Nullable PsiJavaModule findModule(@NotNull String moduleName, @NotNull GlobalSearchScope scope) {
        return null;
    }

    @Override
    public @NotNull Collection<PsiJavaModule> findModules(@NotNull String moduleName,
                                                          @NotNull GlobalSearchScope scope) {
        return List.of();
    }

    @Override
    public @NotNull PsiElementFactory getElementFactory() {
        return PsiElementFactory.getInstance(project);
    }

    @Override
    public @NotNull PsiJavaParserFacade getParserFacade() {
        return getElementFactory();
    }

    @Override
    public @NotNull PsiNameHelper getNameHelper() {
        return PsiNameHelper.getInstance(project);
    }

    @Override
    public @NotNull PsiConstantEvaluationHelper getConstantEvaluationHelper() {
        throw new UnsupportedOperationException("Constant evaluation not supported in standalone mode");
    }

    @Override
    public boolean isPartOfPackagePrefix(@NotNull String packageName) {
        return false;
    }

    @Override
    public boolean isInPackage(@NotNull PsiElement element, @NotNull PsiPackage aPackage) {
        return false;
    }

    @Override
    public boolean arePackagesTheSame(@NotNull PsiElement element1, @NotNull PsiElement element2) {
        return false;
    }

    @Override
    public boolean isConstantExpression(@NotNull PsiExpression expression) {
        return false;
    }
}
