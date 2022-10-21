/*******************************************************************************
 * Copyright (c) 2017 Veracode, Inc. All rights observed.
 *
 * Available for use by Veracode customers as described in the accompanying license agreement.
 *
 * Send bug reports or enhancement requests to support@veracode.com.
 *
 * See the license agreement for conditions on submitted materials.
 ******************************************************************************/
package cadonuno.pipelinescanautotrigger.ui;


import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanFinding;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class creates the scan results window Created by Virtusa on 2/22/2017.
 */
public class ScanResultsWindow implements ToolWindowFactory {
    private static final Color HYPERLINK_COLOR = new JBColor(new Color(1, 108, 93),
            new Color(1, 108, 93));

    private static Color defaultColor = null;
    private static Integer defaultWidth = null;

    public static final int DETAILS_COLUMN_INDEX = 4;
    private static ScanResultsWindow instance;
    private JBTable allResultsTable;
    private JBTable filteredResultsTable;
    private Content filteredFindingsParent;
    private Content allFindingsParent;
    private ToolWindow toolWindow;

    private Project project;

    private final Map<Long, String> detailsMap = new HashMap<>();

    public ScanResultsWindow() {
        instance = this;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        initializeTables();
        toolWindow.getContentManager().addContent(filteredFindingsParent);
        toolWindow.activate(null);
    }

    private void initializeTables() {
        DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (defaultWidth == null) {
                    defaultWidth = component.getWidth();
                }
                if (column == 0) {
                    component.setSize(0, component.getHeight());
                }
                if (column == DETAILS_COLUMN_INDEX) {
                    component.setForeground(HYPERLINK_COLOR);
                } else if (defaultColor == null) {
                    defaultColor = component.getForeground();
                } else {
                    component.setForeground(defaultColor);
                }
                return component;
            }
        };

        allResultsTable = new JBTable();
        allResultsTable.setName("All Findings");
        allResultsTable.setModel(new FindingResultsTableModel());
        allResultsTable.setDefaultRenderer(Object.class, tableRenderer);
        allResultsTable.setRowSelectionAllowed(true);
        JPanel allFindingsPanel = FormBuilder.createFormBuilder()
                .addComponent(new JScrollPane(allResultsTable))
                .getPanel();
        allFindingsPanel.setLayout(new GridLayout(1, 1));

        filteredResultsTable = new JBTable();
        filteredResultsTable.setName("Violating Findings");
        filteredResultsTable.setModel(new FindingResultsTableModel());
        filteredResultsTable.setRowSelectionAllowed(true);
        filteredResultsTable.setDefaultRenderer(Object.class, tableRenderer);
        JPanel filteredFindingsPanel = FormBuilder.createFormBuilder()
                .addComponent(new JScrollPane(filteredResultsTable))
                .getPanel();
        filteredFindingsPanel.setLayout(new GridLayout(1, 1));

        allFindingsParent = toolWindow.getContentManager().getFactory().createContent(allFindingsPanel,
                "All Findings (0)",
                true);
        toolWindow.getContentManager().addContent(allFindingsParent);

        filteredFindingsParent = toolWindow.getContentManager().getFactory().createContent(filteredFindingsPanel,
                "Violating Findings (0)",
                true);
        toolWindow.getContentManager().addContent(filteredFindingsParent);

        filteredResultsTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                mousePressedOnTableEvent(mouseEvent, filteredResultsTable);
            }
        });

        allResultsTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                mousePressedOnTableEvent(mouseEvent, allResultsTable);
            }
        });
    }

    private void mousePressedOnTableEvent(MouseEvent mouseEvent, JBTable clickedTable) {
        Point point = mouseEvent.getPoint();
        int row = clickedTable.rowAtPoint(point);
        int col = clickedTable.columnAtPoint(point);
        if (row == -1) {
            return;
        }
        if (col == DETAILS_COLUMN_INDEX) {
            Optional.ofNullable(detailsMap.get((long) clickedTable.getModel().getValueAt(row, 0)))
                    .ifPresent(DetailsDialog::new);

        } else if (mouseEvent.getClickCount() == 2) {
            jumpToFinding(project, clickedTable, row);
        }
    }

    private void jumpToFinding(Project project, JBTable clickedTable, int row) {
        String fileName = (String) clickedTable.getModel().getValueAt(row, 5);
        long lineNumber = (long) clickedTable.getModel().getValueAt(row, 6);
        PsiFile[] psiFile;

        if (fileName.contains("/")) {
            String[] nameParts = fileName.split("/");
            fileName = nameParts[nameParts.length - 1];
        }
        psiFile = FilenameIndex.getFilesByName(project, fileName,
                GlobalSearchScope.projectScope(project));

        PsiFile issueFile = null;
        for (PsiFile currentFile : psiFile) {
            if (currentFile.getVirtualFile().getPath().endsWith(fileName)) {
                issueFile = currentFile;
            }
        }
        if (issueFile == null) {
            return;
        }
        OpenFileDescriptor desc =
                new OpenFileDescriptor(project, issueFile.getVirtualFile(), (int) (lineNumber - 1), 0);
        desc.navigate(true);
    }

    public static void updateResults(Project project, List<PipelineScanFinding> results,
                                     List<PipelineScanFinding> filteredResults) {
        instance.updateResultsInternal(project, results, filteredResults);
    }

    private void updateResultsInternal(Project project,
                                       List<PipelineScanFinding> results, List<PipelineScanFinding> filteredResults) {
        this.project = project;
        FindingResultsTableModel allFindingsModel = new FindingResultsTableModel();
        results.forEach(element -> {
            detailsMap.put(element.getIssueId(), element.getDetails());
            allFindingsModel.addRow(element.getAsTableRow());
        });
        allResultsTable.setModel(allFindingsModel);
        allFindingsParent.setDisplayName("All Findings (" + results.size() + ")");

        FindingResultsTableModel filteredResultsModel = new FindingResultsTableModel();
        filteredResults.stream().map(PipelineScanFinding::getAsTableRow).forEach(filteredResultsModel::addRow);
        filteredResultsTable.setModel(filteredResultsModel);
        filteredFindingsParent.setDisplayName("Violating Findings (" + filteredResults.size() + ")");
    }

    public void init(ToolWindow window) {
    }

    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}