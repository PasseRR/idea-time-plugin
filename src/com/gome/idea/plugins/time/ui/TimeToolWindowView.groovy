package com.gome.idea.plugins.time.ui

import com.gome.idea.plugins.time.utils.DateUtils
import com.gome.idea.plugins.time.vo.DayVo
import com.gome.idea.plugins.time.vo.MonthVo
import com.gome.idea.plugins.time.vo.WeekVo

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * Gome Time ToolWindow视图
 * @author xiehai1
 * @date 2017/10/27 11:14
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class TimeToolWindowView extends IdeaView {
    // yyyy-MM
    def String month
    def long selectedDay
    def private static final WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]

    TimeToolWindowView() {
        this.init()
    }

    /**
     * ToolWindow初始化
     * @return
     */
    def init() {
        if (!this.month) {
            def peroid = getPeriod(this.month)
            this.setMonth(peroid[0] + "年" + peroid[1] + "月" as String)
        }
        // 月份信息
        def monthVo = this.getMonthVo(this.month)
        // 绘制日历
        super.scrollPane.setViewportView(
            // 网格布局
            super.sb.panel(layout: new GridLayout(1, 2)) {
                // 日历
                panel() {
                    tableLayout {
                        tr {
                            td { label(text: "") }
                            td(align: "center") {
                                button(
                                    icon: sb.imageIcon(url: this.getClass().getResource("/icon/back.png")),
                                    toolTipText: "上一月",
                                    actionPerformed: {
                                        this.previousMonth()
                                    }
                                )
                            }
                            td(align: "center", colspan: 2) { label(text: this.month) }
                            td(align: "center") {
                                button(
                                    icon: sb.imageIcon(url: this.getClass().getResource("/icon/forward.png")),
                                    toolTipText: "下一月",
                                    actionPerformed: {
                                        this.nextMonth()
                                    }
                                )
                            }
                            td(align: "center") {
                                label(text: "<html>出勤:<font color='green'>${monthVo.workHours}</font>h<html>")
                            }
                            td(align: "center") {
                                label(text: "<html>缺勤:<font color='red'>${monthVo.restHours}</font>h<html>")
                            }
                        }
                        tr {
                            WEEKDAYS.each { day ->
                                td(align: "center") {
                                    label(text: day)
                                }
                            }
                        }
                        monthVo.weeks.each { week ->
                            tr {
                                week.days.each { day ->
                                    td {
                                        this.getDayButton(day as DayVo)
                                    }
                                }
                            }
                        }
                    }
                }
                // 工时明细
                panel(name: "detail") {

                }
            }
        )

        // 初始化明细面板
        this.detailPanel(selectedDay)
    }

    /**
     * 获得每月日历
     * @param month 时间字符串
     * @return {@link MonthVo}
     */
    private def getMonthVo(String month) {
        MonthVo monthVo = new MonthVo()
        // 默认星期天为每周第一天
        def calendar = Calendar.getInstance()
        def peroid = getPeriod(month)
        // 设置年月
        calendar.set(Calendar.YEAR, peroid[0] as int)
        calendar.set(Calendar.MONTH, (peroid[1] as int) - 1)
        // 设置日期为当月第一天
        // 设置时间为00:00:00,000
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        def maxMonths = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)

        maxMonths.times { index ->
            def WeekVo weekVo = new WeekVo()
            // 获得当月第一周第一天
            if (index == 0) {
                def dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                // 若1号不是星期天 即不是每周第一天开始
                // 向前补齐该周
                if (dayOfWeek != 1) {
                    // 向前补齐的天数
                    // 星期天为1 星期二为3 则补齐2天
                    def diff = dayOfWeek - 1
                    calendar.add(Calendar.DAY_OF_MONTH, -diff)
                }
                monthVo.setFirstDay(getDayVo(calendar))
            }

            7.times { it ->
                def day = getDayVo(calendar)
                weekVo.days[it] = day
                if (day.isCurrentMonth) {
                    if (day.workHours) {
                        monthVo.workHours += day.workHours
                    }
                    if (day.restHours) {
                        monthVo.restHours += day.restHours
                    }
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // 获得当月第一周最后一天
            if (index == maxMonths - 1) {
                monthVo.setLastDay(getDayVo(calendar))
            }

            monthVo.weeks[index] = weekVo
        }

        monthVo
    }

    /**
     * 根据当前日期获得{@link DayVo}
     * @param calendar 日期
     * @return {@link DayVo}
     */
    private def getDayVo(Calendar calendar) {
        new DayVo(
            date: calendar.get(Calendar.DAY_OF_MONTH),
            datetime: calendar.getTime().getTime(),
            dayOfWeek: calendar.get(Calendar.DAY_OF_WEEK),
            isCurrentMonth: isCurrentMonth(calendar, this.month),
            // TODO 填充工时
            workHours: calendar.get(Calendar.DAY_OF_MONTH) % 7,
            restHours: 0
        )
    }

    /**
     * 根据DayVo信息绘制按钮
     * @param day 天信息
     * @return button
     */
    private def getDayButton(DayVo day) {
        // 设置button宽度高度
        def button = super.sb.button(preferredSize: new Dimension(60, 40), actionCommand: day.datetime as String, actionPerformed: { ActionEvent event ->
            // 按钮命令
            def date = event.getActionCommand() as long
            // 明细面板
            this.detailPanel(date)
        })
        // 是否是周末
        if (day.dayOfWeek == 1 || day.dayOfWeek == 7) {
            button.setBackground(Color.PINK)
        }
        // 是否是今天
        if (isCurrentDay(day)) {
            button.setBackground(Color.YELLOW)
        }
        // button字符串
        def text = new StringBuilder("<html>")
        // 有审核通过的工时
        if (day.workHours) {
            if (day.isCurrentMonth) {
                text.append("<sup><font color='red'>${day.workHours}h</font></sup>")
            } else {
                text.append("<sup><font color='black'>${day.workHours}h</font></sup>")
            }
        }
        // 是否是当月时间
        if (day.isCurrentMonth) {
            text.append("${day.date}")
        } else {
            text.append("<font color='gray'>${day.date}</font>")
        }
        text.append("</html>")
        // 设置按钮文字
        button.setText(text.toString())
        return button
    }

    /**
     * 明细面板
     * @param date 选中日期
     * @return
     */
    private def detailPanel(long date) {
        // 初始化时 默认打开当天的明细面板
        if(!date){
            Calendar calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            date = calendar.getTime().getTime()
        }
        // 设置选择的日期
        this.selectedDay = date
        // TODO 明细数据填充
        def rootPanel = super.scrollPane.getViewport().getView() as JPanel
        rootPanel.remove(1)
        def data = [
            [target: "项目出勤", type: "开发工作", project: "GSR", manager: "陈亚妮", hours: 1],
            [target: "项目出勤", type: "开发工作", project: "GSR", manager: "陈亚妮", hours: 2],
            [target: "项目出勤", type: "开发工作", project: "GSR", manager: "陈亚妮", hours: 3],
            [target: "项目出勤", type: "开发工作", project: "GSR", manager: "陈亚妮", hours: 4]
        ]
        rootPanel.add(super.sb.panel() {
            tableLayout {
                tr {
                    td(align: "left") {
                        label(text: DateUtils.longToDateString(date))
                    }
                }
                tr {
                    td(align: "left") {
                        // 草稿箱面板
                        panel(border: titledBorder(title: "工时草稿箱"), layout: borderLayout()) {
                            def JTable tab = table(background: super.scrollPane.getBackground()) {
                                tableModel(list: data) {
                                    propertyColumn(header: "工时对象", propertyName: "target", editable: false)
                                    propertyColumn(header: "工时分类", propertyName: "type", editable: false)
                                    propertyColumn(header: "项目", propertyName: "project", editable: false)
                                    propertyColumn(header: "项目经理", propertyName: "manager", editable: false)
                                    propertyColumn(header: "工时", propertyName: "hours", editable: false)
                                }
                            }
                            // 右键菜单
                            def menu = sb.popupMenu {
                                menuItem(
                                    text: "删除草稿",
                                    icon: imageIcon(url: this.getClass().getResource("/icon/delete.png")),
                                    actionPerformed: {
                                        // TODO 删除
                                    }
                                )
                                separator()
                                menuItem(
                                    text: "提交审核",
                                    icon: imageIcon(url: this.getClass().getResource("/icon/audit.png")),
                                    actionPerformed: {
                                        // TODO 提交审核
                                    }
                                )
                            }
                            tab.addMouseListener(new MouseAdapter(){
                                @Override
                                void mouseClicked(MouseEvent e) {
                                    if(e.getButton() == MouseEvent.BUTTON3){
                                        def index = tab.rowAtPoint(e.getPoint())
                                        if(index == -1){
                                            return
                                        }
                                        tab.setRowSelectionInterval(index, index)
                                        menu.show(tab, e.getX(), e.getY())
                                    }
                                }
                            })
                            widget(constraints: BorderLayout.NORTH, tab.tableHeader)
                            widget(constraints: BorderLayout.CENTER, tab)
                        }
                    }
                }
                tr {
                    td(align: "left") {
                        panel(border: titledBorder(title: "已提交工时"), layout: borderLayout()) {
                            def tab = table(background: super.scrollPane.getBackground(), enabled: false) {
                                tableModel(list: data) {
                                    propertyColumn(header: "工时对象", propertyName: "target")
                                    propertyColumn(header: "工时分类", propertyName: "type")
                                    propertyColumn(header: "项目", propertyName: "project")
                                    propertyColumn(header: "项目经理", propertyName: "manager")
                                    propertyColumn(header: "工时", propertyName: "hours")
                                }
                            }
                            widget(constraints: BorderLayout.NORTH, tab.tableHeader)
                            widget(constraints: BorderLayout.CENTER, tab)
                        }
                    }
                }
            }
        }, 1)
        // repaint panel
        rootPanel.revalidate()
    }

    /**
     * 下一月面板
     * @return
     */
    private def nextMonth() {
        def peroid = getPeriod(this.month)
        def year = peroid[0] as int
        def month = (peroid[1] as int) + 1
        if (month > 12) {
            year = year + 1
            month = 1
        }

        this.setMonth(year + "年" + month + "月" as String)
        this.init()
    }

    /**
     * 前一月面板
     * @return
     */
    private def previousMonth() {
        def peroid = getPeriod(this.month)
        def year = peroid[0] as int
        def month = (peroid[1] as int) - 1
        if (month < 1) {
            year = year - 1
            month = 12
        }

        this.setMonth(year + "年" + month + "月" as String)
        this.init()
    }

    /**
     * 获得日期年月
     * @param month 年月字符串
     * @return [年 , 月]数组
     */
    private def static getPeriod(String month) {
        def period = []
        if (!month) {
            Calendar calendar = Calendar.getInstance()
            period[0] = calendar.get(Calendar.YEAR)
            period[1] = calendar.get(Calendar.MONTH) + 1
        } else {
            period = month.substring(0, month.length() - 1).split("年")
        }

        return period
    }

    /**
     * 给定日期是否是当月日期
     * @param calendar 日期
     * @return true/false
     */
    private def static isCurrentMonth(Calendar calendar, String month) {
        def peroid = getPeriod(month)
        return (peroid[1] as int) - 1 == calendar.get(Calendar.MONTH)
    }

    /**
     * 是否是今天
     * @param day 日期
     * @return true/false
     */
    private def static isCurrentDay(DayVo day) {
        Calendar calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        day.datetime == calendar.getTime().getTime()
    }
}
