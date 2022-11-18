package cadonuno.pipelinescanautotrigger.pipelinescan;

import java.util.Objects;

public final class PipelineScanFinding {
    private final long issueId;
    private final String severity;
    private final String cweId;
    private final String cweName;
    private final String details;
    private final String fileName;
    private final long lineNumber;

    public PipelineScanFinding(long issueId, String severity, String cweId, String cweName,
                               String details, String fileName, long lineNumber) {
        this.issueId = issueId;
        this.severity = severity;
        this.cweId = cweId;
        this.cweName = cweName;
        this.details = details;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public Object[] getAsTableRow() {
        return new Object[]{getIssueId(), severity, cweId, cweName, "Show Details", getFileName(), getLineNumber()};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PipelineScanFinding) obj;
        return Objects.equals(this.severity, that.severity) &&
                Objects.equals(this.cweId, that.cweId) &&
                Objects.equals(this.getDetails(), that.getDetails()) &&
                Objects.equals(this.getFileName(), that.getFileName()) &&
                this.getLineNumber() == that.getLineNumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, cweId, getDetails(), getFileName(), getLineNumber());
    }

    @Override
    public String toString() {
        return "PipelineScanFinding[" +
                "severity=" + severity + ", " +
                "CweId=" + cweId + ", " +
                "details=" + getDetails() + ", " +
                "fileName=" + getFileName() + ", " +
                "lineNumber=" + getLineNumber() + ']';
    }

    public long getIssueId() {
        return issueId;
    }

    public String getDetails() {
        return details;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLineNumber() {
        return lineNumber;
    }
}
