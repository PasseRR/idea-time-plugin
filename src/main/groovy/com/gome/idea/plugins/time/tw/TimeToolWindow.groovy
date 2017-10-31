package com.gome.idea.plugins.time.tw

import com.gome.idea.plugins.time.ui.TimeToolWindowView
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowManagerAdapter
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import org.jetbrains.annotations.NotNull

import javax.swing.*
/**
 * 工时更新ToolWindow
 * @author xiehai1
 * @date 2017/10/27 11:23
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class TimeToolWindow implements ToolWindowFactory {
    private def static final TOOL_WINDOW_NAME = "Gome Time"

    @Override
    void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        repaint(project, toolWindow)
        
        // toolWindow 状态变化监听
        ToolWindowManagerEx.getInstanceEx(project).addToolWindowManagerListener(
            new ToolWindowManagerAdapter() {
                @Override
                void stateChanged() {
                    // 对所有打开的idea实例进行reload
                    Project[] projects = ProjectManagerEx.getInstanceEx().getOpenProjects()
                    projects.each { it ->
                        // plugin.xml ToolWindow id
                        ToolWindow tw = ToolWindowManagerEx.getInstance(it).getToolWindow(TOOL_WINDOW_NAME)
                        // 激活ToolWindow做刷新操作
                        if (tw != null && tw.isActive()) {
                            repaint(it, tw)
                        }
                    }
                }
            }
        )
    }

    def static repaint(Project project, ToolWindow toolWindow) {
        JComponent component = new TimeToolWindowView().getScrollPane()
        ContentManager contentManager = toolWindow.getContentManager()
        ContentFactory contentFactory = contentManager.getFactory()
        final String contentName = "- Times"
        contentManager.removeAllContents(true)
        contentManager.addContent(contentFactory.createContent(component, contentName, true))
    }
}
