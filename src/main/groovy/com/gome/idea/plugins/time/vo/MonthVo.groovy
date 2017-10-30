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
    // 日历展示当月第一天
    // 可能是上月某天
    def DayVo firstDay
    // 日历展示当月最后一天
    // 可能是下月某天
    def DayVo lastDay
    // 当月合计工时
    // 只计算当月 显示的其他月份不会计算
    def workHours = 0
    // 当月休息工时
    // 只计算当月 显示的其他月份不会计算
    def restHours = 0
    def weeks = []
}
