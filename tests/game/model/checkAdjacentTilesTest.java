package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class checkAdjacentTilesTest {
	
	static Integer[][] board;
	static Coord coord;
	
	static int tileNumber;
	
	@BeforeAll
	static void setUp() {
		Engine.numCols = 3;
		Engine.numRows = 3;	
		
		coord = new Coord(1, 0);
		board = new Integer[Engine.numRows][Engine.numCols];
		
		for (int y = 0; y < Engine.numRows; y++)
	    {
	    	for	(int x = 0; x < Engine.numCols; x++) 
	    	{
	    		board[y][x] = 0;
	    	}
	    }
		
		board[0][0] = 1;
		board[0][1] = 1;
		board[0][2] = 1;
		
		tileNumber = Engine.checkAdjacentTiles(board, coord).size();
	}
	
	@Test
	void test() {
		assertEquals(2, tileNumber, "There should be 2 neighbor tiles");
	}
}
