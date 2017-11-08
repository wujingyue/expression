package expression;

import java.util.Map;
import java.util.TreeMap;

enum ExpressionType {
	OPERATOR, VARIABLE, CONSTANT
}

public class Expression {
	ExpressionType type;
	char op;
	String varName;
	int value;
	Expression leftChild;
	Expression rightChild;

	public Expression(char op, Expression leftChild, Expression rightChild) {
		this.type = ExpressionType.OPERATOR;
		this.op = op;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}

	public Expression(String varName) {
		this.type = ExpressionType.VARIABLE;
		this.varName = varName;
	}

	public Expression(int value) {
		this.type = ExpressionType.CONSTANT;
		this.value = value;
	}

	public String toString() {
		switch (type) {
		case VARIABLE:
			return varName;
		case CONSTANT:
			return String.valueOf(value);
		case OPERATOR:
			return String.format("(%s %c %s)", leftChild, op, rightChild);
		default:
			assert false : String.format("Invalid expression type %s", type);
			return null;
		}
	}

	private int evaluateOperator(char op, int leftValue, int rightValue) {
		switch (op) {
		case '+':
			return leftValue + rightValue;
		case '-':
			return leftValue - rightValue;
		case '*':
			return leftValue * rightValue;
		case '/':
			return leftValue / rightValue;
		default:
			throw new IllegalArgumentException(String.format("'%c' is not a valid operator to evaluate", op));
		}
	}

	public int evaluate() {
		switch (type) {
		case VARIABLE:
			throw new IllegalArgumentException(String.format("Unable to evaluate expression type '%s'", type));
		case CONSTANT:
			return value;
		case OPERATOR:
			return evaluateOperator(op, leftChild.evaluate(), rightChild.evaluate());
		default:
			throw new IllegalArgumentException(String.format("Invalid expression type '%s'", type));
		}
	}

	public Map<String, Integer> evaluateSymbolically() {
		switch (type) {
		case VARIABLE: {
			Map<String, Integer> result = new TreeMap<String, Integer>();
			result.put(varName, 1);
			return result;
		}
		case CONSTANT: {
			Map<String, Integer> result = new TreeMap<String, Integer>();
			result.put("1", value);
			return result;
		}
		case OPERATOR: {
			// TODO: Set `result` to be the larger map between leftChild and rightChild.
			Map<String, Integer> result = leftChild.evaluateSymbolically();
			Map<String, Integer> rightTerms = rightChild.evaluateSymbolically();
			switch (op) {
			// @formatter:off
//			case '+':
//			case '-':
//				rightTerms.forEach((term, coefficient) -> result.merge(term, (op == '+' ? 1 : -1) * coefficient,
//						(c0, c1) -> c0 + c1));
//				break;
			// @formatter:on
			case '+':
				for (Map.Entry<String, Integer> rightTerm : rightTerms.entrySet()) {
					String var = rightTerm.getKey();
					result.put(var, result.getOrDefault(var, 0) + rightTerm.getValue());
				}
				break;
			case '-':
				for (Map.Entry<String, Integer> rightTerm : rightTerms.entrySet()) {
					String var = rightTerm.getKey();
					result.put(var, result.getOrDefault(var, 0) - rightTerm.getValue());
				}
				break;
			default:
				throw new IllegalArgumentException(String.format("Unable to evaluate operator '%c' symbolically", op));
			}
			return result;
		}
		default:
			throw new IllegalArgumentException(String.format("Invalid expression type '%s'", type));
		}
	}
}
