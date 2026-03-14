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
import java.time.format.TextStyle;
import java.util.Locale;

import java.util.ArrayList;

public class JavaFX extends Application {
	TextField temperature,weather;
	Stage window;
	Scene scene1, scene2;
	ArrayList<Period> forecast;
	
	

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
    public void start(Stage primaryStage) throws Exception {
		window = primaryStage;
        window.setTitle("Mistix");
        forecast = WeatherAPI.getForecast("LOT", 77, 70);
        
        //initialize scene 1
        setupScene1();
        window.setScene(scene1);
        window.show();

        if (forecast == null) throw new RuntimeException("Forecast did not load");

        HBox h1 = new HBox(20); 
        h1.setStyle("-fx-padding: 20; -fx-alignment: center;");

        int columnsAdded = 0;
        for (int i = 0; i < forecast.size() - 1 && columnsAdded < 3; i++) {
        	Period dayP = forecast.get(i);
            Period nightP = forecast.get(i + 1);
            
            // Only create a column if it's daytime (prevents showing "Tonight" as a main column)
            if (dayP.isDaytime && !nightP.isDaytime) {
                // Pass true for the very first column added so it says "Today"
                VBox dayColumn = createWeatherColumn(dayP, nightP, columnsAdded == 0);
                h1.getChildren().add(dayColumn);
                columnsAdded++;
                i++;
            }
        }

        Scene scene = new Scene(h1, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createWeatherColumn(Period dayData, Period nightData, boolean isFirst) {
        String headerText;
        
        if (isFirst) {
            headerText = "Today";
        } else {
            // Get actual day name
            headerText = dayData.startTime.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
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

        //Day Section
        VBox daySection = new VBox(5, 
            new Label("Day"), 
            new Label(dayData.temperature + "° " + dayData.shortForecast)
        );

        //Night Section
        VBox nightSection = new VBox(5, 
            new Label("Night"), 
            new Label(nightData.temperature + "° " + nightData.shortForecast)
        );

        //Wind Section
        VBox windSection = new VBox(5, 
            new Label("Wind"), 
            new Label(dayData.windSpeed + " " + dayData.windDirection)
        );

        container.getChildren().addAll(headerBtn, daySection, nightSection, windSection);
        
  
        return container;
    }
    
    private void setupScene2(Period selectedDay) {
    	VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 30; -fx-alignment: center;");

        // Name of Day and Location from Wireframe 
        Label dayLabel = new Label(selectedDay.name); 
        Label locationLabel = new Label("Chicago");
        Label bigTemp = new Label(selectedDay.temperature + "°");
        bigTemp.setStyle("-fx-font-size: 50px;"); // Make it big like the sketch

        // Back Button to return to the 3-day forecast 
        Button backBtn = new Button("Go Back");
        backBtn.setOnAction(e -> window.setScene(scene1));

        // For the "Next 6 Hours" requirement, you'd pull from a different API call 
        // or loop through hourly data if available. For now, placeholders:
        HBox hourlyForecast = new HBox(10);
        hourlyForecast.getChildren().add(new Label("Detailed: " + selectedDay.detailedForecast));

        layout.getChildren().addAll(dayLabel, locationLabel, bigTemp, hourlyForecast, backBtn);
        scene2 = new Scene(layout, 800, 600);
    }

    private void setupScene1() {
        HBox h1 = new HBox(20);
        h1.setStyle("-fx-padding: 20; -fx-alignment: center;");

        int columnsAdded = 0;
        for (int i = 0; i < forecast.size() - 1 && columnsAdded < 3; i++) {
            Period dayP = forecast.get(i);
            Period nightP = forecast.get(i + 1);
            if (dayP.isDaytime && !nightP.isDaytime) {
                h1.getChildren().add(createWeatherColumn(dayP, nightP, columnsAdded == 0));
                columnsAdded++;
                i++;
            }
        }
        scene1 = new Scene(h1, 800, 600);
    }
    	

}
