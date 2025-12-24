package com.intellij.formatter.services.psi;

import com.intellij.injected.editor.DocumentWindow;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.BooleanRunnable;
import com.intellij.psi.impl.PsiDocumentManagerBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Service for managing PSI-to-Document associations in standalone formatting mode.
 *
 * <p>This implementation bridges the PSI (Program Structure Interface) and document
 * systems, enabling the formatter to work with both representations of source code.</p>
 *
 * @see PsiDocumentManagerBase
 */
public class PsiDocumentService extends PsiDocumentManagerBase {

    private final Project project;

    /**
     * Creates a new PSI document service for the specified project.
     *
     * @param project the project context
     */
    public PsiDocumentService(@NotNull Project project) {
        super(project);
        this.project = project;
    }

    @Override
    public @Nullable PsiFile getPsiFile(@NotNull Document document) {
        var file = FileDocumentManager.getInstance().getFile(document);
        return file != null ? PsiManager.getInstance(project).findFile(file) : null;
    }

    @Override
    public @Nullable Document getCachedDocument(@NotNull PsiFile file) {
        var vFile = file.getVirtualFile();
        return vFile != null ? FileDocumentManager.getInstance().getDocument(vFile) : null;
    }

    @Override
    protected boolean finishCommitInWriteAction(@NotNull Document document,
                                                @NotNull List<? extends BooleanRunnable> finishProcessors,
                                                @NotNull List<? extends BooleanRunnable> reparseInjectedProcessors,
                                                boolean synchronously) {
        return true;
    }

    @Override
    protected DocumentWindow freezeWindow(@NotNull DocumentWindow document) {
        return document;
    }

    @Override
    protected void beforeDocumentChangeOnUnlockedDocument(@NotNull FileViewProvider viewProvider) {
    }
}
