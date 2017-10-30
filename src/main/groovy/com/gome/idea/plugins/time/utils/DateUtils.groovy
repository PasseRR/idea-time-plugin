package com.gome.idea.plugins.time.utils

/**
 * @author xiehai1
 * @date 2017/10/27 22:12
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class DateUtils {
    private DateUtils(){

    }

    def static longToDateString(long datetime){
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(new Date(datetime))
        def sb = new StringBuilder()
        sb.append(calendar.get(Calendar.YEAR))
        sb.append("-")
        def month = calendar.get(Calendar.MONTH) + 1
        sb.append(month > 9 ? month : "0" + month)
        def day = calendar.get(Calendar.DAY_OF_MONTH)
        sb.append("-")
        sb.append(day > 9 ? day : "0" + day)

        sb.toString()
    }
}
