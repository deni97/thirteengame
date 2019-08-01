package game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class findScoreTileTest {
	static Engine engine;
	
	static Integer[][] board;
	static Coord coord;
	
	static int tileNumber;
	
	@BeforeAll
	static void setUp() {
		
		Engine.numCols = 50;
		Engine.numRows = 50;
		
		Engine.turnArchive = new ArrayList<GameData>();
		engine = new Engine();
		
		engine.getCurrentState().getBoard()[30][30] = 100;
		
		coord = engine.findScoreTile();
	}
	
	@Test
	void test() {
		assertEquals(30, coord.getX(), "X should be 30");
		assertEquals(30, coord.getY(), "Y should be 30");
	}
}
