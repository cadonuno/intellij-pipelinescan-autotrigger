package cadonuno.pipelinescanautotrigger.settings.base;

import cadonuno.pipelinescanautotrigger.settings.SettingsProvider;
import cadonuno.pipelinescanautotrigger.settings.global.ApplicationSettingsConfigurable;
import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsConfigurable;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.FormBuilder;
import org.jdesktop.swingx.JXHyperlink;

import javax.swing.*;

public class BaseSettingsComponent {
    private final JPanel mainPanel;
    private final JXHyperlink globalSettingsLink = new JXHyperlink();
    private final JXHyperlink projectSettingsLink = new JXHyperlink();

    public BaseSettingsComponent() {
        Settings settings = SettingsProvider.getSettingInstance();
        ApplicationSettingsConfigurable applicationSettingsConfigurable = null;
        ProjectSettingsConfigurable projectSettingsConfigurable = null;
        if (settings != null) {
            applicationSettingsConfigurable = settings.find(ApplicationSettingsConfigurable.class);
            projectSettingsConfigurable = settings.find(ProjectSettingsConfigurable.class);
        }
        globalSettingsLink.setClickedColor(JBColor.BLUE);
        projectSettingsLink.setClickedColor(JBColor.BLUE);
        globalSettingsLink.setText("Global Settings");
        projectSettingsLink.setText("Project Settings");

        ApplicationSettingsConfigurable finalApplicationSettingsConfigurable = applicationSettingsConfigurable;
        globalSettingsLink.addActionListener(e -> {
            if (finalApplicationSettingsConfigurable != null) {
                settings.select(finalApplicationSettingsConfigurable);
            }
        });
        ProjectSettingsConfigurable finalProjectSettingsConfigurable = projectSettingsConfigurable;
        projectSettingsLink.addActionListener(e -> {
            if (finalProjectSettingsConfigurable != null) {
                settings.select(finalProjectSettingsConfigurable);
            }
        });
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(globalSettingsLink)
                .addComponent(projectSettingsLink)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return globalSettingsLink;
    }

}
