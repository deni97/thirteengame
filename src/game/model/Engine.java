package game.model;

import game.notEnoughException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Game engine. Holds current state of the game and contains its logic.
 * 
 * @author student
 */
public class Engine {

	public static ArrayList<GameData> turnArchive;
	
	public static int numCols;
	public static int numRows;

	public static int bombCost = 50;
	public static int undoCost = 20;
	
	// Initialize create new state on Engine initialization
	private GameData currentState = createNewState();
		
	public GameData getCurrentState() {
		return currentState;
	}

	/**
	 * Initializes engine and sets initial gamestate 
	 * based on board dimensions.
	 * 
	 * @return initial gamestate
	 */
	private GameData createNewState() {
		
		Integer[][] board = new Integer[numRows][numCols];
		// Fill the board with initial values
		for (int y = 0; y < numRows; y++)
	    {
	    	for	(int x = 0; x < numCols; x++) 
	    	{
	    		board[y][x] = randomValue(7);
	    	}
	    }
		// Create new game state
		GameData game = new GameData();
		
		game.setBoard(board);
		// Check if the game is already lost
		game.setGameOver(checkGameOver(board));
		game.setScore(GameData.findMax(board));
		game.setMoves(0);
		game.setIndex(0);
		// Archive the state
		turnArchive.add(game);
		
		return game;
	}
	
	/**
	 * Random value for a tile, implements binomial distribution.
	 *  
	 * @param score high score, used as the upper bound
	 * @return random value
	 */
	static public int randomValue(int score) {
		// Gap between the values
		int width = 8;
		// Max is needed to not go below 1
		int lowerBound = Math.max(1, score - width);
		double p = 0.5;
		// Increments from lower bound with p probability, upperbound non-inclusive
		for(int i = lowerBound; i < score - 1; i++) {
			if (Math.random() < p)
	  	    	lowerBound++;
		}
	  	return lowerBound;
	}
	
	/**
	 * Checks if the game is finished i.e. if no tile on the board
	 * has an adjacent neighbor of the same value.
	 * 
	 * @param board 2-d list that represents the board
	 * @return boolean isGameOver
	 */
	static boolean checkGameOver(Integer[][] board) {
		// For each row
		for (int y = 0; y < numRows; y++)
	    {
			// For each column
	    	for	(int x = 0; x < numCols; x++) 
	    	{
	    		// Check if there is any neighboring same value tile
	    		if (checkAdjacentTiles(board, new Coord(x, y)).size() != 0) {
	    			return false;
	    		}
	    	}
	    }
		return true;
	}
	
	/**
	 * Bomb action. Checks if there is enough move-currency, then, 
	 * if there is - replaces the tile, updates current state and
	 * turn archive accordingly.
	 * 
	 * @param coord x and y of a tile that is to be removed
	 * @throws notEnoughException not enough move-currency
	 */
	public void bomb(Coord coord) throws notEnoughException {
		
		Integer[][] board = copyBoard(currentState.getBoard());
		// Arraylist to use fallDown API
		ArrayList<Coord> markedCoords = new ArrayList<Coord>();
		markedCoords.add(coord);
		
		int score = currentState.getScore();
		int moves = currentState.getMoves();
		int index = currentState.getIndex();
		
		if (moves >= bombCost) {
			// Drop the tiles
			board = fallDown(board, markedCoords, score);
			// Update game state
			currentState = new GameData();
			currentState.setBoard(board);
			currentState.setGameOver(checkGameOver(board));
			currentState.setScore(score);
			currentState.setMoves(moves - bombCost);
			currentState.setIndex(index + 1);
			// Store game state
			turnArchive.add(currentState);
			// Double the cost
			bombCost *= 2;
		} else {
			// This exception indicates the need to append text to game info
			throw new notEnoughException();
		}
	}
	
