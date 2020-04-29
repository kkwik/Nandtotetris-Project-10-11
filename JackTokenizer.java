import java.io.*;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Hashtable;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;

public class JackTokenizer {
    Scanner scanFile;
    File writeFile;
    FileWriter writeToFile;
    Hashtable<String, String> symbolTable = new Hashtable<String, String>();
    String line;
    Stack<String> statementStack;
    boolean varDecBefore;
    boolean ifBefore;

    public JackTokenizer(File input)
    {
        try {
            scanFile = new Scanner(input);
            if(!input.getName().endsWith(".jack"))
            {
                System.out.println("Invalid file type: " + input.getName());
                System.exit(-1);
            }
            writeFile = new File(/*input.getAbsolutePath().split("\\.")[0] + "01" + ".xml"*/"C:\\Users\\Kevin\\Desktop\\CPP\\Year2\\Sem 2\\CS 3650\\Tetris\\nand2tetris\\tools\\Main01.xml");
            writeToFile = new FileWriter(writeFile);
        } catch(FileNotFoundException e)
        {
            System.out.println("File not found: " + input.getName());
            System.exit(-1);
        } catch(IOException e)
        {
            System.out.println("IOException");
            System.exit(-1);
        }

        statementStack = new Stack<String>();
        varDecBefore = false;
        ifBefore = false;

        symbolTable.put("class", "keyword");
        symbolTable.put("constructor", "keyword");
        symbolTable.put("method", "keyword");
        symbolTable.put("function", "keyword");
        symbolTable.put("field", "keyword");
        symbolTable.put("static", "keyword");
        symbolTable.put("var", "keyword");
        symbolTable.put("int", "keyword");
        symbolTable.put("char", "keyword");
        symbolTable.put("boolean", "keyword");
        symbolTable.put("void", "keyword");
        symbolTable.put("true", "keyword");
        symbolTable.put("false", "keyword");
        symbolTable.put("null", "keyword");
        symbolTable.put("this", "keyword");
        symbolTable.put("let", "keyword");
        symbolTable.put("do", "keyword");
        symbolTable.put("if", "keyword");
        symbolTable.put("else", "keyword");
        symbolTable.put("while", "keyword");
        symbolTable.put("return", "keyword");

        symbolTable.put("{", "symbol");
        symbolTable.put("}", "symbol");
        symbolTable.put("(", "symbol");
        symbolTable.put(")", "symbol");
        symbolTable.put("[", "symbol");
        symbolTable.put("]", "symbol");
        symbolTable.put(".", "symbol");
        symbolTable.put(",", "symbol");
        symbolTable.put(";", "symbol");
        symbolTable.put("+", "symbol");
        symbolTable.put("-", "symbol");
        symbolTable.put("*", "symbol");
        symbolTable.put("/", "symbol");
        symbolTable.put("&", "symbol");
        symbolTable.put("|", "symbol");
        symbolTable.put("<", "symbol");
        symbolTable.put(">", "symbol");
        symbolTable.put("=", "symbol");
        symbolTable.put("_", "symbol");


    }

    public boolean hasMoreTokens()
    {
        return scanFile.hasNext();
    }

    public void advance()
    {
            line = scanFile.nextLine().split("//")[0].trim();
    }

