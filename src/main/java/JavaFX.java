import javafx.application.Application;

import javafx.scene.Scene;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import weather.Period;
import weather.WeatherAPI;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.ZoneId;
import java.util.ArrayList;

public class JavaFX extends Application {
	TextField temperature,weather;
	Stage window;
	Scene scene1, scene2; // two different scenes
    boolean wasFullScreen = false; //tracks fullscreen state when switching scenes
    private static final ZoneId CHICAGO_ZONE = ZoneId.of("America/Chicago");
    boolean isCelsius = false; //temperature unit toggle
	//two distinct lists of weather data
    ArrayList<Period> dailyForecast; // for the exact forecast temperature
    ArrayList<Period> hourlyForecast; // for the hourly 
	
    //main 
	public static void main(String[] args) {
		launch(args);
	}
	

	// Start method that initializes the scene 1
	@Override
    public void start(Stage primaryStage) throws Exception {
		window = primaryStage;
        window.setTitle("Mistix"); //weather app name
        
        //Using proxy to pull out the data from the NWS API
        dailyForecast = WeatherServiceProxy.getDaily("LOT", 77, 70);
        hourlyForecast = WeatherServiceProxy.getHourly("LOT", 77, 70);
        
        if (dailyForecast == null) throw new RuntimeException("Forecast did not load");


      //initialize scene 1
        setupScene1();
        window.setScene(scene1);
        window.show();
	}


	//Method that sets up scene 1 with a welcome screen to display the first scene of the 3 day forecast
    private void setupScene1() {
        Label welcome = new Label("Mistix Weather");
        welcome.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox h1 = new HBox(30);
        h1.setStyle("-fx-alignment: center;");

        int columnsAdded = 0;
        for (int i = 0; i < dailyForecast.size() - 1 && columnsAdded < 3; i++) {
            Period dayP = dailyForecast.get(i);
            Period nightP = dailyForecast.get(i + 1);

            if (dayP.isDaytime && !nightP.isDaytime) {
                h1.getChildren().add(createWeatherColumn(dayP, nightP, columnsAdded == 0));
                columnsAdded++;
                i++;
            }
        }

        VBox mainLayout = new VBox(30);
        mainLayout.setStyle(
                "-fx-alignment: top-center;" +
                        "-fx-padding: 40;" +
                        "-fx-background-color: linear-gradient(to bottom,#4facfe,#00f2fe);"
        );

        mainLayout.getChildren().addAll(welcome, h1);

        scene1 = new Scene(mainLayout, 850, 650);
    }
    
	
    private VBox createWeatherColumn(Period dayData, Period nightData, boolean isFirst) {
        //adapters for both Day and Night
        WeatherAdapter dayAdapter = new WeatherAdapter(dayData);
        WeatherAdapter nightAdapter = new WeatherAdapter(nightData);

        //setting up the header text to show which day it is.
        String headerText;
        if (isFirst) {
            headerText = "Today";
        } else {
            headerText = dayData.startTime.toInstant()
                    .atZone(ZoneId.of("America/Chicago"))
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }

        //setting up the main white column container
        VBox container = new VBox(15);
        container.setStyle(
                "-fx-background-color: white;" +
                "-fx-padding: 20;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15),10,0,0,4);" +
                "-fx-pref-width: 220;"
        );

        //creating and style the Day button 
        Button headerBtn = new Button(headerText);
        headerBtn.setStyle(
                "-fx-font-size:18px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-color:#4facfe;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:10;" +
                "-fx-padding:8 14 8 14;"
        );
        headerBtn.setMaxWidth(Double.MAX_VALUE);
        headerBtn.setOnAction(e -> {
            wasFullScreen = window.isFullScreen(); //remember state
            setupScene2(dayData);
            window.setScene(scene2);
            window.setFullScreen(wasFullScreen); //restore state
        });

        //making the labels for each container and the style
        Label dayLabel = new Label("Day");
        dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label nightLabel = new Label("Night");
        nightLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label windLabel = new Label("Wind");
        windLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        //using the Adapter and helper methods to create the smaller boxes inside the containers, and helps pass the data for Day and Night categories
        VBox daySection = createSectionBox(dayLabel, dayData.temperature, dayData.shortForecast);
        VBox nightSection = createSectionBox(nightLabel, nightData.temperature, nightData.shortForecast);
        
        //using the adapter for the wind to format the string the right way
        VBox windSection = createWindSection(windLabel, dayAdapter.getWind());

        //now put everything into the container to show all the data and formatting
        container.getChildren().addAll(headerBtn, daySection, nightSection, windSection);

        return container; //return the whole container
    }
    
    
    //creating the wind section box in the container with the wind icon and the correct formatting plus style
    private VBox createWindSection(Label title, String windText) {

        Label windLabel = new Label(windText);
        windLabel.setStyle("-fx-font-size:16px;"); //size

        ImageView icon = getWeatherIcon("wind"); //icon

        VBox section = new VBox(6, title, icon, windLabel);

        section.setStyle(
                "-fx-background-color:#f8f9fa;" +
                        "-fx-padding:12;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-radius:10;" +
                        "-fx-border-color:#e0e0e0;"
        );

        section.setAlignment(javafx.geometry.Pos.CENTER);
        section.setFillWidth(true);

        return section;
    }

    
    //creating the day and night boxes, takes in the data to choose the right icon, the right box size and the correct formatting/style
    private VBox createSectionBox(Label title, int temp, String description) {

        Label tempLabel = new Label(convertTemp(temp) + "°" + (isCelsius ? "C" : "F"));
        tempLabel.setStyle(
                "-fx-font-size:28px;" +
                        "-fx-font-weight:bold;"
        );

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);

        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setAlignment(javafx.geometry.Pos.CENTER);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        descLabel.setStyle("-fx-font-size:13px;");

