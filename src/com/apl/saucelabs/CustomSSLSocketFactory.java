package com.apl.saucelabs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/** Reference from https://stackoverflow.com/questions/1037590/which-cipher-suites-to-enable-for-ssl-socket/23365536#23365536 **/

public class CustomSSLSocketFactory extends javax.net.ssl.SSLSocketFactory {

	public CustomSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        initSSLSocketFactoryEx(null, null, null);
    }

    public CustomSSLSocketFactory(KeyManager[] km, TrustManager[] tm, SecureRandom random) throws NoSuchAlgorithmException, KeyManagementException {
        initSSLSocketFactoryEx(km, tm, random);
    }

    public CustomSSLSocketFactory(SSLContext ctx) {
        initSSLSocketFactoryEx(ctx);
    }

    public String[] getDefaultCipherSuites() {
        return m_ciphers;
    }

    public String[] getSupportedCipherSuites() {
        return m_ciphers;
    }

    public String[] getDefaultProtocols() {
        return m_protocols;
    }

    public String[] getSupportedProtocols() {
        return m_protocols;
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        SSLSocketFactory factory = m_ctx.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(s, host, port, autoClose);

        ss.setEnabledProtocols(m_protocols);
        ss.setEnabledCipherSuites(m_ciphers);

        return ss;
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocketFactory factory = m_ctx.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(address, port, localAddress, localPort);

        ss.setEnabledProtocols(m_protocols);
        ss.setEnabledCipherSuites(m_ciphers);

        return ss;
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        SSLSocketFactory factory = m_ctx.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(host, port, localHost, localPort);

        ss.setEnabledProtocols(m_protocols);
        ss.setEnabledCipherSuites(m_ciphers);

        return ss;
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocketFactory factory = m_ctx.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(host, port);

        ss.setEnabledProtocols(m_protocols);
        ss.setEnabledCipherSuites(m_ciphers);

        return ss;
    }

    public Socket createSocket(String host, int port) throws IOException {
        SSLSocketFactory factory = m_ctx.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(host, port);

        ss.setEnabledProtocols(m_protocols);
        ss.setEnabledCipherSuites(m_ciphers);

        return ss;
    }

    private void initSSLSocketFactoryEx(KeyManager[] km, TrustManager[] tm, SecureRandom random)
            throws NoSuchAlgorithmException, KeyManagementException {
        m_ctx = SSLContext.getInstance("TLS");
        m_ctx.init(km, tm, random);

        m_protocols = GetProtocolList();
        m_ciphers = GetCipherList();
    }

    private void initSSLSocketFactoryEx(SSLContext ctx) {
        m_ctx = ctx;

        m_protocols = GetProtocolList();
        m_ciphers = GetCipherList();
    }

    protected String[] GetProtocolList() {
        String[] preferredProtocols = {"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};
        String[] availableProtocols;

        try {
            SSLSocketFactory factory = m_ctx.getSocketFactory();
            try (SSLSocket socket = (SSLSocket) factory.createSocket()) {
                availableProtocols = socket.getSupportedProtocols();
                Arrays.sort(availableProtocols);
            }
        } catch (Exception e) {
            return new String[] { "TLSv1" };
        }

        List<String> aa = new ArrayList<>();
        for (int i = 0; i < preferredProtocols.length; i++) {
            int idx = Arrays.binarySearch(availableProtocols, preferredProtocols[i]);
            if (idx >= 0)
                aa.add(preferredProtocols[i]);
        }

        return aa.toArray(new String[0]);
    }

    protected String[] GetCipherList() {
        String[] preferredCiphers = {

                "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305",
                "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305",
                "TLS_ECDHE_ECDSA_WITH_CHACHA20_SHA",
                "TLS_ECDHE_RSA_WITH_CHACHA20_SHA",

                "TLS_DHE_RSA_WITH_CHACHA20_POLY1305",
                "TLS_RSA_WITH_CHACHA20_POLY1305",
                "TLS_DHE_RSA_WITH_CHACHA20_SHA",
                "TLS_RSA_WITH_CHACHA20_SHA",

                // Done with bleeding edge, back to TLS v1.2 and below
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",

                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",

                // TLS v1.0 (with some SSLv3 interop)
                "TLS_DHE_RSA_WITH_AES_256_CBC_SHA384",
                "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",

                "TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                "SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA",
                "SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA",

                // RSA key transport sucks, but they are needed as a fallback.
                // For example, microsoft.com fails under all versions of TLS
                // if they are not included. If only TLS 1.0 is available at
                // the client, then google.com will fail too. TLS v1.3 is
                // trying to deprecate them, so it will be interesteng to see
                // what happens.
                "TLS_RSA_WITH_AES_256_CBC_SHA256",
                "TLS_RSA_WITH_AES_256_CBC_SHA",
                "TLS_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_RSA_WITH_AES_128_CBC_SHA"
        };

        String[] availableCiphers;

        try {
            SSLSocketFactory factory = m_ctx.getSocketFactory();
            availableCiphers = factory.getSupportedCipherSuites();
            Arrays.sort(availableCiphers);
        } catch (Exception e) {
            return new String[]{
                    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
                    "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
                    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_RSA_WITH_AES_256_CBC_SHA256",
                    "TLS_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_RSA_WITH_AES_128_CBC_SHA",
                    "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"
            };
        }

        List<String> aa = new ArrayList<>();
        for (int i = 0; i < preferredCiphers.length; i++) {
            int idx = Arrays.binarySearch(availableCiphers, preferredCiphers[i]);
            if (idx >= 0)
                aa.add(preferredCiphers[i]);
        }

        aa.add("TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

        return aa.toArray(new String[0]);
    }

    private SSLContext m_ctx;

    private String[] m_ciphers;
    private String[] m_protocols;

}
