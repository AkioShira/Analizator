package analyzer;

import java.util.*;

class SyntaxAnalyzer {
    private boolean isDescription = false;
    private boolean ifOperator = false;
    private ArrayList<String> identificators = new ArrayList<>();
    private ArrayList<String> boolIdentificators = new ArrayList<>();
    private ArrayList<String> timeBoolIdentificators = new ArrayList<>();
    private ListIterator<Token> list;
    private int block = 0;
    private int skobe = 0;
    private int previous = 0;

    private enum Type {
        OPERAND, DIVIDER, NUMBER, IDENTIFICATOR
    }

    public ArrayList<String> getIdentificators(){
        return identificators;
    }

    private boolean isTypeData(Token token){
        String types[] = {"integer", "real", "boolean"};
        for (String type : types)
            if (token.getValue().equals(type))
                return true;
        return false;
    }

    private boolean isBoolean(Token token){
        String types[] = {"true", "false"};
        for (String type : types)
            if (token.getValue().equals(type))
                return true;
        return false;
    }

    private boolean isBoolIdentificator(Token token){
        for(String s:boolIdentificators)
            if(s.equals(token.getValue()))
                return true;
        return false;
    }

    private boolean isIncrement(Token token){
        String increments[] = {"++", "--"};
        for (String inc : increments)
            if (token.getValue().equals(inc))
                return true;
        return false;
    }

    private boolean isBoolOperation(Token token){
        String operations[] = {"||", "&&"};
        for(String s:operations)
            if(s.equals(token.getValue()))
                return true;
        return false;
    }

    private boolean isOperation(Token token){
        String operations[] = {"+", "-", "*", "/"};
        for(String s:operations)
            if(s.equals(token.getValue()))
                return true;
        return false;
    }

    private boolean isDeclared(Token token){
        for(String s:identificators)
            if(s.equals(token.getValue()))
                return true;
        return false;
    }

    private boolean isConditionOperation(Token token){
        String operations[] = {"==", "<", "<=", ">", ">="};
        for(String s:operations)
            if(s.equals(token.getValue()))
                return true;
        return false;
    }

    private void exitExpression() throws LSException {
        if(skobe>0)
            throw new LSException("Синаксическая ошибка: Не закрыты скобки в выражении");
        else if(skobe<0) {
            list.previous();
        }
        else {
            list.previous();
            skobe = 0;
        }
    }

    /**
     * Синтаксическая проверка блока описания программы
     *  dim <идентификатор> {, <идентификатор> } <тип>
     */
    private void descriptionData() throws LSException {
        if(!isDescription)
            throw new LSException("Синаксическая ошибка: Описание переменных должно быть перед телом программы");
        Token token = list.next();
        if(token.getType().equals(Type.IDENTIFICATOR.toString())) {
            if(isDeclared(token))
                throw new LSException("Семантическая ошибка: Переменная уже была объявлена");
            identificators.add(token.getValue());
            timeBoolIdentificators.add(token.getValue());
        }else
            throw new LSException("Синаксическая ошибка: Не обнаружен индентификатор");
        token = list.next();
        if(token.getValue().equals(","))
            descriptionData();
        else if(token.getValue().equals("boolean"))
            boolIdentificators.addAll(timeBoolIdentificators);
        else if(!isTypeData(token))
            throw new LSException("Синаксическая ошибка: Не верный тип переменной " + token.getValue());
        timeBoolIdentificators.clear();
        if (list.next().getValue().equals("dim"))
            descriptionData();
        else list.previous();
    }

    /**
     * Синтаксис присваивания <идентификатор> := <выражение>
     */
    private void assignment(boolean singleCall) throws LSException {
        Token saveToken;
        Token token = list.next();
        if(isDeclared(token)) {
            saveToken = token;
            token = list.next();
        }else
            throw new LSException("Семантическая ошибка: Переменная "+token.getValue()+" не была объявлена");
        if(!token.getValue().equals(":="))
            throw new LSException("Синаксическая ошибка: За идентификатором должно следовать объявление");
        if(isBoolIdentificator(saveToken))
            boolExpression();
        else
            expression();
        if(!singleCall)
            mainOperator();
    }