    public void compilerRun() throws IOException
    {
        int lineNumb = 0;
        while(hasMoreTokens())
        {
            advance();
            if(line.length() == 0 || line.startsWith("/"))
                continue;
            if(line.startsWith("var"))
            {
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                compileVarDec();
                varDecBefore = true;
            }
            else if(line.startsWith("while"))
            {
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                if(varDecBefore)
                    writeToFile.append("<statements>\n");
                compileWhile();
                varDecBefore = false;
            }
            else if(line.startsWith("do"))
            {
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                if(varDecBefore)
                    writeToFile.append("<statements>\n");
                compileDo();
                varDecBefore = false;
            }
            else if(line.startsWith("let"))
            {
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                if(varDecBefore)
                    writeToFile.append("<statements>\n");
                compileLet();
                varDecBefore = false;
            }
            else if(line.startsWith("return"))
            {
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                if(ifBefore) {
                    ifBefore = false;
                    compileEndCurly();
                }
                if(varDecBefore)
                    writeToFile.append("<statements>\n");
                compileReturn();
                varDecBefore = false;
            }
            else if(line.startsWith("if"))
            {
                if(varDecBefore)
                    writeToFile.append("<statements>\n");
                compileIf();
                varDecBefore = false;
                ifBefore = true;
            }
            else if(line.startsWith("class"))
            {
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                compileClass();
                varDecBefore = false;
            }
            else if(line.startsWith("function") || line.startsWith("constructor") || line.startsWith("method"))
            {
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                compileSubroutine();
                varDecBefore = false;
            }
            else if(line.startsWith("}"))
            {
                compileEndCurly();
                varDecBefore = false;
            }
            else if(line.startsWith("field") || line.startsWith("static"))
            {
                /*if(!varDecBefore)
                    System.exit(-1);*/
                if(ifBefore && !statementStack.peek().equals("if"))
                {
                    ifBefore = false;
                    writeToFile.append("</ifStatement>\n");
                }
                compileClassVarDec();
                varDecBefore = true;
            }
            else if(line.startsWith("else"))
            {
                statementStack.push("if");
                writeToFile.append(tokenHandler("else"));
                writeToFile.append(tokenHandler("{"));
                //statementStack.push("if");
                writeToFile.append("<statements>\n");
            }
            else
                System.out.println("prob: " + line);
        }
        writeToFile.close();
    }
    //keyword, symbol, identifier, int_const, String_const
    public String tokenHandler(String input)
    {
        String returnVal;
        if(symbolTable.containsKey(input))//keyword, symbol, identifier
        {
            returnVal = tokenAngleBracketify(symbolTable.get(input), input);
        }
        else if(isNumber(input))//int_const
        {
            returnVal = tokenAngleBracketify("integerConstant", input);
        }
        else if(input.startsWith("\"") && input.endsWith("\""))//stringConstant
        {
            returnVal = tokenAngleBracketify("stringConstant", input.substring(1, input.length()-1));
        }
        else
        {
            symbolTable.put(input, "identifier");
            returnVal = tokenAngleBracketify("identifier", input);
        }
        return returnVal + "\n";
    }

    public String tokenAngleBracketify(String type, String input)
    {
        return angleBracket(type, true) + " " + input + " " + angleBracket(type, false);
    }

    public String angleBracket(String type, boolean beginningBrackets)
    {
        return "<" + (beginningBrackets ? "" : "/") + type + ">";
    }

    public void compileVarDec() throws IOException
    {
        writeToFile.append("<varDec>\n");
        writeToFile.append(tokenHandler("var"));
        writeToFile.append(tokenHandler(line.split(" ")[1]));
        String identifiers = line.substring(line.indexOf(" ", 4) + 1, line.length()).replaceAll(" ", "");

        while(identifiers.contains(","))
        {
            writeToFile.append(tokenHandler(identifiers.substring(0, identifiers.indexOf(","))));
            writeToFile.append(tokenHandler(","));
            identifiers = identifiers.substring(identifiers.indexOf(",") + 1, identifiers.length());
        }

        writeToFile.append(tokenHandler(identifiers.substring(0, identifiers.length() - 1)));
        writeToFile.append(tokenHandler(";"));
        writeToFile.append("</varDec>\n");
        //writeToFile.append("<statements>\n");
    }

    public void compileStatements() throws IOException
    {
        writeToFile.append("<statements>\n");

        writeToFile.append("</statements>\n");
    }

    public void compileDo() throws IOException
    {
        writeToFile.append("<doStatement>\n");
        writeToFile.append(tokenHandler("do"));
        String tempLine = line.substring(3, line.length());

        while(tempLine.contains("."))//sub calls
        {
            writeToFile.append(tokenHandler(tempLine.substring(0, tempLine.indexOf("."))));
            writeToFile.append(tokenHandler("."));
            tempLine = tempLine.substring(tempLine.indexOf(".") + 1, tempLine.length());
        }

        writeToFile.append(tokenHandler(tempLine.substring(0, tempLine.indexOf("("))));
        writeToFile.append(tokenHandler("("));
        compileExpressionList(tempLine.substring(tempLine.indexOf("(")+1, tempLine.length() -2));
        writeToFile.append(tokenHandler(")"));
        writeToFile.append(tokenHandler(";"));
        writeToFile.append("</doStatement>\n");
    }

    public void compileLet() throws IOException
    {
        writeToFile.append("<letStatement>\n");
        writeToFile.append(tokenHandler("let"));

        String term = line.split(" ")[1];
        String item = "";
        for(int i = 0; i < term.length(); i++)
        {
            if(term.charAt(i) == '[')
            {
                writeToFile.append(tokenHandler(item));
                writeToFile.append(tokenHandler("["));
                item = "";
                compileExpression(term.substring(term.indexOf("[") + 1, term.lastIndexOf("]")));
                writeToFile.append(tokenHandler("]"));
                i = term.lastIndexOf("]");
            }
            else
                item += term.charAt(i);
        }
        if(item.length() > 0)
            writeToFile.append(tokenHandler(item));


        writeToFile.append(tokenHandler("="));
        compileExpression(line.substring(line.indexOf("=") + 1, line.length() - 1));
        writeToFile.append(tokenHandler(";"));
        writeToFile.append("</letStatement>\n");
    }

