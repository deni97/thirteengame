package game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class loadFromArchiveTest {

	static Engine engine;
	
	static GameData data;
	
	static int score;
	
	@BeforeAll
	static void setUp() {
		
		Engine.numCols = 5;
		Engine.numRows = 5;
		
		Engine.turnArchive = new ArrayList<GameData>();
		engine = new Engine();

		data = new GameData();
		data.setScore(1000);
		
		Engine.turnArchive.add(data);
		
		engine.loadFromArchive();
		
		score = engine.getCurrentState().getScore();
	}
	
	@Test
	void test() {		
		assertEquals(1000, score, "1000 should be loaded");
	}
}
