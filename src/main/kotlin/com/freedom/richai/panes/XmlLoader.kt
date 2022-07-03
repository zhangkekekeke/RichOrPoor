package com.freedom.richai.panes

import javafx.fxml.FXMLLoader

 class XmlLoader {

    fun loadXml(xml: String): FXMLLoader {
        val loader = FXMLLoader()
        loader.location = XmlLoader::class.java.getResource(xml)
        return loader
    }
}
