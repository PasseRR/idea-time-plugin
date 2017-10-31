package com.gome.idea.plugins.time.http

import com.gome.idea.plugins.time.settings.TimeSettings
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * Http客户端请求
 * @author xiehai1
 * @date 2017/10/30 15:39
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class TimeHttpClient {
    private def static final int TIME_OUT = 5000
    private def static final TimeSettings SETTINGS = TimeSettings.getInstance()

    private TimeHttpClient() {

    }

    /**
     * 连接服务器是否成功
     * @param url 服务器地址
     * @return true/false
     */
    def static enableConnect(String url) {
        def http = new HTTPBuilder(url)
        http.client.params.setIntParameter("http.connection.timeout", TIME_OUT)
        def flag = false
        try {
            http.request(Method.GET) { req ->
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                response.success = { resp ->
                    flag = true
                }
            }
        } catch (Exception e) {
            println e.getMessage()
        }

        flag
    }

    /**
     * 登录校验
     * @param url 服务器地址
     * @param userName 用户名
     * @param password 密码
     */
    def static login(String url, String userName, String password) {
        def http = new HTTPBuilder(url)
        def flag = false
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = "/manhour/login"
            body = [username: userName, password: password]
            response.success = { resp, json ->
                // 是否登录成功
                flag = json['success']
                if (flag) {
                    http.client.getCookieStore()
                        .getCookies()
                        .find { it ->
                        // 只需要JSESSIONID
                        if (it.getName() == "JSESSIONID") {
                            SETTINGS.setCookies((it.getName() + "=" + it.getValue()) as String)
                            true
                        }

                        false
                    }
                }
            }
        }

        flag
    }


}
