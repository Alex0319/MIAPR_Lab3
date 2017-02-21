package sample;

import by.miapr.algorithm.ClassificationDeterminant;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.UnaryOperator;

public class Controller {
    public Label lblTotalClassificationProbability;
    public Label lblProbabilityOfMissingDetection;
    public Label lblFalseAlarmProbability;
    public TextField txtfldFirstProbability;
    public TextField txtfldSecondProbability;
    public Button btnSearch;
    public Canvas cnvsGraphics;

    private void showAlert(Alert.AlertType type, String title, String headerText, String textError){
        Alert alert=new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(textError);
        alert.show();
    }

    private void setText(TextField textField,String string){
        textField.setText(string);
    }

    public void initialize(){
        UnaryOperator<TextFormatter.Change> filter=change -> {
            String text=change.getText();
            if(text.matches("[0-9]*")){
                return change;
            }
            return null;
        };
        txtfldFirstProbability.setTextFormatter(new TextFormatter<String>(filter));
        txtfldSecondProbability.setTextFormatter(new TextFormatter<String>(filter));
    }

    public void txtfldKeyReleased(KeyEvent keyEvent) {
        TextField txtField=(TextField) keyEvent.getSource();
        String str=txtField.getText(),strResult="";
        if(!str.isEmpty()){
            BigDecimal probability = new BigDecimal(1-Float.parseFloat("0.".concat(str))).setScale(str.length(), RoundingMode.HALF_UP);
            strResult=probability.toString().substring(2);
        }
        setText(txtField.equals(txtfldFirstProbability) ? txtfldSecondProbability : txtfldFirstProbability, strResult);
    }

    public void buttonSearchClick(MouseEvent mouseEvent) {
        if(!(txtfldFirstProbability.getText().isEmpty() || txtfldSecondProbability.getText().isEmpty())){
            ClassificationDeterminant classificationDeterminant=ClassificationDeterminant.getInstance();
            classificationDeterminant.setClassification((int)Math.round(cnvsGraphics.getWidth()),(int)Math.round(cnvsGraphics.getHeight()),
                  cnvsGraphics.getGraphicsContext2D(),Float.parseFloat("0.".concat(txtfldFirstProbability.getText())),Float.parseFloat("0.".concat(txtfldSecondProbability.getText())));
            lblFalseAlarmProbability.setText(Double.toString(classificationDeterminant.getErrorFalseAlarm()));
            lblProbabilityOfMissingDetection.setText(Double.toString(classificationDeterminant.getErrorMissingDetecting()));
            lblTotalClassificationProbability.setText(Double.toString(classificationDeterminant.getSumError()));
        }else{
            showAlert(Alert.AlertType.ERROR,"Ошибка","Некорректный ввод","Заполните все поля");
        }
    }
}
