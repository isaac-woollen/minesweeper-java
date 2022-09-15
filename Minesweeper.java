// Isaac Woollen | CSCI-3063 029 | 807007246
// Minesweeper | Class Project

// Minesweeper Class

import java.awt.*; // For awt package
import javax.swing.*; // For swing package
import java.awt.event.*; // For awt event package
import java.util.*; // For java util packagae
import javax.swing.Timer; // For Timer
import javax.sound.sampled.*; // For Audio Output
import java.io.*; // For file input

/**
	The Minesweeper class (extends JFrame Class)
	runs a minesweeper game using java swing
*/

public class Minesweeper extends JFrame {
	private final int WINDOW_WIDTH = 800; // Holds Window Width
	private final int WINDOW_HEIGHT = 1000; // Holds Window Height
	private JMenuBar menuBar; // Holds menu bar
	private int mx; // Holds current x location of cursor
	private int my; // Holds current y location of cursor
	private int rows; // Holds number of rows
	private int columns; // Holds number of columns
	private int spacing; // Holds spacing between squares
	private int size; // Holds size of squares
	private int xCalibration; // Holds x calibration for cursor selection over squares
	private int yCalibration; // Holds y calibration for cursor selection over squares
	private int numOfmines; // Holds number of mines
	private boolean[][] mines; // Holds location of mines
	private int[][] neighbors; // Holds how many neighboring squares have mines for each square
	private boolean[][] flagged; // Holds location of squares that are flagged
	private boolean[][] revealed; // Holds location of squares that are revealed
	private boolean[][] misFlagged; // Holds location of squares that were wrongly flagged
	private int currentMode; // Holds current game mode as integer
	private Timer timer; // Holds timer object for stopwatch
	private int time; // Holds time limit for current game
	private boolean gameOn; // Holds game status of on or off
	private boolean gameOver; // Holds game status of over or not
	private int currentTime; // Holds current time
	private Clip clip; // Holds Clip object for playing explosion sound effect

	/**
		Minesweeper Constructor
	*/

	public Minesweeper() {
		this.setTitle("Minesweeper"); // Sets title of JFrame
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT); // Sets window size
		this.setResizable(false); // Set to non-resizable window
		this.setLayout(null); // Sets layout to null
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Sets default closing operation

		buildMenuBar(); // Builds menu bar
		buildExplosionSound(); // Builds explosion sound effect

		this.setJMenuBar(menuBar); // Adds menu bar to Minesweeper window

		resetGame(0); // Sets default game to 0 (beginner)

		Pane pane = new Pane(); // Initializes pane

		this.setContentPane(pane); // Sets pane to content pane
		this.setVisible(true); // Displays Minesweeper game

		Move move = new Move(); // Creates move object
		this.addMouseMotionListener(move); // Adds move object to Minesweeper

