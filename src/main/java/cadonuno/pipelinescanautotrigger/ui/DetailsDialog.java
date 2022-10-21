package cadonuno.pipelinescanautotrigger.ui;


import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;

public class DetailsDialog extends JDialog {

    private static final Dimension MAXIMUM_SIZE = new Dimension(400, 460);
    private static final Dimension PREFERED_SIZE = new Dimension(400, 600);

    public DetailsDialog(String detailsHtml) {
        super();
        this.setTitle("Issue Details");

        JButton closeButton = new JButton("OK");
        closeButton.addActionListener(actionEvent -> this.dispose());
        closeButton.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JPanel());
        buttonPanel.add(closeButton);
        buttonPanel.add(new JPanel());
        buttonPanel.setLayout(new GridLayout(1,3));

        JEditorPane editorPane = new JEditorPane("text/html", detailsHtml);
        editorPane.setEditable(false);
        JPanel basePanel = FormBuilder.createFormBuilder()
                .addComponent(editorPane)
                .addComponent(new JBSplitter())
                .addComponent(buttonPanel)
                .getPanel();
        editorPane.setMaximumSize(MAXIMUM_SIZE);
        editorPane.setPreferredSize(PREFERED_SIZE);
        basePanel.setMaximumSize(MAXIMUM_SIZE);
        this.getContentPane().add(basePanel);
        this.setMaximumSize(MAXIMUM_SIZE);

        this.pack();
        
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
