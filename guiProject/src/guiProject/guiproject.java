package guiProject;

import javax.swing.*;
import javax.swing.JPanel;
import java.awt.*;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

interface Drawable {
	abstract void draw(Graphics g, int x, int y, int cellSize);
}

interface Serializable {}

public class guiproject{
	public static Board board;
	public static void main(String[] args)
	{
	JFrame display = new JFrame();
	display.setTitle("GUIBoard");
	display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	display.setSize(500,500);
	display.setResizable(true);
	

	
	board = BoardDesigner.DesignBoard();
	display.getContentPane().add(board);
	
	
	
	JMenuBar MenuBar = createMenu();
	display.setJMenuBar(MenuBar);
	display.setVisible(true);
	
	
	}

public static Board getBoard()
{
	return board;
}
private static JMenuBar createMenu()
{
	
	JMenuBar MenuBar = new JMenuBar();
	
	JMenu FileMenu = new JMenu("File");
	
	JMenuItem LoadBoard = new JMenuItem("Load");
	JMenuItem SaveBoard = new JMenuItem("Save");
	JMenuItem DesignBoard = new JMenuItem("Design");
	FileMenu.add(LoadBoard);
	
	
	LoadBoard.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e)
		{
			
			try(ObjectInputStream s = new ObjectInputStream(new FileInputStream("GUIBoard.ser")))
			{
				Board board = (Board) s.readObject();
				JFrame display = new JFrame();
				display.setTitle("GUIBoard");
				display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				display.setSize(500,500);
				display.setResizable(true);
				display.getContentPane().add(board);
				
				
				
				JMenuBar MenuBar = createMenu();
				display.setJMenuBar(MenuBar);
				display.setVisible(true);
			}
			catch(Exception exception)
			{
				System.err.println("Could not load board.");
			}
		}
	});
	
	FileMenu.add(SaveBoard);
	
	SaveBoard.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e)
		{
			
			try(ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream("GUIBoard.ser")))
			{
				s.writeObject(getBoard());
			}
			catch(Exception exception)
			{
				System.err.println("Could not save board.");
			}
		}
		
	});
	
	FileMenu.add(DesignBoard);
	
	DesignBoard.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e)
		{
			try(ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream("GUIBoard.ser")))
			{
				JFrame display = new JFrame();
				display.setTitle("GUIBoard");
				display.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				display.setSize(500,500);
				display.setResizable(true);
	
				board = BoardDesigner.DesignBoard();
				
				display.getContentPane().add(board);
				JMenuBar MenuBar = createMenu();
				display.setJMenuBar(MenuBar);
				display.setVisible(true);
			}
			catch(Exception exception)
			{
				System.err.println("Could not design board.");
			}
		}
	});
	MenuBar.add(FileMenu);	
	return MenuBar;
}

}


class BoardDesigner{
	
	private static final int boardSize = 32;
	private static final int numWalls = 256;
	private static final int numRewards = 16;
	private static final int numObstacles = 16;
	
