package com.gome.idea.plugins.time.http

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * Http客户端请求
 * @author xiehai1
 * @date 2017/10/30 15:39
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class TimeHttpClient {
    private TimeHttpClient() {

    }

    def static enableConnect(String url) {
        def http = new HTTPBuilder(url)
        http.request(Method.GET){ req ->
            headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
            response.success = { resp, html ->
                println html
            }
        }

    }

    static main(agrs) {
        enableConnect("https://www.baidu.com")
    }
}
