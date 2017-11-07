package expression;

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
}