    public void compileWhile() throws IOException
    {
        writeToFile.append("<whileStatement>\n");
        writeToFile.append(tokenHandler("while"));
        writeToFile.append(tokenHandler("("));
        compileExpression(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
        writeToFile.append(tokenHandler(")"));
        writeToFile.append(tokenHandler("{"));

        writeToFile.append("<statements>\n");
        statementStack.push("while");
    }

    public void compileEndCurly() throws IOException
    {
        String type = statementStack.pop();

        if(!type.equals("class"))
            writeToFile.append("</statements>\n");

        writeToFile.append(tokenHandler("}"));
        if(type.equals("while"))
            writeToFile.append("</whileStatement>\n");
        else if(type.equals("if"))
            type.toString();//writeToFile.append("</ifStatement>\n");
        else if(type.equals("class"))
            writeToFile.append("</class>\n");
        else if(type.equals("function") || type.equals("method") || type.equals("constructor"))
        {
            writeToFile.append("</subroutineBody>\n");
            writeToFile.append("</subroutineDec>\n");
        }
    }

    public void compileReturn() throws IOException
    {
        writeToFile.append("<returnStatement>\n");

        writeToFile.append(tokenHandler("return"));
        if(line.length() > 7)
            writeToFile.append(tokenHandler(line.split(" ")[1].substring(0, line.length() - 1)));
        writeToFile.append(tokenHandler(";"));
        writeToFile.append("</returnStatement>\n");
    }

    public void compileIf() throws IOException
    {
        writeToFile.append("<ifStatement>\n");
        writeToFile.append(tokenHandler("if"));
        writeToFile.append(tokenHandler("("));
        compileExpression(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
        writeToFile.append(tokenHandler(")"));
        writeToFile.append(tokenHandler("{"));

        writeToFile.append("<statements>\n");
        statementStack.push("if");
    }

    public String[] stringToArrString(String in)
    {
        String[] returnVal = new String[1];
        returnVal[0] = in;
        return returnVal;
    }

    public void compileExpressionList(String input) throws IOException//use in function calls
    {
        writeToFile.append("<expressionList>\n");
        if(input.length() > 0) {
            ArrayList<String> expressionList = new ArrayList<>(Arrays.asList(input.replaceAll(", ", ",").split(",")));
            while (expressionList.size() > 0) {
                compileExpression(expressionList.get(0));
                expressionList.remove(0);
                expressionList.trimToSize();
            }
        }
        writeToFile.append("</expressionList>\n");
    }

    public void compileExpression(String input) throws IOException
    {//function call, item in parenth,
        //writeToFile.append("<expression>\n");
        System.out.println(input);
        ArrayList<String> expList = new ArrayList<>();
        String loopTerm = "";
        boolean insideQuote = false;
        for(int i = 0; i < input.length(); i++)
        {

            if(Character.toString(input.charAt(i)).equals("\""))//quotation if
            {
                if(insideQuote)
                {//End quote
                    insideQuote = false;
                    loopTerm += input.charAt(i);
                    expList.add(loopTerm);
                    loopTerm = "";
                }
                else
                {
                    insideQuote = true;
                    loopTerm += input.charAt(i);
                }
            }
            else if(insideQuote)//Inside quote, not "
            {
                loopTerm += input.charAt(i);
            }
            else if(Character.toString(input.charAt(i)).equals(" "))
            {
                if(loopTerm.length() > 0)
                {
                    expList.add(loopTerm);
                    loopTerm = "";
                }
            }
            else if(Character.toString(input.charAt(i)).equals("."))
            {
                expList.add(loopTerm);
                loopTerm = "";
                expList.add(".");
            }
            else if(symbolTable.containsKey(Character.toString(input.charAt(i))) && symbolTable.get(Character.toString(input.charAt(i))).equals("symbol"))
            {
                if(Character.toString(input.charAt(i)).equals("(") || Character.toString(input.charAt(i)).equals(")") || Character.toString(input.charAt(i)).equals("[") || Character.toString(input.charAt(i)).equals("]"))
                    expList.add(loopTerm);
                expList.add(Character.toString(input.charAt(i)));
                loopTerm = "";
            }
            else
            {
                loopTerm += input.charAt(i);
            }
        }
        if(loopTerm.length() > 0)
            expList.add(loopTerm);
        System.out.println(expList.toString());
        compileExpressionR(expList);
    }

    public void compileExpressionR(ArrayList<String> inList) throws IOException
    {
        writeToFile.append("<expression>\n");
        writeToFile.append("<term>\n");
        boolean inTerm = true;
        String prev = "";
        for(int i = 0; i < inList.size(); i++)
        {
            String item = inList.get(i);
            if(item.equals("("))
            {
                if(prev.equals("")) {//Bug that I'm using to tell if this is not a function call
                    writeToFile.append("<term>\n");
                }
                writeToFile.append(tokenHandler(item));
                ArrayList<String> temp = new ArrayList<String>(inList.subList(i + 1, inList.indexOf(")")));
                String list = "";
                for(int j = 0; j < temp.size(); j++)
                    list += temp.get(j);
                if(!prev.equals(""))
                    compileExpressionList(list);
                else
                    compileExpression(list);
                i = inList.indexOf(")")-1;
                System.out.println(list.toString());
            }
            else if(item.equals("["))
            {

                writeToFile.append(tokenHandler(item));
                ArrayList<String> temp = new ArrayList<String>(inList.subList(i + 1, inList.indexOf("]")));
                String list = "";
                for(int j = 0; j < temp.size(); j++)
                    list += temp.get(j);
                compileExpression(list);
                i = inList.indexOf("]") - 1;
            }
            else if(symbolTable.containsKey(item) && symbolTable.get(item).equals("symbol"))
            {
                if(item.equals("+") || item.equals("|") || item.equals("&") || item.equals("*") || item.equals("/"))
                {
                    writeToFile.append("</term>\n");
                }
                else if(item.equals("-"))
                {
                    if(!prev.equals("")) {
                        writeToFile.append("</term>\n");
                    }
                }
                writeToFile.append(tokenHandler(item));
                if(item.equals("-") && prev.equals(""))
                {

                }
            }
            else if(item.length() > 0)
            {
                if(inList.size() == 2)
                    writeToFile.append("<term>\n");
                compileTerm(item);
            }
            prev = item;
        }
        writeToFile.append("</term>\n");
        writeToFile.append("</expression>\n");
    }

    public void compileTerm(String in) throws IOException
    {
        //writeToFile.append("<term>\n");
        writeToFile.append(tokenHandler(in));
        //writeToFile.append("</term>\n");
    }

    public void compileClass() throws IOException
    {
        writeToFile.append("<class>\n");
        writeToFile.append(tokenHandler(line.split(" ")[0]));
        writeToFile.append(tokenHandler(line.split(" ")[1]));
        writeToFile.append(tokenHandler("{"));
        statementStack.push("class");
    }

    public void compileClassVarDec() throws IOException
    {
        writeToFile.append("<classVarDec>\n");
        writeToFile.append(tokenHandler(line.split(" ")[0]));
        writeToFile.append(tokenHandler(line.split(" ")[1]));
        String identifiers = line.substring(line.indexOf(" ", line.indexOf(" ") + 1), line.length()).replaceAll(" ", "");

        while(identifiers.contains(","))
        {
            writeToFile.append(tokenHandler(identifiers.substring(0, identifiers.indexOf(","))));
            writeToFile.append(tokenHandler(","));
            identifiers = identifiers.substring(identifiers.indexOf(",") + 1, identifiers.length());
        }

        writeToFile.append(tokenHandler(identifiers.substring(0, identifiers.length() - 1)));
        writeToFile.append(tokenHandler(";"));
        writeToFile.append("</classVarDec>\n");
    }

    public void compileSubroutine() throws IOException
    {
        writeToFile.append("<subroutineDec>\n");

        writeToFile.append(tokenHandler(line.split(" ")[0]));
        writeToFile.append(tokenHandler(line.split(" ")[1]));//return type
        writeToFile.append(tokenHandler(line.split(" ")[2].substring(0, line.split(" ")[2].indexOf("("))));
        writeToFile.append(tokenHandler("("));
        compileParameterList(line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")));
        writeToFile.append(tokenHandler(")"));

        writeToFile.append("<subroutineBody>\n");
        writeToFile.append(tokenHandler("{"));
        statementStack.push(line.split(" ")[0]);

    }

    public void compileParameterList(String input) throws IOException
    {
        writeToFile.append("<parameterList>\n");

        if(input.length() > 0)
        {
            String[] expList = (input.contains(",") ? input.split(",") : stringToArrString(input));
            for(int i = 0; i < expList.length; i++)
            {
                writeToFile.append(tokenHandler(expList[i].split(" ")[0]));
                writeToFile.append(tokenHandler(expList[i].split(" ")[1]));
                if(i != expList.length - 1)
                    writeToFile.append(tokenHandler(","));
            }
        }

        writeToFile.append("</parameterList>\n");
    }

    private boolean isNumber(String in)
    {
        for(int i = 0; i < in.length(); i++)
            if(!Character.isDigit(in.charAt(i)))
                return false;
        return true;
    }
}
