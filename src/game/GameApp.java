package game;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import game.model.Engine;
import game.model.GameData;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Main part of an app. Responsible for display.
 * 
 * @author student
 */
public class GameApp extends Application {

	public GridPane root_gridPane;
	public static GridPane board_gridPane;

	//public static ArrayList<GameData> turnArchive;
	
	// Color coefficient is used as a multiplier for value while determing the tile color
	private static int colorCoeff;
	// Starting point in the color hue circle
	private static int colorStart;
	
	static boolean bombFlag = false;
	
	public static TextField rowInput;
    
	public static TextField colInput;
	
	public static TextArea txtInfo;
	public static TextField saveNameInput;
	
	public static boolean stopAIFlag = false;
	public static Button startAIBtn;
	
	public static AIType AI = AIType.NONE;
	
	static Engine game;
	
	/**
	 * Initialises the UI
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
	    
		root_gridPane = new GridPane(); 
	    // UI column
        ColumnConstraints colConst2 = new ColumnConstraints();
        colConst2.setMinWidth(110);
        colConst2.setPrefWidth(110);
        root_gridPane.getColumnConstraints().add(colConst2);
        // Tile board column
        ColumnConstraints colConst3 = new ColumnConstraints();
        colConst3.setHgrow(Priority.ALWAYS);
        root_gridPane.getColumnConstraints().add(colConst3 );
        // UI column
        ColumnConstraints colConst4 = new ColumnConstraints();
        colConst4.setMinWidth(110);
        colConst4.setPrefWidth(110);
        root_gridPane.getColumnConstraints().add(colConst4);
        // Two rows
        RowConstraints rowConst2 = new RowConstraints();
        rowConst2.setPercentHeight(85.0);
        root_gridPane.getRowConstraints().add(rowConst2); 
        
        RowConstraints rowConst3 = new RowConstraints();
        rowConst3.setPercentHeight(15.0);
        root_gridPane.getRowConstraints().add(rowConst3); 
        // Board dimensions inputs
        Label lblRow = new Label("rows:");
        rowInput = new TextField();
        
        Label lblCol = new Label("columns:");
        colInput = new TextField();
        // Not letting anything but integers in
        UnaryOperator<Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([1-9][0-9]*)?")) { 
                return change;
            }
            return null;
        };
        
        rowInput.setTextFormatter(
        		new TextFormatter<Integer>(new IntegerStringConverter(), null, integerFilter));

        colInput.setTextFormatter(
        		new TextFormatter<Integer>(new IntegerStringConverter(), null, integerFilter));
        // Container for dimension inputs
        VBox renderInputBox = new VBox();
        renderInputBox.setSpacing(0);
        renderInputBox.getChildren().addAll(lblRow, rowInput, lblCol, colInput);
                
        Button btnRender = new Button("Render");
        btnRender.setOnAction(e -> {
        	// Stops the bot and resets action costs
        	stopAIFlag 	 	= true;
        	Engine.bombCost = 50;
        	Engine.undoCost = 20;
        	// Parse dimension inputs
        	String cols    = colInput.getText();
        	Engine.numCols = Integer.parseInt(cols);
        	
        	String rows    = rowInput.getText();
        	Engine.numRows = Integer.parseInt(rows);
        	
        	renderBoard();
		});
        // Container for alignment
        HBox renderBtnBox = new HBox();
        renderBtnBox.setAlignment(Pos.TOP_RIGHT);
        renderBtnBox.getChildren().add(btnRender);
        VBox renderContainer = new VBox(5);
        renderContainer.setAlignment(Pos.TOP_CENTER);
        renderContainer.getChildren().addAll(renderInputBox, renderBtnBox);
        
        // Game information
        
        Label lblInfo = new Label("Game info:");

        txtInfo = new TextArea();
        txtInfo.setEditable(false);
        txtInfo.setPrefHeight(165);
        txtInfo.setMinHeight(165);
        
        VBox infoBox = new VBox();
        infoBox.getChildren().addAll(lblInfo, txtInfo);
        
        Button bombBtn = new Button("Bomb");
        
        bombBtn.setStyle(
        	    "-fx-border-color: red; "
        	    + "-fx-font-size: 18;"
        	    + "-fx-border-insets: -5; "
        	    + "-fx-border-radius: 5;"
        	    + "-fx-border-width: 2;"
        	    + "-fx-padding: 5;"
        	);
        // Let player cancel the bomb action before clicking
        bombBtn.setOnAction((e) -> {
        	bombFlag = !bombFlag;
        });
        
        Button undoBtn = new Button("Undo");
        
        undoBtn.setStyle(
        	    "-fx-border-color: lightblue; "
        	    + "-fx-font-size: 18;"
        	    + "-fx-border-insets: -5; "
        	    + "-fx-border-radius: 5;"
        	    + "-fx-border-width: 2;"
        	    + "-fx-padding: 5;"
        	);
        
        undoBtn.setOnAction((e) -> GameController.undoClick());

        HBox actionBox = new HBox();
        HBox.setMargin(bombBtn, new Insets(5, 40, 5, 40));
        HBox.setMargin(undoBtn, new Insets(5, 40, 5, 40));
        actionBox.setAlignment(Pos.CENTER);
        actionBox.getChildren().addAll(bombBtn, undoBtn);
        root_gridPane.add(actionBox, 1, 1);
        
        Label lblAI = new Label("AI controls:");
        // Radio buttons for AI,
        // each hold contains enum user data
        final ToggleGroup group = new ToggleGroup();
                
        RadioButton rbRandom = new RadioButton("Random");
        rbRandom.setToggleGroup(group);
        rbRandom.setUserData(AIType.RANDOM);
        rbRandom.setAlignment(Pos.TOP_LEFT);

        RadioButton rbGold = new RadioButton("Scorer");
        rbGold.setToggleGroup(group);
        rbGold.setUserData(AIType.SCORER);
        rbGold.setAlignment(Pos.TOP_LEFT);
         
        RadioButton rbGreedy = new RadioButton("Consecutive");
        rbGreedy.setToggleGroup(group);
        rbGreedy.setUserData(AIType.CONSECUTIVE);
        rbGreedy.setAlignment(Pos.TOP_LEFT);

        VBox radioBox = new VBox();
        radioBox.setAlignment(Pos.TOP_LEFT);
        radioBox.setSpacing(5);
        radioBox.getChildren().addAll(rbRandom, rbGold, rbGreedy);
        // Change AI type on selection
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
        	public void changed(ObservableValue<? extends Toggle> ov,
        		Toggle old_toggle, Toggle new_toggle) {
        			if (group.getSelectedToggle() != null) {
        				AI = (AIType) group.getSelectedToggle().getUserData();
        			}
        	}
        });
        
        startAIBtn = new Button("START AI");
        startAIBtn.setOnAction(e -> {
        	stopAIFlag = false;
        	// Pass maximum number for random AI to strike and pass in thread sleep value
        	GameController.startAI(board_gridPane.getChildren().size(), 15);
		});
        
        Button stopAIBtn = new Button("STOP AI");
        stopAIBtn.setOnAction(e -> {
        	stopAIFlag = true;
		});
        // Container for alignment
        VBox AIBtnsBox = new VBox();
        AIBtnsBox.setAlignment(Pos.TOP_RIGHT);
        AIBtnsBox.setSpacing(5);
        AIBtnsBox.getChildren().addAll(startAIBtn, stopAIBtn);
        
        Label lblSaveName  = new Label("enter savename:");
        saveNameInput = new TextField();
        
        Button saveBtn = new Button("save");
        saveBtn.setOnAction((e) -> {
        	GameController.saveGame();
        });
        
        Button loadBtn = new Button("load");
        loadBtn.setOnAction((e) -> {
        	// Pass parameters to distinguish the call from highscores from that one
        	GameController.loadGame("", null);
        });
        
        HBox saveLoadBox = new HBox();
        saveLoadBox.setAlignment(Pos.CENTER);
        saveLoadBox.setSpacing(5);
        saveLoadBox.getChildren().addAll(saveBtn, loadBtn);
        
        VBox saveContainer = new VBox(1);
        saveContainer.getChildren().addAll(lblSaveName, saveNameInput, saveLoadBox);
        saveContainer.setAlignment(Pos.BOTTOM_CENTER);

        Button highscoresBtn = new Button("Highscores");
        
        highscoresBtn.setStyle(
        	    "-fx-border-color: lightgreen; "
        	    + "-fx-font-size: 14;"
        	    + "-fx-border-insets: -5; "
        	    + "-fx-border-radius: 5;"
        	    + "-fx-border-width: 2;"
        	    + "-fx-padding: 5;"
        	);
        
        highscoresBtn.setOnAction((e) -> GameController.showHighscores(primaryStage));
        
        HBox highscoresBtnContainer = new HBox();
        highscoresBtnContainer.setAlignment(Pos.CENTER);
        highscoresBtnContainer.getChildren().add(highscoresBtn);
        root_gridPane.add(highscoresBtnContainer, 2, 1);

        Region spacer1 = new Region();
        spacer1.setPrefHeight(1000);
        
        // Container for alignment
        VBox leftVBox = new VBox(20);
        leftVBox.setPadding(new Insets(50, 5, 50, 5));
        leftVBox.setAlignment(Pos.TOP_CENTER);
        leftVBox.getChildren().addAll(infoBox, spacer1, lblAI, radioBox, AIBtnsBox);
        
        root_gridPane.add(leftVBox, 0, 0);
        
        Region spacer2 = new Region();
        spacer2.setPrefHeight(1000);
        
        // Container for alignment
        VBox rightVBox = new VBox(20);
        rightVBox.setPadding(new Insets(50, 5, 50, 5));
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.getChildren().addAll(renderContainer, spacer2, saveContainer);

        root_gridPane.add(rightVBox, 2, 0);
        
	    board_gridPane = new GridPane();
	    
	    board_gridPane.setGridLinesVisible(false);
	    
	    board_gridPane.setHgap(0);
	    board_gridPane.setVgap(0);
	    
	    root_gridPane.add(board_gridPane, 1, 0);
	    
	    Scene scene = new Scene(root_gridPane, 750, 750);
	    primaryStage.setTitle("13!");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}	
	
	/**
	 * Initialises the board. Sets the constraints on the grid and fills it with buttons
	 * that go as tiles in the game.
	 * Background color of the buttons is based on the value of board-representaion of the button.
	 */
	public static void renderBoard() {
		
		int numCols = Engine.numCols;
		int numRows = Engine.numRows;
		
		Engine.turnArchive = new ArrayList<GameData>();
 	    game = new Engine();
 	    // Somewhat random colors on each render
 	    colorCoeff = ThreadLocalRandom.current().nextInt(10, 61);
 	    colorStart = ThreadLocalRandom.current().nextInt(0, 360);
 	    // Reset the board
	    board_gridPane.getChildren().clear();
	    board_gridPane.getColumnConstraints().clear();
	    board_gridPane.getRowConstraints().clear();
	    // Constrain
	    for (int i = 0; i < numCols; i++) {
	        ColumnConstraints colConst = new ColumnConstraints();
	        colConst.setPercentWidth(100.0 / numCols);
	        board_gridPane.getColumnConstraints().add(colConst);
	    }
	    for (int i = 0; i < numRows; i++) {
	        RowConstraints rowConst = new RowConstraints();
	        rowConst.setPercentHeight(100.0 / numRows);
	        board_gridPane.getRowConstraints().add(rowConst);         
	    }
	    
	    int count = 0;
	    Integer[][] board = game.getCurrentState().getBoard();
	    // Populate it with colored buttons
	    for (int y = 0; y < numRows; y++)
	    {
	    	for	(int x = 0; x < numCols ; x++) 
	    	{
	    		int value = board[y][x];
	            Button a = new Button(String.valueOf(value));
	            
	            final int cnt = count;
	            a.setOnAction((e) -> GameController.tileClick(cnt));
	            a.setMinSize(Double.MIN_VALUE, Double.MIN_VALUE);
	            a.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
	            
	            a.setBackground(btnBackground(value));
	            
	            board_gridPane.add(a, x, y);
	            count++;
	    	}
	    }
	    txtInfo.setText("Score:  0\nMoves: 0\n\nbomb: " + Engine.bombCost + "\nundo:  " + Engine.undoCost);
	}
	
