package expression;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorTest {
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		evaluator = new ExpressionEvaluator();
	}

	@Test
	void testConstantAddSub() {
		assertEquals(12 + 34 - 56, evaluator.evaluate("12+34-56"));
		assertEquals(12 + 34 - 56, evaluator.evaluateWithoutExpressionTree("12+34-56"));
		assertEquals(12 + 34 - 56, evaluator.evaluateOnlyAddSub("12+34-56"));
	}

	@Test
	void testConstantParenAddSubMulDiv() {
		assertEquals((12 - 34) * 56, evaluator.evaluate("(12-34)*56"));
		assertEquals((12 - 34) * 56, evaluator.evaluateWithoutExpressionTree("(12-34)*56"));
	}

	@Test
	void testInvalidExpression() {
		{
			Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
				evaluator.evaluate("12-34)*56");
			});
			assertTrue(exception.getMessage().contains("Invalid expression"));
		}
		{
			Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
				evaluator.evaluateOnlyAddSub("12-+34");
			});
			assertTrue(exception.getMessage().contains("Invalid expression"));
		}
		{
			Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
				evaluator.evaluateOnlyAddSub("12*34");
			});
			assertTrue(exception.getMessage().contains("Unrecognized operator"));
		}
	}

	private static ExpressionEvaluator evaluator;
}
