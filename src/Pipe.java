import java.util.Random;


public class Pipe {
	private int x,gapTop;
	private int gapHeight = 100;
	private final int WIDTH = 100;
	
	public Pipe(int x){
		this.x = x;
		gapTop = generateGap();
	}
	
	public Pipe(int x, int y){
		this.x = x;
		gapTop = y;
	}
	
	public Pipe(Pipe p){
		x = p.getX();
		gapTop = p.getGapTop();
	}
	
	public int getX(){
		return x;
	}
	
	public int getGapTop(){
		return gapTop;
	}
	
	public int getWidth(){
		return WIDTH;
	}
	
	public int getGapHeight(){
		return gapHeight;
	}
	
	private int generateGap(){
		Random r = new Random();
		return r.nextInt(700);
	}
	
	public String toString(){
		return "Pipe X: " + getX() + " Start Gap Y: " + getGapTop();
	}
	
	public void tick(){
		x-=2;
	}
	
	public void sendToEnd(){
		x = 1400;
		generateGap();
	}
}
