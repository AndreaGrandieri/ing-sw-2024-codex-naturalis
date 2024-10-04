package it.polimi.ingsw.gui.support;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.function.UnaryOperator;


public class FXUtils {
    public static void loadRootFXMLView(Object view) {
        Class<?> viewClass = view.getClass();
        loadRootFXMLView(viewClass, view);
    }

    public static void loadRootFXMLView(Class<?> viewClass, Object view) {
        String resourceName = "%s.fxml".formatted(classKebabName(viewClass));

        FXMLLoader fxmlLoader = new FXMLLoader(viewClass.getResource(resourceName));
        fxmlLoader.setRoot(view);
        fxmlLoader.setControllerFactory(type -> view);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Parent loadRootFXMLViewRoot(Object view) {
        Class<?> viewClass = view.getClass();
        String resourceName = "%s.fxml".formatted(classKebabName(viewClass));

        FXMLLoader fxmlLoader = new FXMLLoader(viewClass.getResource(resourceName));
        fxmlLoader.setRoot(view);
        fxmlLoader.setControllerFactory(type -> view);

        try {
            return fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static String classKebabName(Class<?> viewClass) {
        return viewClass.getSimpleName().replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }

    public static TextFormatter<Object> textFormatterFromRegEx(String regex) {
        return new TextFormatter<>(regexFilter(regex));
    }

    public static UnaryOperator<TextFormatter.Change> regexFilter(String regex) {
        return change -> {
            String newText = change.getControlNewText();
            if (newText.matches(regex)) {
                return change;
            }
            return null;
        };
    }

    public static <T extends Node> T addStyle(T node, String... styleClasses) {
        node.getStyleClass().addAll(styleClasses);
        return node;
    }


    public static Background bgColor(Color c) {
        return new Background(new BackgroundFill(c, null, null));
    }


}
