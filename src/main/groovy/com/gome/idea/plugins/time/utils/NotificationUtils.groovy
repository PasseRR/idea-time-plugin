package com.gome.idea.plugins.time.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

import java.util.concurrent.TimeUnit

/**
 * idea通知工具类
 * @author xiehai1
 * @date 2017/10/31 14:53
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
class NotificationUtils {
    def private NotificationUtils() {

    }

    /**
     * 弹出提示
     * @param event 事件名称
     * @param flag 是否成功
     */
    def static notify(String event, boolean flag) {
        notify(event, "操作${flag ? "成功" : "失败"}!", flag)
    }

    def static notify(String event, String msg, boolean flag){
        final Notification n = new Notification(
            "Time",
            event,
            msg,
            flag ? NotificationType.INFORMATION : NotificationType.ERROR
        )

        Notifications.Bus.notify(n)

        // 异步让提示隐藏
        new Thread({
            TimeUnit.SECONDS.sleep(4)
            n.expire()
        }).start()
    }
}
