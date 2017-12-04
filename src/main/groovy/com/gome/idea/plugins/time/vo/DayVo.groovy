package com.gome.idea.plugins.time.vo

import groovy.transform.Canonical

/**
 * 天信息
 * @author xiehai1
 * @date 2017/10/27 14:18
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
@Canonical
class DayVo {
    // 数字
    def date
    // long型日期
    def datetime
    // 周几
    def dayOfWeek
    // 是否为当月日期
    def isCurrentMonth
    // 当天已审核工时
    def workHours
    // 当天已审核调休
    def restHours
    // 审批中工时
    def auditingHours
}
