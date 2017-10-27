package com.gome.idea.plugins.time.ui

import groovy.swing.SwingBuilder

import javax.swing.*
/**
 * idea视图基类
 * @author xiehai1
 * @date 2017/10/27 11:09
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
abstract class IdeaView {
    def sb = new SwingBuilder()
    JScrollPane scrollPane = sb.scrollPane(verticalScrollBarPolicy: JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
    /**
     * 表单是否修改
     * @return true/有改动 false/未改动
     */
    def isModified(){
        false
    }

    /**
     * 重置表单
     */
    def reset(){
        // do nothing if child not override
    }
}
