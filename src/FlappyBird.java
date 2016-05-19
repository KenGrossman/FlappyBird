import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class FlappyBird extends JFrame{
	
	private static FlappyBird gameBoard;
	private ScoreHandler flappyScoreHandler;
	private Bird playerCharacter;
	
	//Keep a list of pipes and high scores
	private ArrayList<Pipe> pipes;
	private ArrayList<Score> scoreList;
	
	//Create a timer and set staring pace at .5 seconds per tick
	private Timer timer;
	private int timerSpeed = 17;
	private int gameTime;

	//Create main menu buttons
	private JButton newGameButton;
	private JButton scoreButton;
	private JButton mainMenuButton;
	
	//Number of pipes bird has passed
	private int score = 0;
	
	//Create 3 panels used in game and a master panel to contain them all
	private JPanel masterPanel = new JPanel();
	private JPanel gameArea = new JPanel();
	private JPanel mainMenu = new JPanel();
	private JPanel scoreMenu = new JPanel();
	
	//Create cardLayout so I may switch between 3 main Panels
	private CardLayout cards;
	
	//Physics Debugging
	private boolean collisionDetection = true;//collision detection
	private boolean endlessFalling = true;//when bird falls off bottom of screen he appears at top and vice-versa
	
	//Graphics/Console Debugging
	private boolean visiblePipes = true;
	private boolean visibleBird = true;
	private boolean showConsole = true;
	
	//Toggles when GUI is first built so it isnt built multiple times
	private boolean mainBuilt, gameBuilt, scoreBuilt;
	
	public static void main(String[] args) {
		//Build board and make it visible
		gameBoard = new FlappyBird();
		gameBoard.setVisible(true);
		gameBoard.setSize(1200,800);
		gameBoard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Center form on screen
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - gameBoard.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - gameBoard.getHeight()) / 2);
	    gameBoard.setLocation(x, y);
	}
	
	//Create Main Menu and all objects that live there
	private void buildMainMenu(){
		System.out.println("BUILD Main Menu");
		
		mainMenu.setLayout(new GridLayout(3, 1));
		
		//Create and add title to top
		JLabel title = new JLabel("Flappy Bird");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		
		//Create new game button in center and add actionListener
		newGameButton = new JButton("New Game");
		newGameButton.addActionListener(new Listen());
		
		//Create high score button at bottom and add actionListener
		scoreButton = new JButton("High Scores");
		scoreButton.addActionListener(new Listen());
		
		//Add object in this order to panel
		mainMenu.add(title);
		mainMenu.add(newGameButton);
		mainMenu.add(scoreButton);
		
		//Indicated menu has been built
		mainBuilt = true;
	}

	//Create game board including starting pipes and bird, starts timer
	private void buildGameBoard() {
		System.out.println("BUILD Game Board");
		
		pipes = new ArrayList<Pipe>();
		
		//Create 5 pipes every 300 pixels starting at 600
		for(int i = 0; i < 5; i++){
			pipes.add(new Pipe(600 + (300*i) ));
		}

		//Create bird
		playerCharacter = new Bird();
		
    	//Add Key Binding for hopping bird
    	gameArea.getInputMap().put(KeyStroke.getKeyStroke("2"), "hop");
    	gameArea.getActionMap().put("hop", new HopAction());
    	
    	//Add Key Binding for resetting game
    	gameArea.getInputMap().put(KeyStroke.getKeyStroke("3"), "reset");
    	gameArea.getActionMap().put("reset", new ResetAction());
    	
    	//Add Key Binding for returning to main menu
    	gameArea.getInputMap().put(KeyStroke.getKeyStroke("4"), "main");
    	gameArea.getActionMap().put("main", new MenuAction());
    	
    	//Allows panel to listen for keystrokes
    	gameArea.requestFocus();
		
		//Create and start timer
		timer = new Timer(timerSpeed, new Listen());
    	timer.start();
    	
    	score = 0;
    	
    	gameBuilt = true;
	}

	//Create High Score Menu(IN PROGRESS...)
	private void buildScoreMenu(){
		System.out.println("BUILD Score Menu");

		
		scoreMenu.setLayout(new BorderLayout());
		
		//Create and add title to top
		JLabel title = new JLabel("Top 3 High Scores");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		scoreMenu.add(title, BorderLayout.NORTH);
		
		//Create main menu button at bottom and add actionListener
		mainMenuButton = new JButton("Return to Main Menu");
		mainMenuButton.addActionListener(new Listen());
		scoreMenu.add(mainMenuButton, BorderLayout.SOUTH);
		
		//Create a Panel for Position, Name, and Score
		JPanel scorePanel = new JPanel(new GridLayout(10 , 2));
		JLabel name, score;
		
		//List top ten people 
		for(int i = 0; i < 10; i++){
			
			if(i >= scoreList.size()){
				name = new JLabel("AAAAA");
				score = new JLabel("0");
			}
			else{
				name = new JLabel(scoreList.get(i).getName());
				score = new JLabel(String.valueOf(scoreList.get(i).getScore()));
			}
		
			//Center Labels
			name.setHorizontalAlignment(SwingConstants.CENTER);
			score.setHorizontalAlignment(SwingConstants.CENTER);
			
			//Add labels to scorePanel
			scorePanel.add(name);
			scorePanel.add(score);
		}
		
		//Add score Panel to scoreMenu
		scoreMenu.add(scorePanel, BorderLayout.CENTER);
		
		scoreBuilt = true;
	}
	
	//Chooses opening panel 
	public FlappyBird(){
		//Estanblish connection to scoreHandler and get all scores from database
		flappyScoreHandler = new ScoreHandler();
		scoreList = flappyScoreHandler.getScores();
		
		//Create card layout to switch between panels 
		cards = new CardLayout();
		
		//Add Panels to master panel
        masterPanel.setLayout(cards);
        masterPanel.add(mainMenu, "main");
        masterPanel.add(gameArea, "game");
        masterPanel.add(scoreMenu, "score");
        masterPanel.setVisible(true);
        
		add(masterPanel);
		
		//Build then display main menu
		buildMainMenu();
	}
	
	//Creates and display graphics using a buffer strategy every tick
	private void render(){
		BufferStrategy bs = gameBoard.getBufferStrategy();
		
		//If buffer doesn't exist create a triple buffer
		if(bs == null){
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();

		//Draw gameBoard and paint it yellow
		g.setColor(Color.YELLOW);
		g.fillRect(0, 0, gameBoard.getWidth(), gameBoard.getHeight());

		//If show pipes is on
		if(visiblePipes){
			
			//Build a pipe that is as tall as the game board
			g.setColor(Color.GREEN);
			for(int i = 0; i < pipes.size(); i++){
				g.fillRect(pipes.get(i).getX(), 0, pipes.get(i).getWidth(), gameBoard.getHeight());
			}
			
			//Build a gap for the bird to fly through
			g.setColor(Color.BLUE);
			for(int i = 0; i < pipes.size(); i++){
				g.fillRect(pipes.get(i).getX(), pipes.get(i).getGapTop(), pipes.get(i).getWidth(), pipes.get(i).getGapHeight());
			}
		}
		
		//Draw Bird
		if(visibleBird)
			g.drawImage(playerCharacter.getImage(), playerCharacter.getX(), playerCharacter.getY(), playerCharacter.getWidth(), playerCharacter.getHeigth(), null);

		//Draw Score
		g.setColor(Color.BLUE);
		g.drawString(Integer.toString(score), gameBoard.getWidth()/2, 50);		
		
		//Dispose of old graphics and display buffer
		g.dispose();
		bs.show();
	}
	
	//Shows user their final score. If they are in the top 5 they will get to enter their name 
	private void dead(){
		System.out.println("YOU DIED!");
		
		timer.stop();
		
		//Determine if score is in top 5 list must be sorted for this to work if so prompt for input regardless display score
		String playerName = (String)JOptionPane.showInputDialog(
                gameBoard,
                "Your Score: "
                + score + "\n Enter your name",
                "John Doe");
		
		//DONT ACCEPT NULL VALUE
		
		
		System.out.println(score + ", " + playerName);
		
		scoreList.add(new Score(score, playerName));
		
		sortScores();
		
		flappyScoreHandler.writeFile();
	}
	
	private void sortScores(){
		//SORT THE SCORES DUH
	}
	
	//Calculate if bird has collided with obstacle if not tick bird
	private void birdLogic(Pipe p){
	
		//If collision Detection has been turned on
		if(collisionDetection){
			
			//There is a pipe in collision range 
			if(p != null){
				//Check if bird hit pipe
	    		if(playerCharacter.getY() > (p.getGapTop() + p.getGapHeight()) || playerCharacter.getY() < p.getGapTop())
	    			dead();
	    		
	    		//Tick bird
	    		else
	    			playerCharacter.tick(gameTime, endlessFalling);
			}
			
			//Bird is flying in open air
			else{
				if(!endlessFalling){
		    		//Check if bird hit ground or ceiling
		    		if(playerCharacter.getY() > gameBoard.getHeight() + 25 || playerCharacter.getY() <= 25)
		    			dead();
		    		//Tick bird
		    		else
		    			playerCharacter.tick(gameTime, endlessFalling);
				}
				else
					playerCharacter.tick(gameTime, endlessFalling);
			}
		}else
			playerCharacter.tick(gameTime, endlessFalling);
	}
	
	//Calculate if pipe is in a dangerous location or off screen the tick pipes
	private Pipe pipeLogic(){
		Pipe p = null;
		
		//Tick pipes
		for(int i = 0; i < pipes.size(); i++){
			pipes.get(i).tick();
			
			//If pipe has gone off screen to the left send it back to the right
			if(pipes.get(i).getX() < -100){
				pipes.get(i).sendToEnd();
			}
			//if bird is between left and right bounds of pipe flag pipe as a danger
			else if(pipes.get(i).getX() <= playerCharacter.getX() && (pipes.get(i).getX() + pipes.get(i).getWidth()) > playerCharacter.getX()){
				p = new Pipe(pipes.get(i));
			}
			//if bird has just passed pipe increment score
			else if((pipes.get(i).getX() + pipes.get(i).getWidth()) == playerCharacter.getX())
				score++;
    	}
		
		//Return either a null pipe or a dangerous pipe
		return p;
	}
	
	//Action Listener for buttons and timer
	private class Listen implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
	    {
	    	//If game hasnt been built build it then show game
	    	if(e.getSource() == newGameButton){
		    	System.out.println("newgamebutton");
		    	if(!gameBuilt)
		    		buildGameBoard();
		    	cards.show(masterPanel, "game");
		    	gameArea.requestFocus();
	    	}
	    	
	    	//If score menu hasnt been built build it then show score menu
	    	else if(e.getSource() == scoreButton){
		    	System.out.println("scorebutton");

		    	if(!scoreBuilt)
		    		buildScoreMenu();
		    	cards.show(masterPanel, "score");
	    	}
	    	
	    	//If menu hasnt been built build it then show menu
	    	else if(e.getSource() == mainMenuButton){
		    	System.out.println("mainMenubutton");
		    	if(!mainBuilt)
		    		buildMainMenu();
		    	cards.show(masterPanel, "main");
	    	}
	    	
	    	//On timer tick
	    	else if(e.getSource() == timer){
	    		//Paint objects 
	    		render();
	    		
	    		//Update pipes and bird
	    		Pipe dangerPipe = pipeLogic();
	    		birdLogic(dangerPipe);
	    		
	    		//Increment physics timer
	    		gameTime++;
	    		
	    		//Print console data
	    		if(showConsole)
	    			console();
	    	}
	    }

		private void console() {
			//Print Bird Position
			System.out.println(playerCharacter.toString());
			
			//Print Pipe Positions
			for(int i = 0; i < pipes.size(); i++){
				System.out.println(pipes.get(i).toString());
			}
			
			System.out.println();	
		}
	}

	//When user pressed a key bird hops
	public class HopAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			gameTime = 0;
			playerCharacter.hop(gameTime);	
		}
	}
	
	//When user pressed a key game resets
	public class ResetAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			System.out.println("reset game");
			gameTime = 0;
			buildGameBoard();
		}
	}
	
	//When user presses a key return to menu
	public class MenuAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			System.out.println("Main Menu");
			gameTime = 0;
			timer.restart();
			timer.stop();
			gameBuilt = false;
			cards.show(masterPanel, "main");
		}
	}
}

