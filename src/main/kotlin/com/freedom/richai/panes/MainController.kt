package com.img.load.panes

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.FlowPane

class MainController {
    @FXML
    lateinit var et_earningTime: TextField
    @FXML
    lateinit var cb_keyWord: ListView<String>
    @FXML
    lateinit var text_totalPage: Label
    @FXML
    lateinit var text_filterFund: Label
    @FXML
    lateinit var btn_TotalPage: Button

    @FXML
    lateinit var container: AnchorPane
    @FXML
    lateinit var scrollPane: ScrollPane
    @FXML
    lateinit var book_container: FlowPane

}