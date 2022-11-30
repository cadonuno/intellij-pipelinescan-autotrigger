package cadonuno.pipelinescanautotrigger.ui;

import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

public class DetailsDialog {
    private static final Logger LOG = Logger.getInstance(DetailsDialog.class);

    private static final Dimension BASE_SIZE = new Dimension(400, 100);
    private static final Dimension PREFERRED_SIZE = new Dimension(400, 1024);

    public static void show(String detailsHtml) {
        LOG.info("Creating details window for:");
        LOG.info(detailsHtml);
        JOptionPane.showMessageDialog(null, getEditorPane(detailsHtml), "Issue Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private static JEditorPane getEditorPane(String detailsHtml) {
        JEditorPane editorPane = new JEditorPane("text/html", wrapInHtmlTag(detailsHtml));
        editorPane.setEditable(false);
        editorPane.setSize(BASE_SIZE);
        editorPane.setMaximumSize(PREFERRED_SIZE);
        enableLinks(editorPane);
        return editorPane;
    }

    private static String wrapInHtmlTag(String detailsHtml) {
        return "<html><body style='width: 400px'>" + detailsHtml + "</body></html>";
    }

    private static void enableLinks(JEditorPane editorPane) {
        editorPane.addHyperlinkListener(hyperlinkEvent -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(hyperlinkEvent.getEventType())) {
                System.out.println(hyperlinkEvent.getURL());
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(hyperlinkEvent.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}

