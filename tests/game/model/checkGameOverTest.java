package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class checkGameOverTest {
	static Integer[][] board;
	
	static boolean isGameOver;
	static boolean isNotGameOver;
	
	@BeforeAll
	static void setUp() {
		Engine.numCols = 2;
		Engine.numRows = 2;
		
		board = new Integer[Engine.numRows][Engine.numCols];
		
		int count = 0;
		
		for (int y = 0; y < Engine.numRows; y++)
	    {
	    	for	(int x = 0; x < Engine.numCols; x++) 
	    	{
	    		board[y][x] = count;
	    		count++;
	    	}
	    }
		isGameOver = Engine.checkGameOver(board);
		
		for (int y = 0; y < Engine.numRows; y++)
	    {
	    	for	(int x = 0; x < Engine.numCols; x++) 
	    	{
	    		board[y][x] = 1;
	    	}
	    }
		isNotGameOver = Engine.checkGameOver(board);
	}
	
	@Test
	void test() {
		assertTrue(isGameOver, "A board without repeating values should mean game over");
		
		assertFalse(isNotGameOver, "A board filled with a single value should not mean game over");
	}
}
