package analyzer;

import java.util.ArrayList;

public class Analyzer {
    private LexicalAnalyzer LexAnalizer = new LexicalAnalyzer();
    private SyntaxAnalyzer SynAnalizer = new SyntaxAnalyzer();
    private ArrayList<String> identificators;
    private String dividerPair[];
    private String dividerSingle[];
    private String operands[];
    private int number, identificator = 0;

    private enum Type {
        OPERAND, DIVIDER, NUMBER, IDENTIFICATOR
    }

    public void setOperations(String[] operands){
        this.operands = operands;
    }

    public void setDividerPair(String[] dividerPair){
        this.dividerPair = dividerPair;
    }

    public void setDividerSingle(String[] dividerSingle){
        this.dividerSingle = dividerSingle;
    }

    private void setOperandN(Token token){
        for(int i=0; i<operands.length; i++){
            if(token.getValue().equals(operands[i])){
                token.setNumber(i);
            }
        }
    }

    private void setDividerN(Token token){
        for(int i=0; i<dividerSingle.length; i++){
            if(token.getValue().equals(dividerSingle[i])){
                token.setNumber(i);
            }
        }
        int j=0;
        for(int i=dividerSingle.length; i<dividerSingle.length+dividerPair.length; i++){
            if(token.getValue().equals(dividerPair[j])){
                token.setNumber(i);
            }
            j++;
        }
    }

    private void setIndetificatorN(Token token){
        for(String s:identificators)
            if(token.getValue().equals(s))
                token.setNumber(identificator++);
    }

    public ArrayList<Token> analize(String line) throws LSException {
        ArrayList<Token> tokens;
        LexAnalizer.setOperations(operands);
        LexAnalizer.setDividerPair(dividerPair);
        LexAnalizer.setDividerSingle(dividerSingle);
        tokens = LexAnalizer.analize(line);
        tokens = SynAnalizer.analize(tokens);
        identificators = SynAnalizer.getIdentificators();
        for(Token token: tokens){
            if(token.getType().equals(Type.OPERAND.toString()))
                setOperandN(token);
            if(token.getType().equals(Type.DIVIDER.toString()))
                setDividerN(token);
            if(token.getType().equals(Type.NUMBER.toString()))
                token.setNumber(number++);
            if(token.getType().equals(Type.IDENTIFICATOR.toString()))
                setIndetificatorN(token);
        }
        return tokens;
    }

    public ArrayList<String> getIdentificators(){
        return identificators;
    }
}
