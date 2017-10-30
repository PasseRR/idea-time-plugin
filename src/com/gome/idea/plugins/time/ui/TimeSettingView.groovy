package com.gome.idea.plugins.time.ui

import com.gome.idea.plugins.time.settings.TimeSettings

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
                            this.urlField = textField(name: "url", text: this.getValue("url"), preferredSize: dimension)
                        }
                        td {
                            button(text: "测试链接", actionPerformed: {

                            })
                        }
                    }
                    tr {
                        td {
                            label(text: "用户名")
                        }
                        td {
                            this.userNameField = textField(name: "userName", text: this.getValue("userName"), preferredSize: dimension)
                        }
                    }
                    tr {
                        td {
                            label(text: "密码")
                        }
                        td {
                            this.passField = passwordField(name: "password", text: this.getValue("password"), preferredSize: dimension)
                        }
                        td {
                            button(text: "测试登录", actionPerformed: {

                            })
                        }
                    }
                }
            }
        )
    }

    @Override
    def isModified() {
        return this.urlField.getText() != this.getValue("url") ||
            this.userNameField.getText() != this.getValue("userName") ||
            String.valueOf(this.passField.getPassword()) != this.getValue("password")
    }

    @Override
    def reset() {
        this.urlField.setText(this.getValue("url"))
        this.userNameField.setText(this.getValue("userName"))
        this.passField.setText(this.getValue("password"))
    }

    @Override
    def apply() {
        settings.setUrl(this.urlField.getText())
        settings.setUserName(this.userNameField.getText())
        settings.setPassword(String.valueOf(this.passField.getPassword()))
    }

    private def getValue(String key) {
        def value = settings.getProperty(key)
        value ? value as String : ""
    }
}
