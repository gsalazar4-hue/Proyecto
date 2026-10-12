package enph;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import static enph.Estilo.*;

/** Navegación entre ventanas y comportamiento común del encabezado y del marco de tablet. */
public final class Nav {
    private Nav() {}

    public enum Rol { ENCUESTADOR, JEFE }

    /** Tamaño, título y posición iniciales de una ventana (se llama después de initComponents). */
    public static void configurar(JFrame f, String titulo, int ancho, int alto) {
        f.setTitle(titulo);
        f.setSize(ancho, alto);
        f.setMinimumSize(new Dimension(Math.min(ancho, 980), Math.min(alto, 660)));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        normalizarFuentes(f.getContentPane());
    }

    /** Abre la ventana siguiente y cierra la actual. */
    public static void ir(JFrame actual, JFrame siguiente) {
        siguiente.setLocationRelativeTo(actual);
        siguiente.setVisible(true);
        actual.dispose();
    }

    public static void mensaje(JFrame f, String texto) {
        JOptionPane.showMessageDialog(f, texto, "ENPH", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Conecta los enlaces del encabezado del encuestador. */
    public static void enlazarEncabezado(JFrame f, JButton visita, JButton hogar, JButton salir) {
        visita.addActionListener(e -> ir(f, new VentanaVisitas()));
        hogar.addActionListener(e -> ir(f, new VentanaAsignados()));
        salir.addActionListener(e -> ir(f, new VentanaInicio()));
    }

    /** Fondo degradado que rodea al dispositivo (tablet) del jefe de hogar. */
    public static class FondoTablet extends JPanel {
        public FondoTablet() {
            super(new GridBagLayout());
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setPaint(new GradientPaint(0, 0, new Color(0xB4, 0xC6, 0xE2),
                    getWidth(), getHeight(), new Color(0xFD, 0xE0, 0xCD)));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /** Escribe en el encabezado el nombre del jefe de hogar (ventanas del dispositivo del jefe). */
    public static void datosJefe(JLabel nombre, JLabel rol) {
        nombre.setText(Datos.hogar.jefe);
        rol.setText("Jefe de hogar de " + Datos.hogar.codigo);
    }

    /**
     * Ventanas compartidas: el diseñador muestra la versión del encuestador. Para el jefe de hogar se mueven los
     * mismos componentes dentro del marco de tablet.
     */
    public static void aplicarRol(Rol rol, JPanel raiz, JPanel encabezado, JPanel enlaces,
                                  JLabel nombre, JLabel rolEtiqueta, JComponent cuerpo) {
        if (rol == Rol.ENCUESTADOR) return;
        datosJefe(nombre, rolEtiqueta);
        enlaces.setVisible(false);
        raiz.removeAll();
        FondoTablet fondo = new FondoTablet();
        JPanel dispositivo = new JPanel(new BorderLayout());
        dispositivo.setBorder(new LineBorder(MARCO_TABLET, 14, true));
        dispositivo.add(encabezado, BorderLayout.NORTH);
        dispositivo.add(cuerpo, BorderLayout.CENTER);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        c.insets = new Insets(22, 40, 22, 40);
        fondo.add(dispositivo, c);
        raiz.add(fondo, BorderLayout.CENTER);
        raiz.revalidate();
        raiz.repaint();
    }
}
