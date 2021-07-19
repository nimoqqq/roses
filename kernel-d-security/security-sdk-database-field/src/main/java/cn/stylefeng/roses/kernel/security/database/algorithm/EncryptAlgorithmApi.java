package cn.stylefeng.roses.kernel.security.database.algorithm;

import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * 加密算法接口
 * <p>
 * 可根据自身需要自定义实现，默认实现为AES
 *
 * @author majianguo
 * @date 2021/7/3 11:02
 */
public interface EncryptAlgorithmApi {

    /**
     * 加密算法
     *
     * @param encryptedData 加密数据
     * @return {@link java.lang.String}
     * @author majianguo
     * @date 2021/7/3 11:07
     **/
    String encrypt(String encryptedData);

    /**
     * 解密算法
     *
     * @param cipher 待解密密文
     * @return {@link java.lang.String}
     * @author majianguo
     * @date 2021/7/3 11:33
     **/
    String decrypt(String cipher);

    /**
     * 设置加密实例
     * <p>
     * 只有在项目首次初始化配置时调用，随意调用会导致之前加密数据无法解密
     *
     * @author majianguo
     * @date 2021/7/19 10:04
     **/
    void setInstance(SymmetricCrypto instance);
}
