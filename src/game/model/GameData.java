package game.model;

/**
 * Stores a print of current game turn.
 * Values are represented in two-dimensional integer array.
 * This class also holds findMin and findMax utilities.
 * @author student
 */
public final class GameData {
	
	private Integer[][] board;
	private boolean gameOver;
	private Integer score;
	private Integer moves;
	private Integer index;
	
	public void setBoard(Integer[][] board) {
		this.board = board;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public void setMoves(Integer moves) {
		this.moves = moves;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getIndex() {
		return index;
	}

	public Integer[][] getBoard() {
		return board;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public Integer getScore() {
		return score;
	}

	public Integer getMoves() {
		return moves;
	}

	public GameData() {
	}
	
	public static Integer findMax(Integer[][] data) {
		
		int max = 0;
		for (int y = 0; y < Engine.numRows; y++)
	    {
	    	for	(int x = 0; x < Engine.numCols; x++) 
	    	{
	    		if (data[y][x] > max) {
	    			max = data[y][x];
	    		}
	    	}
	    }
		return max;
	}
	
	public static Integer findMin(Integer[][] data) {
		
		int min = Integer.MAX_VALUE;
		for (int y = 0; y < Engine.numRows; y++)
	    {
	    	for	(int x = 0; x < Engine.numCols; x++) 
	    	{
	    		if (data[y][x] < min) {
	    			min = data[y][x];
	    		}
	    	}
	    }
		return min;
	}
}
