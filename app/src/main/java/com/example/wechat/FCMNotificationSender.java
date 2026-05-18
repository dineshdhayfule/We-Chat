package com.example.wechat;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMNotificationSender {

    private static final String TAG = "FCMNotificationSender";
    private static final String PROJECT_ID = "wechat-52f66";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

    // Service Account JSON - Properly formatted as a single line string to avoid parsing errors
    private static final String SERVICE_ACCOUNT_JSON = "{"
            + "\"type\": \"service_account\","
            + "\"project_id\": \"wechat-52f66\","
            + "\"private_key_id\": \"aacf08cf3fa759f1a8d504d9b374dac2a20e66bc\","
            + "\"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQD6+uaTriB7T+8b\\nrL/KYq5ShpnBM25RSt0UrwrEEMCL+9KZszI7OuIwLUnXzDIlrn1c2icPo/46o6WD\\nRbo62lE1zO1p+mgnGIhl1uhK4Lr5vmVXBCo8TpA9l4g/3uUMdgqyWoae43RMkGkG\\nGuzEcERArBIRbU+vQKAibVo0XhsbihyJ/hX+1h9QwKk/bZL37Ez9Tqsi4nZHMWfS\\nvxiG54EBWiCzNc+LG0Kz0y3L5DHG1udoiDTYXlraNa9rTth0AhDIAyNSchhzpMix\\nhh1ZaNTQ+Quhas33v9igjG0Mx/ptAEaogKq6yx1AIwv2CdjXxZ1zbHO8F5mrH0Ri\\n+/jsjbgPAgMBAAECggEABZB8fje1FqbOCVIKKoWHkeLUE9l9D6ZWQngjB/fLHQ9n\\nOusjKPcEPscyROuNDRLSSrW3efxLzacFK2TnXRisBkJV3SylFIDRvVhHbCmRW6oh\\n+wznltCzF/p9FdVuRJl1YUDqjSlD0dK90VnVeAF5MeneuK4wvxTyt/CuuEyB01lw\\nTSEHPHNZ9M/jyA05RUxwsvw6r9QdJFPoKdl7VfMY6GvRrJkQfqZMiiy+d6kwwdQy\\nqJErR93mqWtxViSresLN39hw5/z8AZDmQo1ImslvZofZJGUpIWHULGAbQ7Kmuxb5\\npQ2JmtEThVfoVbScnk7lu/x1hKDIDqQNj3NHsbxRsQKBgQD+7Mt/ZsOhD3TnLV9U\\ng7MrGhTMlqPbDh9FMykMVyeDXXzd7COlWzkzigxYoPgN/3KjAM47BbgqzdxUvrhc\\nTYrPUYttqVf1CzBjA/ZtBrxIa9mq0bxyJ/CnqAoXmILZXCTBv7Zblb1rj8kOQM2E\\nzP1hjidczPG0/ywLmIou/9YTdwKBgQD8CdjYOjy02COotbuOLL36WMFqQQw41SoC\\nmaiDvdtTLPxt3rng8YyTOFmo35MXcT56LwZKr7tpx/hRaMEwUFkxI5qjcAlmym5v\\n/lXfLSOeMikgSKsYPIYm5F/dVDgECmPvUktWqIubxImeSFyDc1izMeCS9GF//o/+\\nNT322YK2KQKBgQDtdbfNUfVStuonWX1eZVtk/+N1+7BgIZSBSjmnVBvrYw8oTYma\\nwxHSb3o2qKLHrzaineJ82kQGI0Jk8k8bM+PkYEoneUIEcUq+QPev8UE7mLLmSn5m\\nO8wQ8BeOiTMBs7JNg/4i66XJZuNa0oReevBfiiIicImKFtN5bbryMr+/2wKBgQCT\\nIGhuKOjhJL8EMDoxT5sC9ibKEPjCgGUKqYo+hWjvz/X3aSoWzsqh8iYct46VA61W\\nA/dnR9hecrZZR45m1rCKR99wgulqEMeRJuYX5rDBG0T4rJL8DfC96ViwygX9Ddey\\nj9ac2rzMjFlNha5DqeTgjkAraO81mXOTEJ+AZw6vqQKBgEAlRS74iNGP/sUX+MUi\\n6wfQmR5s86/BLFrEmeVEaMlcvDrcM2yz6nLtuKymwc0OAVhNk5e/1qoE6hY5t5qz\\nb4AjLH9jvS4rFBquBU+NvCUkwyHnBIdJ2e+/B4t3JyAUqP7aJgVXhcthCyp0abXL\\n01zyLlmoEi16ovx8CBpkkl29\\n-----END PRIVATE KEY-----\\n\","
            + "\"client_email\": \"firebase-adminsdk-81yi8@wechat-52f66.iam.gserviceaccount.com\","
            + "\"client_id\": \"100480175412152229744\","
            + "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\","
            + "\"token_uri\": \"https://oauth2.googleapis.com/token\","
            + "\"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\","
            + "\"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-81yi8%40wechat-52f66.iam.gserviceaccount.com\","
            + "\"universe_domain\": \"googleapis.com\""
            + "}";

    public static void sendNotification(Context context, String token, String title, String body, String senderId, String senderName) {
        new Thread(() -> {
            try {
                String accessToken = getAccessToken();
                if (accessToken == null) {
                    Log.e(TAG, "Failed to get access token");
                    return;
                }

                OkHttpClient client = new OkHttpClient();
                
                JSONObject messageObject = new JSONObject();
                messageObject.put("token", token);

                JSONObject notificationObject = new JSONObject();
                notificationObject.put("title", title);
                notificationObject.put("body", body);
                messageObject.put("notification", notificationObject);

                JSONObject dataObject = new JSONObject();
                dataObject.put("senderId", senderId);
                dataObject.put("senderName", senderName);
                messageObject.put("data", dataObject);

                JSONObject mainObject = new JSONObject();
                mainObject.put("message", messageObject);

                RequestBody requestBody = RequestBody.create(
                        mainObject.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(FCM_URL)
                        .header("Authorization", "Bearer " + accessToken)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Failed to send notification (v1): " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String responseBody = response.body() != null ? response.body().string() : "empty body";
                        if (response.isSuccessful()) {
                            Log.i(TAG, "Notification sent successfully (v1): " + responseBody);
                        } else {
                            Log.e(TAG, "Failed to send notification (v1). Code: " + response.code() + ", Body: " + responseBody);
                        }
                        response.close();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error in sendNotification: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private static String getAccessToken() throws IOException {
        InputStream stream = new ByteArrayInputStream(SERVICE_ACCOUNT_JSON.getBytes());
        GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