	public static Board DesignBoard()
	{
		BoardObject[][] objects = new BoardObject[boardSize][boardSize];
		
		int entranceXCoordinate = generateCoordinate();
		int entranceYCoordinate = 0;
		int exitXCoordinate = generateCoordinate();
		int exitYCoordinate = boardSize - 1;
		
		
		objects[entranceXCoordinate][entranceYCoordinate] = new Entrance(entranceXCoordinate,entranceYCoordinate);
		objects[exitXCoordinate][exitYCoordinate] = new Exit(exitXCoordinate,exitYCoordinate);

		generateBoardTokens(objects, numWalls, WallObstacle.class);
		generateBoardTokens(objects, numRewards, Coin.class);
		generateBoardTokens(objects, numRewards, SpeedBoost.class);
		generateBoardTokens(objects, numRewards, PowerBoost.class);
		generateBoardTokens(objects, numObstacles, Spike.class);
		generateBoardTokens(objects, numObstacles, Void.class);

		
	
		Board board = new Board(objects, entranceXCoordinate, entranceYCoordinate,exitXCoordinate, exitYCoordinate );
		return board;
	}

	
	public static <T extends BoardObject> void generateBoardTokens(BoardObject[][] objects, int numObstacles, Class<T> obstacle)
	{
		Random random = new Random();
		int numObstaclesCreated = 0;
		while(numObstaclesCreated < numObstacles)
		{
			int xCoordinate;
			int yCoordinate;
			
			
			xCoordinate = random.nextInt(boardSize);
			yCoordinate = random.nextInt(boardSize);
				
			if(objects[xCoordinate][yCoordinate] == null)
			try {
			objects[xCoordinate][yCoordinate] = obstacle.getDeclaredConstructor(int.class, int.class).newInstance(xCoordinate,yCoordinate);
			numObstaclesCreated++;
			}catch(Exception e) {
				System.out.println("Failed to generate.");
			}	
			
		}
	}
	
	public static int generateCoordinate() {
		Random random = new Random();
		return random.nextInt(boardSize);
		
	}
	
}

class Board extends JPanel implements Serializable{
	private static final int boardSize = 32;
	private BoardObject[][] objects;
	private int entranceXCoordinate, entranceYCoordinate;
	private int exitXCoordinate, exitYCoordinate;
	
	
	public Board(BoardObject[][] objectArray, int entranceX, int entranceY, int exitX, int exitY)
	{
		this.objects = objectArray;
		this.entranceXCoordinate = entranceX;
		this.entranceYCoordinate = entranceY;
		this.exitXCoordinate = exitX;
		this.exitYCoordinate = exitY;
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		
		int originX = 10;
		int originY = 10;
		int cellSide = 20;
		
		g.setColor(Color.DARK_GRAY);
		g.drawLine(originX, originY, originX, cellSide * boardSize + 10);
		g.drawLine(originX, originY, cellSide * boardSize + 10, originY);
		g.drawLine(originX, cellSide * boardSize + 10, cellSide * boardSize + 10, cellSide * boardSize + 10);
		g.drawLine(cellSide * boardSize + 10, originY, cellSide * boardSize + 10, cellSide * boardSize + 10);
		
		for(int k = 0; k < boardSize; k++)
		{
			for(int l = 0; l < boardSize; l ++)
			{
				if(objects[k][l] != null)
				{
					objects[k][l].draw(g, k * cellSide + 10, l * cellSide + 10, cellSide);
				}
			}
		}
		
		int textX = 700;
		int textY = 10;
		
		g.setFont(new Font("Arial", Font.PLAIN, 16));
		g.setColor(Color.BLACK);
		String legend = "Legend:";
		g.drawString(legend, textX, textY);
		
		g.setColor(Color.red);
		g.fillRect(700, 30, cellSide, cellSide);
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(700, 50, cellSide, cellSide);
		
		g.setColor(Color.gray);
		g.fillRect(700, 70, cellSide, cellSide);
		
		g.setColor(Color.yellow);
		g.fillRect(700, 90, cellSide, cellSide);
		
		g.setColor(Color.magenta);
		g.fillRect(700, 110, cellSide, cellSide);
		
		g.setColor(Color.blue);
		g.fillRect(700, 130, cellSide, cellSide);
		
		g.setColor(Color.black);
		g.fillRect(700, 150, cellSide, cellSide);
		
		g.setColor(Color.lightGray);
		g.fillRect(700, 170, cellSide, cellSide);
		
		g.setColor(Color.ORANGE);
		g.fillRect(700, 190, cellSide, cellSide);
		
		
		
		g.setColor(Color.black);
		legend = "Entrance";
		g.drawString(legend, 720, 50);
		
		legend = "Border Wall";
		g.drawString(legend, 720, 70);
		
		legend = "Wall Obstacle";
		g.drawString(legend, 720, 90);
		
		legend = "Coin Reward";
		g.drawString(legend, 720, 110);
		
		legend = "Power Up";
		g.drawString(legend, 720, 130);
		
		legend = "Speed Boost";
		g.drawString(legend, 720, 150);
		
		legend = "Void";
		g.drawString(legend, 720, 170);
		
		legend = "Spike";
		g.drawString(legend, 720, 190);
		
		legend = "Exit";
		g.drawString(legend, 720, 210);
		
	}
	public int getBoardSize() 
	{
		return boardSize;
	}
	
