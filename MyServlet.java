package ServletPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/weather")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	// taking the input from html
    	String city = request.getParameter("city");
        if (city == null || city.isEmpty()) {
            throw new ServletException("City parameter is missing or empty.");
        }

        String key = "2dad4553b968f93756d347f6908d01d4";
        // this line convert into url format ex- 'new delhi' then it convert into 'new%20dehli' using this(StandardCharsets.UTF_8.toString())i.e. UTF_8 encoding
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
     // api url
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + encodedCity + "&appid=" + key;
     // String -> url
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

//        This line checks if the response code is not equal to 200 (HttpURLConnection.HTTP_OK).
//        If the response code is anything other than 200, it means the request was not successful.
  /*      int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorMessage = "API request failed with response code: " + responseCode;
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    Scanner sc = new Scanner(errorStream);
                    StringBuilder sb = new StringBuilder();
                    while (sc.hasNext()) {
                        sb.append(sc.nextLine());
                    }
                    sc.close();
                    errorMessage += "\nError response: " + sb.toString();
                }
            }
            throw new ServletException(errorMessage);
        }  */
      //Reading data from network
        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
     // store in string
        StringBuilder sb = new StringBuilder();
     // scan every character from the data
        Scanner sc = new Scanner(reader);

        while (sc.hasNext()) {
            sb.append(sc.nextLine());
        }
        sc.close();
     // convert the sringBuilder to json format for separate picking data
        Gson gson = new Gson();
        // in jsonObject all data is getting
        JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
        if (jsonObject == null) {
            throw new ServletException("Failed to parse JSON response.");
        }
     // 1000 gives the real time and date 
        long DateTime = jsonObject.get("dt").getAsLong() * 1000L;
        String date = new Date(DateTime).toString();

        // In api temp is store in key value pair in main object
        double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
        int tempInCelsius = (int) (temperatureKelvin - 273.15);

        int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();

        double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
     // main is same name in api
        String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
        System.out.println(weatherCondition);
     // set the data as request attribute(set the data to send on web)
        request.setAttribute("date", date);
        request.setAttribute("temperature", tempInCelsius);
        request.setAttribute("city", city);
        request.setAttribute("humidity", humidity);
        request.setAttribute("windSpeed", windSpeed);
        request.setAttribute("weatherCondition", weatherCondition);
//		request.setAttribute("weatherData", sb.toString());
        connection.disconnect();
     // forward the request to index.jsp for rendering
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
