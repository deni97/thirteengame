package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class randomValueTest {

static Engine engine;
	
	static int firstValue;
	static int secondValue;
	
	@BeforeAll
	static void setUp() {
		firstValue  = Engine.randomValue(5);
		secondValue = Engine.randomValue(108);
	}
	
	@Test
	void test() {
		assertTrue(firstValue > 0 && firstValue < 5, "Result should be in (0, 5) range   " + firstValue);
		assertTrue(secondValue > 99 && secondValue < 108, "Result should be in (99, 108) range   " + secondValue);
	}
}
