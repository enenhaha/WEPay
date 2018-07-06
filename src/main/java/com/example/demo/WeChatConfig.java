package com.example.demo;

import com.github.wxpay.sdk.WXPayConfig;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by chenqingfeng on 28/06/2017.
 */
public class WeChatConfig implements WXPayConfig {
    private byte[] certData;
    private String appid; //appid
    private String mchid; //商户 ID
    private String key; //商户平台设置的加密 key
    private String certPath; //证书路径

    /**
     *
     * @param app_id 微信开放平台的app_id
     * @param mch_id 商户号
     * @param key 商户号的key
     * @param cert_path //证书路径，退款需填，否则置空即可
     */
    public WeChatConfig(String app_id, String mch_id, String key, String cert_path) throws Exception{
        this.appid = app_id;
        this.mchid = mch_id;
        this.key = key;
        this.certPath = cert_path;
        if (StringUtils.isNotBlank(this.certPath)) {
            File file = new File(certPath);
            InputStream certStream = new FileInputStream(file);
            this.certData = new byte[(int) file.length()];
            certStream.read(this.certData);
            certStream.close();
        }
    }

    @Override
    public String getAppID() {
        return this.appid;
    }

    @Override
    public String getMchID() {
        return this.mchid;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public InputStream getCertStream() {
        return new ByteArrayInputStream(this.certData);
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 8000;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 10000;
    }

    public String toStrings() {
        return "WeChatConfig{" +
                "appid='" + appid + '\'' +
                ", mchid='" + mchid + '\'' +
                ", key='" + key + '\'' +
                ", certPath='" + certPath + '\'' +
                '}';
    }
}
