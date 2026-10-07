package src;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        try {
            WebServer.start(8080);
            System.out.println("Web server started at http://localhost:8080");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Uncomment to run as desktop application
        // SwingUtilities.invokeLater(() -> {
        //     new Login().setVisible(true);
        // });
    }
}