        ImageView icon = getWeatherIcon(description);

        VBox section = new VBox(6, title, icon, tempLabel, descLabel);

        section.setStyle(
                "-fx-background-color:#f8f9fa;" +
                        "-fx-padding:12;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-radius:10;" +
                        "-fx-border-color:#e0e0e0;"
        );
        
        //size of the box
        section.setAlignment(javafx.geometry.Pos.CENTER);
        section.setFillWidth(true);
        section.setPrefHeight(200);
        section.setMinWidth(250);

        return section;
    }
    

    //convert temperature if needed
    private int convertTemp(int tempF) {
        if (!isCelsius) return tempF;
        return (int)Math.round((tempF - 32) * 5.0 / 9.0);
    }
    
    
    //setting up scene 2 where it shows the one day forecast with the day, temperature, weather description, 
    //a next 6 hour forecast, precipitation, wind and with a back button to navigate back to scene 1
    private void setupScene2(Period selectedDay) {
        //the background layer of the blue gradient
        VBox mainOuter = new VBox(20);
        mainOuter.setStyle("-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe); " +
                           "-fx-alignment: center; -fx-padding: 40;");

        //the white box that holds all the information
        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 4); " +
                      "-fx-max-width: 600; -fx-alignment: center;");
        
        //Labels for the day, name of the city, temperature, short description, next 6 hours, 
        Label dayLabel = new Label(selectedDay.name);
        dayLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label locationLabel = new Label("Chicago");
        locationLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        Label bigTemp = new Label(convertTemp(selectedDay.temperature) + "°" + (isCelsius ? "C" : "F"));
        bigTemp.setStyle("-fx-font-size: 80px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label forecastDesc = new Label(selectedDay.shortForecast);
        forecastDesc.setStyle("-fx-font-size: 18px; -fx-text-fill: #34495e;");

        //button to toggle temperature unit
        Button unitToggle = new Button(isCelsius ? "Switch to °F" : "Switch to °C");
        unitToggle.setStyle("-fx-background-color:#4facfe; -fx-text-fill:white; -fx-font-weight:bold; " +
                "-fx-padding:6 14; -fx-background-radius:10;");

        unitToggle.setOnAction(e -> {
            isCelsius = !isCelsius; //flip unit
            setupScene2(selectedDay); //refresh scene
            window.setScene(scene2);
        });

        Label hourlyHeader = new Label("Next 6 Hours");
        hourlyHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        
        HBox hourlyHBox = new HBox(12);
        hourlyHBox.setStyle("-fx-alignment: center;");

        //logic to determine which 6 hours to show
        int startIndex = 0;
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(CHICAGO_ZONE);

        //if selected day is today, start from current hour
        boolean isToday = selectedDay.isDaytime &&
                selectedDay.startTime.toInstant().atZone(CHICAGO_ZONE).toLocalDate()
                        .equals(now.toLocalDate());

        if (isToday) {
            for (int i = 0; i < hourlyForecast.size(); i++) {
                java.time.ZonedDateTime hourTime = hourlyForecast.get(i).startTime.toInstant().atZone(CHICAGO_ZONE);
                if (!hourTime.isBefore(now)) {
                    startIndex = i;
                    break;
                }
            }
        }

        //if future day, start at 8 AM
        else {
            for (int i = 0; i < hourlyForecast.size(); i++) {
                java.time.ZonedDateTime hourTime = hourlyForecast.get(i).startTime.toInstant().atZone(CHICAGO_ZONE);
                if (hourTime.getHour() >= 8) {
                    startIndex = i;
                    break;
                }
            }
        }

        //add next 6 hours
        for (int i = startIndex; i < startIndex + 6 && i < hourlyForecast.size(); i++) {
            hourlyHBox.getChildren().add(createHourlyBox(hourlyForecast.get(i)));
        }

        //the box to hold the percipitation and wind boxes
        HBox detailsRow = new HBox(20);
        detailsRow.setStyle("-fx-alignment: center;");

        //using adpater and creating the boxes for precipitation and the wind
        WeatherAdapter adapter = new WeatherAdapter(selectedDay);
        VBox precipBox = createDetailBox("Precipitation ☂", adapter.getPrecip());
        VBox windBox = createDetailBox("Wind 🌬", adapter.getWind());

        detailsRow.getChildren().addAll(precipBox, windBox);

        //creating the back button to go between scenes
        Button backBtn = new Button("← Go Back");
        backBtn.setStyle("-fx-background-color: #4facfe; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            window.setScene(scene1);
            window.setFullScreen(wasFullScreen); //restore state
        });

        //getting all the information and putting it into the box
        card.getChildren().addAll(dayLabel, locationLabel, bigTemp, forecastDesc, unitToggle,
                hourlyHeader, hourlyHBox, detailsRow, backBtn);
        mainOuter.getChildren().add(card);

        scene2 = new Scene(mainOuter, 850, 750); //size check
    }
 
    //creating the hourly boxes for the next 6 hours, 6 different sections that each have the 
    //data of the weather as time progresses with the time
    private VBox createHourlyBox(Period hourData) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH);
        String timeStr = hourData.startTime.toInstant().atZone(CHICAGO_ZONE).format(dtf);

        //labels of the times and temperature
        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label tempLabel = new Label(convertTemp(hourData.temperature) + "°" + (isCelsius ? "C" : "F"));
        tempLabel.setStyle("-fx-font-size: 16px;");

        VBox box = new VBox(4, timeLabel, tempLabel);
        //style of the box with the color and correct formatting
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-alignment: center; " +
                     "-fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
        box.setMinWidth(70);
        return box;
    }
    
    //styling the box with the title and temperature value
    private VBox createDetailBox(String title, String value) {
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14px;");
        
        VBox box = new VBox(5, t, v);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10; " +
                     "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-min-width: 180; -fx-alignment: center;");
        return box;
    }

    
    //conditions to change the icons based on the weather description
    private ImageView getWeatherIcon(String forecast) {

        String f = forecast.toLowerCase();
        String path = "icons/cloud.png";

        if (f.contains("sun") || f.contains("clear")) {
            path = "icons/sunny.png";
        }
        else if (f.contains("snow")) {
            path = "icons/snow.png";
        }
        else if (f.contains("rain") || f.contains("shower")) {
            path = "icons/rain.png";
        }
        else if (f.contains("storm") || f.contains("thunder")) {
            path = "icons/storm.png";
        }
        else if (f.contains("wind")) {
            path = "icons/wind.png";
        }

        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView icon = new ImageView(img);

        icon.setFitWidth(40);
        icon.setFitHeight(40);

        return icon;
    }
    
    
    //Adapter design pattern
    //defining the adapter class to translate the raw data into user readable data/strings
    class WeatherAdapter {
        private Period period; //the original object to hold the data from the NWS API
        public WeatherAdapter(Period period) { this.period = period; } //constructor to find the specific weather period
        //Formatting the weather summary to show the temperature data as "70° Rain" which is easy to read
        public String getWeatherSummary() {
            return period.temperature + "° " + period.shortForecast;
        }
        //Same logic to format the wind with the wind speed and the wind direction in the same line, user ready
        public String getWind() { return period.windSpeed + " " + period.windDirection; }
        //Same logic but first checks that if there is any precipitation, it returns the percentage with the "%" symbol but if not then "0%"
        public String getPrecip() {
            return (period.probabilityOfPrecipitation != null ? period.probabilityOfPrecipitation.value : 0) + "%";
        }
    }
    
    //Proxy design pattern
    //Defining the proxy class and static because it is nested
    static class WeatherServiceProxy {
    	//storage arrays
        private static ArrayList<Period> cachedDaily; //one specifically for the daily forecast
        private static ArrayList<Period> cachedHourly; //one specifically for the hourly forecast
        
        //Method used to request the daily weather from the NWS API. It takes in the region and the coordinates for Chicago
        public static ArrayList<Period> getDaily(String reg, int x, int y) {
        	
            //checks if we have already fetched the data needed and if the cache for the daily is null, 
        	//then this is the first time its being call
        	if (cachedDaily == null) {
                System.out.println("Proxy: Fetching fresh daily data..."); //printing to prove that the proxy is working
                cachedDaily = WeatherAPI.getForecast(reg, x, y);
            }
            return cachedDaily; //returning the data
        }
        
        
        //Same method logic used to request the hourly weather from the NWS API. It takes in the region and the coordinates for Chicago
        public static ArrayList<Period> getHourly(String reg, int x, int y) {
            
        	//same logic and checks if we have already fetched the data needed and if the cache for the daily is null, 
        	//then this is the first time its being call
        	if (cachedHourly == null) {
                System.out.println("Proxy: Fetching fresh hourly data..."); //printing to prove that the proxy is working
                cachedHourly = WeatherAPI.getHourlyForecast(reg, x, y);
            }
            return cachedHourly; //returning the data
        }
    }

}
