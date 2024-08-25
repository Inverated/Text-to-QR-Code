import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI {
    private static JFrame frame = new JFrame();
    private static JPanel panel = new JPanel();
    public static void main(String[] args) {
        frame.setSize(500,300);
        frame.setResizable(true);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        


        

        frame.setVisible(true);
    }

}
