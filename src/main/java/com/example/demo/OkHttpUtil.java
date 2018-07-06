package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;

public class OkHttpUtil {

    public static JSONObject sendGet(String url, String param) throws IOException {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = null;
        String urlNameString = url + "?" + param;
        Request request = new Request.Builder()
                .url(urlNameString)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("user-agent",
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .build();

        Response response = client.newCall(request).execute();
        if(response.isSuccessful()) {
            String responseStr = response.body().string();
            jsonObject = JSON.parseObject(responseStr);
        }
        return jsonObject;
    }
}
