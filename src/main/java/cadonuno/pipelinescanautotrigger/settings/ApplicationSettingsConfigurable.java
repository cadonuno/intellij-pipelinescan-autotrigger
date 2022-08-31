package cadonuno.pipelinescanautotrigger.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ApplicationSettingsConfigurable implements Configurable {
    private ApplicationSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Pipeline Scan Auto-Trigger";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new ApplicationSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ApplicationSettingsState settings = ApplicationSettingsState.getInstance();
        boolean modified = !mySettingsComponent.getFileToScanText().equals(settings.getFileToScan());
        modified |= mySettingsComponent.isShouldFailOnVeryHigh() != settings.isShouldFailOnVeryHigh();
        modified |= !mySettingsComponent.getApiIdText().equals(settings.getApiId());
        modified |= !mySettingsComponent.getApiKeyText().equals(settings.getApiKey());
        modified |= mySettingsComponent.isShouldFailOnHigh() != settings.isShouldFailOnHigh();
        modified |= mySettingsComponent.isShouldFailOnMedium() != settings.isShouldFailOnMedium();
        modified |= mySettingsComponent.isShouldFailOnLow() != settings.isShouldFailOnLow();
        modified |= mySettingsComponent.isShouldFailOnInformational() != settings.isShouldFailOnInformational();
        modified |= mySettingsComponent.isEnabled() != settings.isEnabled();
        return modified;
    }

    @Override
    public void apply() {
        ApplicationSettingsState settings = ApplicationSettingsState.getInstance();
        settings.setFileToScan(mySettingsComponent.getFileToScanText());
        settings.setApiId(mySettingsComponent.getApiIdText());
        settings.setApiKey(mySettingsComponent.getApiKeyText());
        settings.setShouldFailOnVeryHigh(mySettingsComponent.isShouldFailOnVeryHigh());
        settings.setShouldFailOnHigh(mySettingsComponent.isShouldFailOnHigh());
        settings.setShouldFailOnMedium(mySettingsComponent.isShouldFailOnMedium());
        settings.setShouldFailOnLow(mySettingsComponent.isShouldFailOnLow());
        settings.setShouldFailOnInformational(mySettingsComponent.isShouldFailOnInformational());
        settings.setEnabled(mySettingsComponent.isEnabled());
    }

    @Override
    public void reset() {
        ApplicationSettingsState settings = ApplicationSettingsState.getInstance();
        mySettingsComponent.setFileToScanText(settings.getFileToScan());
        mySettingsComponent.setApiIdText(settings.getApiId());
        mySettingsComponent.setApiKeyText(settings.getApiKey());
        mySettingsComponent.setShouldFailOnVeryHigh(settings.isShouldFailOnVeryHigh());
        mySettingsComponent.setShouldFailOnHigh(settings.isShouldFailOnHigh());
        mySettingsComponent.setShouldFailOnMedium(settings.isShouldFailOnMedium());
        mySettingsComponent.setShouldFailOnLow(settings.isShouldFailOnLow());
        mySettingsComponent.setShouldFailOnInformational(settings.isShouldFailOnInformational());
        mySettingsComponent.setEnabled(settings.isEnabled());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
