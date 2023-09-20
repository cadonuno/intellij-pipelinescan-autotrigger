package cadonuno.pipelinescanautotrigger.settings.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class JLinkLabel extends JLabel {
    protected JLinkLabel(String linkText, String linkURL) {
        super(linkText);
        URI tempUri = null;
        try {
            tempUri = new URI(linkURL);
        } catch (URISyntaxException e) {
            //swallow, for now
        }
        setForeground(Color.BLUE.darker());
        setFont(new Font(getFont().getFontName(), getFont().getStyle(), 10));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        URI uri = tempUri;
        if (uri == null) {
            setVisible(false);
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(uri);
                    setForeground(Color.MAGENTA.darker());
                } catch (IOException ioe) {
                    // swallow for now
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setText("<html><a href=''>" + linkText + "</a></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setText(linkText);
            }
        });
    }
}
