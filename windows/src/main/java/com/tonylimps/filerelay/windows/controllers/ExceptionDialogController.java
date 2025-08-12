package com.tonylimps.filerelay.windows;

import com.tonylimps.filerelay.core.Core;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExceptionDialogController {
    @FXML
    private Label messageLabel;
    @FXML
    private TextArea exceptionArea;
    private String _message;
    private String _stackTrace;
    private static ExceptionDialogController instance;
    @FXML
    private void initialize() {
        instance = this;
        messageLabel.setText(_message);
        exceptionArea.setText(_stackTrace);
    }
    @FXML
    public void close(){
        System.exit(1);
    }
    @FXML
    public void saveErrorLog() throws IOException {
        String dateString = Core.getCurrentTime();
        File log = new File("err-"+dateString+".log");
        log.createNewFile();
        FileWriter fw = new FileWriter(log);
        fw.write(dateString + "\n"+_stackTrace);
        fw.close();
        messageLabel.setText("Saved.");
    }
    public static void setInfo(String message, String stackTrace) {
        instance._setInfo(message, stackTrace);
    }
    private void _setInfo(String message, String stackTrace) {
        _message = message;
        _stackTrace = stackTrace;
        messageLabel.setText(_message);
        exceptionArea.setText(_stackTrace);
    }
}
