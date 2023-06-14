package cadonuno.pipelinescanautotrigger.ui;

import javax.swing.*;

public final class MessageHandler {
    private MessageHandler() {
        // Not needed
    }

    public static void showMessagePopup(String aMessage) {
        JOptionPane.showMessageDialog(null, aMessage);
    }

    public static void showErrorPopup(String title, String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage,  title, JOptionPane.ERROR_MESSAGE);
    }
}
