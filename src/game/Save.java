package game;

import java.util.ArrayList;

import game.model.GameData;

/**
 * A class that holds all the game-relevant information for further serialization
 * and storing in the file.
 * Functions as a JavaBean class to qualify for XMLEncoding\Decoding.
 * @author student
 */
public class Save implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	private int numCols = 0;
	private int numRows = 0;
	
	private ArrayList<GameData> turnArchive;

	private int bombCost = 50;
	public int getNumCols() {
		return numCols;
	}

	public void setNumCols(int numCols) {
		this.numCols = numCols;
	}

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public ArrayList<GameData> getTurnArchive() {
		return turnArchive;
	}

	public void setTurnArchive(ArrayList<GameData> turnArchive) {
		this.turnArchive = turnArchive;
	}

	public int getBombCost() {
		return bombCost;
	}

	public void setBombCost(int bombCost) {
		this.bombCost = bombCost;
	}

	public int getUndoCost() {
		return undoCost;
	}

	public void setUndoCost(int undoCost) {
		this.undoCost = undoCost;
	}

	private int undoCost = 20;
	// Empty constructor to go as a JavaBean convention class
	public Save() {
	}
}
