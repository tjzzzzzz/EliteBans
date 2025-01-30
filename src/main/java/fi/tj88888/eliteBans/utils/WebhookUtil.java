package fi.tj88888.eliteBans.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;


public class WebhookUtil {
    private static void sendWebhook(String webhookUrl, String content) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject embed = new JSONObject()
                    .put("title", content.split("\n")[0])
                    .put("description", content)
                    .put("color", 0x9b59b6);

            JSONObject json = new JSONObject()
                    .put("embeds", new JSONObject[]{embed});

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }

            connection.getInputStream().close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logCommand(String command, String executor, String target, String reason, String webhookUrl) {
        String content = String.format("**Command:** %s\n**Executor:** %s\n**Target:** %s\n**Reason:** %s",
                command, executor, target, reason);
        sendWebhook(webhookUrl, content);
    }

}