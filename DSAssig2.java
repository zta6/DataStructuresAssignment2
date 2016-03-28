/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package dsassig2;

/**
 *
 * @author Zach
 */
import java.util.LinkedList;
import java.util.Scanner;
import java.io.*;

public class DSAssig2 {

    public static LinkedList<Double> operands = new LinkedList<Double>();//create stacks for operators and operands
    public static LinkedList<Character> operators = new LinkedList<Character>();
    public static char[] variables = new char[100];
    public static Double[] values = new Double[100];

    public static void main(String[] args) throws IOException {

        Reader r = new BufferedReader(new InputStreamReader(System.in));
        StreamTokenizer st = new StreamTokenizer(r);//create buffered reader and tokenizer
        st.eolIsSignificant(true);//set end of line to be signSystem.inificant so that streamtokenizer stops after one line
        st.ordinaryChar((int) '/');//set division symbol and subtraction symbol to be ordinary characters
        st.ordinaryChar((int) '-');

        int i;//create int to hold value of next token
        Character nextChar;//create character variable
        double num;
        boolean lastOperator = false, lastOperand = false;//booleans to check for invalid expressions
        int openParen = 0, closedParen = 0;

        System.out.println("Please enter an infix mathematical expression");
        while ((i = st.nextToken()) != StreamTokenizer.TT_EOL) {//while the token isnt the end of line character read more tokens
            switch (i) {
                case StreamTokenizer.TT_EOL:
                    System.out.println("End of line");
                    return;
                case StreamTokenizer.TT_NUMBER://if the token is a number
                    if (lastOperand) {//check if the last entry was an operand as well
                        System.out.println("It appears that you've entered two operands in a row. Please try again with a valid expression");
                        System.exit(0);
                    }
                    num = st.nval;//create a double with the value of the token
                    System.out.println("Processing token: " + num);
                    operands.push((Double) num);//push the number to the operands stack
                    lastOperator = false;//set booleans to check for invalid expressions
                    lastOperand = true;
                    break;
                case StreamTokenizer.TT_WORD://if the token is a word 
                    nextChar = '$';//assign value to nextChar that won't be accidentally interpreted as a variable

                    if (st.sval.length() == 1) {//if the word is only one character
                        nextChar = st.sval.charAt(0);//use the first character as a variable
                    }
                    if (Character.isLetter(nextChar)) { //check to see if the character is a valid variable name
                        if (lastOperand) {//check for invalid expressions
                            System.out.println("It appears that you've entered two operands in a row. Please try again with a valid expression");
                            System.exit(0);
                        }
                        operands.push(getValue(nextChar));//push user-chosen value of variable on to stack
                        lastOperand = true;
                        lastOperator = false;//set booleans to check for invalid expressions
                    } else {
                        System.out.println("It appears that you've entered a word or an invalid special character. Please try again with a valid expression");//print out error message
                        System.exit(0);
                    }
                    break;
                default://if the token is a single character
                    nextChar = (char) i;//cast value of ascii character and hold it in a char variable
                    System.out.println("Processing Token: " + nextChar);
                    if (isOperator(nextChar)) {//if the character is a valid operator
                        if (lastOperator && nextChar != '(' && nextChar != ')') {//if the last token was an operator and this one isnt a parenthesis close the program and print an error message
                            System.out.println("It appears that you've entered two operators in a row. Please try again with a valid expression");
                            System.exit(0);
                        }
                        if (nextChar == ')') {
                            closedParen++;//increment parenthesis count and set booleans to check for multiple operators and operands in a row 
                            lastOperator = false;
                        } else {
                            lastOperator = true;
                            lastOperand = false;
                        }
                        if (nextChar == '(') {//increment open parenthesis count
                            openParen++;
                        }
                        while (higherPrecedence(nextChar) && operators.peek() != '(') {//if the new operator has LOWER precedence than the operator already on the stack, then evaluate what is already on the stack
                            evaluate();
                        }
                        operators.push(nextChar);//lastly push the new operator onto the top of the stack
                    } else {
                        System.out.println("INVALID CHARACTER INPUT");//if the operator isnt a valid character then print out an error message
                        System.exit(0);//and end program
                    }
            }
            System.out.println("Operator Stack-->top: " + operators.toString());//print stacks at the end of each iteration
            System.out.println("Operand Stack-->top: " + operands.toString());
            System.out.println();

        }
        if (openParen != closedParen) {
            System.out.println("Parenthesis mismatch, please try again.");
            System.exit(0);
        }
        while (operators.peek() != null) {//finish calculation
            evaluate();
            System.out.println("Operator Stack-->top: " + operators.toString());//print stacks at the end of each iteration
            System.out.println("Operand Stack-->top: " + operands.toString());
            System.out.println();
        }

        System.out.println("Answer: " + operands.pop().toString());//dislpay answer 
    }

