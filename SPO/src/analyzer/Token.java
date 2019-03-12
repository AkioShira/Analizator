package analyzer;

public class Token {
    private String value;
    private String type;
    private int number;
    private int table;

    Token(String value, String type){
        this.value = value;
        this.type = type;
        switch (type){
            case "OPERAND": table = 0; break;
            case "DIVIDER": table = 1; break;
            case "NUMBER": table = 2; break;
            case "IDENTIFICATOR": table = 3; break;
        }
    }

    public String getType(){
        return type;
    }

    public String getValue(){
        return value;
    }

    public void setNumber(int number){
        this.number = number;
    }

    @Override
    public String toString(){
        return "("+table+","+number+")";
    }
}
