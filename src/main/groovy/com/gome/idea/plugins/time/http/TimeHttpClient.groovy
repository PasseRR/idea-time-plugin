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

    /**
     * 重新登录并保存cookie 根据已经设置的地址、用户名、密码
     * @return
     */
    def private static retryCookie(){
        return login(SETTINGS.getUrl(), SETTINGS.getUserName(), SETTINGS.getPassword())
    }

    /**
     * 获得当月工时数据
     * @param startDay 当月第一天
     * @param endDay 当月最后一天
     * @return
     */
    def static listMonth(String startDay, String endDay) {
        return listMonth0(startDay, endDay, false)
    }
    /**
     * {@link TimeHttpClient#listMonth(String startDay, String endDay)}
     * @param startDay
     * @param endDay
     * @param isRetried 是否已经重试
     * @return
     */
    def private static listMonth0(String startDay, String endDay, boolean isRetried) {
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = "/manhour/manhour/list/month"
            headers.'Cookie' = SETTINGS.getCookies()
            body = [startDay: startDay, endDay: endDay]
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!isRetried && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试一次
            return listMonth0(startDay, endDay, true)
        }

        respJson['result']
    }

    /**
     * 草稿查询
     * @param day 指定日期
     * @return
     */
    def static listDraft(long day){
        return listDraft0(day, false)
    }

    def private static listDraft0(long day, boolean isRetried){
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = "/manhour/manhour/list/day/${day}"
            headers.'Cookie' = SETTINGS.getCookies()
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!isRetried && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试一次
            return listDraft0(day, true)
        }

        respJson['result']
    }

    def static listAudit(long day){
        return listAudit0(day, false)
    }

    def private static listAudit0(long day, boolean isRetried){
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = "/manhour/manhour/list/audit/day/${day}"
            headers.'Cookie' = SETTINGS.getCookies()
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!isRetried && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试一次
            return listAudit0(day, true)
        }

        respJson['result']
    }

    /**
     * 草稿删除
     * @param id 工时记录id
     */
    def static deleteDraft(int id){
        return deleteDraft0(id, false)
    }

    def private static deleteDraft0(int id, boolean isRetried){
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.DELETE, ContentType.JSON) { req ->
            uri.path = "/manhour/manhour/delete/${id}"
            headers.'Cookie' = SETTINGS.getCookies()
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!isRetried && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试一次
            return deleteDraft0(id, true)
        }

        respJson['success']
    }

    /**
     * 草稿提交审核
     * @param id
     * @return
     */
    def static submitDraft(int id){
        return submitDraft0(id, false)
    }

    def private static submitDraft0(int id, boolean isRetried){
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.PUT, ContentType.JSON) { req ->
            uri.path = "/manhour/manhour/audit/update"
            body = [ids: id as String, auditType: 2]
            headers.'Cookie' = SETTINGS.getCookies()
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!isRetried && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试一次
            return submitDraft0(id, true)
        }

        respJson['success']
    }
}
