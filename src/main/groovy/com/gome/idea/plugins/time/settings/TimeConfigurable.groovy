package com.gome.idea.plugins.time.settings

import com.gome.idea.plugins.time.ui.TimeSettingView
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable

import javax.swing.*
/**
 * Time配置面板
 * @author xiehai1
 * @date 2017/10/30 14:29
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class TimeConfigurable implements SearchableConfigurable {
    @Override
    String getId() {
        "Time"
    }

    @Override
    Runnable enableSearch(String s) {
        null
    }

    @Override
    String getDisplayName() {
        this.getId()
    }

    @Override
    String getHelpTopic() {
        this.getId()
    }

    @Override
    JComponent createComponent() {
        TimeSettingView.getInstance().getScrollPane()
    }

    @Override
    boolean isModified() {
        TimeSettingView.getInstance().isModified()
    }

    @Override
    void apply() throws ConfigurationException {
        TimeSettingView.getInstance().apply()
    }

    @Override
    void reset() {
        TimeSettingView.getInstance().reset()
    }

    @Override
    void disposeUIResources() {

    }
}
