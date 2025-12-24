package com.intellij.formatter.services.psi;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.JavaResolveResult;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiResolveHelper;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.impl.source.resolve.ParameterTypeInferencePolicy;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.infos.MethodCandidateInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal implementation of {@link PsiResolveHelper} for standalone formatting.
 *
 * <p>This implementation provides stub behavior for PSI resolution operations.
 * All accessibility checks return true, and resolve operations return empty results,
 * which is appropriate for code formatting that doesn't require actual symbol resolution.</p>
 *
 * @see PsiResolveHelper
 * @see JavaPsiFacadeService
 */
class MinimalPsiResolveHelper implements PsiResolveHelper {

    @Override
    public @NotNull JavaResolveResult resolveConstructor(@NotNull PsiClassType type,
                                                         @NotNull PsiExpressionList argumentList,
                                                         @NotNull PsiElement place) {
        return JavaResolveResult.EMPTY;
    }

    @Override
    public JavaResolveResult @NotNull [] multiResolveConstructor(@NotNull PsiClassType type,
                                                                 @NotNull PsiExpressionList argumentList,
                                                                 @NotNull PsiElement place) {
        return JavaResolveResult.EMPTY_ARRAY;
    }

    @Override
    public @NotNull CandidateInfo @NotNull [] getReferencedMethodCandidates(@NotNull PsiCallExpression call,
                                                                            boolean dummyImplicitConstructor) {
        return CandidateInfo.EMPTY_ARRAY;
    }

    @Override
    public @NotNull CandidateInfo @NotNull [] getReferencedMethodCandidates(@NotNull PsiCallExpression call,
                                                                            boolean dummyImplicitConstructor,
                                                                            boolean checkVarargs) {
        return CandidateInfo.EMPTY_ARRAY;
    }

    @Override
    public boolean hasOverloads(@NotNull PsiCallExpression call) {
        return false;
    }

    @Override
    public @Nullable PsiClass resolveReferencedClass(@NotNull String referenceText, @NotNull PsiElement context) {
        return null;
    }

    @Override
    public @Nullable PsiVariable resolveReferencedVariable(@NotNull String referenceText, @NotNull PsiElement context) {
        return null;
    }

    @Override
    public @Nullable PsiVariable resolveAccessibleReferencedVariable(@NotNull String referenceText, @NotNull PsiElement context) {
        return null;
    }

    @Override
    public boolean isAccessible(@NotNull PsiMember member,
                                @NotNull PsiElement place,
                                @Nullable PsiClass accessObjectClass) {
        return true;
    }

    @Override
    public boolean isAccessible(@NotNull PsiMember member,
                                @Nullable PsiModifierList modifierList,
                                @NotNull PsiElement place,
                                @Nullable PsiClass accessObjectClass,
                                @Nullable PsiElement currentFileResolveScope) {
        return true;
    }

    @Override
    public boolean isAccessible(@NotNull PsiPackage pkg, @NotNull PsiElement place) {
        return true;
    }

    @Override
    public @Nullable PsiType inferTypeForMethodTypeParameter(@NotNull PsiTypeParameter typeParameter,
                                                             PsiParameter @NotNull [] parameters,
                                                             PsiExpression @NotNull [] arguments,
                                                             @NotNull PsiSubstitutor partialSubstitutor,
                                                             @Nullable PsiElement parent,
                                                             @NotNull ParameterTypeInferencePolicy policy) {
        return null;
    }

    @Override
    public @NotNull PsiSubstitutor inferTypeArguments(PsiTypeParameter @NotNull [] typeParameters,
                                                      PsiParameter @NotNull [] parameters,
                                                      PsiExpression @NotNull [] arguments,
                                                      @NotNull PsiSubstitutor partialSubstitutor,
                                                      @NotNull PsiElement parent,
                                                      @NotNull ParameterTypeInferencePolicy policy) {
        return partialSubstitutor;
    }

    @Override
    public @NotNull PsiSubstitutor inferTypeArguments(PsiTypeParameter @NotNull [] typeParameters,
                                                      PsiParameter @NotNull [] parameters,
                                                      PsiExpression @NotNull [] arguments,
                                                      @NotNull MethodCandidateInfo info,
                                                      @NotNull PsiElement parent,
                                                      @NotNull ParameterTypeInferencePolicy policy,
                                                      @NotNull LanguageLevel languageLevel) {
        return info.getSubstitutor();
    }

    @Override
    public @NotNull PsiSubstitutor inferTypeArguments(PsiTypeParameter @NotNull [] typeParameters,
                                                      PsiType @NotNull [] leftTypes,
                                                      PsiType @NotNull [] rightTypes,
                                                      @NotNull LanguageLevel languageLevel) {
        return PsiSubstitutor.EMPTY;
    }

    @Override
    public @Nullable PsiType getSubstitutionForTypeParameter(PsiTypeParameter typeParam,
                                                             PsiType param,
                                                             PsiType arg,
                                                             boolean isContraVariantPosition,
                                                             @NotNull LanguageLevel languageLevel) {
        return null;
    }

    @Override
    public @NotNull LanguageLevel getEffectiveLanguageLevel(@Nullable VirtualFile file) {
        return LanguageLevel.HIGHEST;
    }
}
