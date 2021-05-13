package org.sample.chapter10.threadlocal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginContext {
    private final static ThreadLocal<LoginContext> holder = new ThreadLocal<>();

    /**
     * SSO账号
     */
    private String username;

    /**
     * 用户中文名称
     */
    private String fullname;

    /**
     * 取出登录的上下文
     *
     * @return null 如果没有的话
     */
    public static LoginContext getLoginContext() {
        return holder.get();
    }

    /**
     * 设置登录上下文
     * @param loginContext 登录上下文
     */
    public static void setLoginContext(LoginContext loginContext) {
        holder.set(loginContext);
    }
}
