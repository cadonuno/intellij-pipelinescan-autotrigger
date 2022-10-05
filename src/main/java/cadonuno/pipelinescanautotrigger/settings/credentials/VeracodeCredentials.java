package cadonuno.pipelinescanautotrigger.settings.credentials;

import java.util.Objects;

public final class VeracodeCredentials {
    private final String apiId;
    private final String apiKey;

    public VeracodeCredentials(String apiId, String apiKey) {
        this.apiId = apiId;
        this.apiKey = apiKey;
    }

    public String getApiId() {
        return apiId;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VeracodeCredentials) obj;
        return Objects.equals(this.apiId, that.apiId) &&
                Objects.equals(this.apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiId, apiKey);
    }

    @Override
    public String toString() {
        return "VeracodeCredentials[" +
                "apiId=" + apiId + ", " +
                "apiKey=" + apiKey + ']';
    }

}
