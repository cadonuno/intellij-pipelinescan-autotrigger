package cadonuno.pipelinescanautotrigger.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.JBTable;

public class ToolWindowOwner {
    private final Project project;
    private final ToolWindow toolWindow;
    private final JBTable allResultsTable;
    private final JBTable filteredResultsTable;
    private final Content filteredFindingsParent;
    private final Content allFindingsParent;

    public ToolWindowOwner(Project project, ToolWindow toolWindow, JBTable allResultsTable, JBTable filteredResultsTable,
                           Content filteredFindingsParent, Content allFindingsParent) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.allResultsTable = allResultsTable;
        this.filteredResultsTable = filteredResultsTable;
        this.filteredFindingsParent = filteredFindingsParent;
        this.allFindingsParent = allFindingsParent;
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
}
