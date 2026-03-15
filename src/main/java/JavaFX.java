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
	Scene scene1, scene2;
	//two distinct lists of weather data
    ArrayList<Period> dailyForecast;
    ArrayList<Period> hourlyForecast;
	

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
    public void start(Stage primaryStage) throws Exception {
		window = primaryStage;
        window.setTitle("Mistix");
        
        dailyForecast = WeatherAPI.getForecast("LOT", 77, 70);
        hourlyForecast = WeatherAPI.getHourlyForecast("LOT", 77, 70);
        
        if (dailyForecast == null) throw new RuntimeException("Forecast did not load");


      //initialize scene 1
        setupScene1();
        window.setScene(scene1);
        window.show();
	}

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
//    	LocalDate forecastDate = dayData.startTime.toInstant()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDate();
//                
//        // Get local time in Chicago
//        LocalDate today = LocalDate.now(ZoneId.of("America/Chicago"));
//        
    	String headerText;
        
    	if (isFirst) {
            headerText = "Today";
        } else {
            // Use the Day Name from the API for future days
            headerText = dayData.startTime.toInstant()
                    .atZone(ZoneId.of("America/Chicago"))
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }
    	
        VBox container = new VBox(15);
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15),10,0,0,4);" +
                        "-fx-pref-width: 220;"
        );

        //scene1 setup

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
        
        //action
        headerBtn.setOnAction(e-> {
        	setupScene2(dayData); 
        	window.setScene(scene2);
        });
        
        Label dayLabel = new Label("Day");
        dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label nightLabel = new Label("Night");
        nightLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label windLabel = new Label("Wind");
        windLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        //Day Section
     // Helper to create the styled "tf" boxes from your wireframe
        VBox daySection = createSectionBox(dayLabel, dayData.temperature, dayData.shortForecast);
        VBox nightSection = createSectionBox(nightLabel, nightData.temperature, nightData.shortForecast);
        VBox windSection = createWindSection(windLabel, dayData.windSpeed + " " + dayData.windDirection);

        container.getChildren().addAll(headerBtn, daySection, nightSection, windSection);
        
  
        return container;
    }

    private VBox createWindSection(Label title, String windText) {

        Label windLabel = new Label(windText);
        windLabel.setStyle("-fx-font-size:16px;");

        ImageView icon = getWeatherIcon("cloud");

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

    private VBox createSectionBox(Label title, int temp, String description) {

        Label tempLabel = new Label(temp + "°");
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

        section.setAlignment(javafx.geometry.Pos.CENTER);
        section.setFillWidth(true);
        section.setPrefHeight(200);
        section.setMinWidth(250);

        return section;
    }
    
    
   
    private void setupScene2(Period selectedDay) {
        // 1. The main outer layout with the blue gradient
        VBox mainOuter = new VBox(20);
        mainOuter.setStyle("-fx-background-color: linear-gradient(to bottom, #4facfe, #00f2fe); " +
                           "-fx-alignment: center; -fx-padding: 40;");

        // 2. The white "Card" that holds all the info
        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 4); " +
                      "-fx-max-width: 600; -fx-alignment: center;");

        Label dayLabel = new Label(selectedDay.name);
        dayLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label locationLabel = new Label("Chicago");
        locationLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        Label bigTemp = new Label(selectedDay.temperature + "°");
        bigTemp.setStyle("-fx-font-size: 80px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label forecastDesc = new Label(selectedDay.shortForecast);
        forecastDesc.setStyle("-fx-font-size: 18px; -fx-text-fill: #34495e;");

        // Next 6 Hours Section
        Label hourlyHeader = new Label("Next 6 Hours");
        hourlyHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        HBox hourlyHBox = new HBox(12);
        hourlyHBox.setStyle("-fx-alignment: center;");
        for (int i = 0; i < 6 && i < hourlyForecast.size(); i++) {
            hourlyHBox.getChildren().add(createHourlyBox(hourlyForecast.get(i)));
        }

        // Details Row (Precip and Wind)
        HBox detailsRow = new HBox(20);
        detailsRow.setStyle("-fx-alignment: center;");

        // Styled Precipitation Box
        VBox precipBox = createDetailBox("Precipitation ☂", 
            (selectedDay.probabilityOfPrecipitation != null ? selectedDay.probabilityOfPrecipitation.value : 0) + "%");

        // Styled Wind Box
        VBox windBox = createDetailBox("Wind 🌬", selectedDay.windSpeed + " " + selectedDay.windDirection);

        detailsRow.getChildren().addAll(precipBox, windBox);

        // Back Button - Styled like Scene 1 buttons
        Button backBtn = new Button("← Go Back");
        backBtn.setStyle("-fx-background-color: #4facfe; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        backBtn.setOnAction(e -> window.setScene(scene1));

        card.getChildren().addAll(dayLabel, locationLabel, bigTemp, forecastDesc, hourlyHeader, hourlyHBox, detailsRow, backBtn);
        mainOuter.getChildren().add(card);

        scene2 = new Scene(mainOuter, 850, 750);
    }
    
    private VBox createHourlyBox(Period hourData) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH);
        String timeStr = hourData.startTime.toInstant().atZone(ZoneId.systemDefault()).format(dtf);

        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label tempLabel = new Label(hourData.temperature + "°");
        tempLabel.setStyle("-fx-font-size: 16px;");

        VBox box = new VBox(4, timeLabel, tempLabel);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-alignment: center; " +
                     "-fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
        box.setMinWidth(70);
        return box;
    }
    
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

    private ImageView getWeatherIcon(String forecast) {

        String f = forecast.toLowerCase();
        String path = "icons/cloud.png";

        if (f.contains("sun") || f.contains("clear")) {
            path = "icons/sunny.png";
        }
        else if (f.contains("rain") || f.contains("shower")) {
            path = "icons/rain.png";
        }
        else if (f.contains("snow")) {
            path = "icons/snow.png";
        }
        else if (f.contains("storm") || f.contains("thunder")) {
            path = "icons/storm.png";
        }

        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView icon = new ImageView(img);

        icon.setFitWidth(40);
        icon.setFitHeight(40);

        return icon;
    }

}
