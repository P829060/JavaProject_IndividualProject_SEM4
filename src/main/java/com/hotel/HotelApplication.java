package com.hotel;

import com.hotel.util.ViewLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotelApplication extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(ViewLoader.load("login-view.fxml"), 1000, 680);
        scene.getStylesheets().add(ViewLoader.resource("css/app.css").toExternalForm());
        stage.setTitle("Hotel Management Application");
        stage.setMinWidth(980);
        stage.setMinHeight(660);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        HotelContext.shutdown();
    }
}