	/**
	 * Updates the already-initialised board, sets the dispayed values and updates color
	 * of the tiles.
	 */
	public static void updateBoard() {
		
		int count = 0;
		
		Integer[][] board = game.getCurrentState().getBoard();
		// Update value, text and background of each button
	    for (int y = 0; y < Engine.numRows; y++)
	    {
	    	for	(int x = 0; x < Engine.numCols ; x++) 
	    	{
	    		int value = board[y][x];	    		
	            Button a = (Button) board_gridPane.getChildren().get(count);
	            
	            a.setText(String.valueOf(value));
	            a.setBackground(btnBackground(value));
	            
	            count++;
	    	}
	    }
	}
	
	/**
	 * Updates the info-box, displaying current game state: score, moves, action costs, also
	 * displays the game-over message.
	 */
	public static void updateInfo() {
		
		GameData state = game.getCurrentState();
		// Update score, moves, etc.
		// Append a conditional optional gameover message
		txtInfo.setText("Score:  " + state.getScore().toString() + 
				"\nMoves: " + state.getMoves().toString() + 
				"\n\nbomb: " + Engine.bombCost + "\nundo:  " + Engine.undoCost
				+ (state.isGameOver() ? "\n\n GAME\n  OVER" : ""));
	}
	
	/**
	 * A helper method that returns a background for the tiles based on input values.
	 * Color format is hsb for the ease of binding the change in color with increment
	 * in value: value times colorCoefficient rotatoes around the hue-circle.
	 * @param value input value of the tile
	 * @return background object with colored fill
	 */
	private static Background btnBackground(int value) {
		// Get hsb color for button background
		return new Background(new BackgroundFill(Color.hsb((double)(colorStart + value * colorCoeff), 1.0, 1.0, 0.5), null, null));
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}