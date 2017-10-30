package com.gome.idea.plugins.time.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import groovy.transform.Canonical
import org.jdom.Element

/**
 * 工时配置存储
 * @author xiehai1
 * @date 2017/10/30 14:19
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
@Canonical
@State(
    name = "TimeSettings",
    storages = [
        @Storage(
            id = "TimeSettings",
            file = "\$APP_CONFIG\$/format.xml"
        )
    ]
)
class TimeSettings implements PersistentStateComponent<Element> {
    // 工时系统url
    def String url
    // 用户名
    def String userName
    // 密码
    def String password
    // 登录cookies
    def String cookies

    @Override
    Element getState() {
        Element element = new Element("TimeSettings")
        element.setAttribute("url", this.url ? this.url : "")
        element.setAttribute("userName", this.userName ? this.userName : "")
        element.setAttribute("password", this.password ? this.password : "")
        element.setAttribute("cookies", this.cookies ? this.cookies : "")
        element
    }

    @Override
    void loadState(Element element) {
        this.url = element.getAttribute("url")
        this.userName = element.getAttribute("userName")
        this.password = element.getAttribute("password")
        this.cookies = element.getAttribute("cookies")
    }

    def static TimeSettings getInstance() {
        ServiceManager.getService(TimeSettings.class)
    }
}
