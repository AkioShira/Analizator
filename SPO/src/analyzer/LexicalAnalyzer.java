package analyzer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LexicalAnalyzer {
    private String dividerPair[];
    private String dividerSingle[];
    private String operands[];

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

    private boolean tryParseInt(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean tryParseDouble(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOperand(String token) throws LSException {
        try {
            for (String operand : operands)
                if (token.equals(operand))
                    return true;
            return false;
        }catch (Exception e){
            throw new LSException(this.getClass().getName() + ": Не передан массив операндов");
        }
    }

    private boolean isIdentificator(String token){
        String sPattern = "^[A-Za-z][A-Za-z0-9_-]*";
        Pattern p = Pattern.compile(sPattern);
        Matcher m = p.matcher(token);
        return m.matches();
    }

    private boolean isDividerPair(String token) throws LSException {
        try {
            for (String aDividerPair : dividerPair)
                if (token.equals(aDividerPair))
                    return true;
            return false;
        }catch (Exception e){
            throw new LSException(this.getClass().getName() + ": Не передан массив двухсимвольных разделителей");
        }
    }

    private boolean isDividerSingle(String token) throws LSException {
        try {
            for (String aDividerSingle : dividerSingle)
                if (token.equals(aDividerSingle))
                    return true;
            return false;
        }catch (Exception e){
            throw new LSException(this.getClass().getName() + ": Не передан массив односимвольных разделителей");
        }
    }

    /**
     * Получить токен
     */
    private String getToken(String line, int index) throws LSException {
        int i = index;
        //Проверить на соответствие разделителям
        if(i<line.length()-1)
            if(isDividerPair(line.substring(i, i+2)))
                return line.substring(i, i+2);
        if(i<line.length())
            if(isDividerSingle(line.substring(i, i+1)))
                return line.substring(i, i+1);
        //Если цифра либо буква, то читать до разделителя либо пробела
        if(Character.isLetterOrDigit(line.charAt(i))){
            for(;i < line.length();) {
                if(i<line.length()-1)
                    if(isDividerPair(line.substring(i, i+2)))
                        return line.substring(index, i);
                if(i<line.length())
                    if(isDividerSingle(line.substring(i, i+1)))
                        return line.substring(index, i);
                if(Character.isWhitespace(line.charAt(i)))
                    return line.substring(index, i);
                else i++;
            }
        }
        //Иначе читать до пробела
        else{
            for(;i < line.length();) {
                if(Character.isWhitespace(line.charAt(i)))
                    return line.substring(index, i);
                else i++;
            }

        }
        return line.substring(index, i);
    }

    private void setTokenType(ArrayList<Token> tokens, String token) throws LSException {
        if(isOperand(token))
            tokens.add(new Token(token, Type.OPERAND.toString()));
        else if(tryParseInt(token)||tryParseDouble(token))
            tokens.add(new Token(token, Type.NUMBER.toString()));
        else if(isIdentificator(token))
            tokens.add(new Token(token, Type.IDENTIFICATOR.toString()));
        else if(isDividerPair(token)||isDividerSingle(token))
            tokens.add(new Token(token, Type.DIVIDER.toString()));
        else
            throw new LSException("Лексическая ошибка: Неизвестная лексема \"" + token+"\"");
    }

    private int ignoreChars(ArrayList<Token> tokens, String begin, String end, String line, int i, boolean isSave) throws LSException {
        int j=0;
        String token = getToken(line, i);
        if (token.equals(begin)) {
            if(isSave)
                setTokenType(tokens, token);
            i += token.length();
            j += token.length();
            for (; i < line.length(); ) {
                token = getToken(line, i);
                if (token.equals(end)) {
                    if(isSave)
                        setTokenType(tokens, token);
                    j += token.length();
                    return j;
                }else if(i+token.length() == line.length()){
                    j += token.length();
                    return j;
                }else {
                    i++;
                    j++;
                }
            }
        }
        return 0;
    }

    /**
     * Лексический анализ программы
     * @param line
     * @return
     */
    public ArrayList<Token> analize(String line) throws LSException {
        ArrayList<Token> tokens = new ArrayList<>();
        String s = line.replaceAll("[ \n]", "");
        if(s.isEmpty())
            throw new LSException("Лексическая ошибка: Программный код не обнаружен");
        for(int i=0; i<line.length();){
            //Убрать пробелы перед токеном
            if(Character.isWhitespace(line.charAt(i))) {
                i++;
                continue;
            }
            int ignore=0;
            //Игнорировать комментарии
            ignore+=ignoreChars(tokens, "(*", "*)", line, i, false);
            //Игнорировать строки
            ignore+=ignoreChars(tokens, "'", "'", line, i, true);
            if(ignore!=0) {
                i+= ignore;
                continue;
            }
            //Получить токен
            String token = getToken(line, i);
            //System.out.println("- " + token);
            //Определить тип для токена
            setTokenType(tokens, token);
            i+= token.length();
        }
        return tokens;
    }
}
