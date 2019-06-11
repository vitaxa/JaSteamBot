package com.vitaxa.jasteambot.steam.web.http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpParameters {

    private static final Logger LOG = LoggerFactory.getLogger(HttpParameters.class);

    private final Map<String, String> params;
    private final HttpMethod method;

    public HttpParameters(HttpMethod method) {
        this(new HashMap<>(), method);
    }

    public HttpParameters(Map<String, String> params, HttpMethod method) {
        this.params = params;
        this.method = method;
    }

    public HttpRequestBase buildRequest(String url) {
        switch (method) {
            case POST:
                final HttpPost httpPost = new HttpPost(url);

                // Build post params
                final List<NameValuePair> urlParameters = new ArrayList<>(params.size());
                params.forEach((k, v) -> urlParameters.add(new BasicNameValuePair(k, v)));
                try {
                    final HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
                    // Set params
                    httpPost.setEntity(postParams);
                } catch (UnsupportedEncodingException e) {
                    LOG.error("Can't set post params. Encoding exception", e);
                }
                return httpPost;
            case GET:
                final StringBuilder sb = new StringBuilder();

                // Add params to url
                if (!params.isEmpty()) {
                    sb.append("?");
                    buildFromParams(sb, params);
                }

                final String resultUrl = url + sb.toString();

                return new HttpGet(resultUrl);
            case HEAD:
                return new HttpHead(url);
            default:
                throw new AssertionError("Unsupported method type: " + method.toString());
        }

    }

    private void buildFromParams(StringBuilder sb, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOG.error("Can't build string from parameters. {} method", method.toString());
                LOG.error("Encoding exception", e);
            }
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
