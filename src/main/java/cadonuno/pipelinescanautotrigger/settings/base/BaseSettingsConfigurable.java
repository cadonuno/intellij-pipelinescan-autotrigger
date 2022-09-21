package cadonuno.pipelinescanautotrigger.settings.base;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class BaseSettingsConfigurable implements Configurable {
    private BaseSettingsComponent baseSettingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Global Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        baseSettingsComponent = new BaseSettingsComponent();
        return baseSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
        // do nothing
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return baseSettingsComponent.getPreferredFocusedComponent();
    }
}
