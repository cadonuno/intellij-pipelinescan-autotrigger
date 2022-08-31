package cadonuno.pipelinescanautotrigger.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(name = "cadonuno.pipelinescanautotrigger.settings.PipelineScanAutoTriggerConfigurationService",
        storages = @Storage("SdkSettingsPlugin.xml"))
public class ApplicationSettingsState implements PersistentStateComponent<ApplicationSettingsState> {
    private String fileToScan = "./target/*";
    private boolean shouldFailOnVeryHigh = true;
    private boolean shouldFailOnHigh = true;
    private boolean shouldFailOnMedium = false;
    private boolean shouldFailOnLow = false;
    private boolean shouldFailOnInformational = false;

    private String apiId = "";
    private String apiKey = "";

    public static ApplicationSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ApplicationSettingsState.class);
    }

    @Override
    public @Nullable ApplicationSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApplicationSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getFileToScan() {
        return fileToScan;
    }

    public void setFileToScan(String fileToScan) {
        this.fileToScan = fileToScan;
    }

    public boolean isShouldFailOnInformational() {
        return shouldFailOnInformational;
    }

    public void setShouldFailOnInformational(boolean shouldFailOnInformational) {
        this.shouldFailOnInformational = shouldFailOnInformational;
    }

    public boolean isShouldFailOnLow() {
        return shouldFailOnLow;
    }

    public void setShouldFailOnLow(boolean shouldFailOnLow) {
        this.shouldFailOnLow = shouldFailOnLow;
    }

    public boolean isShouldFailOnMedium() {
        return shouldFailOnMedium;
    }

    public void setShouldFailOnMedium(boolean shouldFailOnMedium) {
        this.shouldFailOnMedium = shouldFailOnMedium;
    }

    public boolean isShouldFailOnHigh() {
        return shouldFailOnHigh;
    }

    public void setShouldFailOnHigh(boolean shouldFailOnHigh) {
        this.shouldFailOnHigh = shouldFailOnHigh;
    }

    public boolean isShouldFailOnVeryHigh() {
        return shouldFailOnVeryHigh;
    }

    public void setShouldFailOnVeryHigh(boolean shouldFailOnVeryHigh) {
        this.shouldFailOnVeryHigh = shouldFailOnVeryHigh;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }
}