	/** 
	 * Undo action. Checks if there is enough move-currency, then, 
	 * if there is - gets previous gameState from the archive,
	 * stores all its content but the moves in the current state.
	 * Removes 2 last actions from the archive to achieve continuous
	 * undo-ing.
	 *
	 * @throws notEnoughException not enough move-currency
	 */
	public void undo() throws notEnoughException {
		
		int moves = currentState.getMoves();
		
		if (moves >= undoCost) {
			int index = currentState.getIndex();
			// Get previous game state from the archive
			GameData prevTurn = turnArchive.get(index - 1);

			Integer[][] board = copyBoard(prevTurn.getBoard());
			// Update current state
			currentState = new GameData();
			currentState.setBoard(board);
			currentState.setGameOver(prevTurn.isGameOver());
			currentState.setScore(prevTurn.getScore());
			currentState.setMoves(moves - undoCost);
			currentState.setIndex(index - 1);
			// Remove 2 states from atchive because previous turn is overwritten
			turnArchive.remove(index);
			turnArchive.remove(index - 1);
			// Store state in the archive
			turnArchive.add(currentState);
			// Double the cost
			undoCost *= 2;
		} else {
			// This exception indicates the need to append text to game info
			throw new notEnoughException();
		}
	}
	
	/**
	 * Main game mechanic. Takes board coordinate of a click,
	 * checks for its adjacent equal-value tiles, if there is any,
	 * then checks for their adjacent tiles and increments 
	 * the clicked one while removing the others and initiating the "fall".
	 * Each time a new high score is reached currently smallest by the value
	 * tile is replaced.
	 * 
	 * Updates currentState and stores it in the archive.
	 * 
	 * @param coord board-coordinate of the click
	 */
	public void increment(Coord coord) {
		// Return if no increment is possible
		if (currentState.isGameOver()) {
			return;
		}

		Integer[][] board = copyBoard(currentState.getBoard());
		
		// Return if there is no nearby same value tiles
		ArrayList<Coord> coords = findAdjacentTiles(board, coord);
		if (coords.size() == 0) {
			return;
		}
		// Store current maximum
		int oldScore = GameData.findMax(board);
		// Increment the clicked tile
		board[coord.getY()][coord.getX()]++;

		int score = GameData.findMax(board);
		int lowest = GameData.findMin(board);
		// Remove the lowest current present value if a new high score is reached
		if (score > oldScore) {
			for (int y = 0; y < numRows; y++)
		    {
		    	for	(int x = 0; x < numCols; x++) 
		    	{
		    		int value = board[y][x];
		    		if (value == lowest) {
			    		board[y][x] = randomValue(score);
		    		}
		    	}
		    }
		}
		// Drop the tiles
		board = fallDown(board, coords, score);
		
		int moves = currentState.getMoves();
		int index = currentState.getIndex();
		// Update current state
		currentState = new GameData();
		currentState.setBoard(board);
		currentState.setGameOver(checkGameOver(board));
		currentState.setScore(GameData.findMax(board));
		currentState.setMoves(moves + 1);
		currentState.setIndex(index + 1);
		// Store it in the archive
		GameData archiveItem = new GameData();
		archiveItem.setBoard(currentState.getBoard());
		archiveItem.setGameOver(currentState.isGameOver());
		archiveItem.setScore(currentState.getScore());
		archiveItem.setMoves(currentState.getMoves());
		archiveItem.setIndex(currentState.getIndex());
		turnArchive.add(archiveItem);
	}
	
	/**
	 * Implements the "fall". Removes tiles at input coordinates, then 
	 * all tiles "move" down as far as possible. After than the gaps
	 * are filled with random values.
	 * 
	 * @param board 2-d list that represents the board
	 * @param coords all coordinates marked for removal 
	 * @param score current high score
	 * @return updated board
	 */
	Integer[][] fallDown(Integer[][] board, ArrayList<Coord> coords, int score) {
		// Marked for replacement
		ArrayList<Coord> markedCoords = new ArrayList<Coord>();
		// Works on each column of the marked coordinates
		Set<Integer> columns = new HashSet<Integer>();
		
		for (Coord tile : coords) {
			columns.add(tile.getX());
		}
		// Dropping column after column as they are indifferent towards each other
		for (int x : columns) {
			List<Integer> rows = new ArrayList<Integer>();
			// Get all used rows of the column
			for (Coord tile : coords) {
				if (tile.getX() == x) {
					rows.add(tile.getY());
				}
			}
			// Sort it
			Collections.sort(rows);
			// And move down from the top, dragging the values down
			for (int row : rows) {
				for (int i = row; i > 0; i--) {
					board[i][x] = board[i - 1][x];
				}
			}
			// We need to place the exact number of previously removed coordinates up top
			for (int i = 0; i < rows.size(); i++) {
				markedCoords.add(new Coord(x, i));
			}
		}
		// Get a random value for each coordinate
		for (Coord tile : markedCoords) {
			board[tile.getY()][tile.getX()] = randomValue(score);
		}
		return board;
	}
	
