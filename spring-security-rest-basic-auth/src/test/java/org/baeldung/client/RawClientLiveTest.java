package org.baeldung.client;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.Ignore;
import org.junit.Test;

public class RawClientLiveTest {

    // tests

    @Test
    public final void whenSecuredRestApiIsConsumed_then200OK() throws ClientProtocolException, IOException {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        final int timeout = 30; // seconds
        final RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).setSocketTimeout(timeout).build();
        final HttpGet getMethod = new HttpGet("http://localhost:8080/spring-security-rest-basic-auth/api/bars/1");
        getMethod.setConfig(requestConfig);

        final int hardTimeout = 10; // seconds
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (getMethod != null) {
                    getMethod.abort();
                }
            }
        };
        new Timer(true).schedule(task, hardTimeout * 1000);

        final HttpResponse response = httpClient.execute(getMethod);
        System.out.println("HTTP Status of response: " + response.getStatusLine().getStatusCode());
    }

    @Test(expected = SSLPeerUnverifiedException.class)
    @Ignore("Only for a server that has HTTPS enabled (on 8443)")
    public final void whenHttpsUrlIsConsumed_thenException() throws ClientProtocolException, IOException {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        final String urlOverHttps = "https://localhost:8443/spring-security-rest-basic-auth/api/bars/1";
        final HttpGet getMethod = new HttpGet(urlOverHttps);
        final HttpResponse response = httpClient.execute(getMethod);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    }

    @Test
    public final void givenHttpClientPre4_3_whenAcceptingAllCertificates_thenCanConsumeHttpsUriWithSelfSignedCertificate() throws IOException, GeneralSecurityException {
        final TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public final boolean isTrusted(final X509Certificate[] certificate, final String authType) {
                return true;
            }
        };
        final SSLSocketFactory sf = new SSLSocketFactory(acceptingTrustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("https", 8443, sf));
        final ClientConnectionManager ccm = new PoolingClientConnectionManager(registry);

        final CloseableHttpClient httpClient = new DefaultHttpClient(ccm);

        final String urlOverHttps = "https://localhost:8443/spring-security-rest-basic-auth/api/bars/1";
        final HttpGet getMethod = new HttpGet(urlOverHttps);
        final HttpResponse response = httpClient.execute(getMethod);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    }

    @Test
    public final void givenHttpClientPost4_3_whenAcceptingAllCertificates_thenCanConsumeHttpsUriWithSelfSignedCertificate() throws IOException, GeneralSecurityException {
        final SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        final CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        // new

        final String urlOverHttps = "https://localhost:8443/spring-security-rest-basic-auth/api/bars/1";
        final HttpGet getMethod = new HttpGet(urlOverHttps);
        final HttpResponse response = httpClient.execute(getMethod);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    }

}
