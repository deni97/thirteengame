package game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class undoTest {
	
	static Engine engine;
	
	static int difference;
	
	static int movesBefore;
	static int movesAfter;
	
	static Coord coord;
	
	static Integer[][] boardBefore;
	static Integer[][] boardAfter;
	
	@BeforeAll
	static void setUp() {
		
		Engine.numCols = 5;
		Engine.numRows = 5;
		
		Engine.turnArchive = new ArrayList<GameData>();
		engine = new Engine();
		
		Engine.undoCost = 1;

		boardBefore = engine.copyBoard(engine.getCurrentState().getBoard());
		
		loop:
		for (int y = 0; y < Engine.numRows; y++) {
			for (int x = 0; x < Engine.numCols; x++) {
				int size = Engine.findAdjacentTiles(boardBefore, new Coord(x, y)).size();
				if (size > 0) {
					coord = new Coord(x, y);
					break loop;
				}
			}
		}
		movesBefore = engine.getCurrentState().getMoves();
		
		engine.increment(coord);
		
		boardAfter = engine.copyBoard(engine.getCurrentState().getBoard());
		movesAfter = engine.getCurrentState().getMoves();
		
		for (int y = 0; y < Engine.numRows; y++) {
			for (int x = 0; x < Engine.numCols; x++) {
				difference = boardBefore[y][x] - boardAfter[y][x];
			}
		}
	}
	
	@Test
	void test() {		
		assertEquals(0, difference, "There should be no difference in boards");
		assertNotEquals(movesBefore, movesAfter, "Move currency should differ");
	}
}