	public BoardObject[][] getObjects()
	{
		return objects;
	}
	
	public int getEntranceXCoordinate()
	{
		return entranceXCoordinate;
	}

	public int getEntranceYCoordinate()
	{
		return entranceYCoordinate;
	}
	
	public int getExitXCoordinate()
	{
		return exitXCoordinate;	
	}
	
	public int getExitYCoordinate()
	{
		return exitYCoordinate;	
	}
}

abstract class BoardObject extends JPanel implements Serializable, Drawable{
	private int xCoordinate;
	private int yCoordinate;
	protected Color color;
	public BoardObject(int x, int y, Color color)
	{
		this.xCoordinate = x;
		this.yCoordinate = y;
		this.color = color;
		
	}
	
	public int getXCoordinate() {
		return xCoordinate;
	}
	
	public int getYCoordinate() {
		return yCoordinate;
	}
	
	public abstract void printObjectCoordinates(int x, int y);
	
	public abstract void interactableMessage();
	
	
}
class Coin extends BoardObject{
	
	public Coin (int x, int y) {
		super(x,y, Color.YELLOW);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillOval(x, y, cellSize, cellSize);

	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This coin's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("You received a coin!");
	}
}

class SpeedBoost extends BoardObject{
	public SpeedBoost (int x, int y) {
		super(x,y, Color.BLUE);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillOval(x, y, cellSize, cellSize);
	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This speed boost's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("You received a speed boost!");
	}
}

class PowerBoost extends BoardObject{
	public PowerBoost (int x, int y) {
		super(x,y,Color.MAGENTA);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillOval(x, y, cellSize, cellSize);
	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This power boost's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("You received a power boost!");
	}
}


class WallObstacle extends BoardObject {
	public WallObstacle (int x, int y) {
		super(x,y, Color.GRAY);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillRect(x, y, cellSize, cellSize);
	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This wall obstacle's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("You've hit a wall obstacle!");
	}

}

class Void extends BoardObject {
	public Void (int x, int y) {
		super(x,y, Color.black);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillRect(x, y, cellSize, cellSize);
	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This void's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("You're stuck in a void! Get out!!");
	}
}

class Spike extends BoardObject {
	public Spike (int x, int y) {
		super(x,y, Color.LIGHT_GRAY);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillRect(x, y, cellSize, cellSize);
	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This spike's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("You've hit a spike! Ouch!");
	}
}

class Entrance extends BoardObject {
	
	public Entrance(int x, int y) {
		super(x,y, Color.RED);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillRect(x, y, cellSize, cellSize);
	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This entrance's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("This is the entrance.");
	}
	
}

class Exit extends BoardObject {
	public Exit(int x, int y) {
		super(x,y, Color.ORANGE);
	}
	
	public void draw (Graphics g, int x, int y, int cellSize)
	{
		g.setColor(color);
		g.fillRect(x, y, cellSize, cellSize);
	}
	
	public void printObjectCoordinates(int x, int y)
	{
		System.out.println("This exit's coordinates are: " + x + " " + y);
	}
	
	public void interactableMessage()
	{
		System.out.println("You've found the exit! Yippee!");
	}
}

abstract class People extends BoardObject {
	String name;
	
	public People(int x, int y, String name)
	{
		super(x, y, Color.CYAN);
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
}

