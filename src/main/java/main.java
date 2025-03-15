
import javax.swing.*;
import java.awt.event.*;
public class main extends JFrame {

    private JButton buttoncont;

    public main(){

        setTitle("Double Sliced Bread Company");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton buttoncont= new JButton("Continue");
        buttoncont.addActionListener(e->showSignup());
        add(buttoncont);

    }
    private void showSignup() {
        getContentPane().removeAll();
        getContentPane().add(new signup());
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new main().setVisible(true);
        });
    }

}
