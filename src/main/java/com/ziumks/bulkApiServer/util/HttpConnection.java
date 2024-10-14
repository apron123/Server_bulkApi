package com.ziumks.bulkApiServer.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.ziumks.bulkApiServer.util.exception.HttpConnectionException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConnection<T> {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnection.class);
    private ResponseHandler<T> responseHandler = null;

    public HttpConnection() {
        this.responseHandler = (ResponseHandler<T>) new BasicResponseHandler();
    }

    public HttpConnection(ResponseHandler<T> handler) {
        this.responseHandler = handler;
    }

    private UrlEncodedFormEntity paramMapToFormEntity(Map<String, Object> paramMap) throws HttpConnectionException {
        UrlEncodedFormEntity entity = null;
        if (paramMap != null && paramMap.size() > 0) {
            List<NameValuePair> urlParameters = new ArrayList();
            Set<String> reqParamKeyset = paramMap.keySet();
            Iterator var5 = reqParamKeyset.iterator();

            while (var5.hasNext()) {
                String key = (String) var5.next();
                urlParameters.add(new BasicNameValuePair(key, (String) paramMap.get(key)));
            }

            entity = new UrlEncodedFormEntity(urlParameters, Charset.forName("UTF-8"));
        }

        return entity;
    }

    public T doGet(String url, Map<String, Object> reqHeader, Map<String, Object> reqParam) throws HttpConnectionException {
        T result = this.doConnect("GET", url, reqHeader, reqParam);
        return result;
    }

    public T doPost(String url, Map<String, Object> reqHeader, Map<String, Object> reqParam) throws HttpConnectionException {
        T result = this.doConnect("POST", url, reqHeader, reqParam);
        return result;
    }

    public T doPost(String url, Map<String, Object> reqHeader, String reqBody) throws HttpConnectionException {
        T result = this.doConnect("POST", url, reqHeader, reqBody);
        return result;
    }

    public T doPut(String url, Map<String, Object> reqHeader, String reqBody) throws HttpConnectionException {
        T result = this.doConnect("PUT", url, reqHeader, reqBody);
        return result;
    }

    public T doPatch(String url, Map<String, Object> reqHeader, String reqBody) throws HttpConnectionException {
        T result = this.doConnect("PATCH", url, reqHeader, reqBody);
        return result;
    }

    public T doDelete(String url, Map<String, Object> reqHeader, Map<String, Object> reqParam) throws HttpConnectionException {
        T result = this.doConnect("DELETE", url, reqHeader, reqParam);
        return result;
    }

    private T doConnect(String method, String url, Map<String, Object> reqHeader, Map<String, Object> reqParam) throws HttpConnectionException {
        T result = null;

        try {
            URI targetUri = (new URL(url)).toURI();
            HttpRequestBase request = null;
            Set rqHeaderKetSet;
            if (!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("DELETE")) {
                if (!method.equalsIgnoreCase("POST")) {
                    throw new HttpConnectionException("NOT Supported method");
                }

                HttpPost post = new HttpPost(targetUri);
                UrlEncodedFormEntity entity = this.paramMapToFormEntity(reqParam);
                if (entity != null) {
                    post.setEntity(entity);
                }

                request = post;
            } else {
                if (reqParam != null && reqParam.size() > 0) {
                    rqHeaderKetSet = reqParam.keySet();
                    URIBuilder builder = new URIBuilder(targetUri);
                    Iterator var10 = rqHeaderKetSet.iterator();

                    while (var10.hasNext()) {
                        String key = (String) var10.next();
                        builder.addParameter(key, (String) reqParam.get(key));
                    }

                    targetUri = builder.build();
                }

                if (method.equalsIgnoreCase("GET")) {
                    request = new HttpGet(targetUri);
                } else {
                    request = new HttpDelete(targetUri);
                }
            }

            if (reqHeader != null && reqHeader.size() > 0) {
                rqHeaderKetSet = reqHeader.keySet();
                Iterator var20 = rqHeaderKetSet.iterator();

                while (var20.hasNext()) {
                    String key = (String) var20.next();
                    ((HttpRequestBase) request).addHeader(key, reqHeader.get(key).toString());
                }
            }

            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute((HttpUriRequest) request);
            result = this.responseHandler.handleResponse(response);
            return result;
        } catch (MalformedURLException var12) {
            logger.error("Malformed URL Exception", var12);
            throw new HttpConnectionException("Malformed URL Exception");
        } catch (URISyntaxException var13) {
            logger.error("Illegal URI Syntax", var13);
            throw new HttpConnectionException("Illegal URI Syntax");
        } catch (UnsupportedEncodingException var14) {
            logger.error("Unsupported Encoding for Request Parameter", var14);
            throw new HttpConnectionException("Unsupported Encoding for Request Parameter");
        } catch (ClientProtocolException var15) {
            logger.error(var15.getMessage(), var15);
            throw new HttpConnectionException("Client Procotol Exception");
        } catch (IOException var16) {
            logger.error(var16.getMessage(), var16);
            throw new HttpConnectionException("I/O Exception");
        }
    }

    private T doConnect(String method, String url, Map<String, Object> reqHeader, String reqBody) throws HttpConnectionException {
        T result = null;

        try {
            URI targetUri = (new URL(url)).toURI();
            HttpEntityEnclosingRequestBase request = null;
            switch (method.toUpperCase()) {
                case "POST":
                    request = new HttpPost(targetUri);
                    break;
                case "PUT":
                    request = new HttpPut(targetUri);
                    break;
                case "PATCH":
                    request = new HttpPatch(targetUri);
                    break;
                default:
                    throw new HttpConnectionException("NOT Supported method");
            }

            if (!Strings.isNullOrEmpty(reqBody)) {
                StringEntity stringBody = new StringEntity(reqBody, Charset.forName("UTF-8"));
                ((HttpEntityEnclosingRequestBase) request).setEntity(stringBody);
            }

            if (reqHeader != null && reqHeader.size() > 0) {
                Set<String> rqHeaderKetSet = reqHeader.keySet();
                Iterator var19 = rqHeaderKetSet.iterator();

                while (var19.hasNext()) {
                    String key = (String) var19.next();
                    ((HttpEntityEnclosingRequestBase) request).addHeader(key, reqHeader.get(key).toString());
                }
            }

            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute((HttpUriRequest) request);
            result = this.responseHandler.handleResponse(response);
            return result;
        } catch (MalformedURLException var11) {
            logger.error("Malformed URL Exception", var11);
            throw new HttpConnectionException("Malformed URL Exception");
        } catch (URISyntaxException var12) {
            logger.error("Illegal URI Syntax", var12);
            throw new HttpConnectionException("Illegal URI Syntax");
        } catch (UnsupportedEncodingException var13) {
            logger.error("Unsupported Encoding for Request Parameter", var13);
            throw new HttpConnectionException("Unsupported Encoding for Request Parameter");
        } catch (ClientProtocolException var14) {
            logger.error(var14.getMessage(), var14);
            throw new HttpConnectionException("Client Procotol Exception");
        } catch (IOException var15) {
            logger.error(var15.getMessage(), var15);
            throw new HttpConnectionException("I/O Exception");
        }
    }

    public class Method {
        static final String GET = "GET";
        static final String POST = "POST";
        static final String PUT = "PUT";
        static final String PATCH = "PATCH";
        static final String DELETE = "DELETE";

        public Method() {
        }
    }
}
