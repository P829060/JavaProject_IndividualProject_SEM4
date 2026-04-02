package com.hotel.util;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public final class ViewLoader {

    private ViewLoader() {
    }

    public static Parent load(String viewName) {
        try {
            return new FXMLLoader(resource(viewName)).load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load view: " + viewName, exception);
        }
    }

    public static URL resource(String path) {
        URL resource = ViewLoader.class.getResource("/com/hotel/" + path);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        return resource;
    }
}
