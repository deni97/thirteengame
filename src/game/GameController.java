package game;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import game.model.Coord;
import game.model.Engine;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 * Ties together the main view component and the model of the game.
 * Contains, in addition to main functionality,
 *  AI-methods, save-load and highscore features.
 *  
 * @author student
 */
public class GameController {
	/**
	 * Main click method. Checks for the bombflag and executes the intended action.
	 * If it is invoked from any thread other than the main application thread,
	 * UI is not updated. The AI runs at another thread, 
	 * hence the Platform.runLater method to allow for updates.
	 * @param id an id of the clicked button
	 */
	public static void tileClick(int id) {
		try {
			// If click is not meant to bomb, increment
			if (GameApp.bombFlag == false) {
				GameApp.game.increment(idToCoord(id));
			} else {
				// Bomb the tile, reset the flag
				GameApp.bombFlag = false;
				GameApp.game.bomb(idToCoord(id));
			}
			// For there is an AI, there is another thread
			// UI updates in javafx require main application thread
			// those lines handle those tasks to that special thread
			Platform.runLater(new Runnable(){
				@Override public void run() {
					// Update score etc.
					GameApp.updateBoard();
					GameApp.updateInfo();
				}
			});
		} catch (notEnoughException e) {
			// If bomb needs more moves, point that out
			GameApp.txtInfo.appendText("\n\nNot\nenough\nmoves");
		}
	}
	
	/**
	 * Ties undo action and button.
	 * If undo indicates the lack of resources by its exception the
	 * game info text reflects that.
	 */
	public static void undoClick() {
		try {
			GameApp.game.undo();
			GameApp.updateBoard();
			GameApp.updateInfo();
		} catch (notEnoughException e) {
			// If undo needs more moves, point that out
			GameApp.txtInfo.appendText("\n\nNot\nenough\nmoves");
		}
	}
	
