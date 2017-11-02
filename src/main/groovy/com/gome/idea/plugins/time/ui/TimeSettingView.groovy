package com.gome.idea.plugins.time.ui

import com.gome.idea.plugins.time.http.TimeHttpClient
import com.gome.idea.plugins.time.settings.TimeSettings
import com.intellij.openapi.ui.Messages

import javax.swing.*
import java.awt.*

/**
 * Gome Time设置视图
 * @author xiehai1
 * @date 2017/10/30 14:34
 * @Copyright ( c ) gome inc Gome Co.,LTD
 */
@Singleton(lazy = true, strict = false)
class TimeSettingView extends IdeaView {
    private def JTextField urlField
    private def JTextField userNameField
    private def JPasswordField passField
    private def final TimeSettings settings = TimeSettings.getInstance()
    private def final Dimension dimension = new Dimension(180, 25)

    def private TimeSettingView() {
        super.scrollPane.setViewportView(
            super.sb.panel() {
                tableLayout() {
                    tr {
                        td {
                            label(text: "服务器")
                        }
                        td {
                            this.urlField = sb.textField(
                                name: "url",
                                text: settings.getUrl(),
                                preferredSize: dimension,
                                toolTipText: "工时系统服务器地址(如:http://10.112.70.1)"
                            )
                        }
                        td {
                            button(text: "测试链接", actionPerformed: {
                                TimeHttpClient.enableConnect(this.urlField.getText()) ?
                                    Messages.showMessageDialog("连接成功!", "Time", Messages.getInformationIcon()) :
                                    Messages.showMessageDialog("连接失败!", "Time", Messages.getErrorIcon())
                            }, toolTipText: "测试工时系统是否可以访问")
                        }
                    }
                    tr {
                        td {
                            label(text: "用户名")
                        }
                        td {
                            this.userNameField = textField(
                                name: "userName",
                                text: settings.getUserName(),
                                preferredSize: dimension,
                                toolTipText: "工时系统用户名"
                            )
                        }
                    }
                    tr {
                        td {
                            label(text: "密码")
                        }
                        td {
                            this.passField = passwordField(
                                name: "password",
                                text: settings.getPassword(),
                                preferredSize: dimension,
                                toolTipText: "工时系统用户名"
                            )
                        }
                        td {
                            button(text: "测试登录", actionPerformed: {
                                TimeHttpClient.login(
                                    this.urlField.getText(),
                                    this.userNameField.getText(),
                                    String.valueOf(this.passField.getPassword())
                                ) ? Messages.showMessageDialog("登录成功!", "Time", Messages.getInformationIcon()) :
                                    Messages.showMessageDialog("登录失败!", "Time", Messages.getErrorIcon())
                            }, toolTipText: "测试是否可以登录")
                        }
                    }
                }
            }
        )
    }

    @Override
    def isModified() {
        return this.urlField.getText() != settings.getUrl() ||
            this.userNameField.getText() != settings.getUserName() ||
            String.valueOf(this.passField.getPassword()) != settings.getPassword()
    }

    @Override
    def reset() {
        this.urlField.setText(settings.getUrl())
        this.userNameField.setText(settings.getUserName())
        this.passField.setText(settings.getPassword())
    }

    @Override
    def apply() {
        settings.setUrl(this.urlField.getText())
        settings.setUserName(this.userNameField.getText())
        settings.setPassword(String.valueOf(this.passField.getPassword()))
    }
}
