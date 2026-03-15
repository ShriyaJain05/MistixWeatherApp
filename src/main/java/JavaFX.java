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
		Label welcome = new Label("Welcome to Mistix!");
		welcome.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 20 0 10 0;");
		
        HBox h1 = new HBox(20);
        h1.setStyle("-fx-padding: 20; -fx-alignment: center;");

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
        VBox mainLayout = new VBox(10); 
        mainLayout.setStyle("-fx-alignment: top-center; -fx-background-color: #f4f4f4;");
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
        container.setStyle("-fx-border-color: black; -fx-padding: 15; -fx-border-width: 2; -fx-pref-width: 220;");
        
        //scene1 setup
        
        Button headerBtn = new Button(headerText);
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
        VBox daySection = createSectionBox(dayLabel, dayData.temperature + "° " + dayData.shortForecast);
        VBox nightSection = createSectionBox(nightLabel, nightData.temperature + "° " + nightData.shortForecast);
        VBox windSection = createSectionBox(windLabel, dayData.windSpeed + " " + dayData.windDirection);
        

        container.getChildren().addAll(headerBtn, daySection, nightSection, windSection);
        
  
        return container;
    }
    
    private VBox createSectionBox(Label title, String dataText) {

        Label dataLabel = new Label(dataText);
        dataLabel.setWrapText(true);

        VBox section = new VBox(5, title, dataLabel);
        
        // Applying the dashed/dotted border style shown in your wireframe
        section.setStyle("-fx-border-color: black; " +
                         "-fx-border-style: dashed; " + 
                         "-fx-border-width: 1; " +
                         "-fx-padding: 10; " +
                         "-fx-background-color: #ffffff;"); 
        section.setPrefHeight(125);
        section.setMinWidth(200);
        
        return section;
    }
    
    
   
    private void setupScene2(Period selectedDay) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 30; -fx-alignment: center; -fx-background-color: #f0f8ff;");

        Label dayLabel = new Label(selectedDay.name); 
        Label locationLabel = new Label("Chicago");
        Label bigTemp = new Label(selectedDay.temperature + "°");
        bigTemp.setStyle("-fx-font-size: 60px; -fx-font-weight: bold;");

        // Above and Beyond: Next 6 Hours [cite: 40]
        Label hourlyHeader = new Label("Next 6 hours");
        HBox hourlyHBox = new HBox(10);
        hourlyHBox.setStyle("-fx-alignment: center;");
        
        for (int i = 0; i < 6 && i < hourlyForecast.size(); i++) {
            hourlyHBox.getChildren().add(createHourlyBox(hourlyForecast.get(i)));
        }

        // Details Row
        HBox detailsRow = new HBox(30);
        detailsRow.setStyle("-fx-alignment: center;");

        // Above and Beyond: Precipitation 
        VBox precipBox = new VBox(5);
        precipBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-border-width: 1;");
        Label pVal = new Label("Precipitation: " + (selectedDay.probabilityOfPrecipitation != null ? selectedDay.probabilityOfPrecipitation.value : 0) + "%");
        precipBox.getChildren().addAll(new Label("Precipitation ☂"), pVal);

        VBox windBox = new VBox(5);
        windBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-border-width: 1;");
        windBox.getChildren().addAll(new Label("Wind 🌬"), new Label("Speed: " + selectedDay.windSpeed), new Label("Direction " + selectedDay.windDirection));

        detailsRow.getChildren().addAll(precipBox, windBox);

        Button backBtn = new Button("Go Back");
        backBtn.setOnAction(e -> window.setScene(scene1)); // Requirement: Swap back 

        layout.getChildren().addAll(dayLabel, locationLabel, bigTemp, new Label(selectedDay.shortForecast), 
                                    hourlyHeader, hourlyHBox, detailsRow, backBtn);
        scene2 = new Scene(layout, 850, 700);
    }
    
    private VBox createHourlyBox(Period hourData) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #7f8c8d; -fx-padding: 10; -fx-alignment: center; -fx-background-color: white;");
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH);
        String timeStr = hourData.startTime.toInstant().atZone(ZoneId.systemDefault()).format(dtf);
        
        box.getChildren().addAll(new Label(timeStr), new Label(hourData.temperature + "°"));
        return box;
    }

    	

}
