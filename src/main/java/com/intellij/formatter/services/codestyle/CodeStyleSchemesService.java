package com.intellij.formatter.services.codestyle;

import com.intellij.psi.codeStyle.CodeStyleScheme;
import com.intellij.psi.codeStyle.CodeStyleSchemes;
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemeImpl;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing code style schemes in standalone formatting mode.
 *
 * <p>This implementation provides a minimal code style scheme management
 * supporting a single default scheme. Custom schemes can be created and
 * added, but the functionality is simplified for standalone use.</p>
 *
 * @see CodeStyleSchemes
 * @see CodeStyleScheme
 */
public class CodeStyleSchemesService extends CodeStyleSchemes {

    private final List<CodeStyleScheme> schemes = new ArrayList<>();
    
    @Getter
    private final CodeStyleScheme defaultScheme;
    
    @Getter
    @Setter
    private CodeStyleScheme currentScheme;

    public CodeStyleSchemesService() {
        this.defaultScheme = new CodeStyleSchemeImpl("Default", true, null);
        this.schemes.add(defaultScheme);
        this.currentScheme = defaultScheme;
    }

    @Override
    public CodeStyleScheme createNewScheme(String name, CodeStyleScheme parentScheme) {
        return new CodeStyleSchemeImpl(name, false, parentScheme);
    }

    @Override
    public void deleteScheme(@NotNull CodeStyleScheme scheme) {
        if (!scheme.isDefault()) {
            schemes.remove(scheme);
        }
    }

    @Override
    public @Nullable CodeStyleScheme findSchemeByName(@NotNull String name) {
        return schemes.stream()
                .filter(scheme -> name.equals(scheme.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addScheme(@NotNull CodeStyleScheme scheme) {
        if (!schemes.contains(scheme)) {
            schemes.add(scheme);
        }
    }

    @Override
    public List<CodeStyleScheme> getAllSchemes() {
        return new ArrayList<>(schemes);
    }
}
