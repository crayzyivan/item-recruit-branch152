package com.item.framework.net;

import java.util.Map;

public interface HttpClient5Service {
    String doPost(String url, String body, Map<String, String> headers);
    void doPostAsync(String url, String body, Map<String, String> headers);
    String doGet(String url, Map<String, String> headers);
} 