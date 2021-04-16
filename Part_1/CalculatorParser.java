import java.io.IOException;
import java.io.InputStream;

/**
 * Grammar:
 * expr -> term expr2
 * expr2 -> op1 term expr2
 *          | ε
 * term -> factor term2
 * term2 -> op2 factor term2
 *          | ε
 * factor -> num
 *          | (expr)
 * op1 -> +
 *       |-
 * op2 -> **
 * num -> 0
 *       | digit2
 *       | digit2 num2
 * num2 -> digit
 *        | digit num2
 * digit2 -> 1...9
 * digit -> 0...9
 */

public class CalculatorParser {
    private final InputStream in;
    private int lookahead;

    public CalculatorParser(InputStream in) throws IOException  {
        this.lookahead = in.read();
        this.in = in;
    }

    // Evaluate the expression given, if parsed correctly, parse error otherwise.
    public int eval() throws IOException, ParseError {
        int res = expr();
        if(lookahead != '\n' && lookahead != -1){
            throw new ParseError();
        } else {
            return res;
        }
    }

    // Move the lookahead symbol.
    private void move(int input) throws IOException, ParseError {
        if (lookahead == input)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private static int pow(int base, int exponent) {
        if (exponent < 0)
            return 0;
        if (exponent == 0)
            return 1;
        if (exponent == 1)
            return base;

        if (exponent % 2 == 0) //even exp -> b ^ exp = (b^2)^(exp/2)
            return pow(base * base, exponent/2);
        else                   //odd exp -> b ^ exp = b * (b^2)^(exp/2)
            return base * pow(base * base, exponent/2);
    }

    /*--------------------Implementation of the grammar rules--------------------*/

    private int expr() throws IOException, ParseError {
        if(lookahead == '\n' || lookahead == -1){
            return 0;
        } else if(digit(lookahead) || lookahead == '('){
            int lhs = term();
            return expr2(lhs);
        } else throw new ParseError();
    }

    private int term() throws IOException, ParseError {
        if(digit(lookahead) || lookahead == '('){
            return pow(factor(), term2());
        } else {
            throw new ParseError();
        }

    }

    private int term2() throws IOException, ParseError {
        if(op2()){
            return pow(factor(), term2());
        } else if(op1() || lookahead == ')' || lookahead == '\n' || lookahead != -1){
            return 1;
        } else throw new ParseError();
    }

    private int expr2(int lhs) throws IOException, ParseError {
        int rhs;
        int op;
        if(op1()){
            op = lookahead;
            move(lookahead);
            rhs = term();

            switch(op){
                case '+':
                    lhs = lhs + rhs;
                    break;

                case '-':
                    lhs = lhs - rhs;
                    break;

            }
            return expr2(lhs);

        } else if(lookahead == ')' || lookahead == '\n' || lookahead != -1){
            return lhs;
        } else throw new ParseError();
    }

    private int factor() throws IOException, ParseError {
        int res;
        if(digit(lookahead)){
            return num(0);
        } else if(lookahead == '('){
            move(lookahead);
            res = expr();
            if(lookahead == ')'){
                move(lookahead);
            } else {
                throw new ParseError();
            }
            return res;
        } else throw new ParseError();
    }

    private boolean digit(int input){
        return '0' <= input && input <= '9';
    }
    private boolean digit2(int input){
        return '1' <= input && input <= '9';
    }

    public int evalDigit(int input) {
        return input - '0';
    }

    private boolean op1(){
        return lookahead == '+' | lookahead == '-';
    }

    private boolean op2() throws IOException, ParseError {
        if(lookahead == '*'){
            move(lookahead);
            if(lookahead != '*'){
                throw new ParseError();
            }
            move(lookahead);
            return true;
        }
        return false;
    }

    public int num(int res) throws IOException, ParseError {
        int value;
        int ret;
        if(lookahead == '0'){
            move(lookahead);
            return 0;
        } else if(digit2(lookahead)){
            value = evalDigit(lookahead);
            res = res*10 + value;
            move(lookahead);
            ret = num2(res);
            if(ret != -1){
                return ret;
            } else {
                return res;
            }
        } else {
            return -1;
        }

    }
    public int num2(int res) throws IOException, ParseError {
        int value;
        int ret;
        if(digit(lookahead)){
            value = evalDigit(lookahead);
            res = res*10 + value;
            move(lookahead);
            ret = num2(res);

            if(ret != -1){
                return ret;
            } else {
                return res;
            }
        } else {
            return -1;
        }
    }

}
