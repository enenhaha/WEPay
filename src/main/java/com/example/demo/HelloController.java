package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

@RestController
public class HelloController {
    //    @RequestMapping("/hello")
//    public String hello(){
//        return "hello1";
//    }
//
    public static final String AM_APPID = "******************";//appid
    public static final String AM_MCH_ID = "******************";//微信支付商户号
    public static final String AM_MCH_KEY = "******************";//商户支付key
    public static final String AM_Secret = "******************";//AM_Secret 微信公众号处可以看到

    @RequestMapping("/wxapi/pay")
    public R pay(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {
            String code = params.get("code").toString();//获取code
            String orderNo = params.get("orderNo").toString();//获取code


            Map params1 = new HashMap();
            params1.put("secret", AM_Secret);
            params1.put("appid", AM_APPID);
            params1.put("grant_type", "authorization_code");
            params1.put("code", code);
            String result = HttpGetUtil.httpRequestToString("https://api.weixin.qq.com/sns/oauth2/access_token", params1);
            com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) JSON.parse(result);
            String openid = jsonObject.get("openid").toString();

            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "");

            Map<String, String> data = new HashMap();
            data.put("body", "测试商品");//支付时候显示的名称
            data.put("out_trade_no", orderNo);//数据库内的订单号
            data.put("device_info", "WEB");
            data.put("fee_type", "CNY");//货币种类

            String total_fee = "1";//金额

            data.put("total_fee", total_fee);//单位为分  只能为整数    OrdersInfo.getPrice()
            data.put("spbill_create_ip", request.getRemoteHost());
            data.put("notify_url", "http://qijimianliu.iok.la/wxapi/refund");//支付完成后回调地址 接口
            data.put("trade_type", "JSAPI");
            data.put("openid", openid);//openid

