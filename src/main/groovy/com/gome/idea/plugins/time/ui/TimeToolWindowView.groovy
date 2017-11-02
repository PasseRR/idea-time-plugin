package com.gome.idea.plugins.time.ui

import com.gome.idea.plugins.time.http.TimeHttpClient
import com.gome.idea.plugins.time.utils.DateUtils
import com.gome.idea.plugins.time.utils.HourUtils
import com.gome.idea.plugins.time.utils.NotificationUtils
import com.gome.idea.plugins.time.vo.DayVo
import com.gome.idea.plugins.time.vo.MonthVo
import com.gome.idea.plugins.time.vo.WeekVo
import com.intellij.openapi.project.Project

import javax.swing.*
import javax.swing.plaf.basic.BasicComboBoxRenderer
import javax.swing.table.DefaultTableCellRenderer
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
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
    def Project project
    // 工时对象
    def JComboBox comboBox1
    // 工时分类
    def JComboBox comboBox2
    // 项目
    def JComboBox comboBox3
    // 工时
    def JComboBox comboBox4
    // 简述
    def JTextField commentTextField
    // cache instance by project
    def private static INSTANCES = [:]
    def private static final WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]

    private TimeToolWindowView(Project project) {
        this.project = project
        this.init()
    }

    def static getInstance(Project project) {
        if (!INSTANCES.get(project)) {
            synchronized (TimeToolWindowView.class) {
                if (!INSTANCES.get(project)) {
                    INSTANCES.put(project, new TimeToolWindowView(project))
                }
            }
        }

        INSTANCES.get(project) as TimeToolWindowView
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
            super.sb.panel(layout: new FlowLayout(FlowLayout.LEFT, 20, 20)) {
                // 日历
                panel(name: "calendar") {
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
                                label(text: "<html>出勤:<font color='green'>${monthVo.workHours as int}</font>h<html>")
                            }
                            td(align: "center") {
                                label(text: "<html>缺勤:<font color='red'>${monthVo.restHours as int}</font>h<html>")
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
        // 默认星期天为每周第一天
        def calendar = Calendar.getInstance()
        def peroid = getPeriod(month)
        def selectedYear = peroid[0] as int
        def selectedMonth = (peroid[1] as int) - 1
        // 设置年月
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth)
        // 设置日期为当月第一天
        // 设置时间为00:00:00,000
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        def maxMonths = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)
        def dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // 若1号不是星期天 即不是每周第一天开始
        // 向前补齐该周
        if (dayOfWeek != 1) {
            // 向前补齐的天数
            // 星期天为1 星期二为3 则补齐2天
            def diff = dayOfWeek - 1
            calendar.add(Calendar.DAY_OF_MONTH, -diff)
        }
        MonthVo monthVo = new MonthVo(maxMonths, calendar.getTime())
        // 当月工时
        def result = TimeHttpClient.listMonth(monthVo.getFirstDay(), monthVo.getLastDay())

        maxMonths.times { index ->
            def WeekVo weekVo = new WeekVo()

            7.times { it ->
                def day = getDayVo(calendar, result)
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

            monthVo.weeks[index] = weekVo
        }

        monthVo
    }

    /**
     * 根据当前日期获得{@link DayVo}
     * @param calendar 日期
     * @return {@link DayVo}
     */
    private def getDayVo(Calendar calendar, def result) {
        def dayVo = new DayVo(
            date: calendar.get(Calendar.DAY_OF_MONTH),
            datetime: calendar.getTime().getTime(),
            dayOfWeek: calendar.get(Calendar.DAY_OF_WEEK),
            isCurrentMonth: isCurrentMonth(calendar, this.month)
        )

        result.find { it ->
            if (it['day'] == calendar.getTime().getTime()) {
                dayVo.setWorkHours(it['workHour'])
                dayVo.setRestHours(it['restHour'])
            }
        }

        dayVo
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
                text.append("<sup><font color='green'>${day.workHours as int}h</font></sup>")
            } else {
                text.append("<sup><font color='black'>${day.workHours as int}h</font></sup>")
            }
        }
        // 是否是当月时间
        if (day.isCurrentMonth) {
            text.append("${day.date}")
        } else {
            text.append("<font color='gray'>${day.date}</font>")
        }
        // 是否有休假
        if (day.restHours) {
            if (day.isCurrentMonth) {
                text.append("<sup><font color='red'>${day.restHours as int}h</font></sup>")
            } else {
                text.append("<sup><font color='gray'>${day.restHours as int}h</font></sup>")
            }
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
        if (!date) {
            Calendar calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            date = calendar.getTime().getTime()
        }
        // 设置选择的日期
        this.selectedDay = date
        def rootPanel = super.scrollPane.getViewport().getView() as JPanel
        rootPanel.remove(1)
        def draft = TimeHttpClient.listDraft(date)
        def audit = TimeHttpClient.listAudit(date)

        // 默认选中项目出勤
        this.comboBox1 = sb.comboBox(items: TimeHttpClient.getAttendanceTypes(),
            selectedIndex: 2, renderer: new BasicComboBoxRenderer() {
            @Override
            Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value) {
                    super.setText(String.valueOf((value as Map).get("name")))
                }

                this
            }
        }, itemStateChanged: { ItemEvent e ->
            if (e.getStateChange() == ItemEvent.SELECTED) {
                def selectedItem = e.getItem() as Map
                def id = selectedItem.get("id") as int
                // 法定缺勤 disable项目选择
                if (id == 13) {
                    this.comboBox3.setEnabled(false)
                } else {
                    this.comboBox3.setEnabled(true)
                }
                // 设置工时分类联动
                def items = TimeHttpClient.getByAttendanceTypes(id)
                this.comboBox2.removeAllItems()
                items.each { it ->
                    this.comboBox2.addItem(it)
                }
            }
        })

        // 工时分类
        // 默认选中开发工作
        this.comboBox2 = sb.comboBox(items: TimeHttpClient.getByAttendanceTypes(this.getComboBox1SelectedId()),
            selectedIndex: 16, renderer: new BasicComboBoxRenderer() {
            @Override
            Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value) {
                    super.setText(String.valueOf((value as Map).get("name")))
                }

                this
            }
        })

        // 项目选择
        this.comboBox3 = sb.comboBox(items: TimeHttpClient.listProjects(), renderer: new BasicComboBoxRenderer() {
            @Override
            Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value) {
                    super.setText(String.valueOf((value as Map).get("name")))
                }

                this
            }
        })

        this.comboBox4 = sb.comboBox(items: HourUtils.getHours(),
            selectedIndex: 7, renderer: new BasicComboBoxRenderer() {
            @Override
            Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value) {
                    super.setText(String.valueOf((value as Map).get("text")))
                }

                this
            }
        })

        this.commentTextField = sb.textField(preferredSize: new Dimension(145, 25))

        // 明细面板
        rootPanel.add(super.sb.panel() {
            tableLayout {
                tr {
                    td {
                        panel(border: titledBorder(title: DateUtils.longToDateString(date))) {
                            tableLayout {
                                tr {
                                    td {
                                        label(text: "工时对象")
                                    }
                                    td {
                                        // 默认选中项目出勤
                                        widget(this.comboBox1)
                                    }
                                    td {
                                        label(text: "工时分类")
                                    }
                                    td {
                                        widget(this.comboBox2)
                                    }
                                    td {
                                        label(text: "项目名称")
                                    }
                                    td {
                                        widget(this.comboBox3)
                                    }
                                    td {
                                        label(text: "工时")
                                    }
                                    td {
                                        widget(this.comboBox4)
                                    }
                                    td {
                                        label(text: "简述")
                                    }
                                    td {
                                        widget(this.commentTextField)
                                    }
                                    td {
                                        button(icon: sb.imageIcon(url: this.getClass().getResource("/icon/add.png")),
                                            text: "添加", toolTipText: "添加", actionPerformed: {
                                            String comment = this.commentTextField.getText()
                                            if (comment && comment.length() > 20) {
                                                NotificationUtils.notify("工时添加", "简述长度不能超过20!", false)
                                                return
                                            }
                                            NotificationUtils.notify(
                                                "工时添加",
                                                TimeHttpClient.saveHours(this.getSaveHourRequest(date, comment)) as boolean
                                            )
                                            // 重新加载明细
                                            this.detailPanel(this.selectedDay)
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
                tr {
                    td {
                        // 草稿箱面板
                        panel(border: titledBorder(title: "工时草稿箱"), layout: borderLayout()) {
                            def JTable tab = table(background: super.scrollPane.getBackground(),
                                // 只能选中单行
                                selectionMode: ListSelectionModel.SINGLE_SELECTION) {
                                tableModel(list: draft) {
                                    propertyColumn(header: "ID", propertyName: "id", editable: false)
                                    propertyColumn(header: "日期", propertyName: "day", editable: false, cellRenderer: new DefaultTableCellRenderer() {
                                        @Override
                                        protected void setValue(Object value) {
                                            this.setText(DateUtils.longToDateString(value as long))
                                        }
                                    })
                                    propertyColumn(header: "工时对象", propertyName: "attendanceTypeName", editable: false)
                                    propertyColumn(header: "工时分类", propertyName: "manhourTypeName", editable: false)
                                    propertyColumn(header: "项目", propertyName: "projectName", editable: false, preferredWidth: 90)
                                    propertyColumn(header: "项目经理", propertyName: "projectManagerName", editable: false)
                                    propertyColumn(header: "工时", propertyName: "hour", editable: false)
                                    propertyColumn(header: "简述", propertyName: "content", editable: false)
                                    propertyColumn(header: "创建时间", propertyName: "createTime", editable: false, preferredWidth: 150, cellRenderer: new DefaultTableCellRenderer() {
                                        @Override
                                        protected void setValue(Object value) {
                                            this.setText(DateUtils.longToDateTimeString(value as long))
                                        }
                                    })
                                }
                            }
                            // 右键菜单
                            def menu = sb.popupMenu {
                                menuItem(
                                    text: "删除草稿",
                                    icon: imageIcon(url: this.getClass().getResource("/icon/delete.png")),
                                    actionPerformed: {
                                        int index = tab.getSelectedRow()
                                        if (-1 != index) {
                                            def id = tab.getValueAt(index, 0) as int
                                            NotificationUtils.notify("删除草稿", TimeHttpClient.deleteDraft(id) as boolean)
                                            // 重新加载明细
                                            this.detailPanel(this.selectedDay)
                                        }
                                    }
                                )
                                separator()
                                menuItem(
                                    text: "提交审核",
                                    icon: imageIcon(url: this.getClass().getResource("/icon/audit.png")),
                                    actionPerformed: {
                                        int index = tab.getSelectedRow()
                                        if (-1 != index) {
                                            def id = tab.getValueAt(index, 0) as int
                                            NotificationUtils.notify("提交审核", TimeHttpClient.submitDraft(id) as boolean)
                                            // 重新加载明细
                                            this.detailPanel(this.selectedDay)
                                        }
                                    }
                                )
                            }
                            tab.addMouseListener(new MouseAdapter() {
                                @Override
                                void mouseClicked(MouseEvent e) {
                                    if (e.getButton() == MouseEvent.BUTTON3) {
                                        def index = tab.rowAtPoint(e.getPoint())
                                        if (index == -1) {
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
                    td {
                        panel(border: titledBorder(title: "已提交工时"), layout: borderLayout()) {
                            def tab = table(background: super.scrollPane.getBackground(), enabled: false) {
                                tableModel(list: audit) {
                                    propertyColumn(header: "ID", propertyName: "id", editable: false)
                                    propertyColumn(header: "日期", propertyName: "day", editable: false, cellRenderer: new DefaultTableCellRenderer() {
                                        @Override
                                        protected void setValue(Object value) {
                                            this.setText(DateUtils.longToDateString(value as long))
                                        }
                                    })
                                    propertyColumn(header: "工时对象", propertyName: "attendanceTypeName", editable: false)
                                    propertyColumn(header: "工时分类", propertyName: "manhourTypeName", editable: false)
                                    propertyColumn(header: "项目", propertyName: "projectName", editable: false, preferredWidth: 90)
                                    propertyColumn(header: "项目经理", propertyName: "projectManagerName", editable: false)
                                    propertyColumn(header: "工时", propertyName: "hour", editable: false)
                                    propertyColumn(header: "简述", propertyName: "content", editable: false)
                                    propertyColumn(header: "审核状态", propertyName: "auditType", editable: false, cellRenderer: new DefaultTableCellRenderer() {
                                        @Override
                                        protected void setValue(Object value) {
                                            int auditStatus = value as int
                                            switch (auditStatus) {
                                                case 2:
                                                    this.setText("审核中")
                                                    this.setForeground(Color.YELLOW)
                                                    break
                                                case 3:
                                                    this.setText("审核通过")
                                                    this.setForeground(Color.GREEN)
                                                    break
                                                case 4:
                                                    this.setText("审核未通过")
                                                    this.setForeground(Color.RED)
                                                    break
                                                default: break
                                            }
                                        }
                                    })
                                    propertyColumn(header: "审核意见", propertyName: "auditComment", editable: false)
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
        rootPanel.validate()
        rootPanel.repaint()
    }

    def private getSaveHourRequest(long day, String content) {
        def combo1 = this.comboBox1.getSelectedItem() as Map
        def combo2 = this.comboBox2.getSelectedItem() as Map
        def combo3 = this.comboBox3.getSelectedItem() as Map
        def combo4 = this.comboBox4.getSelectedItem() as Map

        [
            attendanceTypeId: combo1.get("id") as int,
            manhourTypeId   : combo2.get("id"),
            hour            : combo4.get("value") as String,
            content         : content ? content : "",
            day             : day as String,
            // 若可用则为正常出勤 否则为缺勤
            projectId       : comboBox3.isEnabled() ? combo3.get("id") as int : ""
        ]
    }

    def private getComboBox1SelectedId() {
        def selectedItem = this.comboBox1.getSelectedItem() as Map

        selectedItem.get("id") as int
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