    private void conditionExpression() throws LSException {
        Token token = list.next();
        previous++;
        if(isBoolean(token)||isBoolIdentificator(token)||token.getValue().equals("!")) {
            backPrevious();
            boolExpression();
        }else if(token.getValue().equals("("))
            conditionExpression();
        else if(token.getType().equals(Type.IDENTIFICATOR.toString())) {
            if (!isDeclared(token))
                throw new LSException("Семантическая ошибка: Переменная " + token.getValue() + " не была объявлена");
            backPrevious();
            conditionArifmeticExpression();
        }else if(token.getType().equals(Type.NUMBER.toString())) {
            backPrevious();
            conditionArifmeticExpression();
        }else
            throw new LSException("Синтаксическая ошибка: Неопознанное выражение в условии");
    }

    private void backPrevious(){
        for(;previous>0;){
            list.previous();
            previous--;
        }
    }

    private void conditionArifmeticExpression() throws LSException {
        expression();
        if(!isConditionOperation(list.next()))
            throw new LSException("Синтаксическая ошибка: Не найден оператор условия");
        expression();
    }

    /**
     * Синтаксис логическое <выражение>
     */
    private void boolExpression() throws LSException {
        Token token = list.next();
        while(token.getValue().equals("!"))
            token = list.next();
        while(token.getValue().equals("(")) {
            skobe++;
            token = list.next();
        }
        while(token.getValue().equals("!"))
            token = list.next();
        if(isBoolean(token))
            token = list.next();
        else if(isDeclared(token)&&isBoolIdentificator(token))
            token = list.next();
        else
            throw new LSException("Семантическая ошибка: Ожидалось логическое выражение");
        if(token.getValue().equals(")")&&skobe>0) {
            skobe--;
            token = list.next();
        }
        if(isBoolOperation(token)){
            boolExpression();
        }else {
            exitExpression();
        }
    }

    /**
     * Синтаксис арифметическое <выражение>
     */
    private void expression() throws LSException {
        boolean increment = false;
        boolean number = false;
        Token token = list.next();
        //Может быть скобка или инкремент
        while(token.getValue().equals("(")) {
            skobe++;
            token = list.next();
        }
        if(isIncrement(token)) {
            increment = true;
            token = list.next();
        }
        if(token.getType().equals(Type.IDENTIFICATOR.toString())) {
            if(!isDeclared(token))
                throw new LSException("Семантическая ошибка: Переменная "+token.getValue()+" не была объявлена");
            if(isBoolIdentificator(token))
                throw new LSException("Семантическая ошибка: Переменная "+token.getValue()+" не применима к арифметическим выражениям");
            token = list.next();
        }else if(token.getType().equals(Type.NUMBER.toString())){
            if(increment)
                throw new LSException("Семантическая ошибка: Инкремент применим только к идентификаторам");
            number = true;
            token = list.next();
        }else {
            list.previous();
            throw new LSException("Синаксическая ошибка: После " + list.previous().getValue() + " ожидалась переменная либо выражение");
        }
        if(isIncrement(token)) {
            if(number)
                throw new LSException("Семантическая ошибка: Инкремент применим только к идентификаторам");
            else
                token = list.next();
        }
        while(token.getValue().equals(")")&&skobe>0) {
            skobe--;
            token = list.next();
        }
        if(isOperation(token))
            expression();
        else {
            exitExpression();
        }
    }

    /**
     * readln идентификатор {, <идентификатор> }
     */
    private void readOperand() throws LSException {
        Token token = list.next();
        if(!token.getType().equals(Type.IDENTIFICATOR.toString()))
            throw new LSException("Синтаксическая ошибка: Не обнаружен индетификатор");
        if(!isDeclared(token))
            throw new LSException("Семантическая ошибка: Переменная "+token.getValue()+" не была объявлена");
        if(list.next().getValue().equals(","))
            readOperand();
        else{
            list.previous();
            mainOperator();
        }
    }

    private void writeOperand() throws LSException {
        Token token = list.next();
        if(token.getValue().equals("'")) {
            list.next();
        }else {
            try{
                list.previous();
                boolExpression();
            }catch (LSException e){
                list.previous();
                expression();
            }
        }
        token = list.next();
        if(token.getValue().equals(","))
            writeOperand();
        else{
            list.previous();
            mainOperator();
        }
    }

