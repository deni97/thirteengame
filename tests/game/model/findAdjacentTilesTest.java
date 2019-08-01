package game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

class findAdjacentTilesTest {

	static Integer[][] board;
	static Coord coord;
	
	static int tileNumber;
	@BeforeAll
	static void setUp() {
		Engine.numCols = 6;
		Engine.numRows = 6;	
		
		coord = new Coord(1, 0);
		board = new Integer[Engine.numRows][Engine.numCols];
		
		for (int y = 0; y < Engine.numRows; y++)
	    {
	    	for	(int x = 0; x < Engine.numCols; x++) 
	    	{
	    		board[y][x] = 1;
	    	}
	    }
		
		tileNumber = Engine.findAdjacentTiles(board, coord).size();
	}
	
	@Test
	void test() {
		assertEquals(35, tileNumber, "There should be 2 neighbor tiles");
	}
}
