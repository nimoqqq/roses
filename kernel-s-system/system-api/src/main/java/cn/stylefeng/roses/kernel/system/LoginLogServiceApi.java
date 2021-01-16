package cn.stylefeng.roses.kernel.system;

import cn.stylefeng.roses.kernel.system.pojo.loginlog.request.SysLoginLogRequest;

/**
 * 登录日志api接口
 *
 * @author chenjinlong
 * @date 2021/1/13 11:12
 */
public interface LoginLogServiceApi {

    /**
     * 添加日志
     *
     * @param sysLoginLogRequest 参数
     * @author chenjinlong
     * @date 2021/1/13 10:56
     */
    void add(SysLoginLogRequest sysLoginLogRequest);

    /**
     * 登录成功
     *
     * @param userId 用户id
     * @author chenjinlong
     * @date 2021/1/13 11:36
     */
    void loginSuccess(Long userId);

    /**
     * 登录失败
     *
     * @param userId     用户id
     * @param llgMessage 错误信息
     * @author chenjinlong
     * @date 2021/1/13 11:36
     */
    void loginFail(Long userId, String llgMessage);

    /**
     * 登出成功
     *
     * @param userId 用户id
     * @author chenjinlong
     * @date 2021/1/13 11:36
     */
    void loginOutSuccess(Long userId);

    /**
     * 登出失败
     *
     * @param userId 用户id
     * @author chenjinlong
     * @date 2021/1/13 11:36
     */
    void loginOutFail(Long userId);

    /**
     * 清空
     *
     * @author chenjinlong
     * @date 2021/1/13 10:55
     */
    void deleteAll();
}