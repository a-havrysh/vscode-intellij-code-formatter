package com.intellij.formatter.services.formatting;

import com.intellij.openapi.project.Project;
import com.intellij.pom.PomModelAspect;
import com.intellij.pom.core.impl.PomModelImpl;
import com.intellij.pom.tree.TreeAspect;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import lombok.Getter;

/**
 * Service providing POM (Program Object Model) functionality for standalone formatting.
 *
 * <p>This implementation provides the minimal model aspects required for code formatting:
 * {@link TreeAspect} for AST operations and {@link PostprocessReformattingAspect}
 * for post-formatting operations.</p>
 *
 * @see PomModelImpl
 * @see MinimalPostprocessReformattingAspect
 */
public class PomModelService extends PomModelImpl {

    @Getter
    private final PostprocessReformattingAspect reformattingAspect = new MinimalPostprocessReformattingAspect();

    @Getter
    private final TreeAspect treeAspect = new TreeAspect();

    /**
     * Creates a new POM model service for the specified project.
     *
     * @param project the project context
     */
    public PomModelService(Project project) {
        super(project);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PomModelAspect> T getModelAspect(Class<T> aClass) {
        if (aClass == PostprocessReformattingAspect.class) {
            return (T) reformattingAspect;
        }
        if (aClass == TreeAspect.class) {
            return (T) treeAspect;
        }
        return super.getModelAspect(aClass);
    }
}