		Click click = new Click(); // Creates click object 
		this.addMouseListener(click); // Adds click object to Minesweeper

	}

	/**
		The Pane Class (extends JPanel class) is
		responsible for displaying all content of
		the minesweeper game.
	*/

	public class Pane extends JPanel {
		
		/**
			The paintComponent method using graphics to
			dipslay all content of game based on the current
			state of the game.
			@param g holds Graphics object
		*/

		public void paintComponent(Graphics g) {

			g.setColor(new Color(200, 200, 200)); // Sets Background color
			g.fillRect(0, 0, 800, 1000); // Displays background

			int digitSpace = 700; // Holds x location of stopwatch
			int temp = currentTime; // Temporary int to keep track of each digit of time

			// For 3 times, display current time in seconds
			for(int t = 0; t < 3; t++) {
				String filename = "time" + Integer.toString(temp%10) + ".png"; // Creates string to hold filename of image
				ImageIcon timeDigit = new ImageIcon("Images\\" + filename); // Creates new icon with image from filename
				timeDigit.paintIcon(this, g, digitSpace, 50); // Displays image, which is a digit of time
				temp /= 10; // Divides temp by 10 for next digit
				digitSpace -= 50; // Moves digit space back 50px
			}

			// If Time is up
			if(currentTime == time) {
				ImageIcon timesUp = new ImageIcon("Images\\TimesUp.png"); // Creates icon to display Time's Up
				timesUp.paintIcon(this, g, 600, 50); // Displays "Time's Up" over timer
				setLose(); // Sets game to lose
			}

			// For each row
			for(int i = 0; i < rows; i++) {
				// For each column
				for(int j = 0; j < columns; j++) {

					// If revealed
					if(revealed[i][j]) {
						// If is a mine
						if(mines[i][j]) {
							ImageIcon img = new ImageIcon("Images\\mine" + Integer.toString(currentMode) + ".gif"); // Create icon to display mine explosion
							img.paintIcon(this, g, 120 + spacing + (i*(size+(spacing*2))), 150 + spacing + (j*(size+(spacing*2)))); // Displays mine explosion

							// If the game is not over
							if(!gameOver) {
								setLose(); // Set game to lose
							}
						}
						// If not mine
						else {
							String filename = Integer.toString(currentMode) + Integer.toString(neighbors[i][j]) + ".png"; // Creates string to hold filename of image
							ImageIcon img = new ImageIcon("Images\\" +filename); // Create icon to display amount of mines around
							img.paintIcon(this, g, 120 + spacing + (i*(size+(spacing*2))), 150 + spacing + (j*(size+(spacing*2)))); // Displays amount of mines around (if no mines arround, then nothing is displayed)
						}
						continue; // Goes to next element in loop
					}

					// If misflagged
					if(misFlagged[i][j]) {
						ImageIcon x = new ImageIcon("images\\" + Integer.toString(currentMode) + "x.png"); // Create icon to display red x
						x.paintIcon(this, g, 120 + spacing + (i*(size+(spacing*2))), 150 + spacing + (j*(size+(spacing*2)))); // Displays red x
						continue; // Goes to next element in loop
					}

					// If mouse is currently over square and game is not over
					if(mx >= xCalibration + 2*spacing + (i*(size+(spacing*2))) &&
					   mx < xCalibration + 2*spacing + (i*(size+(spacing*2))) + size &&
					   my >= yCalibration + 2*spacing + (j*(size+(spacing*2))) &&
					   my < yCalibration + 2*spacing + (j*(size+(spacing*2))) + size &&
					   !gameOver) {
						g.setColor(Color.gray); // Sets color to display a lighter shade of gray on square
					}
					else{
						g.setColor(new Color(100, 100, 100)); // Sets color to display dark shade of gray on square
					}
					g.fillRect(120 + spacing + (i*(size+(spacing*2))), 150 + spacing + (j*(size+(spacing*2))), size, size); // Displays square

					// If flagged
					if(flagged[i][j]) {
						ImageIcon flag = new ImageIcon("images\\" + Integer.toString(currentMode) + "flag.png"); // Create icon to display flag
						flag.paintIcon(this, g, 120 + spacing + (i*(size+(spacing*2))), 150 + spacing + (j*(size+(spacing*2)))); // Display flag on square
					}
				}
			}
		}
	}

	/**
		The buildMenuBar method build the menu bar and it's components.
	*/

	public void buildMenuBar() {
		JMenu menu = new JMenu("Menu"); // Creates new JMenu
		
		JMenuItem menuItem = new JMenuItem("Beginner"); // Creates menu item titled, "Beginner"
		menuItem.addActionListener(new ModeListener(0)); // Adds modeListener to menu item
		menu.add(menuItem); // Adds menu item to menu

		menuItem = new JMenuItem("Intermediate"); // Creates menu item titled, "Intermediate"
		menuItem.addActionListener(new ModeListener(1)); // Adds modeListener to menu item
		menu.add(menuItem); // Adds menu item to menu

		menuItem = new JMenuItem("Advanced"); // Creates menu item titled, "Advanced"
		menuItem.addActionListener(new ModeListener(2)); // Adds modeListener to menu item
		menu.add(menuItem); // Adds menu item to menu

		menuBar = new JMenuBar(); // Initializes menuBar
		menuBar.add(menu); // Adds menu to menu bar
	}

	/**
		The buildExplosionSound method builds a Clip object to play an explosion sound
		when a mine explodes
	*/

	public void buildExplosionSound() {
		try {
			String filename = "Explosion.wav"; // Creates string to hold filename of sound
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filename).getAbsoluteFile()); // Creates audio stream object to hold sound effect
			clip = AudioSystem.getClip(); // Initializes clip
			clip.open(audioInputStream); // Opens sound with clip
		}
		// Catches any exceptions throw by method
		catch (Exception ex) {
			ex.printStackTrace(); // Prints where error occured
		}
	}

	/**
		The resetGame method resets the game.
		@param mode holds the game mode to reset the game to
	*/

	public void resetGame(int mode) {
		gameOn = false; // Game is not on
		gameOver = false; // Game is not over
		setMode(mode); // Sets the mode
		hideBoxes(); // Hides all the squares, so they are not revealed
		plantMines(); // Plants mines
		assignNeighbors(); // Determines how many mines near each square
		setFlags(); // Resets all squares to not have flags
		setMisFlags(); // Resets all squares to not have misflags
		setUpTimer(); // Sets up timer based on currentMode
	}

	/**
		The setMode method set the game mode.
		@param mode holds mode to set to current mode
	*/

	public void setMode(int mode) {

		// If mode is Beginner
		if(mode == 0) {
			rows = 7; // Sets number of rows
			columns = 9; // Sets number of columns
			spacing = 6; // Sets spacing of squares
			size = 66; // Sets size of squares
			xCalibration = 123; // Sets horizontal calibration
			yCalibration = 205; // Sets vertical calibration
			numOfmines = 10; // Sets number of mines
			currentMode = 0; // Sets currentMode
			time = 60; // Sets time limit
		}

		// If mode is Intermediate
		else if(mode == 1) {
			rows = 13; // Sets number of rows
			columns = 18; // Sets number of columns
			spacing = 3; // Sets spacing of squares
			size = 35; // Sets size of squares
			xCalibration = 127; // Sets horizontal calibration
			yCalibration = 208; // Sets vertical calibration
			numOfmines = 35; // Sets number of mines
			currentMode = 1; // Sets currentMode
			time = 3*60; // Sets time limit
		}

		// If mode is Advanced
		else if(mode == 2) {
			rows = 22; // Sets number of rows
			columns = 25; // Sets number of columns
			spacing = 2; // Sets spacing of squares
			size = 20; // Sets size of squares
			xCalibration = 127; // Sets horizontal calibration
			yCalibration = 208; // Sets vertical calibration
			numOfmines = 91; // Sets number of mines
			currentMode = 2; // Sets currentMode
			time = 10*60; // Sets time limit
		}

		// If mode passed was invalid
		else {
			System.out.println("Invalid mode!"); // Prints invalid mode
		}
	}

	/**
		The hideBoxes method sets all squares to be unrevealed
	*/

	public void hideBoxes() {
		revealed = new boolean[rows][columns]; // Initalizes revealed array

		// For each row
		for(int i = 0; i < rows; i++) {
			// For each column
			for(int j = 0; j < columns; j++) {
				revealed[i][j] = false; // Hide square
			}
		}
	}

	/**
		The plantMines method randomly assigns amount of mines to squares
	*/

	public void plantMines() {
		Random rand = new Random(); // Creates new random object
		mines = new boolean[rows][columns]; // Initializes mines array

		int x, y; // Temporary varibles to be assigned random numbers

		// For number of mines
		for(int i = 0; i < numOfmines; i++) {
			x = rand.nextInt(rows); // Assigns random number to x based on rows
			y = rand.nextInt(columns); // Assigns random number to y based on columns

			// If not a mine
			if(!mines[x][y]) {
				mines[x][y] = true; // Assign mine to location
			}

			// If already a mine
			else {
				i--; // Decrement i, so the loop runs at least one more time
			}
		}
	}

	/**
		The inBoxX method determines what row the cursor is currently in.
		@return row cursor is in
	*/

	public int inBoxX() {
		// For each row
		for(int i = 0; i < rows; i++) {
			// If cursor is in this row
			if(mx >= xCalibration + 2*spacing + (i*(size+(spacing*2))) &&
		  	   mx < xCalibration + 2*spacing + (i*(size+(spacing*2))) + size) {
				return i; // Return row number
			}
		}
		return -1; // Returns if not in a row
	}

	/**
		The inBoxY method determines what column the cursor is currently in.
		@return column cursor is in
	*/

	public int inBoxY() {
		// For each column
		for(int j = 0; j < columns; j++) {
			// If cursor is in this column
			if(my >= yCalibration + 2*spacing + (j*(size+(spacing*2))) &&
		  	   my < yCalibration + 2*spacing + (j*(size+(spacing*2))) + size) {
				return j; // Return column number
			}
		}
		return -1; // Returns if not in a column
	}

	/**
		The assignNeighbors method assigns how many mines are near
		each square.
	*/

	public void assignNeighbors() {
		neighbors = new int[rows][columns]; // Initializes neighbors array

		// For each row
		for(int x = 0; x < rows; x++) {
			// For each column
			for(int y = 0; y < columns; y++) {
				neighbors[x][y] = 0; // Set amount of near mines to 0
			}
		}

		// For each current row
		for(int cx = 0; cx < rows; cx++) {
			// For each current column
			for(int cy = 0; cy < columns; cy++) {
				// For each row
				for(int x = 0; x < rows; x++) {
					// For each column
					for(int y = 0; y < columns; y++) {
						// If current square is a mine
						if(isNeighbor(x, y, cx, cy)) {
							neighbors[cx][cy] += 1; // Increment amount of near mines
						}
					}
				}
			}
		}
	}

	/**
		The isNeighbor method determins whether a near square is near and has a mine
		@param x holds row to be checked if near
		@param y holds column to be check if near
		@param cx holds current row
		@param cy holds current column
		@return true if is mine and is near, returns false if not near and/or is not a mine
	*/

	public boolean isNeighbor(int x, int y, int cx, int cy) {
		// If it is a near
		if(Math.abs(cx - x) <= 1 && Math.abs(cy-y) <= 1) {
			// If it is a mine
			if(mines[x][y])	{
				return true; // Return is a neighboring mine
			}
		}
		return false; // Returns is not a neighboring mine
	}

	/**
		The resetIcons method resets the explosion animation
	*/

	public void resetIcons() {
		ImageIcon img = new ImageIcon("Images\\mine" + Integer.toString(currentMode) + ".gif"); // Create icon object that holds image to be reset
		img.getImage().flush(); // Resets icon to be used for another game
	}

	/**
		The resetSound method resets the explosion sound clip
	*/

	public void resetSound() {
		clip.stop(); // Stops the clip with the sound
		clip.flush(); // Resets the sound
		clip.setFramePosition(0); // Sets the clip to start at beginning
	}

	/**
		The setUpTimer method sets the timer to start at 0 and count up
	*/

	public void setUpTimer() {
		timer = new Timer(1000, new TimerListener()); // Initializes timer
		currentTime = 0; // Sets start time to 0
	}

	/**
		The startTimer method starts the timer
	*/

	public void startTimer() {
		timer.start(); // Starts timer
	}

	/**
		The stopTimer method stops the timer
	*/

	public void stopTimer() {
		timer.stop(); // Stops timer
	}

	/**
		The setFlags method sets all squares to not have flags.
	*/

	public void setFlags() {
		flagged = new boolean[rows][columns]; // Initializes flagged array

		// For every row
		for(int i = 0; i < rows; i++) {
			// For every column
			for(int j = 0; j < columns; j++) {
				flagged[i][j] = false; // Sets current element to false
			}
		}
	}

	/**
		The setMisFlags method sets all squares to not be misflagged
	*/

	public void setMisFlags() {
		misFlagged = new boolean[rows][columns]; // Initializes misFlagged array

		// For every row
		for(int i = 0; i < rows; i++) {
			// For every column
			for(int j = 0; j < columns; j++) {
				misFlagged[i][j] = false; // Sets current element to false
			}
		}
	} 

	/**
		The isWin method checks whether there is a win
		@return if win or not
	*/

	public boolean isWin() {
		// For every row
		for(int i = 0; i < rows; i++) {
			// For every column
			for(int j = 0; j < columns; j++) {
				// If there is a mine that is not flagged
				if(mines[i][j] && !flagged[i][j]) {
					return false; // Not a win
				}
			}
		}
		gameOn = false; // Game is no longer on
		return true; // Is a win
	}

	/**
		The setLose method sets current game to a lose
	*/

	public void setLose() {
		clip.start(); // Starts explosion sound

		// For every row
		for(int i = 0; i < rows; i++) {
			// For every column
			for(int j = 0; j < columns; j++) {
				// If is a mine
				if(mines[i][j]) {
					// If not flagged
					if(!flagged[i][j]) {
						revealed[i][j] = true; // Reveals mine
					}
				}
				// If not a mine
				else {
					// If flagged
					if(flagged[i][j]) {
						misFlagged[i][j] = true; // Square is misFlagged
					}
				}
			}
		}
		gameOn = false; // Game is no longer on
		gameOver = true; // Game is over
	}


	/**
		The Move class (implements MouseMotionListener) detects
		whether the mouse was moved or not. If it is moved, then
		it's new coordinates are retrived
	*/

	public class Move implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			// Not being used
		}

		/**
			The mouseMoved method is an Overided method.
			If the mouse moves, the method finds the mouses new
			x and y coordinates.
			@param e is event of mouse (moved)
		*/

		@Override
		public void mouseMoved(MouseEvent e) {
			mx = e.getX(); // Sets mouse x position
			my = e.getY(); // Sets mouse y position
		}
	}

	/**
		The click class(implements MouseListener) detects if
		the mouse has been clicked or not.
	*/

	public class Click implements MouseListener {

		/**
			The mouseClicked method performs some action based on where the mouse was
			clicked on the game and which mouse button was clicked.
			@param e is mouse event (clicked)
		*/

		@Override
		public void mouseClicked(MouseEvent e) {
			// If click is in a square, the game is not over, and there is not a win
			if(inBoxX() != -1 && inBoxY() != -1 && !gameOver && !isWin()) {
				//If the click was a left click and the current square is flagged
				if(SwingUtilities.isLeftMouseButton(e) && !flagged[inBoxX()][inBoxY()]) {
					revealed[inBoxX()][inBoxY()] = true;// Reveal square

					// If game was not on
					if(!gameOn) {
						gameOn = true; // Game is now on
						startTimer(); // Start timer
					}
				}
				// If mouse click was a right click
				if(SwingUtilities.isRightMouseButton(e)) {
					// If current square is not flagged
					if(!flagged[inBoxX()][inBoxY()]) {
						flagged[inBoxX()][inBoxY()] = true; // Flag the square
					}
					// If current square is flagged
					else {
						flagged[inBoxX()][inBoxY()] = false; // Unflag the square
					}
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// Not being used
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// Not being used
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// Not being used
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Not being used
		}
	}

	/**
		The TimerListener class (implements ActionListener) checks the time
		and increments it as long as the time limit is not reached
	*/

	public class TimerListener implements ActionListener {

		/**
			The actionPerformed method either increments the currentTime or
			stops the timer
			@param e holds Action event of timer
		*/

		public void actionPerformed(ActionEvent e) {
			// If time limit is reached, game is off, or there is a win
			if(currentTime == time - 1 || !gameOn || isWin()) {
				timer.stop(); // Stop the timer
			}
			currentTime++; // Increments time
		}
	}

	/**
		The ModeListener class (implements ActionListener) is used to
		change the mode when user selects a different mode
	*/

	public class ModeListener implements ActionListener {
		private int mode; // Holds mode

		/**
			ModeListener constructor
			@param m
		*/

		public ModeListener(int m) {
			mode = m; // Sets mode to m
		}


		/**
			The actionPerformed method changes the mode if
			a mode change action is performed
			@param e holds mode change action performed
		*/

		@Override
		public void actionPerformed(ActionEvent e) {
			stopTimer(); // Stops timer
			resetIcons(); // Resets icons
			resetSound(); // Resets Sound
			resetGame(mode); // Resets Game using mode
		}
	}
}

/*
	CREDITS
	~~~~~~~
	Source Code (by Isaac Woollen) written with Sublime Text 3
	Animation/Image Design (by Isaac Woollen) made with Inkscape and Adobe Photoshop
	Sound Design (by Isaac Woollen) made with Ableton 10 Lite
*/