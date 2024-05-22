package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.WeatherForecastInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String city = bufferedReader.readLine();
            String informationType = bufferedReader.readLine();
            if (city == null || city.isEmpty() || informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }
            HashMap<String, WeatherForecastInformation> data = serverThread.getData();
            WeatherForecastInformation weatherForecastInformation = null;
            if (data.containsKey(city)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(city);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");

                String pageSourceCode = "";
//                if(false) {
//                    HttpPost httpPost = new HttpPost(Constants.WEB_SERVICE_ADDRESS);
//                    List<NameValuePair> params = new ArrayList<>();
//                    params.add(new BasicNameValuePair("q", city));
//                    //params.add(new BasicNameValuePair("mode", Constants.WEB_SERVICE_MODE));
//                    params.add(new BasicNameValuePair("APPID", Constants.WEB_SERVICE_API_KEY));
//                    params.add(new BasicNameValuePair("units", Constants.UNITS));
//                    UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
//                    httpPost.setEntity(urlEncodedFormEntity);
//                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
//
//                    pageSourceCode = httpClient.execute(httpPost, responseHandler);

                String query = Constants.WEB_SERVICE_ADDRESS +
                        "?q=" + city + "&APPID=" +
                        Constants.WEB_SERVICE_API_KEY +
                        "&units=" + Constants.UNITS;


                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url(query)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    pageSourceCode = response.body().string();
                }


                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else
                    Log.i(Constants.TAG, pageSourceCode);

                // Updated for openweather API
                JSONObject content = new JSONObject(pageSourceCode);

                JSONArray weatherArray = content.getJSONArray(Constants.WEATHER);
                JSONObject weather;
                String condition = "";
                for (int i = 0; i < weatherArray.length(); i++) {
                    weather = weatherArray.getJSONObject(i);
                    condition += weather.getString(Constants.MAIN) + " : " + weather.getString(Constants.DESCRIPTION);

                    if (i < weatherArray.length() - 1) {
                        condition += ";";
                    }
                }

                JSONObject main = content.getJSONObject(Constants.MAIN);
                String temperature = main.getString(Constants.TEMP);
                String pressure = main.getString(Constants.PRESSURE);
                String humidity = main.getString(Constants.HUMIDITY);

                JSONObject wind = content.getJSONObject(Constants.WIND);
                String windSpeed = wind.getString(Constants.SPEED);

                weatherForecastInformation = new WeatherForecastInformation(
                        temperature, windSpeed, condition, pressure, humidity
                );
                serverThread.setData(city, weatherForecastInformation);
            }

            if (weatherForecastInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }
            String result = null;
            switch (informationType) {
                case Constants.ALL:
                    result = weatherForecastInformation.toString();
                    break;
                case Constants.TEMPERATURE:
                    result = weatherForecastInformation.getTemperature();
                    break;
                case Constants.WIND_SPEED:
                    result = weatherForecastInformation.getWindSpeed();
                    break;
                case Constants.CONDITION:
                    result = weatherForecastInformation.getCondition();
                    break;
                case Constants.HUMIDITY:
                    result = weatherForecastInformation.getHumidity();
                    break;
                case Constants.PRESSURE:
                    result = weatherForecastInformation.getPressure();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
            }
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
