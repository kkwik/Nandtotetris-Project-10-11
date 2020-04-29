import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        File fi = new File("C:\\Users\\Kevin\\Desktop\\CPP\\Year2\\Sem 2\\CS 3650\\Tetris\\nand2tetris\\projects\\10\\Square\\SquareGame.jack");
        JackTokenizer jt = new JackTokenizer(fi);
        jt.compilerRun();
    }
}