            WXPay wxpay = new WXPay(config);
            Map<String, String> resp = wxpay.unifiedOrder(data);
            if ("SUCCESS".equals(resp.get("result_code")) && "OK".equals(resp.get("return_msg"))) {

                String timestamp = System.currentTimeMillis() / 1000L + "";
                SortedMap<String, String> finalpackage = new TreeMap<String, String>();
                String packages = "prepay_id=" + resp.get("prepay_id");
                finalpackage.put("appId", resp.get("appid"));
                finalpackage.put("timeStamp", timestamp);
                finalpackage.put("nonceStr", resp.get("nonce_str"));
                finalpackage.put("package", packages);
                finalpackage.put("signType", "MD5");
                String signature = WXPayUtil.generateSignature(finalpackage, config.getKey());
                finalpackage.put("paySign", signature);

                R r = R.ok();
                r.put("data", finalpackage);
                r.put("type", true);
                return r;

            } else {
                R r = R.ok();
                r.put("code", 200);
                r.put("msg", "支付失败");
                r.put("type", true);
                return r;
            }
        } catch (Exception e) {
            R r = R.error();
            r.put("type", false);
            r.put("msg", "啊哦~服务器出错了");
            r.put("errorCode", "000");
            return r;
        }


    }


    /**
     * 回调
     */
    @RequestMapping("/wxapi/payBack")
    public String payBack(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) {

        //System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        try {
            StringBuilder notifyData = new StringBuilder(); // 支付结果通知的xml格式数据
            String inputLine;
            while ((inputLine = request.getReader().readLine()) != null) {
                notifyData.append(inputLine);
            }
            request.getReader().close();

            Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData.toString());  // 转换成map
            final String out_trade_no = notifyMap.get("out_trade_no");
            final String transaction_id = notifyMap.get("transaction_id");


            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "");
            WXPay wxpay = new WXPay(config);
            String resXml;
            if (wxpay.isPayResultNotifySignatureValid(notifyMap)) {
                /*
                 *
                 * 这里写修改你的平台上的订单支付状态代码
                 *
                 * */

                //if (orderEntity.getState() == 0) {//未付款

                //}

                // 签名正确
                // 进行处理。
                // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
                resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml> ";
            } else {
                // 签名错误，如果数据里没有sign字段，也认为是签名错误
                resXml = "<xml>return_code><![CDATA[FAIL]]></return_code>return_msg><![CDATA[报文为空]]></return_msg></xml>";
            }
            return resXml;

        } catch (Exception e) {
            e.printStackTrace();
            return "<xml>return_code><![CDATA[FAIL]]></return_code>return_msg><![CDATA[服务器错误]]></return_msg></xml>";
        }

    }


    @RequestMapping("/wxapi/refund")
    public R refund(@RequestParam Map<String, Object> params) throws Exception {

        try {


            R r = R.ok();

            String out_trade_no = params.get("orderNo").toString();//商户订单号
            String total_fee = "1";//金额

            Map<String, String> data = new HashMap();

            data.put("appid", AM_APPID);
            data.put("mch_id", AM_MCH_ID);
            data.put("out_trade_no", out_trade_no); //out_trade_no   商户订单号
//        data.put("transaction_id", transaction_id);//out_refund_no 微信订单号    和商户订单号二选一即可

            data.put("out_refund_no", out_trade_no);//out_refund_no 商户退款单号
            data.put("total_fee", total_fee);       //total_fee 订单金额
            data.put("refund_fee", total_fee);      //refund_fee 退款金额
            data.put("refund_fee_type", "CNY");     //refund_fee_type 退款货币种类 需与支付一致

            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "D://Users//lwj//Desktop//cert//apiclient_cert.p12");

            WXPay wxpay = new WXPay(config);
            Map<String, String> resp = wxpay.refund(data);


            try {
                if ("SUCCESS".equals(resp.get("result_code")) && "OK".equals(resp.get("return_msg"))) {
                    /*
                     *
                     *   退款成功后 写的代码地方
                     *
                     * */
                    r.put("type", true);
                    r.put("msg", "退款成功");
                    System.out.println("订单号：" + out_trade_no + "退款成功");
                    return r;
                } else {
                    r.put("type", true);
                    r.put("msg", "退款失败");
                    return r;
                }

            } catch (Exception e) {
                r.put("type", true);
                r.put("msg", "退款失败");
                return r;
            }
        } catch (Exception e) {
            R r = R.error();
            r.put("type", false);
            r.put("msg", "啊哦~服务器出错了");
            r.put("errorCode", "000");
            return r;
        }


    }

    /*
     * 二维码支付
     * */
    @RequestMapping("/wxapi/qrcode")
    public R qrcode(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {

            R r = R.ok();

            String out_trade_no = params.get("orderNo").toString();//商户订单号
            String total_fee = "1";//金额

            Map<String, String> data = new HashMap();

            data.put("appid", AM_APPID);
            data.put("mch_id", AM_MCH_ID);
            data.put("body", "二维码支付");
            data.put("out_trade_no", out_trade_no); //out_trade_no   商户订单号
            data.put("total_fee", total_fee);       //total_fee 订单金额
            data.put("spbill_create_ip", request.getRemoteHost());
            data.put("notify_url", "http://qijimianliu.iok.la/wxapi/payBack");//这个回调需要微信支付商户平台设置 不然是不会回调（应该吧）
            data.put("trade_type", "NATIVE");


            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "");

            WXPay wxpay = new WXPay(config);
            Map<String, String> resp = wxpay.unifiedOrder(data);

            try {
                if ("SUCCESS".equals(resp.get("result_code")) && "OK".equals(resp.get("return_msg"))) {

                    SortedMap<String, String> finalpackage = new TreeMap<String, String>();
                    finalpackage.put("code_url", resp.get("code_url"));


                    r.put("type", true);
                    r.put("data", finalpackage);
                } else {
                    r.put("type", false);
                }
                return r;
            } catch (Exception e) {
                r.put("type", false);
                r.put("msg", "退款失败");
                return r;
            }


        } catch (Exception e) {
            R r = R.error();
            r.put("type", false);
            r.put("msg", "啊哦~服务器出错了");
            r.put("errorCode", "000");
            return r;
        }
    }
}
