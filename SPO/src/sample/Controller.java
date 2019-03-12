package sample;

import analyzer.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;

public class Controller{
    private String dividerPair[] = {"!=", "==", "<=", ">=", "||", "&&", ":=", "(*", "*)", "++", "--", "{/", "/}"};
    private String dividerSingle[] = {",", "=", ">", "<", "+", "-", "*", "/", "!", "[", "]", "(", ")", "'", ";"};
    private String operands[] = { "dim", "begin", "end", "integer", "real", "boolean", "if", "else",
            "for", "to", "step", "next", "while", "readln", "writeln", "true", "false"};

    private Stage mainStage;

    @FXML
    private ListView<String> operandList;

    @FXML
    private ListView<String> identificatorList;

    @FXML
    private ListView<String> dividerList;

    @FXML
    private ListView<String> numberList;

    @FXML
    private TextArea codeArea;


    @FXML
    private ListView<String> resultList;

    @FXML
    private TextArea resultAnalis;

    public void initialize(){
        for (String operand : operands) operandList.getItems().add(operand);
        for (String aDividerSingle : dividerSingle) dividerList.getItems().add(aDividerSingle);
        for (String aDividerPair : dividerPair) dividerList.getItems().add(aDividerPair);
    }

    @FXML
    void analizerButton(ActionEvent event) throws LSException {
        resultList.getItems().clear();
        numberList.getItems().clear();
        identificatorList.getItems().clear();
        Analyzer analizer = new Analyzer();
        analizer.setOperations(operands);
        analizer.setDividerPair(dividerPair);
        analizer.setDividerSingle(dividerSingle);
        ArrayList<Token> tokens;
        try {
            tokens = analizer.analize(codeArea.getText());
            for(Token token : tokens) {
                if(token.getType().equals("NUMBER")) {
                    numberList.getItems().add(token.getValue());
                }
                resultList.getItems().add(token.toString());
            }
            ArrayList<String> identificators = analizer.getIdentificators();
            for(String s:identificators)
                identificatorList.getItems().add(s);
            resultAnalis.setText("Анализ успешно завершен");
        }catch(Exception e){
            resultAnalis.setText(e.getMessage());
        }
    }

    @FXML
    void closeStage(ActionEvent event) {
        mainStage.close();
    }

    @FXML
    void newButton(ActionEvent event) {
        codeArea.clear();
        resultList.getItems().clear();
        numberList.getItems().clear();
        identificatorList.getItems().clear();
        resultAnalis.setText("Ожидание анализа");
    }

    @FXML
    void infoButton(ActionEvent event) throws IOException {
        Stage info = new Stage();
        Pane root = FXMLLoader.load(getClass().getResource("info.fxml"));
        Scene scene = new Scene(root);
        info.setTitle("Информация");
        info.setScene(scene);
        info.initStyle(StageStyle.UTILITY);
        info.setResizable(false);
        info.show();
    }

    public void setStage(Stage stage) {
        mainStage = stage;
    }
}
