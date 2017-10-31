package com.gome.idea.plugins.time.vo

import groovy.transform.Canonical
/**
 * 一个月包含多少周
 * @author xiehai1
 * @date 2017/10/27 14:15
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
@Canonical
class MonthVo {
    // 日历展示当月第一天 long型时间戳字符串
    // 可能是上月某天
    def String firstDay
    // 日历展示当月最后一天 long型时间戳字符串
    // 可能是下月某天
    def String lastDay
    // 当月合计工时
    // 只计算当月 显示的其他月份不会计算
    def workHours = 0
    // 当月休息工时
    // 只计算当月 显示的其他月份不会计算
    def restHours = 0
    def weeks = []

    /**
     * @param maxWeeks 当月最大周数
     * @param startDate 开始日期
     * @return
     */
    def MonthVo(int maxWeeks, Date startDate){
        this.firstDay = String.valueOf(startDate.getTime())

        Calendar calendar = Calendar.getInstance()
        calendar.setTime(startDate)
        calendar.add(Calendar.WEEK_OF_YEAR, maxWeeks)
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        this.lastDay = String.valueOf(calendar.getTime().getTime())
    }
}
