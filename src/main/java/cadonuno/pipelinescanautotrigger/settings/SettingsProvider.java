package cadonuno.pipelinescanautotrigger.settings;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.options.ex.Settings;
import org.jetbrains.annotations.Nullable;

public class SettingsProvider {
    private static Settings settings = null;
    public static Settings getSettingInstance() {
        if (settings == null) {
            settings = getSettingsInstanceInternal();
        }

        return settings;
    }

    @Nullable
    private static Settings getSettingsInstanceInternal() {
        DataContext context = DataManager.getInstance().getDataContextFromFocus().getResult();
        return context != null ? Settings.KEY.getData(context) : null;
    }
}
