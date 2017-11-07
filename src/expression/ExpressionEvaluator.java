package expression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class ExpressionEvaluator {

	public static void main(String[] args) {
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		int result = evaluator.evaluate(args[0]);
		System.out.println(result);
	}

	@Deprecated
	public int evaluateOnlyAddSub(String expression) {
		List<String> tokens = tokenize(expression);

		int result = 0;
		boolean signIsPositive = true;
		String lastToken = null;
		for (String token : tokens) {
			if (lastToken != null && !(isOperator(lastToken) ^ isOperator(token))) {
				throw new IllegalArgumentException(
						String.format("Invalid expression: '%s' and '%s' should not be adjacent", lastToken, token));
			}
			if (isOperator(token)) {
				if (token.equals("+")) {
					signIsPositive = true;
				} else if (token.equals("-")) {
					signIsPositive = false;
				} else {
					throw new IllegalArgumentException(String.format("Unrecognized operator '%s'", token));
				}
			} else {
				result += Integer.parseInt(token) * (signIsPositive ? 1 : -1);
			}
			lastToken = token;
		}
		return result;
	}

	public int evaluate(String expressionString) {
		return buildExpressionFromTokens(tokenize(expressionString)).evaluate();
	}

	private List<String> tokenize(String expression) {
		List<String> tokens = new ArrayList<String>();
		int i = 0;
		while (i < expression.length()) {
			char ch = expression.charAt(i);
			if (Character.isDigit(ch)) {
				String token = "";
				while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
					token += expression.charAt(i);
					++i;
				}
				tokens.add(token);
			} else {
				tokens.add(Character.toString(ch));
				++i;
			}
		}
		return tokens;
	}

	private static boolean isOperator(String token) {
		final String[] validOperators = { "+", "-", "*", "/", "(", ")" };
		return Arrays.asList(validOperators).contains(token);
	}

	private static boolean precedes(char op0, char op1) {
		return (op1 == '*' || op1 == '/') && (op0 == '+' || op0 == '-');
	}

	private static boolean shouldPush(char op0, char op1) {
		assert op0 != ')' : "')' should never appear in the operator stack.";
		// + - * / ( )
		// ==============
		// + F F T T T F
		// - F F T T T F
		// * F F F F T F
		// / F F F F T F
		// ( T T T T T T
		if (op0 == '(' || op1 == '(') {
			return true;
		}
		if (op1 == ')') {
			return false;
		}
		return precedes(op1, op0);
	}

	private static void evaluateTopOperator(Deque<Character> operatorStack, Deque<Integer> operandStack) {
		if (operandStack.isEmpty()) {
			throw new IllegalArgumentException("Invalid expression");
		}
		int rightChild = operandStack.removeLast();
		if (operandStack.isEmpty()) {
			throw new IllegalArgumentException("Invalid expression");
		}
		int leftChild = operandStack.removeLast();
		char op = operatorStack.removeLast();

		switch (op) {
		case '+':
			operandStack.addLast(leftChild + rightChild);
			break;
		case '-':
			operandStack.addLast(leftChild - rightChild);
			break;
		case '*':
			operandStack.addLast(leftChild * rightChild);
			break;
		case '/':
			operandStack.addLast(leftChild / rightChild);
			break;
		default:
			assert false : "Operator '%c' cannot be evaluated.";
		}
	}

	@Deprecated
	public int evaluateWithoutExpressionTree(String expressionString) {
		List<String> tokens = tokenize(expressionString);

		Deque<Character> operatorStack = new ArrayDeque<Character>();
		Deque<Integer> operandStack = new ArrayDeque<Integer>();
		for (String token : tokens) {
			if (isOperator(token)) {
				assert token.length() == 1;
				char op = token.charAt(0);
				while (!operatorStack.isEmpty() && !shouldPush(operatorStack.getLast(), op)) {
					evaluateTopOperator(operatorStack, operandStack);
				}
				operatorStack.addLast(op);
				if (op == ')') {
					operatorStack.removeLast();
					if (operatorStack.isEmpty() || operatorStack.getLast() != '(') {
						throw new IllegalArgumentException("Invalid expression");
					}
					operatorStack.removeLast();
				}
			} else {
				operandStack.addLast(Integer.parseInt(token));
			}
		}
		while (!operatorStack.isEmpty()) {
			evaluateTopOperator(operatorStack, operandStack);
		}
		if (operandStack.size() != 1) {
			throw new IllegalArgumentException("Invalid expression");
		}
		return operandStack.getFirst();
	}

	private static void evaluateTopOperatorSymbolically(Deque<Character> operatorStack,
			Deque<Expression> operandStack) {
		if (operandStack.isEmpty()) {
			throw new IllegalArgumentException("Invalid expression");
		}
		Expression rightChild = operandStack.removeLast();
		if (operandStack.isEmpty()) {
			throw new IllegalArgumentException("Invalid expression");
		}
		Expression leftChild = operandStack.removeLast();
		operandStack.addLast(new Expression(operatorStack.removeLast(), leftChild, rightChild));
	}

	private Expression buildExpressionFromTokens(List<String> tokens) {
		Deque<Character> operatorStack = new ArrayDeque<Character>();
		Deque<Expression> operandStack = new ArrayDeque<Expression>();
		for (String token : tokens) {
			if (isOperator(token)) {
				assert token.length() == 1;
				char op = token.charAt(0);
				while (!operatorStack.isEmpty() && !shouldPush(operatorStack.getLast(), op)) {
					evaluateTopOperatorSymbolically(operatorStack, operandStack);
				}
				operatorStack.addLast(op);
				if (op == ')') {
					operatorStack.removeLast();
					if (operatorStack.isEmpty() || operatorStack.getLast() != '(') {
						throw new IllegalArgumentException("Invalid expression");
					}
					operatorStack.removeLast();
				}
			} else {
				Expression operand;
				try {
					int intValue = Integer.parseInt(token);
					operand = new Expression(intValue);
				} catch (NumberFormatException ex) {
					operand = new Expression(token);
				}
				operandStack.addLast(operand);
			}
		}
		while (!operatorStack.isEmpty()) {
			evaluateTopOperatorSymbolically(operatorStack, operandStack);
		}
		if (operandStack.size() != 1) {
			throw new IllegalArgumentException("Invalid expression");
		}
		return operandStack.getFirst();
	}
}