    private void ifOperand() throws LSException {
        if(!list.next().getValue().equals("("))
            throw new LSException("Синтаксическая ошибка: Пропущена скобка перед выражением в операторе if");
        conditionExpression();
        if(!list.next().getValue().equals(")"))
            throw new LSException("Синтаксическая ошибка: Пропущена скобка после выражением в операторе if");

        if(!list.next().getValue().equals("begin"))
            throw new LSException("Синтаксическая ошибка: Ожидался оператор begin в операторе if");
        ifOperator = true;
        mainOperator();
        if(!list.next().getValue().equals("end"))
            throw new LSException("Синтаксическая ошибка: Не закрыт оператор условия в операторе if");
        if(list.next().getValue().equals("else")){
            if(list.next().getValue().equals("if"))
                ifOperand();
            else {
                mainOperator();
                if (!list.next().getValue().equals("end"))
                    throw new LSException("Синтаксическая ошибка: Не закрыт оператор условия в операторе if");
            }
        }else
            list.previous();
        ifOperator = false;
        mainOperator();
    }

    private void whileOperand() throws LSException {
        if(!list.next().getValue().equals("("))
            throw new LSException("Синтаксическая ошибка: Пропущена скобка перед выражением в цикле while");
        conditionExpression();
        if(!list.next().getValue().equals(")"))
            throw new LSException("Синтаксическая ошибка: Пропущена скобка после выражением в цикле while");
        if(!list.next().getValue().equals("begin"))
            throw new LSException("Синтаксическая ошибка: Ожидался оператор begin в цикле while");
        block++;
        mainOperator();
    }

    private void forOperand() throws LSException {
        Token token = list.next();
        if(!token.getType().equals(Type.IDENTIFICATOR.toString()))
            throw new LSException("Синтаксическая ошибка: Ожидалось присваивание в цикле for");
        if(isBoolIdentificator(token))
            throw new LSException("Синтаксическая ошибка: Boolean переменные нельзя использовать в цикле for");
        list.previous();
        assignment(true);
        if(!list.next().getValue().equals("to"))
            throw new LSException("Синтаксическая ошибка: Ожидалась to в цикле for");
        conditionExpression();
        if(!list.next().getValue().equals("["))
            throw new LSException("Синтаксическая ошибка: Ожидалась [ в цикле for");
        if(!list.next().getValue().equals("step"))
            throw new LSException("Синтаксическая ошибка: Ожидалась step в цикле for");
        expression();
        if(!list.next().getValue().equals("]"))
            throw new LSException("Синтаксическая ошибка: Ожидалась ] в цикле for");
        if(!list.next().getValue().equals("begin"))
            throw new LSException("Синтаксическая ошибка: Ожидался оператор begin в цикле for");
        block++;
        mainOperator();
    }

    /**
     * Оператор begin <оператор> end
     */
    private void mainOperator() throws LSException {
        Token token = list.next();
        switch (token.getValue()) {
            case "readln":
                readOperand();
                break;
            case "writeln":
                writeOperand();
                break;
            case "for":
                forOperand();
                break;
            case "while":
                whileOperand();
                break;
            case "if":
                ifOperand();
                break;
            case "begin":
                block++;
                mainOperator();
                break;
            case "(*":
                list.next();
                mainOperator();
                break;
            case "end":
                if(ifOperator){
                    list.previous();
                    return;
                }
                if(block>0) {
                    block--;
                    mainOperator();
                }else{
                    list.previous();
                    return;
                }
                break;
            default:
                if(token.getType().equals(Type.IDENTIFICATOR.toString())) {
                    list.previous();
                    assignment(false);
                }else
                    throw new LSException("Синаксическая ошибка: "+token.getValue()+" не является началом оператора");
                break;
        }
    }

    /**
     * Синтаксический анализ программы
     */
    public ArrayList<Token> analize(ArrayList<Token> tokens) throws LSException {
        list = tokens.listIterator();
        try {
            if (!list.next().getValue().equals("{/"))
                throw new LSException("Синаксическая ошибка: Не обнаружено начало программы");
            isDescription = true;

            if(list.next().getValue().equals("dim"))
                descriptionData();
            else list.previous();
            isDescription = false;
            if(list.next().getValue().equals("begin"))
                mainOperator();
            else
                throw new LSException("Синаксическая ошибка: Не найдено тела программы");

            if(!list.next().getValue().equals("end"))
                throw new LSException("Синаксическая ошибка: Не найден конец тела программы");
            if (!list.next().getValue().equals("/}"))
                throw new LSException("Синаксическая ошибка: Не найден оператор завершения программы");
            if (!list.next().getValue().equals("end"))
                throw new LSException("Синаксическая ошибка: Не найден оператор завершения программы");
            if(list.hasNext())
                throw new LSException("Синаксическая ошибка: Найдены символы после конца программы");

        }catch (NoSuchElementException e){
            throw new LSException("Синаксическая ошибка: Не ожиданное завершение программы");
        }
        return tokens;
    }
}
