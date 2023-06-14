package cadonuno.pipelinescanautotrigger.settings.global;

import cadonuno.pipelinescanautotrigger.settings.credentials.CredentialsTypeEnum;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(name = "cadonuno.pipelinescanautotrigger.settings.PipelineScanAutoTriggerConfigurationService",
        storages = @Storage("PipelineAutoTrigger.xml"))
public class ApplicationSettingsState implements PersistentStateComponent<ApplicationSettingsState> {
    private boolean shouldFailOnVeryHigh = true;
    private boolean shouldFailOnHigh = true;
    private boolean shouldFailOnMedium = false;
    private boolean shouldFailOnLow = false;
    private boolean shouldFailOnInformational = false;

    private String apiId = "";
    private String apiKey = "";

    private CredentialsTypeEnum credentialsType = CredentialsTypeEnum.LiteralCredentials;
    private String credentialsProfileName = "default";

    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String optArgs;

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

    public String getFailOnSeverity() {
        StringBuilder failOnSeverityBuilder = new StringBuilder();
        boolean isFirst = true;
        if (shouldFailOnVeryHigh) {
            failOnSeverityBuilder.append("VeryHigh");
            isFirst = false;
        }
        isFirst = appendSeverityIfEnabled(failOnSeverityBuilder, shouldFailOnHigh, "High", isFirst);
        isFirst = appendSeverityIfEnabled(failOnSeverityBuilder, shouldFailOnMedium, "Medium", isFirst);
        isFirst = appendSeverityIfEnabled(failOnSeverityBuilder, shouldFailOnLow, "Low", isFirst);
        appendSeverityIfEnabled(failOnSeverityBuilder, shouldFailOnInformational, "Informational", isFirst);
        return failOnSeverityBuilder.toString();
    }

    private boolean appendSeverityIfEnabled(StringBuilder failOnSeverityBuilder, boolean shouldFail,
                                            String severityAsString, boolean isFirst) {
        if (shouldFail) {
            if (!isFirst) {
                failOnSeverityBuilder.append(",");
            }
            failOnSeverityBuilder.append(severityAsString);
            return false;
        }
        return true;
    }

    public CredentialsTypeEnum getCredentialsType() {
        return credentialsType;
    }

    public void setCredentialsType(CredentialsTypeEnum credentialsType) {
        this.credentialsType = credentialsType;
    }

    public String getCredentialsProfileName() {
        return credentialsProfileName;
    }

    public void setCredentialsProfileName(String credentialsProfileName) {
        this.credentialsProfileName = credentialsProfileName;
    }

    public String getOptArgs() {
        return optArgs;
    }

    public void setOptArgs(String optArgs) {
        this.optArgs = optArgs;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
}
