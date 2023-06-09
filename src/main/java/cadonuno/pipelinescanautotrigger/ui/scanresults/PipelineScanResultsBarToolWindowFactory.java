/*******************************************************************************
 * Copyright (c) 2017 Veracode, Inc. All rights observed.
 *
 * Available for use by Veracode customers as described in the accompanying license agreement.
 *
 * Send bug reports or enhancement requests to support@veracode.com.
 *
 * See the license agreement for conditions on submitted materials.
 ******************************************************************************/
package cadonuno.pipelinescanautotrigger.ui.scanresults;


import cadonuno.pipelinescanautotrigger.PipelineScanAutoPrePushHandler;
import cadonuno.pipelinescanautotrigger.pipelinescan.PipelineScanFinding;
import cadonuno.pipelinescanautotrigger.settings.project.ProjectSettingsState;
import cadonuno.pipelinescanautotrigger.ui.issuedetails.IssueDetailsToolWindow;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.TaskInfo;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.concurrency.SwingWorker;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class PipelineScanResultsBarToolWindowFactory implements ToolWindowFactory {
    private static final String TOOL_WINDOW_ID = "Veracode Pipeline Scan Results";
    private static final Color HYPERLINK_COLOR = new JBColor(new Color(1, 108, 93),
            new Color(1, 108, 93));
    private static final String ALL_FINDINGS = "All Findings";
    private static final String FINDINGS_VIOLATING_CRITERIA = "Findings Violating Criteria";
    private static final GridLayoutManager MAIN_PANEL_LAYOUT = new GridLayoutManager(2, 1,
            JBUI.emptyInsets(), -1, -1);

    private static final GridLayout SINGLE_ITEM_LAYOUT = new GridLayout(1, 1);
    private static final int RESULTS_FOOTER_HEIGHT = 35;
    private static final Dimension RESULTS_FOOTER_MINIMUM_SIZE = new Dimension(100, RESULTS_FOOTER_HEIGHT);
    private static final Dimension RESULTS_FOOTER_PREFERRED_SIZE = new Dimension(512, RESULTS_FOOTER_HEIGHT);
    private static final Dimension RESULTS_FOOTER_MAXIMUM_SIZE = new Dimension(1920, RESULTS_FOOTER_HEIGHT);
    private static final TaskInfo PROGRESS_TASK_INFO = new TaskInfo() {
        @Override
        public @NotNull
        @NlsContexts.ProgressTitle String getTitle() {
            return "Veracode pipeline scan";
        }

        @Override
        public @NlsContexts.Button String getCancelText() {
            return "Cancel";
        }

        @Override
        public @NlsContexts.Tooltip String getCancelTooltipText() {
            return null;
        }

        @Override
        public boolean isCancellable() {
            return true;
        }
    };

    private static Color defaultColor = null;
    private static Integer defaultWidth = null;

    public static final int DETAILS_COLUMN_INDEX = 4;
    private final static Map<Project, ToolWindowOwner> projectToToolWindowMap = new HashMap<>();

    private final static Map<Project, BackgroundableProcessIndicator> projectToProgressIndicatorWindowMap = new HashMap<>();

    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private static final DefaultTableCellRenderer TABLE_RENDERER = new DefaultTableCellRenderer() {
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

    private static PipelineScanResultsBarToolWindowFactory instance;
    private final Map<Long, String> detailsMap = new HashMap<>();

    public PipelineScanResultsBarToolWindowFactory() {
        instance = this;
    }

    public static PipelineScanResultsBarToolWindowFactory getInstance() {
        return instance;
    }

    public static void closeProgressWindow(Project project) {
        getProgressWindowForProject(project)
                .ifPresent(progressWindow -> {
                    ApplicationManager.getApplication()
                            .invokeLater(() -> {
                                projectToProgressIndicatorWindowMap.remove(project);
                                progressWindow.dispose();
                                Optional.ofNullable(projectToToolWindowMap.get(project))
                                        .ifPresent(windowOwner -> windowOwner.setScanButtonsEnabled(true));
                    });
                });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        initializeTables(project, toolWindow);
        toolWindow.activate(null);
    }

    private void initializeTables(Project project, ToolWindow toolWindow) {
        JBTable allResultsTable = initializeResultsTable(ALL_FINDINGS);
        JBTable filteredResultsTable = initializeResultsTable(FINDINGS_VIOLATING_CRITERIA);

        FindingsPanelOwner allFindingsPanelOwner = getFindingsPanel(project, allResultsTable);
        Content allFindingsParent = toolWindow.getContentManager().getFactory()
                .createContent(allFindingsPanelOwner.getPanel(),
                        ALL_FINDINGS + " (0)",
                        true);
        toolWindow.getContentManager().addContent(allFindingsParent);

        FindingsPanelOwner filteredFindingsPanelOwner = getFindingsPanel(project, filteredResultsTable);
        Content filteredFindingsParent = toolWindow.getContentManager().getFactory()
                .createContent(filteredFindingsPanelOwner.getPanel(),
                        FINDINGS_VIOLATING_CRITERIA + " (0)",
                        true);
        toolWindow.getContentManager().addContent(filteredFindingsParent);

        filteredResultsTable.addMouseListener(
                getResultsTableMouseListener(project, filteredResultsTable)
        );

        allResultsTable.addMouseListener(
                getResultsTableMouseListener(project, allResultsTable));
        toolWindow.getContentManager().addContent(filteredFindingsParent);
        projectToToolWindowMap.put(project, new ToolWindowOwner(project, toolWindow, allResultsTable,
                filteredResultsTable, filteredFindingsParent, allFindingsParent,
                allFindingsPanelOwner.getStartScanButton(),
                filteredFindingsPanelOwner.getStartScanButton()));
    }

    @NotNull
    private FindingsPanelOwner getFindingsPanel(Project project, JBTable resultsTable) {
        JPanel findingsPanel = new JPanel();
        FindingsPanelOwner findingsPanelOwner = new FindingsPanelOwner(findingsPanel);
        findingsPanel.setLayout(MAIN_PANEL_LAYOUT);
        findingsPanel.add(initializeScrollPanel(resultsTable),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        findingsPanel.add(getStartScanButtonPanel(project, findingsPanelOwner),
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK,
                        GridConstraints.ANCHOR_SOUTH,
                        RESULTS_FOOTER_MINIMUM_SIZE,
                        RESULTS_FOOTER_PREFERRED_SIZE,
                        RESULTS_FOOTER_MAXIMUM_SIZE, 0, false));
        return findingsPanelOwner;
    }

    @NotNull
    private MouseAdapter getResultsTableMouseListener(Project project, JBTable resultsTable) {
        return new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                mousePressedOnTableEvent(mouseEvent, project, resultsTable);
            }
        };
    }

    private JPanel initializeScrollPanel(JBTable resultsTable) {
        JPanel resultsScrollPanel = new JPanel();
        JBScrollPane resultsScrollPane = new JBScrollPane(resultsTable);
        resultsScrollPanel.add(resultsScrollPane);
        resultsScrollPanel.setLayout(SINGLE_ITEM_LAYOUT);
        return resultsScrollPanel;
    }

    private JBTable initializeResultsTable(String tableName) {
        JBTable resultsTable = new JBTable();
        resultsTable.setName(tableName);
        resultsTable.setModel(new FindingResultsTableModel());
        resultsTable.setRowSelectionAllowed(true);
        resultsTable.setDefaultRenderer(Object.class, TABLE_RENDERER);
        return resultsTable;
    }

    private JPanel getStartScanButtonPanel(Project project, FindingsPanelOwner findingsPanelOwner) {
        JButton startScanButton = new JButton("Run Pipeline Scan");
        startScanButton.addActionListener(getStartScanAction(project));
        findingsPanelOwner.setStartScanButton(startScanButton);
        JPanel buttonParent = new JPanel();
        buttonParent.add(startScanButton);
        buttonParent.setLayout(new FlowLayout(FlowLayout.RIGHT));
        return buttonParent;
    }

    @NotNull
    private ActionListener getStartScanAction(Project project) {
        return e ->
                Optional.ofNullable(project)
                        .ifPresent(this::startScanOnProject);

    }

    private void startScanOnProject(Project project) {
        if (!isScanning.get()) {
            isScanning.set(true);
            Optional.ofNullable(projectToToolWindowMap.get(project))
                    .ifPresent(windowOwner -> windowOwner.setScanButtonsEnabled(false));
            boolean successfullyStarted = false;
            try {
                successfullyStarted = tryStartScan(project);
            } finally {
                if (!successfullyStarted) {
                    isScanning.set(false);
                    closeProgressWindow(project);
                }
            }
        } else {
            getProgressWindowForProject(project)
                    .filter(progressWindow -> progressWindow.isCanceled() || !progressWindow.isRunning())
                    .ifPresent(progressWindow -> isScanning.set(false));
        }
    }

    private boolean tryStartScan(Project project) {
        try {

            PipelineScanAutoPrePushHandler handler =
                    PipelineScanAutoPrePushHandler.getProjectHandler(project)
                            .orElseGet(() -> new PipelineScanAutoPrePushHandler(project));

            BackgroundableProcessIndicator progressWindow = new BackgroundableProcessIndicator(handler.getProject(), PROGRESS_TASK_INFO);
            progressWindow.setTitle("Running pipeline scan");
            projectToProgressIndicatorWindowMap.put(project, progressWindow);

            Runnable outerRunnable = () ->
                    ProgressManager.getInstance().runProcess(() -> {
                        try {
                            handler.startScan(progressWindow, true);
                        } catch (ProcessCanceledException pce) {
                            //process was cancelled, let's just stop
                        } finally {
                            isScanning.set(false);
                            closeProgressWindow(project);
                        }

                    }, progressWindow);

            new SwingWorker<>() {
                @Override
                public Object construct() {
                    outerRunnable.run();
                    return null;
                }
            }.start();
            return true;
        } catch (Exception e) {
            //in case of any error, we just need to notify the outer method
            return false;
        }
    }

    private void mousePressedOnTableEvent(MouseEvent mouseEvent, Project project, JBTable clickedTable) {
        Point point = mouseEvent.getPoint();
        int row = clickedTable.rowAtPoint(point);
        int col = clickedTable.columnAtPoint(point);
        if (row == -1) {
            return;
        }
        TableModel tableModel = clickedTable.getModel();
        if (col == DETAILS_COLUMN_INDEX) {
            Optional.ofNullable(detailsMap.get((long) tableModel.getValueAt(row, 0)))
                    .ifPresent(detailsHtml -> IssueDetailsToolWindow.getCurrentOrMakeNewInstance()
                            .setDetailsAndShow(project, detailsHtml));

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

    public void updateResultsForProject(Project project, List<PipelineScanFinding> results,
                                        List<PipelineScanFinding> filteredResults) {
        ToolWindowOwner toolWindowOwner = projectToToolWindowMap.get(project);
        if (toolWindowOwner == null) {
            ToolWindow tempWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
            if (tempWindow != null) {
                tempWindow.show();
                toolWindowOwner = projectToToolWindowMap.get(project);
            }
            if (toolWindowOwner == null) {
                return;
            }
        }
        FindingResultsTableModel allFindingsModel = new FindingResultsTableModel();
        results.forEach(element -> {
            detailsMap.put(element.getIssueId(), element.getDetails());
            allFindingsModel.addRow(element.getAsTableRow());
        });
        toolWindowOwner.getAllResultsTable().setModel(allFindingsModel);
        toolWindowOwner.getAllFindingsParent().setDisplayName(ALL_FINDINGS + " (" + results.size() + ")");

        FindingResultsTableModel filteredResultsModel = new FindingResultsTableModel();
        filteredResults.stream()
                .map(PipelineScanFinding::getAsTableRow)
                .forEach(filteredResultsModel::addRow);
        toolWindowOwner.getFilteredResultsTable().setModel(filteredResultsModel);
        toolWindowOwner.getFilteredFindingsParent().setDisplayName(FINDINGS_VIOLATING_CRITERIA + " (" + filteredResults.size() + ")");
    }

    @Override
    public void init(@NotNull ToolWindow window) {
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return Optional.ofNullable(project.getService(ProjectSettingsState.class))
                .isPresent();
    }

    public static void handleIsScanEnabledChange(Project project, boolean isScanEnabled) {
        Optional.ofNullable(projectToToolWindowMap.get(project))
                .ifPresent(pipelineScanResultsBarToolWindowFactory ->
                        pipelineScanResultsBarToolWindowFactory.setScanButtonsEnabled(isScanEnabled));
    }

    @NotNull
    private static Optional<BackgroundableProcessIndicator> getProgressWindowForProject(Project project) {
        return Optional.ofNullable(projectToProgressIndicatorWindowMap.get(project));
    }
}