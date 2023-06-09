package cadonuno.pipelinescanautotrigger.ui.scanresults;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.JBTable;

import javax.swing.*;

public class ToolWindowOwner {
    private final Project project;
    private final ToolWindow toolWindow;
    private final JBTable allResultsTable;
    private final JBTable filteredResultsTable;
    private final Content filteredFindingsParent;
    private final Content allFindingsParent;
    private final JButton allFindingsStartScanButton;
    private final JButton filteredResultsStartScanButton;


    public ToolWindowOwner(Project project, ToolWindow toolWindow, JBTable allResultsTable, JBTable filteredResultsTable,
                           Content filteredFindingsParent, Content allFindingsParent, JButton allFindingsStartScanButton, JButton filteredResultsStartScanButton) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.allResultsTable = allResultsTable;
        this.filteredResultsTable = filteredResultsTable;
        this.filteredFindingsParent = filteredFindingsParent;
        this.allFindingsParent = allFindingsParent;
        this.allFindingsStartScanButton = allFindingsStartScanButton;
        this.filteredResultsStartScanButton = filteredResultsStartScanButton;
    }

    public Content getAllFindingsParent() {
        return allFindingsParent;
    }

    public Content getFilteredFindingsParent() {
        return filteredFindingsParent;
    }

    public JBTable getFilteredResultsTable() {
        return filteredResultsTable;
    }

    public JBTable getAllResultsTable() {
        return allResultsTable;
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    public Project getProject() {
        return project;
    }

    public void setScanButtonsEnabled(boolean isScanEnabled) {
        allFindingsStartScanButton.setEnabled(isScanEnabled);
        filteredResultsStartScanButton.setEnabled(isScanEnabled);
    }
}
