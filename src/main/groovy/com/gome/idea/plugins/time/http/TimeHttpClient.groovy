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
    // 工时对象(attendanceType)缓存 list
    private def static final ATTENDANCE_TYPES = []
    // 工时分类(byAttendanceType)缓存 Map<AttendanceTypeId, byAttendanceType>
    private def static final BY_ATTENDANCE_TYPES = [:]
    // 项目列表缓存缓存100项
    private def static final PROJECTS = [:]

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
    def private static retryCookie() {
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
    def static listDraft(long day) {
        return listDraft0(day, false)
    }

    def private static listDraft0(long day, boolean isRetried) {
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

    def static listAudit(long day) {
        return listAudit0(day, false)
    }

    def private static listAudit0(long day, boolean isRetried) {
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
    def static deleteDraft(int id) {
        return deleteDraft0(id, false)
    }

    def private static deleteDraft0(int id, boolean isRetried) {
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
    def static submitDraft(int id) {
        return submitDraft0(id, false)
    }

    def private static submitDraft0(int id, boolean isRetried) {
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

    /**
     * 查询工时对象
     * @return 工时对象列表
     */
    def static getAttendanceTypes() {
        if (!ATTENDANCE_TYPES.size()) {
            // 查询工时对象写入缓存
            listAttendanceTypes(false)
        }

        ATTENDANCE_TYPES
    }

    def private static listAttendanceTypes(boolean flag) {
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = "/manhour/attendanceType/all"
            headers.'Cookie' = SETTINGS.getCookies()
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!flag && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试
            listAttendanceTypes(true)
        }

        if (respJson['result']) {
            ATTENDANCE_TYPES.addAll(respJson['result'])
        }
    }

    /**
     * 查询工时分类
     * @param type 工时对象类型
     * @return 工时分类列表
     */
    def static getByAttendanceTypes(int type) {
        if (!BY_ATTENDANCE_TYPES.get(type)) {
            BY_ATTENDANCE_TYPES.put(type, [])
            // 查询工时分类写入缓存
            listByAttendanceTypes(type, false)
        }

        BY_ATTENDANCE_TYPES.get(type)
    }

    def private static listByAttendanceTypes(int id, boolean flag) {
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = "/manhour/manhourType/byAttendanceType/${id}"
            headers.'Cookie' = SETTINGS.getCookies()
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!flag && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试
            listByAttendanceTypes(id, true)
        }

        if (respJson['result']) {
            BY_ATTENDANCE_TYPES.put(id, respJson['result'])
        }
    }

    /**
     * 获得项目列表
     * @return
     */
    def static listProjects() {
        return listProjects0(false)
    }

    def private static listProjects0(boolean isRetried) {
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = "/manhour/project/search"
            headers.'Cookie' = SETTINGS.getCookies()
            body = [rows: 100, page: 1, type: 3]
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!isRetried && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试一次
            return listProjects0(true)
        }

        respJson['result']['data']
    }

    /**
     * 工时草稿保存
     * @param requestBody
     * @return
     */
    def static saveHours(def requestBody) {
        saveHours0(requestBody, false)
    }

    def private static saveHours0(def requestBody, boolean isRetried) {
        def respJson = [:]
        def http = new HTTPBuilder(SETTINGS.getUrl())
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = "/manhour/manhour/create"
            headers.'Cookie' = SETTINGS.getCookies()
            body = requestBody
            response.success = { resp, json ->
                respJson = json
            }
        }

        if (!isRetried && !respJson['success']) {
            // 如果session超时 再次登录
            retryCookie()
            // 重试一次
            return saveHours0(requestBody, true)
        }

        respJson['success']
    }
}
