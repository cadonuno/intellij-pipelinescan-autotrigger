package cadonuno.pipelinescanautotrigger;

import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanWrapper;
import cadonuno.pipelinescanautotrigger.settings.ApplicationSettingsState;
import com.intellij.dvcs.push.PrePushHandler;
import com.intellij.dvcs.push.PushInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PipelineScanAutoPrePushHandler implements PrePushHandler {
    @Override
    public @NotNull
    @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "Pipeline Scan Auto Trigger Listener";
    }

    @Override
    public @NotNull Result handle(@NotNull List<PushInfo> list, @NotNull ProgressIndicator progressIndicator) {
        ApplicationSettingsState applicationSettingsState = ApplicationSettingsState.getInstance();
        if (list.isEmpty() || !applicationSettingsState.isEnabled()) {
            return Result.OK;
        }
        boolean hasFailedScan = false;
        try (PipelineScanWrapper pipelineScanWrapper = PipelineScanWrapper.acquire()) {
            //call scan
            pipelineScanWrapper.startScan(applicationSettingsState);
            //analyze filtered_results json
            //TODO: implement way of picking file and fail criteria

            // if filtered results is not empty:
            hasFailedScan = pipelineScanWrapper.hasFailedScan();
        }
        return hasFailedScan ? Result.ABORT : Result.OK;
    }
}
