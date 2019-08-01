package game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class copyBoardTest {

	static Engine engine;
	
	static int scoreBeforeLoad;
	static int scoreAfterLoad;
	
	static Integer[][] board;
	static Integer[][] copiedBoard;
	
	static int difference;
	
	@BeforeAll
	static void setUp() {
		Engine.numCols = 5;
		Engine.numRows = 5;
		
		Engine.turnArchive = new ArrayList<GameData>();
		engine = new Engine();
		
		board = engine.getCurrentState().getBoard();
		copiedBoard = engine.copyBoard(board);
		
		for (int y = 0; y < Engine.numRows; y++) {
			for (int x = 0; x < Engine.numCols; x++) {
				difference = board[y][x] - copiedBoard[y][x];
			}
		}
	}
	@Test
	void test() {
		assertEquals(0, difference, "There should be no difference in boards");
	}

}
