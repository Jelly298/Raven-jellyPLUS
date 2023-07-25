package keystrokesmod.client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class StatsUtils {

    public static String HypixelAPIKey = "cea3dc2a-6df3-44ff-99df-0e5fa19646dc";

    public static String getTextFromURL(String _url) {
        String r = "";
        HttpURLConnection con = null;

        try {
            URL url = new URL(_url);
            con = (HttpURLConnection)url.openConnection();
            r = getTextFromConnection(con);
        } catch (IOException exception) {
            r = exception.toString();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return r;
    }

    private static String getTextFromConnection(HttpURLConnection connection) {

        if (connection != null) {

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String result;
                try {
                    StringBuilder stringBuilder = new StringBuilder();

                    String input;
                    while((input = bufferedReader.readLine()) != null) {
                        stringBuilder.append(input);
                    }

                    String res = stringBuilder.toString();
                    connection.disconnect();

                    result = res;
                } finally {
                    bufferedReader.close();
                }

                return result;
            } catch (Exception ignored) {return ignored.toString().substring(57, 60); }//get server error code
        }

        return "Error";
    }

    public static String getStats(String UUID) {

        String URLstring = "https://api.hypixel.net/player?key=" + HypixelAPIKey + "&uuid=" + UUID;
        String stats;

        try {
            stats = getTextFromURL(URLstring);
            return stats;

        } catch (Exception ignored) {}

        return "Error";
    }


}
