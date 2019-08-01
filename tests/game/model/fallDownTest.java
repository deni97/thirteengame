package game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class fallDownTest {

	static Engine engine;
	
	static ArrayList<Coord> coords;
	
	static int scoreBefore;
	static int scoreAfter;
	
	static Integer[][] board;
	
	@BeforeAll
	static void setUp() {
		Engine.numCols = 1;
		Engine.numRows = 6;
		
		Engine.turnArchive = new ArrayList<GameData>();
		engine = new Engine();
		
		coords = new ArrayList<Coord>();
		
		coords.add(new Coord(0, 2));
		
		coords.add(new Coord(0, 5));
		
		board = engine.getCurrentState().getBoard();
		
		board[4][0] = -1;
		board[1][0] = 0;
		
		engine.fallDown(board, coords, 6);
	}
	
	@Test
	void test() {
		assertEquals(-1, board[5][0], "-1 should go down one step");		
		assertEquals(0, board[3][0], "0 should go down two steps");
	}

}
