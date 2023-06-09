/*******************************************************************************
 * Copyright (c) 2017 Veracode, Inc. All rights observed.
 *
 * Available for use by Veracode customers as described in the accompanying license agreement.
 *
 * Send bug reports or enhancement requests to support@veracode.com.
 *
 * See the license agreement for conditions on submitted materials.
 ******************************************************************************/
package cadonuno.pipelinescanautotrigger.ui.issuedetails;


import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;
import java.util.Optional;

public class IssueDetailsToolWindow implements ToolWindowFactory {
    private static final String TOOL_WINDOW_ID = "Veracode Pipeline Scan Issue Details";
    private static IssueDetailsToolWindow instance;

    private JEditorPane detailsTextArea;

    public IssueDetailsToolWindow() {
        instance = this;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        initializePanel(toolWindow);
        toolWindow.activate(null);
    }

    private void initializePanel(ToolWindow toolWindow) {
        toolWindow.getContentManager().addContent(toolWindow.getContentManager().getFactory()
                .createContent(buildPanel(),
                        "Findings Details",
                        true));
    }

    private JPanel buildPanel() {
        JPanel basePanel = new JPanel();

        detailsTextArea = new JEditorPane("text/html", "") {
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        detailsTextArea.setEditable(false);
        enableLinks(detailsTextArea);

        JBScrollPane scrollPane = new JBScrollPane(detailsTextArea);

        basePanel.setLayout(new BorderLayout());
        basePanel.add(scrollPane, BorderLayout.CENTER);
        return basePanel;
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

    @Override
    public void init(@NotNull ToolWindow window) {
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return Optional.ofNullable(project.getService(ProjectSettingsState.class))
                .isPresent();
    }

    public static IssueDetailsToolWindow getCurrentOrMakeNewInstance() {
        return Optional.ofNullable(instance)
                .orElseGet(IssueDetailsToolWindow::new);
    }

    public void setDetailsAndShow(Project project, String detailsHtml) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow != null) {
            toolWindow.show();
        }
        if (toolWindow == null || detailsTextArea == null) {
            return;
        }
        detailsTextArea.setText(wrapInHtmlTag(detailsHtml));
    }

}