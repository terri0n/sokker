package com.raqueto.sokkerasistente;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);

        myWebView.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(myWebView.getContext())
                        .setTitle(view.getTitle())
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        });

                b.show();

                // Indicate that we're handling this manually
                return true;
            }
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result)
            {
                new AlertDialog.Builder(view.getContext()).setMessage(message).setCancelable(true).show();
                result.confirm();
                return true;
            }
            @Override
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
            {
                // if a view already exists then immediately terminate the new one
                if (mCustomView != null)
                {
                    callback.onCustomViewHidden();
                }

            }


        });

        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        // Use the API 11+ calls to disable the controls
        new Runnable() {
            public void run() {
                myWebView.getSettings().setDisplayZoomControls(false);
            }
        }.run();


        myWebView.loadUrl("https://raqueto.com/sokker/asistente");

        myWebView.setWebViewClient(new WebViewClient() {
            // Abrir enlaces en el webview
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                return false;
//            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            private Map<String, String> convertResponseHeaders(Map<String, List<String>> headers) {
                Map<String, String> responseHeaders = new HashMap<>();

                for (Map.Entry<String, List<String>> item : headers.entrySet()) {
                    StringBuilder value = new StringBuilder();

                    for (String headerVal : item.getValue()) {
                        value.append((value.length() == 0) ? "" : ",").append(headerVal);
                    }

                    responseHeaders.put(item.getKey(), value.toString());
                }

                return responseHeaders;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                final String url = request.getUrl().toString();
                if (!SokkerProxy.isSokkerUrl(url)) {
                    return super.shouldInterceptRequest(view, request);
                }

                final String method = request.getMethod();
                final Map<String, String> requestHeaders = request.getRequestHeaders();
                if (SokkerProxy.isCorsPreflight(method, url, requestHeaders)) {
                    return new WebResourceResponse(
                            "text/plain",
                            "UTF-8",
                            204,
                            "No Content",
                            SokkerProxy.buildCorsPreflightResponseHeaders(requestHeaders),
                            new ByteArrayInputStream(new byte[0])
                    );
                }

                String ext = MimeTypeMap.getFileExtensionFromUrl(url);
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod(method);
                    for (Map.Entry<String, String> header : SokkerRequestHeaders.forSokker(
                            SokkerProxy.buildForwardHeaders(requestHeaders)).entrySet()) {
                        conn.setRequestProperty(header.getKey(), header.getValue());
                    }
                    conn.setDoInput(true);
                    conn.setUseCaches(false);

                    if (SokkerProxy.isLegacyXmlSessionLogin(method, url)) {
                        String body = SokkerProxy.buildLegacyLoginBody(url);
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                        try (OutputStream out = conn.getOutputStream()) {
                            out.write(body.getBytes(StandardCharsets.UTF_8));
                        }
                    }

                    Map<String, String> responseHeaders = convertResponseHeaders(conn.getHeaderFields());
                    // Habilito CORS
                    responseHeaders.put("Access-Control-Allow-Origin", "*");
                    responseHeaders.put("Access-Control-Allow-Methods", method);

                    return new WebResourceResponse(
                            mime,
                            conn.getContentEncoding(),
                            conn.getResponseCode(),
                            conn.getResponseMessage(),
                            responseHeaders,
                            SokkerProxy.responseStream(conn)
                    );

                } catch (Exception e) {
                    Log.e(this.toString(), "Sokker proxy failed: " + e.getClass().getSimpleName());
                }
                return null;
            }
        });

    }

    // Hacer q el botón de volver vuelva en el navegador
    @Override
    public void onBackPressed() {
        if(myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