	/**
	 * Initiates the AIs based on the current selection.
	 * @param upperBound helps with the scoring of random AI
	 * @param sleep sleep parameter of the Thread, lower it down at CPU's risk
	 */
	public static void startAI(int upperBound, int sleep) {
		// New thread to allow simultaneous cooperation of a bot and human
		// Also while main thread is occupied there is no way to stop the robot
		new Thread(() -> {
			// Stop the robot if it lost you a game
			if (GameApp.game.getCurrentState().isGameOver()) {
				GameApp.stopAIFlag = true;
			}
			// Game is not over and stopAI button was not pressed
			while (!GameApp.game.getCurrentState().isGameOver() && !GameApp.stopAIFlag) {
				try {
					// Without sleep thread works way too hard
					// and it is impossible to follow
					Thread.sleep(sleep);
					int id = 0;
					// Checking radio buttons for selection
					// Getting an id from the right bot
					switch (GameApp.AI) {
						case RANDOM:
							id = random(0, upperBound);
							break;
						case SCORER:
							id = digGold();
							break;
						case CONSECUTIVE:
							id = fatStack();
							break;
						case NONE:
							return;
					}
					// Get the button to click by an id
					Button btn = (Button) GameApp.board_gridPane.getChildren().get(id);
					// Click the button
					btn.fire();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	/**
	 * Consecutive AI's view on the problem.
	 * Finds the largest tile-neighborhood and rats the id.
	 * @return an id of the hood
	 */
	
	private static int fatStack() {
		
		Engine game = GameApp.game;
		Integer[][] board = game.getCurrentState().getBoard();

		int max = 0;
		Coord maxCoord = new Coord(0, 0);
		// Checking for stopAI btn clicks on each iteration for responsiveness
		for (int y = 0; y < Engine.numRows && !GameApp.stopAIFlag; y++) {
			for (int x = 0; x < Engine.numCols && !GameApp.stopAIFlag; x++) {
				// Get the size of the neighborhood
				int size = Engine.findAdjacentTiles(board, new Coord(x, y)).size();
				if (size > max) {
					// Store the coordinate if it is the biggest
					maxCoord = new Coord(x, y);
					max = size;
				}
			}
		}
		// Give back a number
		return coordToId(maxCoord);
	}
	/**
	 * Randomly tries to find an incrementable tile.
	 * @param lowerBound inclusive
	 * @param upperBound non-inclusive
	 * @return an id of a tile to click on
	 */
	
	private static int random(int lowerBound, int upperBound) {
		
		int id = 0;
		Engine game = GameApp.game;
		Integer[][] board = game.getCurrentState().getBoard();
		// Try to find a place to click again and again before success or not
		while (!GameApp.stopAIFlag && !game.getCurrentState().isGameOver()) {
			// Get random number that represents an id of the button
			id = ThreadLocalRandom.current().nextInt(lowerBound, upperBound);
			// Check for clickability and return on success
			if (Engine.checkAdjacentTiles(board, idToCoord(id)).size() > 0) {
				return id;
			}
		}
		return id;
	}
	/**
	 * Scorer AI main tool.
	 * Finds the largest tile by value, checks if it is incrementable. 
	 * If not, AI proceeds to sniff the area around it in an ever-expanding square area 
	 * around it until it finds a tile to click on. Rinse, repeat.
	 * @return an id of a tile to click on
	 */
	
	private static int digGold() {
		
		int id = 0;
		Engine game = GameApp.game;
		Integer[][] board = game.getCurrentState().getBoard();
		// Find high score tile
		// If there is more than one, the one that appears first 
		// in a y-x double for loop wins
		Coord goldCoord = game.findScoreTile();
		// That algorithm checks a square area around the high score tile for clickability
		// starting from that tile itself growing by 1 in two directions thus incrementing the loop by two		
		// It goes on until it finds a place to click or until an explicit order to stop
		
		// Length of a side
		for (int i = 0; !GameApp.stopAIFlag; i += 2) {
			// Iterates through an inner square
			for (int y = 0; y <= i && !GameApp.stopAIFlag; y++) {
				for (int x = 0; x <= i && !GameApp.stopAIFlag; x++) {
					// Convert inner square coordinates in the actual ones
					int checkX = goldCoord.getX() - i/2 + x;
					int checkY = goldCoord.getY() - i/2 + y;
					Coord checkCoord = new Coord(checkX, checkY);
					// Check if the coordinate is still pointing at the board
					if (checkX >= 0 && checkX < Engine.numCols &&
							checkY >= 0 && checkY < Engine.numRows) {
						// Any neighbors? Return
						if (Engine.checkAdjacentTiles(board, checkCoord).size() > 0) {
							return coordToId(checkCoord);
						}
					} else {
						// Check the next one
						continue;
					}
				}
			}
		}
		return id;
	}
	
	/**
	 * A helper method that converts an id of a button into the coordinates of the board.
	 * @param id an id of the button
	 * @return a board coordinate
	 */
	private static Coord idToCoord(int id) {		
		
		int count = 0;
		// Count one by one
		for (int y = 0; y < Engine.numRows; y++) {
			for (int x = 0; x < Engine.numCols; x++) {
				if (count == id) {
					return new Coord(x, y);
				}
				count++;
			}
		}
		return new Coord(0, 0);
	}
	
	/**
	 * A helper method that converts the coordinates of the board into an id of a button
	 * @param coord input board coordinate
	 * @return an id of the button
	 */
	private static int coordToId(Coord coord) {		
		
		int id = 0;
		// Check each one for the value
		for (int y = 0; y < Engine.numRows; y++) {
			for (int x = 0; x < Engine.numCols; x++) {
				if (x == coord.getX() && y == coord.getY()) {
					return id;
				}
				id++;
			}
		}
		return id;
	}	

	/**
	 * Save game functionality.
	 * Stops the AI, gets a name from input. Puts all the required data into Save object,
	 * encodes it into an .xml file that contains input name, score and move count.
	 * Saves .xml file in user main folder, .thirteen/saves directory.
	 * Creates the directory if it doesn't exist.
	 */
	public static void saveGame() {
		
		Engine game = GameApp.game;
		
		Integer moves = game.getCurrentState().getMoves();
		Integer score = game.getCurrentState().getScore();
		// Get file name
		// there is no safety check
		String gameName = GameApp.saveNameInput.getText();
		// Stop the bot before saving
		GameApp.stopAIFlag = true;
		// Put relevant data inside the save
		Save save = new Save();
		save.setNumCols(Engine.numCols);
		save.setNumRows(Engine.numRows);
		save.setTurnArchive(Engine.turnArchive);
		save.setBombCost(Engine.bombCost);
		save.setUndoCost(Engine.undoCost);
		// Get directory or create if it doesn't exist
		File recordsDir = new File(System.getProperty("user.home"), ".thirteen/saves");
		 if (!recordsDir.exists()) {
			 recordsDir.mkdirs();
		 }
		// Create filename, store score and moves in it
		final String SERIALIZED_FILE_NAME = recordsDir.getAbsolutePath() + "\\" +
		 gameName + "_" + score.toString() + "_" + moves.toString() + ".xml";
		// Try to write
		XMLEncoder encoder = null;
		// It takes its time
		try {
			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(SERIALIZED_FILE_NAME)));
			encoder.writeObject(save);
		} catch (FileNotFoundException fileNotFound) {
			System.out.println("ERROR: While Creating the File " + SERIALIZED_FILE_NAME + " .xml");
			return;
		}
		encoder.close();
	}
	
	/**
	 * Restore game functionality. 
	 * If called from the highscores to load, uses its path.
	 * Else lets the user choose using the explorer.
	 * @param path file path or ""
	 * @param dialog modal window if present
	 */
	public static void loadGame(String path, Stage dialog) {
		
		String SERIALIZED_FILE_NAME = "";
		// Checking if it is loaded from highscores or from the main window
		if (path == "") {
			FileChooser fileChooser = new FileChooser();
			// Get directory or create if it doesn't exist
			File recordsDir = new File(System.getProperty("user.home"), ".thirteen/saves");
			if (! recordsDir.exists()) {
				recordsDir.mkdirs();
			}
			fileChooser.setInitialDirectory(recordsDir);
			fileChooser.setTitle("Open Save File");
			// Show all .xml
			fileChooser.getExtensionFilters().addAll(
					new ExtensionFilter("Saves", "*.xml"));
			File selectedFile = fileChooser.showOpenDialog(null);
			// If something was chosen get its path
			if (selectedFile != null) {
				SERIALIZED_FILE_NAME = selectedFile.getAbsolutePath();
			}
		} else {
			// If game is loaded from highscores get path straight
			SERIALIZED_FILE_NAME = path;
		}
		// Decode
		XMLDecoder decoder = null;
		try {
			decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(SERIALIZED_FILE_NAME)));
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File " + SERIALIZED_FILE_NAME + ".xml not found");
			return;
		}
		// Stop the ai
		GameApp.stopAIFlag = true;
		Save save;
		// Get Save object
		try {
			save = (Save) decoder.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			decoder.close();		
			return;
		}
		decoder.close();	
		
		Integer numRows = save.getNumRows();
		Integer numCols = save.getNumCols();
		// Load current state from save
		Engine.numRows = numRows;
		Engine.numCols = numCols;
		
		GameApp.renderBoard();
		
		Engine.turnArchive = save.getTurnArchive();
		
		GameApp.game.loadFromArchive();
				
		GameApp.rowInput.setText(numRows.toString());
		GameApp.colInput.setText(numCols.toString());
		
		Engine.bombCost = save.getBombCost();
		Engine.undoCost = save.getUndoCost();
		
		GameApp.updateInfo();
		// Close the modal if request came from highscores
		if (dialog != null) {
			dialog.close();
		}
	}

	/**
	 * Initiates a modal window that displays highscores.
	 * High scores are 10 save files with the best results. Since highscores are save files,
	 * there is a feature to load the high score game.
	 * Finds all files in saves directory, gets game information based on the generated save file,
	 * puts it in the list. 
	 * Sorts the lists, initiates the modal window.
	 * @param primaryStage main stage to set the owner of the modal
	 */
	public static void showHighscores(Stage primaryStage) {
		
		List<Highscore> scores = new ArrayList<Highscore>();
		// Get filenames
		File[] files = new File(System.getProperty("user.home"), ".thirteen/saves").listFiles();
		// Get scores and moves from file names
		for (File file : files) {
		    if (file.isFile()) {
		    	String fileName = file.getName();
		    	String fullPath = file.getAbsolutePath();
		    	String pattern = "(.*?)_(\\d+)_(\\d+)";
		    	
		        Pattern r = Pattern.compile(pattern);
		        Matcher m = r.matcher(fileName);
		        // Add scores to list
		        if (m.find()) {
		        	Highscore highscore = new Highscore(m.group(1), 
		        			Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), fullPath);
		        	scores.add(highscore);
		        } else {
		            System.out.println("No match");
		        }
		    }
		}
		// Sort highscores
		Collections.sort(scores, Collections.reverseOrder());
		// Align container
		VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.TOP_CENTER);
        // Initialize modal
		Stage dialog = new Stage();
		// For 10 first high scores or less if there is not enough
		for (int i = 0; i < 10 && i < scores.size() - 1; i++) {
			// Get a score
			Highscore highscore = scores.get(i);
			// Containers are for alignment purposes
			HBox scoreContainer = new HBox();
			HBox scoreBox = new HBox();
			scoreBox.setAlignment(Pos.CENTER_LEFT);
			Label lblScore = new Label((i + 1) + ". " + "Score: " + highscore.score + 
					" Moves: " + highscore.moves);

			scoreBox.getChildren().add(lblScore);
			
			Label nameLbl = new Label("Name: " + highscore.name);
						
			Button loadScoreBtn = new Button("Load");
			// Pass loadGame required parameters
			loadScoreBtn.setOnAction(e -> {
	        	loadGame(highscore.path, dialog);
			});
			
			HBox nameBox = new HBox();
			nameBox.setAlignment(Pos.CENTER_LEFT);
			nameBox.getChildren().add(nameLbl);
				        
	        HBox loadBox = new HBox();
	        loadBox.getChildren().addAll(loadScoreBtn);
	        loadBox.setAlignment(Pos.CENTER_RIGHT);
			scoreContainer.getChildren().addAll(scoreBox, nameBox, loadBox);
			
			HBox.setHgrow(scoreBox, Priority.ALWAYS);
			HBox.setHgrow(nameBox, Priority.ALWAYS);
			// Add a high score entry to the list
			vbox.getChildren().add(scoreContainer);
		}
		Button closeModalBtn = new Button("Close");
		closeModalBtn.setAlignment(Pos.BOTTOM_RIGHT);
		closeModalBtn.setOnAction(e -> {
			dialog.close();
		});
		
		vbox.getChildren().add(closeModalBtn);

		Scene scene = new Scene(vbox, 300, 380);
		dialog.setScene(scene);
		dialog.sizeToScene();
		dialog.setTitle("Highscores");
		
		dialog.initOwner(primaryStage);
		dialog.initModality(Modality.APPLICATION_MODAL); 
		dialog.showAndWait();
	}
}
