package com.example.probuilder;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class AuthJsonRequest extends JsonObjectRequest {

    private final Context context;

    public AuthJsonRequest(
            Context context,
            int method,
            String url,
            JSONObject body,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        super(method, url, body, listener, errorListener);
        this.context = context;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        Map<String, String> headers = new HashMap<>();
        if (!token.isEmpty()) {
            headers.put("Authorization", token);
        }
        return headers;
    }
}
