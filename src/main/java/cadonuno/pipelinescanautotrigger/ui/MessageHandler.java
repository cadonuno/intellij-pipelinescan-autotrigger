package cadonuno.pipelinescanautotrigger.ui;

import javax.swing.*;

public final class MessageHandler {
    private MessageHandler() {
        // Not needed
    }

    public static void showMessagePopup(String aMessage) {
        JOptionPane.showMessageDialog(null, aMessage);
    }
}
