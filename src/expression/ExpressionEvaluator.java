package expression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {

	public static void main(String[] args) {
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		int result = evaluator.evaluate(args[0]);
		System.out.println(result);
	}

	public int evaluateOnlyAddSub(String expression) {
		List<String> tokens = tokenize(expression);

		int accumulation = 0;
		boolean signIsPositive = true;
		String lastToken = null;
		for (String token : tokens) {
			if (lastToken != null && !(isOperator(lastToken) ^ isOperator(token))) {
				throw new IllegalArgumentException(
						String.format("Invalid expression: '%s' and '%s' should not be adjacent", lastToken, token));
			}
			if (isOperator(token)) {
				assert token.length() == 1;
				if (token.equals("+")) {
					signIsPositive = true;
				} else if (token.equals("-")) {
					signIsPositive = false;
				} else {
					throw new IllegalArgumentException(String.format("Unrecognized operator '%s'", token));
				}
			} else {
				accumulation += Integer.parseInt(token) * (signIsPositive ? 1 : -1);
			}
			lastToken = token;
		}
		return accumulation;
	}

	public int evaluate(String expressionString) {
		return buildExpressionFromTokens(tokenize(expressionString)).evaluate();
	}

	private List<String> tokenize(String expression) {
		List<String> tokens = new ArrayList<String>();
		int i = 0;
		while (i < expression.length()) {
			char ch = expression.charAt(i);
			if (isOperator(ch)) {
				tokens.add(Character.toString(ch));
				++i;
			} else if (Character.isDigit(ch)) {
				String token = "";
				while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
					token += expression.charAt(i);
					++i;
				}
				tokens.add(token);
			} else if (Character.isLetter(ch)) {
				String token = "";
				while (i < expression.length() && Character.isLetterOrDigit(expression.charAt(i))) {
					token += expression.charAt(i);
					++i;
				}
				tokens.add(token);
			} else {
				throw new IllegalArgumentException(String.format("Char '%c' is not allowed in an expression.", ch));
			}
		}
		return tokens;
	}

	private static boolean isOperator(String token) {
		return token.length() == 1 && isOperator(token.charAt(0));
	}

	private static boolean isOperator(char ch) {
		final String validOperators = "+-*/()";
		return validOperators.indexOf(ch) >= 0;
	}

	private static boolean precedes(char op0, char op1) {
		return (op1 == '*' || op1 == '/') && (op0 == '+' || op0 == '-');
	}

	private static boolean shouldPush(char op0, char op1) {
		assert op0 != ')' : "')' should never appear in the operator stack.";
		// @formatter:off
		//   + - * / ( )
		// ==============
		// + F F T T T F
		// - F F T T T F
		// * F F F F T F
		// / F F F F T F
		// ( T T T T T T
		// @formatter:on
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

		int result = 0;
		switch (op) {
		case '+':
			result = leftChild + rightChild;
			break;
		case '-':
			result = leftChild - rightChild;
			break;
		case '*':
			result = leftChild * rightChild;
			break;
		case '/':
			result = leftChild / rightChild;
			break;
		default:
			assert false : "Operator '%c' cannot be evaluated.";
		}

		operandStack.addLast(result);
	}

	public int evaluateWithoutExpressionTree(String expressionString) {
		List<String> tokens = tokenize(expressionString);

		Deque<Character> operatorStack = new ArrayDeque<Character>();
		Deque<Integer> operandStack = new ArrayDeque<Integer>();
		for (String token : tokens) {
			if (!isOperator(token)) {
				operandStack.addLast(Integer.parseInt(token));
				continue;
			}

			assert token.length() == 1;
			char op = token.charAt(0);
			if (op == ')') {
				while (!operatorStack.isEmpty() && operatorStack.getLast() != '(') {
					evaluateTopOperator(operatorStack, operandStack);
				}
				if (operatorStack.isEmpty()) {
					throw new IllegalArgumentException("Invalid expression");
				}
				operatorStack.removeLast();
			} else {
				while (!operatorStack.isEmpty() && !shouldPush(operatorStack.getLast(), op)) {
					evaluateTopOperator(operatorStack, operandStack);
				}
				operatorStack.addLast(op);
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

	public Expression simplify(String expressionString) {
		Expression expression = buildExpressionFromTokens(tokenize(expressionString));
		Map<String, Integer> terms = expression.evaluateSymbolically();
		return buildExpressionFromTerms(terms);
	}

	private Expression buildExpressionFromTerms(Map<String, Integer> terms) {
		Expression accumulation = null;
		for (Map.Entry<String, Integer> term : terms.entrySet()) {
			if (term.getValue() == 0) {
				continue;
			}
			Expression current = new Expression(term.getValue());
			if (!term.getKey().equals("1")) {
				// TOOD: The coefficient can be omitted if it equals 1. E.g. "a" instead of
				// "1*a".
				current = new Expression('*', current, new Expression(term.getKey()));
			}
			if (accumulation == null) {
				accumulation = current;
			} else {
				// TODO: If `term` has a negative coefficient, it would be better to connect
				// `accumulation` and `current` with a minus sign. E.g. "a-b" instead of
				// "a+-1*b".
				accumulation = new Expression('+', accumulation, current);
			}
		}
		if (accumulation == null) {
			accumulation = new Expression(0);
		}
		return accumulation;
	}

	public Expression simplifyByFlattening(String expressionString) {
		Expression expression = buildExpressionFromTokens(tokenize(expressionString));
		Map<String, Integer> terms = expression.flattenIntoTerms();
		return buildExpressionFromTerms(terms);
	}
}
