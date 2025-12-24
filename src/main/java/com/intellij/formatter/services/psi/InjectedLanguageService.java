package com.intellij.formatter.services.psi;

import com.intellij.injected.editor.DocumentWindow;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.lang.injection.InjectedLanguageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Minimal implementation of {@link InjectedLanguageManager} for standalone formatting.
 *
 * <p>This implementation provides stub behavior for language injection operations,
 * as language injection is not required for basic code formatting.</p>
 *
 * @see InjectedLanguageManager
 */
public class InjectedLanguageService extends InjectedLanguageManager {

    @Override
    public PsiLanguageInjectionHost getInjectionHost(@NotNull FileViewProvider injectedProvider) {
        return null;
    }

    @Override
    public @Nullable PsiLanguageInjectionHost getInjectionHost(@NotNull PsiElement injectedElement) {
        return null;
    }

    @Override
    public @NotNull TextRange injectedToHost(@NotNull PsiElement injectedContext, @NotNull TextRange injectedTextRange) {
        return injectedTextRange;
    }

    @Override
    public int injectedToHost(@NotNull PsiElement injectedContext, int injectedOffset) {
        return injectedOffset;
    }

    @Override
    public int injectedToHost(@NotNull PsiElement injectedContext, int injectedOffset, boolean minHostOffset) {
        return injectedOffset;
    }

    @Override
    public void registerMultiHostInjector(@NotNull MultiHostInjector injector, @NotNull Disposable parentDisposable) {
    }

    @Override
    public @NotNull String getUnescapedText(@NotNull PsiElement injectedNode) {
        return injectedNode.getText();
    }

    @Override
    public @NotNull List<TextRange> intersectWithAllEditableFragments(@NotNull PsiFile injectedPsi,
                                                                      @NotNull TextRange rangeToEdit) {
        return List.of(rangeToEdit);
    }

    @Override
    public boolean isInjectedFragment(@NotNull PsiFile injectedFile) {
        return false;
    }

    @Override
    public @Nullable PsiElement findInjectedElementAt(@NotNull PsiFile hostFile, int hostDocumentOffset) {
        return null;
    }

    @Override
    public @Nullable List<Pair<PsiElement, TextRange>> getInjectedPsiFiles(@NotNull PsiElement host) {
        return null;
    }

    @Override
    public void dropFileCaches(@NotNull PsiFile file) {
    }

    @Override
    public PsiFile getTopLevelFile(@NotNull PsiElement element) {
        return element.getContainingFile();
    }

    @Override
    public @NotNull List<DocumentWindow> getCachedInjectedDocumentsInRange(@NotNull PsiFile hostPsiFile,
                                                                           @NotNull TextRange range) {
        return List.of();
    }

    @Override
    public void enumerate(@NotNull PsiElement host, @NotNull PsiLanguageInjectionHost.InjectedPsiVisitor visitor) {
    }

    @Override
    public void enumerateEx(@NotNull PsiElement host,
                            @NotNull PsiFile containingFile,
                            boolean probeUp,
                            @NotNull PsiLanguageInjectionHost.InjectedPsiVisitor visitor) {
    }

    @Override
    public @NotNull List<TextRange> getNonEditableFragments(@NotNull DocumentWindow window) {
        return List.of();
    }

    @Override
    public boolean mightHaveInjectedFragmentAtOffset(@NotNull Document hostDocument, int hostOffset) {
        return false;
    }

    @Override
    public @NotNull DocumentWindow freezeWindow(@NotNull DocumentWindow document) {
        return document;
    }

    @Override
    public boolean shouldInspectionsBeLenient(@NotNull PsiElement element) {
        return false;
    }

    @Override
    public boolean isFrankensteinInjection(@NotNull PsiElement element) {
        return false;
    }

    @Override
    public boolean isInjectedViewProvider(@NotNull FileViewProvider viewProvider) {
        return false;
    }
}
