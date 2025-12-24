package com.intellij.formatter.services.document;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DocumentCommitProcessor;
import com.intellij.psi.impl.PsiDocumentManagerBase;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal implementation of {@link DocumentCommitProcessor} for standalone formatting.
 *
 * <p>This implementation provides no-op commit operations since document
 * synchronization is not required in standalone formatting mode.</p>
 *
 * @see DocumentCommitProcessor
 */
public class DocumentCommitService implements DocumentCommitProcessor {

    @Override
    public void commitSynchronously(@NotNull Document document,
                                    @NotNull Project project,
                                    @NotNull PsiFile psiFile) {
    }

    @Override
    public void commitAsynchronously(@NotNull Project project,
                                     @NotNull PsiDocumentManagerBase documentManager,
                                     @NotNull Document document,
                                     @NotNull Object reason,
                                     @NotNull ModalityState modality) {
    }
}
