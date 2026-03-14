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
	

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Mistix");
        ArrayList<Period> forecast = WeatherAPI.getForecast("LOT", 77, 70);

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

        Button headerBtn = new Button(headerText);
        headerBtn.setMaxWidth(Double.MAX_VALUE);

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

}
