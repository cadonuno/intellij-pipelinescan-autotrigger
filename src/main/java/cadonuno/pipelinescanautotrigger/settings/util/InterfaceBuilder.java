package cadonuno.pipelinescanautotrigger.settings.util;

import com.intellij.ui.components.JBLabel;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.util.List;

public class InterfaceBuilder {
    public static JComponent makeLabelWithSupportingLink(String baseLabelText, String linkText, String url) {
        JPanel panel = new JPanel();
        panel.setLayout(new HorizontalLayout());
        panel.add(new JBLabel(baseLabelText));
        panel.add(new JLinkLabel(linkText, url));
        return panel;
    }

    public static JPanel addBorderToPanel(JPanel panel) {
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return panel;
    }

    public static JPanel makeBasePanel(List<JComponent> components) {
        JPanel basePanel = new JPanel();
        basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
        components.forEach(basePanel::add);
        return basePanel;
    }
}
