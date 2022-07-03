package com.freedom.richai.panes;

import com.img.load.panes.MainController;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;


import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainPane implements EventHandler<MouseEvent> {
    Pane root;
    MainController controller;

    public MainPane() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(XmlLoader.class.getResource("mainpane.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = loader.getController();
    }

    public Pane getRoot() {
        return root;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {

    }
}
