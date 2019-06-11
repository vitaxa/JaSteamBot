package com.vitaxa.jasteambot.steam.web;

import com.vitaxa.jasteambot.helper.IOHelper;
import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.webapi.WebAPI;
import uk.co.thomasc.steamkit.types.KeyValue;
import uk.co.thomasc.steamkit.util.KeyDictionary;
import uk.co.thomasc.steamkit.util.WebHelpers;
import uk.co.thomasc.steamkit.util.crypto.CryptoException;
import uk.co.thomasc.steamkit.util.crypto.CryptoHelper;
import uk.co.thomasc.steamkit.util.crypto.RSACrypto;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class SteamWeb {

    private final static Logger LOG = LoggerFactory.getLogger(SteamWeb.class);

    private static final int CONNECT_TIMEOUT = 15000;

    private static final int REQUEST_TIMEOUT = 20000;

    private static final int SOCKET_TIMEOUT = 30000;

    private final CloseableHttpClient httpClient;
    private final HttpClientContext httpContext;
    private final CookieStore cookieStore;

    private String token;
    private String sessionId;
    private String tokenSecure;

    private Consumer<HttpResponse> responseCallback;

    public SteamWeb() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setCircularRedirectsAllowed(true)
                .setRelativeRedirectsAllowed(true)
                .setRedirectsEnabled(true)
                .build();
        this.httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(requestConfig)
                .build();
        this.cookieStore = new BasicCookieStore();
        // Create local HTTP context
        this.httpContext = HttpClientContext.create();
        // Bind custom cookie store to the local context
        this.httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
    }

    public String fetch(String url, HttpParameters params) {
        return fetch(url, params, null, true, "");
    }

    public String fetch(String url, HttpParameters params, boolean ajax) {
        return fetch(url, params, null, ajax, "");
    }

    public String fetch(String url, HttpParameters params, RequestConfig options) {
        return fetch(url, params, options, true, "");
    }

    public String fetch(String url, HttpParameters params, String referer) {
        return fetch(url, params, null, true, referer);
    }

    public String fetch(String url, HttpParameters params, RequestConfig options, String referer) {
        return fetch(url, params, options, true, referer);
    }

    public String fetch(String url, HttpParameters params, boolean ajax, String referer) {
        return fetch(url, params, null, ajax, referer);
    }

    public String fetch(String url, HttpParameters params, RequestConfig options, boolean ajax) {
        return fetch(url, params, options, ajax, "");
    }

    public String fetch(String url, HttpParameters params, RequestConfig options, boolean ajax, String referer) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("url can't be null");
        }
        if (params == null) {
            throw new IllegalArgumentException("params can't be null");
        }
        String response = "";

        try (final CloseableHttpResponse httpResponse = request(url, params, null, ajax, referer)) {
            final HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                response = IOHelper.decode(IOHelper.read(responseEntity.getContent()));
                EntityUtils.consumeQuietly(responseEntity);
            }
        } catch (IOException e) {
            LOG.error("Exception while reading response", e);
        }

        return response;
    }

    public CloseableHttpResponse request(String url, HttpParameters params) {
        return request(url, params, null);
    }

    public CloseableHttpResponse request(String url, HttpParameters params, RequestConfig config) {
        return request(url, params, config, true, "");
    }

    public CloseableHttpResponse request(String url, HttpParameters params, RequestConfig config, boolean ajax, String referer) {
        LOG.debug("URL: {}", url);

        // Build request based on method
        final HttpRequestBase requestBase = params.buildRequest(url);
        if (config != null) {
            requestBase.setConfig(config);
        }

        return executeRequest(requestBase, ajax, referer);
    }

    public boolean authenticate(String myUniqueId, SteamClient client, String myLoginKey, String apiKey) {
        sessionId = Base64.getEncoder().encodeToString(IOHelper.encode(myUniqueId));

        // Generate an AES session key.
        byte[] sessionKey = CryptoHelper.generateRandomBlock(32);

        // rsa encrypt it with the public key for the universe we're on
        if (client.getUniverse() != null) {
            RSACrypto rsa = new RSACrypto(KeyDictionary.getPublicKey(client.getUniverse()));
            byte[] cryptedSessionKey = rsa.encrypt(sessionKey);

            byte[] loginKey = new byte[20];
            System.arraycopy(IOHelper.encode(myLoginKey), 0, loginKey, 0, myLoginKey.length());

            // aes encrypt the loginkey with our session key
            byte[] cryptedLoginKey = new byte[0];
            try {
                cryptedLoginKey = CryptoHelper.symmetricEncrypt(loginKey, sessionKey);
            } catch (CryptoException e) {
                e.printStackTrace();
            }

            if (cryptedLoginKey.length <= 0) return false;

            KeyValue authResult;

            // Get the Authentication Result
            try {
                WebAPI userAuth = new WebAPI("ISteamUserAuth", apiKey);
                authResult = userAuth.authenticateUser(String.valueOf(client.getSteamID().convertToUInt64()),
                        WebHelpers.urlEncode(cryptedSessionKey),
                        WebHelpers.urlEncode(cryptedLoginKey),
                        "POST",
                        "true");
                if (authResult == null) {
                    throw new IllegalStateException("Empty auth result");
                }
                final KeyValue tokenVal = authResult.get("token");
                final KeyValue tokenSecureVal = authResult.get("tokensecure");
                if (tokenVal == KeyValue.INVALID || tokenSecureVal == KeyValue.INVALID) {
                    throw new IllegalStateException("Invalid token");
                }
                this.token = tokenVal.asString();
                this.tokenSecure = tokenSecureVal.asString();

                // Adding cookies to the cookie container.
                Map<String, String> cookies = MapHelper.newHashMapWithExpectedSize(3);
                cookies.put("sessionid", this.sessionId);
                cookies.put("steamLogin", this.token);
                cookies.put("steamLoginSecure", this.tokenSecure);

                addCookies(cookies);
            } catch (Exception e) {
                LOG.error("Unable to authenticate bot", e);
                this.token = this.tokenSecure = null;
                return false;
            }
        } else {
            throw new IllegalStateException("Null steam client");
        }

        return true;
    }

    public boolean verifyCookies() {
        boolean cookieExist = false;
        try (final CloseableHttpResponse response = request("https://steamcommunity.com/", new HttpParameters(HttpMethod.HEAD))) {
            for (Cookie cookie : getCookies()) {
                if (cookie.getName().startsWith("steamLoginSecure") && !cookie.getValue().equalsIgnoreCase("deleted")) {
                    cookieExist = true;
                }
            }
        } catch (IOException e) {
            LOG.error("Couldn't check cookies", e);
        }
        return cookieExist;
    }

    private CloseableHttpResponse executeRequest(HttpRequestBase httpRequest, boolean ajax, String referer) {
        // Add header to request
        addHeader(httpRequest, referer, ajax);

        // Execute request
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpRequest, httpContext);
            LOG.debug("Response Status: {}", httpResponse.getStatusLine().getStatusCode());

            if (responseCallback != null) {
                responseCallback.accept(httpResponse);
            }

            return httpResponse;
        } catch (IOException e) {
            LOG.error("Exception while executing request", e);
        }
        return null;
    }

    private void addCookies(Map<String, String> cookies) {
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            // Create cookie
            BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
            cookie.setDomain("steamcommunity.com");
            cookie.setPath("/");

            // Add cookie to cookie store
            cookieStore.addCookie(cookie);
        }
    }

    private void addHeader(HttpRequestBase http, String referer, boolean ajax) {
        http.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko)" +
                " Chrome/31.0.1650.57 Safari/537.36");
        http.addHeader("Accept", "application/json, text/javascript;q=0.9, */*;q=0.5");
        http.addHeader("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
        http.addHeader("Referer", referer.isEmpty() ? "https://steamcommunity.com/trade/1" : referer);
        if (ajax) {
            http.addHeader("X-Requested-With", "XMLHttpRequest");
            http.addHeader("X-Prototype-Version", "1.7");
        }
    }

    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getToken() {
        return token;
    }

    public String getTokenSecure() {
        return tokenSecure;
    }

    public Consumer<HttpResponse> getResponseCallback() {
        return responseCallback;
    }

    public void setResponseCallback(Consumer<HttpResponse> responseCallback) {
        this.responseCallback = responseCallback;
    }
}
