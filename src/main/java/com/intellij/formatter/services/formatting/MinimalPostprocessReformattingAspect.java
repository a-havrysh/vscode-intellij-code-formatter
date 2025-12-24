package com.intellij.formatter.services.formatting;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Computable;
import com.intellij.pom.event.PomModelEvent;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;

/**
 * Minimal implementation of {@link PostprocessReformattingAspect} for standalone formatting.
 *
 * <p>This implementation provides pass-through behavior for all reformatting operations,
 * executing runnables and computables immediately without any postponement or
 * post-processing logic.</p>
 *
 * @see PostprocessReformattingAspect
 * @see PomModelService
 */
class MinimalPostprocessReformattingAspect extends PostprocessReformattingAspect {

    @Override
    public void disablePostprocessFormattingInside(Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> T disablePostprocessFormattingInside(Computable<T> computable) {
        return computable.compute();
    }

    @Override
    public void postponeFormattingInside(Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> T postponeFormattingInside(Computable<T> computable) {
        return computable.compute();
    }

    @Override
    public void forcePostprocessFormatInside(PsiFile psiFile, Runnable runnable) {
        runnable.run();
    }

    @Override
    public void forcePostprocessFormat(PsiFile psiFile, Disposable disposable) {
        // No-op in standalone mode
    }

    @Override
    public void update(PomModelEvent pomModelEvent) {
        // No-op in standalone mode
    }

    @Override
    public void doPostponedFormatting() {
        // No-op in standalone mode
    }

    @Override
    public void doPostponedFormatting(FileViewProvider fileViewProvider) {
        // No-op in standalone mode
    }

    @Override
    public boolean isViewProviderLocked(FileViewProvider fileViewProvider) {
        return false;
    }

    @Override
    public boolean isDocumentLocked(Document document) {
        return false;
    }

    @Override
    public void assertDocumentChangeIsAllowed(FileViewProvider fileViewProvider) {
        // No-op in standalone mode
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public void clear() {
        // No-op in standalone mode
    }
}
