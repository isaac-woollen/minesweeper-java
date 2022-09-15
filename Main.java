// Isaac Woollen | CSCI-3063 029 | 807007246
// Minesweeper | Class Project
// Main Class

public class Main implements Runnable {

	Minesweeper ms = new Minesweeper();
	public static void main(String[] args) {
		new Thread(new Main()).start();
	}

	@Override
	public void run() {
		while(true){
			ms.repaint();
		}
	}
}
