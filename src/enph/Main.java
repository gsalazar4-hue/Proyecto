package enph;

import javax.swing.SwingUtilities;

/** Punto de entrada: lo primero que se muestra es la ventana de selección de usuario. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaInicio().setVisible(true));
    }
}
