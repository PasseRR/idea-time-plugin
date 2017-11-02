package com.gome.idea.plugins.time.utils

/**
 * 工时工具类
 * @author xiehai1
 * @date 2017/11/02 09:52
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class HourUtils {
    def private static final HOURS = []

    private HourUtils() {

    }

    /**
     * 默认返回1~15工时列表
     * @return
     */
    def static getHours() {
        if (!HOURS.size()) {
            synchronized (HourUtils.class) {
                if (!HOURS.size()) {
                    15.times { it ->
                        def hour = it + 1
                        HOURS[it] = [text: "${hour}h", value: hour]
                    }
                }
            }
        }


        HOURS
    }
}
