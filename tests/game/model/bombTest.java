package game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import game.notEnoughException;

class bombTest {
	
	static Engine engine;
	
	//static ArrayList<Integer> columnBeforeBomb;
	//static ArrayList<Integer> columnAfterBomb;
	static Integer[] columnBeforeBomb;
	static Integer[] columnAfterBomb;
	
	static Integer[][] board;
	
	@BeforeAll
	static void setUp() {
		Engine.numCols = 1;
		Engine.numRows = 10;
		
		Engine.turnArchive = new ArrayList<GameData>();
				
		engine = new Engine();

		engine.getCurrentState().setMoves(1);
		
		columnBeforeBomb = new Integer[10];
		columnAfterBomb  = new Integer[10];
		
		board = engine.getCurrentState().getBoard();
		for	(int y = 0; y < Engine.numRows; y++) {
			columnBeforeBomb[y] = board[y][0];
	    }

		Engine.bombCost = 1;
		try {
			engine.bomb(new Coord(0, 9));
		} catch (notEnoughException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		board = engine.getCurrentState().getBoard();
		for	(int y = 0; y < Engine.numRows; y++) {
	    		columnAfterBomb[y] = board[y][0];
	    }
	}
	
	@Test
	void test() {
		assertNotEquals(columnBeforeBomb, columnAfterBomb, "Status quo should change after the bombing");
	}

}
