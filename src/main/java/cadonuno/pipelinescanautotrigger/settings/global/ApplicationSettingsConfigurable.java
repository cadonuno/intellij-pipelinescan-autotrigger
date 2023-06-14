package cadonuno.pipelinescanautotrigger.settings.global;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ApplicationSettingsConfigurable implements Configurable {
    private ApplicationSettingsComponent applicationSettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Pipeline Scan Auto-Trigger";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return applicationSettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        applicationSettingsComponent = new ApplicationSettingsComponent();
        return applicationSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ApplicationSettingsState settings = ApplicationSettingsState.getInstance();
        boolean modified = applicationSettingsComponent.isShouldFailOnVeryHigh() != settings.isShouldFailOnVeryHigh();
        modified |= !applicationSettingsComponent.getApiIdText().equals(settings.getApiId());
        modified |= !applicationSettingsComponent.getApiKeyText().equals(settings.getApiKey());
        modified |= applicationSettingsComponent.isShouldFailOnHigh() != settings.isShouldFailOnHigh();
        modified |= applicationSettingsComponent.isShouldFailOnMedium() != settings.isShouldFailOnMedium();
        modified |= applicationSettingsComponent.isShouldFailOnLow() != settings.isShouldFailOnLow();
        modified |= applicationSettingsComponent.isShouldFailOnInformational() != settings.isShouldFailOnInformational();
        modified |= applicationSettingsComponent.getCredentialsType() != settings.getCredentialsType();
        modified |= !applicationSettingsComponent.getCredentialsProfileName().equals(settings.getCredentialsProfileName());
        modified |= !applicationSettingsComponent.getProxyHost().equals(settings.getProxyHost());
        modified |= !applicationSettingsComponent.getProxyPort().equals(settings.getProxyPort());
        modified |= !applicationSettingsComponent.getProxyUsername().equals(settings.getProxyUsername());
        modified |= !applicationSettingsComponent.getProxyPassword().equals(settings.getProxyPassword());
        modified |= !applicationSettingsComponent.getOptArgs().equals(settings.getOptArgs());
        return modified;
    }

    @Override
    public void apply() {
        ApplicationSettingsState settings = ApplicationSettingsState.getInstance();
        settings.setApiId(applicationSettingsComponent.getApiIdText());
        settings.setApiKey(applicationSettingsComponent.getApiKeyText());
        settings.setShouldFailOnVeryHigh(applicationSettingsComponent.isShouldFailOnVeryHigh());
        settings.setShouldFailOnHigh(applicationSettingsComponent.isShouldFailOnHigh());
        settings.setShouldFailOnMedium(applicationSettingsComponent.isShouldFailOnMedium());
        settings.setShouldFailOnLow(applicationSettingsComponent.isShouldFailOnLow());
        settings.setShouldFailOnInformational(applicationSettingsComponent.isShouldFailOnInformational());
        settings.setCredentialsType(applicationSettingsComponent.getCredentialsType());
        settings.setCredentialsProfileName(applicationSettingsComponent.getCredentialsProfileName());
        settings.setProxyHost(applicationSettingsComponent.getProxyHost());
        settings.setProxyPort(applicationSettingsComponent.getProxyPort());
        settings.setProxyUsername(applicationSettingsComponent.getProxyUsername());
        settings.setProxyPassword(applicationSettingsComponent.getProxyPassword());
        settings.setOptArgs(applicationSettingsComponent.getOptArgs());
    }

    @Override
    public void reset() {
        ApplicationSettingsState settings = ApplicationSettingsState.getInstance();
        applicationSettingsComponent.setApiIdText(settings.getApiId());
        applicationSettingsComponent.setApiKeyText(settings.getApiKey());
        applicationSettingsComponent.setShouldFailOnVeryHigh(settings.isShouldFailOnVeryHigh());
        applicationSettingsComponent.setShouldFailOnHigh(settings.isShouldFailOnHigh());
        applicationSettingsComponent.setShouldFailOnMedium(settings.isShouldFailOnMedium());
        applicationSettingsComponent.setShouldFailOnLow(settings.isShouldFailOnLow());
        applicationSettingsComponent.setShouldFailOnInformational(settings.isShouldFailOnInformational());
        applicationSettingsComponent.setCredentialsType(settings.getCredentialsType());
        applicationSettingsComponent.setCredentialsProfileName(settings.getCredentialsProfileName());
        applicationSettingsComponent.setProxyHost(settings.getProxyHost());
        applicationSettingsComponent.setProxyPort(settings.getProxyPort());
        applicationSettingsComponent.setProxyUsername(settings.getProxyUsername());
        applicationSettingsComponent.setProxyPassword(settings.getProxyPassword());
        applicationSettingsComponent.setOptArgs(settings.getOptArgs());
    }

    @Override
    public void disposeUIResources() {
        applicationSettingsComponent = null;
    }
}
