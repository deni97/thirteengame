package game;

/**
 * Class used in highscores. Holds basic game results and a path to the savefile 
 * for a quick load. Implements comparable interface to sort the results.
 * @author student
 */
public class Highscore implements Comparable<Highscore>{
	
	String name;
	int score;
	int moves;
	String path;
	
	public Highscore(String name, int score, int moves, String path) {
		this.moves = moves;
		this.score = score;
		this.name  = name;
		this.path  = path;
	}

	@Override
	public int compareTo(Highscore scr) {
		if ((score > scr.score) || 
				(score == scr.score && moves > scr.moves))  
			return 1; 
		else if (score == scr.score && moves == scr.moves)  
			return 0;  
		else  
			return -1;
	}
}
