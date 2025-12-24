package com.intellij.formatter.services.formatting;

import com.intellij.formatting.FormattingProgressCallback;
import com.intellij.formatting.FormattingProgressCallbackFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal implementation of {@link FormattingProgressCallbackFactory} for standalone formatting.
 *
 * <p>This implementation returns null for progress callbacks since progress
 * reporting is not required in standalone formatting mode.</p>
 *
 * @see FormattingProgressCallbackFactory
 */
public class FormattingProgressCallbackService implements FormattingProgressCallbackFactory {

    @Override
    public @Nullable FormattingProgressCallback createProgressCallback(@NotNull Project project,
                                                                       @NotNull PsiFile psiFile,
                                                                       @Nullable Document document) {
        return null;
    }
}
