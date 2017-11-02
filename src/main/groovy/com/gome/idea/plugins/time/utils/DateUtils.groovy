package com.gome.idea.plugins.time.utils
/**
 * 日期工具类
 * @author xiehai1
 * @date 2017/10/27 22:12
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class DateUtils {
    private DateUtils() {

    }

    /**
     * 小于10的数字自动补零
     * @param value
     * @return
     */
    def private static autoAppendZero(int value) {
        value > 9 ? value : "0" + value
    }

    /**
     * long型转时间字符串
     * @param datetime
     * @return yyyy-MM-dd
     */
    def static longToDateString(long datetime) {
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(new Date(datetime))
        def sb = new StringBuilder()
        sb.append(calendar.get(Calendar.YEAR))
        sb.append("-")
        sb.append(autoAppendZero(calendar.get(Calendar.MONTH) + 1))
        sb.append("-")
        sb.append(autoAppendZero(calendar.get(Calendar.DAY_OF_MONTH)))

        sb.toString()
    }

    /**
     * long型转时间字符串
     * @param datetime
     * @return yyyy-MM-dd HH:mm:ss
     */
    def static longToDateTimeString(long datetime) {
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(new Date(datetime))
        def sb = new StringBuilder()
        sb.append(calendar.get(Calendar.YEAR))
        sb.append("-")
        sb.append(autoAppendZero(calendar.get(Calendar.MONTH) + 1))
        sb.append("-")
        sb.append(calendar.get(Calendar.DAY_OF_MONTH))
        sb.append(" ")
        sb.append(autoAppendZero(calendar.get(Calendar.HOUR_OF_DAY)))
        sb.append(":")
        sb.append(autoAppendZero(calendar.get(Calendar.MINUTE)))
        sb.append(":")
        sb.append(autoAppendZero(calendar.get(Calendar.SECOND)))

        sb.toString()
    }
}