    public static Double getValue(char test) {//method to get value of variable

        double value = Double.MIN_VALUE;//initialize double to min value
        boolean flag = false;//boolean to determine when to stop the do while loop
        
        if(getCachedVariable(test) != Double.MAX_VALUE){//if the variable has been used before, get its stored value and return it
            return getCachedVariable(test);
        }
        
        do {
            System.out.println("Please enter the value that would would like '" + test + "' to represent: ");//ask user for input
            try {
                Scanner user = new Scanner(System.in);
                value = Double.parseDouble(user.nextLine());//try to parse input to double
                flag = false;
            } catch (Exception e) {//catch exception and print error, make flag = true so that loop keeps going
                flag = true;
                System.out.println("Please enter a numerical value");
            }
        } while (flag);
        
        addVariableToCache(test,value);//store new variables in the cache arrays
        
        return (Double) value;
    }

    public static boolean higherPrecedence(char newOperator) {//function to return a boolean indicating that the operator on the top of the stack has higher precedence than the argument passed
        if (operators.peek() != null) {//checkif there is an operator on the stack
            Character secondOperator = operators.peek();
            return (precedenceValue(newOperator) <= precedenceValue(secondOperator));//compare precedence values of operators
        }
        return false; //otherwise return false
    }

    public static Double getCachedVariable(char test) {//see if a variable has been cached from a previous use
        for (int i = 0; i < variables.length; i++) {
            if (test == variables[i]) {
                return values[i];//if variable is found return the value
            }
        }
        return Double.MAX_VALUE;//else return double max value
    }
    public static void addVariableToCache(char test, double value){//add variable and it's associated value to cached variables
        for(int i = 0; i < variables.length; i++){
            if(variables[i] == '\u0000'){//loop until a null character is found, then add the new character at this spot
                variables[i] = test;
                values[i] = value;
                break;
            }
        }
        
    }

public static int precedenceValue(char operator) {//method to return integer value representing the precedence of an operator
        switch (operator) {
            case ')':
                return 0;
            case '-':
            case '+':
                return 1;
            case '*':
            case '/':
                return 2;
            case '^':
                return 3;
            case '(':
                return 4;
        }
        throw new IllegalArgumentException();//throw exception if the argument is not one of the above operators
    }

    public static boolean isOperator(char c) {//check to see if the argument is one of the possible operators
        return (c == '+' || c == '-' || c == '/' || c == '*' || c == '^' || c == '(' || c == ')');
    }

    public static void evaluate() {//method to get values for mathematical operation, perform the operation, then push the value
        if (operators.peek() != null) {
            Character oper = operators.pop();

            if (oper == ')') {//if operator is closed parenthesis
                if (operators.peek() != null) {
                    if (operators.peek() == '(') {//check if next operator is an open parenthesis
                        operators.pop();//then pop closed parenthesis off the stack too
                    } else {//otherwise pop the next operator so that the operaion inside the parenthesis can be done. 
                        Double secondOperand = null;
                        Double firstOperand = null;//initialize operands to null
                        try {//try to pop operands off the stack
                            oper = operators.pop();
                            secondOperand = operands.pop();
                            firstOperand = operands.pop();
                            operands.push((Double) performOneStep(oper, firstOperand, secondOperand));//push result of mathematical operation on to operands stack
                        } catch (java.util.NoSuchElementException n) {
                            operators.push(oper);
                            operands.push(secondOperand);//if a no such element exception is then this means that the second operand was null,because we already checked the first one above
                        }
                    }
                }
            } else if (operands.peek() != null && oper != '(') {//if there are operands to operate on
                Double secondOperand = null;
                Double firstOperand = null;//initialize operands to null
                try {//try to pop operands off the stack
                    secondOperand = operands.pop();
                    firstOperand = operands.pop();
                    operands.push((Double) performOneStep(oper, firstOperand, secondOperand));//push result of mathematical operation on to operands stack
                } catch (java.util.NoSuchElementException n) {
                    operators.push(oper);
                    operands.push(secondOperand);//if a no such element exception is then this means that the second operand was null,because we already checked the first one above
                }
            }
        }
    }

    public static double performOneStep(Character operator, Double firstOperand, Double secondOperand) {//function to perform one mathematical operation
        switch (operator) {
            case '+':
                return firstOperand + secondOperand;
            case '*':
                return firstOperand * secondOperand;
            case '/':
                if (secondOperand == 0) {
                    System.out.println("The expression you've entered contains a division by 0. Please try again with a new expression");
                    System.exit(0);
                }
                return firstOperand / secondOperand;
            case '-':
                return firstOperand - secondOperand;
            case '^':
                return Math.pow(firstOperand, secondOperand);
        }
        return Double.MAX_VALUE;//if function fails return max value
    }
}