	/**
	 * A method that finds all neighboring tiles with the same value, 
	 * i.e. all tiles around input coordinate eligible to removal.
	 * 
	 * @param board 2-d list that represents the board
	 * @param coord coordinate of a tile to search from
	 * @return list of neighboring coordinates
	 */
	public static ArrayList<Coord> findAdjacentTiles(Integer[][] board, Coord coord) {
		
		ArrayList<Coord> coords = new ArrayList<Coord>();
		// Check around the coordinate and push them in the list
		for (Coord tile : checkAdjacentTiles(board, coord)) {
		    coords.add(tile);
		}
		int count;
		// At least once
		do {
			count = 0;
			int size = coords.size();
			// For all the values in the list
			for (int i = 0; i < size; i++) {
				Coord tile = coords.get(i);
				// Try to find neighboring tiles
				for (Coord tile1 : checkAdjacentTiles(board, tile)) {
					// And if it is not in the list and it is not the input coordinate
					if (coords.indexOf(tile1) == -1 && !tile1.equals(coord)) {
						// Add it it the list
						coords.add(tile1);
						count++;
						// Update the list size to allow the outer for loop iterate on it
						size = coords.size();
				    }
				}
			}
		// Continue if at least one tile was added
		} while (count > 0);
		return coords;
	}
	
	/**
	 * A helper method used to find adjacent tiles around a single coordinate.
	 * @param board 2-d list that represents the board
	 * @param coord coordinate of a tile to search from
	 * @return list of neighboring coordinates
	 */
	public static ArrayList<Coord> checkAdjacentTiles(Integer[][] board, Coord coord) {
		
		ArrayList<Coord> coords = new ArrayList<Coord>();
		
		int x = coord.getX();
		int y = coord.getY();
		int value = board[y][x];
		// Checking the outskirts for equal values while minding the gap
		// Bottom
		if (y + 1 < numRows) {
			if (board[y + 1][x] == value) {
				coords.add(new Coord(x, y + 1));
			}
		}
		// Top
		if (y - 1 >= 0) {
			if (board[y - 1][x] == value) {
				coords.add(new Coord(x, y - 1));
			}
		}
		// Right
		if (x + 1 < numCols) {
			if (board[y][x + 1] == value) {
				coords.add(new Coord(x + 1, y));
			}
		}
		// Left
		if (x - 1 >= 0) {
			if (board[y][x - 1] == value) {
				coords.add(new Coord(x - 1, y));
			}
		}
		return coords;
	}
	
	/**
	 * A helper method that makes a clone of a board, allows 
	 * to pass/store the values in the board instead of its reference.
	 * 
	 * @param board 2-d list that represents the board
	 * @return a copy of input
	 */
	Integer[][] copyBoard(Integer[][] currentBoard) {
		
		Integer[][] board = new Integer[numRows][numCols];
		// Store values into a freshly created two-dimensional array
		for (int y = 0; y < numRows; y++)
	    {
	    	for	(int x = 0; x < numCols; x++) 
	    	{
	    		board[y][x] = currentBoard[y][x];
	    	}
	    }
		return board;
	}
	
	/**
	 * A method that returns the board coordinates of
	 * the high score tile.
	 * Used as an API for scorer AI.
	 * 
	 * @return a coordinate of the high score tile
	 */
	public Coord findScoreTile() {
		
		int max = 0;
		Coord coord = new Coord(0, 0);
		
		Integer[][] board = currentState.getBoard();
		// Iterate through rows and columns
		for (int y = 0; y < numRows; y++)
	    {
	    	for	(int x = 0; x < numCols; x++) 
	    	{
	    		if (board[y][x] > max) {
	    			// Save current maximum and coord just in case
	    			max = board[y][x];
	    			coord = new Coord(x, y);
	    		}
	    	}
	    }
		return coord;
	}

	/**
	 * Updates current gamestate with the last entry from the archive.
	 */
	public void loadFromArchive() {
		// Get last item
		currentState = turnArchive.get(turnArchive.size() - 1);
	}
}
