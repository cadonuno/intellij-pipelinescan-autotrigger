package cadonuno.pipelinescanautotrigger.ui;

import javax.swing.*;

public class FindingsPanelOwner {
    private final JPanel panel;
    private JButton startScanButton;

    public FindingsPanelOwner(JPanel panel) {
        this.panel = panel;
    }


    public JPanel getPanel() {
        return panel;
    }

    public JButton getStartScanButton() {
        return startScanButton;
    }

    public void setStartScanButton(JButton startScanButton) {
        this.startScanButton = startScanButton;
    }
}
