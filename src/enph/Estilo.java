package enph;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Colores, fuentes y componentes visuales compartidos por todas las ventanas. */
public final class Estilo {
    private Estilo() {}

    public static final Color NARANJA_CLARO = new Color(0xFF, 0xB8, 0x71);
    public static final Color NARANJA_TEXTO = new Color(0xC9, 0x6E, 0x12);
    public static final Color NARANJA = new Color(0xEB, 0x80, 0x18);
    public static final Color FONDO = new Color(0xF2, 0xF2, 0xF2);
    public static final Color GRIS_ENC = new Color(0xCB, 0xCB, 0xCB);
    public static final Color GRIS_TXT = new Color(0x55, 0x55, 0x55);
    public static final Color GRIS_BORDE = new Color(0x88, 0x88, 0x88);
    public static final Color GRIS_LINEA = new Color(0xCC, 0xCC, 0xCC);
    public static final Color GRIS_DESHAB_TXT = new Color(0x8A, 0x8A, 0x8A);
    public static final Color AZUL_BG = new Color(0x88, 0xCC, 0xFF);
    public static final Color AZUL_TXT = new Color(0x2A, 0x7B, 0xD0);
    public static final Color VERDE_BG = new Color(0xB0, 0xD3, 0x46);
    public static final Color VERDE_TXT = new Color(0x6B, 0x7F, 0x25);
    public static final Color ROSA_BG = new Color(0xFF, 0xC0, 0xB5);
    public static final Color ROSA_TXT = new Color(0xD4, 0x68, 0x5E);
    public static final Color CAJA_FORM = new Color(0xE8, 0xE8, 0xE8);
    public static final Color CAMPO_DESHAB = new Color(0xD4, 0xD4, 0xD4);
    public static final Color MARCO_TABLET = new Color(0x40, 0x44, 0x4A);
    // Ventana de inicio
    public static final Color INICIO_LATERAL = new Color(0xFF, 0xCC, 0x99);
    public static final Color INICIO_TEXTO = new Color(0xFF, 0x99, 0x33);
    public static final Color INICIO_BOTON_TXT = new Color(0xFF, 0x99, 0x00);

    private static final String FAMILIA = elegirFuente();

    private static String elegirFuente() {
        Set<String> disponibles = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Poppins", "Segoe UI", "Helvetica Neue", "Arial"}) {
            if (disponibles.contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    public static Font fuente(int estilo, int tam) { return new Font(FAMILIA, estilo, tam); }

    public static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---------------- Botones ----------------
    /** Botón redondeado (se ve gris cuando está deshabilitado). Sus colores se editan desde el diseñador. */
    public static class Boton extends JButton {
        private Color bg = NARANJA, fg = Color.WHITE;
        private boolean ficha;

        /** Constructor que usa el diseñador de NetBeans. */
        public Boton() {
            this("Botón", NARANJA, Color.WHITE, false, fuente(Font.BOLD, 18), 230, 48);
        }

        public Boton(String texto, Color bg, Color fg, boolean ficha, Font fuente, int ancho, int alto) {
            super(texto);
            this.bg = bg; this.fg = fg; this.ficha = ficha;
            setFont(fuente);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(ancho, alto));
        }

        public Color getColorFondo() { return bg; }
        public void setColorFondo(Color c) { this.bg = c; repaint(); }
        public Color getColorTexto() { return fg; }
        public void setColorTexto(Color c) { this.fg = c; repaint(); }
        public boolean isFicha() { return ficha; }
        public void setFicha(boolean f) { this.ficha = f; repaint(); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color fondo = isEnabled() ? bg : GRIS_ENC;
            Color texto = isEnabled() ? fg : GRIS_DESHAB_TXT;
            if (getModel().isPressed() && isEnabled()) fondo = fondo.darker();
            g.setColor(fondo);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g.setColor(texto);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String[] lineas = getText().split("\n");
            int y = (getHeight() - fm.getHeight() * lineas.length) / 2 + fm.getAscent();
            for (String ln : lineas) {
                int x = ficha ? 26 : (getWidth() - fm.stringWidth(ln)) / 2;
                g.drawString(ln, x, y);
                y += fm.getHeight();
            }
            if (ficha) {
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() - 40, cy = getHeight() / 2;
                g.drawPolyline(new int[]{cx - 6, cx + 6, cx - 6}, new int[]{cy - 15, cy, cy + 15}, 3);
            }
            g.dispose();
        }
    }


    /** Casilla del prototipo: cuadro redondeado gris; azul con una marca blanca cuando está seleccionada. */
    public static class Casilla extends JCheckBox {
        public Casilla() {
            setOpaque(false);
            setHorizontalAlignment(CENTER);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setOpaque(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int lado = Math.min(34, Math.min(getWidth(), getHeight()) - 4);
            int x = (getWidth() - lado) / 2, y = (getHeight() - lado) / 2;
            if (isSelected()) {
                g.setColor(new Color(0x28, 0x7F, 0xD5));
                g.fillRoundRect(x, y, lado, lado, 8, 8);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawPolyline(new int[]{x + lado / 5, x + lado * 2 / 5, x + lado * 4 / 5},
                        new int[]{y + lado / 2, y + lado * 7 / 10, y + lado * 3 / 10}, 3);
            } else {
                g.setColor(FONDO);
                g.fillRoundRect(x, y, lado, lado, 8, 8);
                g.setColor(GRIS_LINEA);
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(x + 1, y + 1, lado - 2, lado - 2, 8, 8);
            }
            g.dispose();
        }
    }

    private static class CasillaRenderer implements TableCellRenderer {
        private final Casilla c = new Casilla();

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            c.setSelected(Boolean.TRUE.equals(v));
            return c;
        }
    }

    /** Enlace de texto del encabezado (sin fondo ni borde). */
    public static class Enlace extends JButton {
        public Enlace() {
            super("Enlace");
            setFont(fuente(Font.BOLD, 15));
            setForeground(NARANJA_TEXTO);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    /** Cambia la familia de las fuentes definidas en el diseñador por la mejor disponible en este equipo. */
    public static void normalizarFuentes(Component c) {
        if (c.isFontSet() && !Font.MONOSPACED.equals(c.getFont().getFamily())
                && !Font.MONOSPACED.equals(c.getFont().getName())) {
            Font f = c.getFont();
            c.setFont(new Font(FAMILIA, f.getStyle(), f.getSize()));
        }
        if (c instanceof Container) {
            for (Component h : ((Container) c).getComponents()) normalizarFuentes(h);
        }
    }

    /** Escribe un valor en una celda de las fichas de información. */
    public static void valor(JLabel l, String texto) {
        l.setText("<html>" + esc(texto) + "</html>");
    }

    /** Aplica el estilo de la aplicación a una tabla creada en el diseñador. */
    public static void estilizar(JTable t, JScrollPane sp, int[] anchos) {
        t.setBackground(FONDO);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(GRIS_LINEA);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(0xFF, 0xE3, 0xC4));
        t.setSelectionForeground(Color.BLACK);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setDefaultRenderer(Object.class, new CeldaRenderer());
        t.setDefaultRenderer(Boolean.class, new CasillaRenderer());
        t.setDefaultEditor(Boolean.class, new DefaultCellEditor(new Casilla()));
        for (int i = 0; i < anchos.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        JTableHeader th = t.getTableHeader();
        th.setReorderingAllowed(false);
        th.setResizingAllowed(false);
        th.setPreferredSize(new Dimension(10, 54));
        th.setDefaultRenderer((tb, v, sel, foco, fila, col) -> {
            String s = v == null ? "" : v.toString();
            if (!s.startsWith("<html>")) s = "<html><div style='text-align:center'>" + esc(s) + "</div></html>";
            JLabel lb = new JLabel(s, SwingConstants.CENTER);
            lb.setOpaque(true);
            lb.setBackground(GRIS_ENC);
            lb.setForeground(GRIS_TXT);
            lb.setFont(fuente(Font.BOLD, 13));
            lb.setBorder(new MatteBorder(0, 0, 2, 0, GRIS_BORDE));
            return lb;
        });
        sp.setColumnHeaderView(th);
        sp.setBorder(new LineBorder(GRIS_BORDE, 2, true));
        sp.getViewport().setBackground(FONDO);
    }

    /** Habilita o deshabilita un campo junto con su etiqueta. */
    public static void habilitar(JComponent c, JLabel etiqueta, boolean activo) {
        c.setEnabled(activo);
        c.setBackground(activo ? FONDO : CAMPO_DESHAB);
        etiqueta.setForeground(activo ? Color.BLACK : GRIS_DESHAB_TXT);
    }

    // ---------------- Paneles y etiquetas ----------------
    public static JLabel etiqueta(String t, int estilo, int tam) {
        JLabel x = new JLabel(t);
        x.setFont(fuente(estilo, tam));
        return x;
    }

    /** Cuadro de dos columnas (etiqueta / valor). */
    public static JPanel ficha(String[][] filas) {
        JPanel p = new JPanel(new GridLayout(filas.length, 2));
        p.setBorder(new LineBorder(GRIS_BORDE, 2, true));
        for (String[] f : filas) {
            JLabel a = new JLabel(f[0], SwingConstants.CENTER);
            a.setOpaque(true);
            a.setBackground(GRIS_ENC);
            a.setForeground(GRIS_TXT);
            a.setFont(fuente(Font.BOLD, 13));
            a.setBorder(new LineBorder(GRIS_BORDE, 1));
            a.setPreferredSize(new Dimension(150, 42));
            JLabel b = new JLabel("<html>" + esc(f[1]) + "</html>");
            b.setOpaque(true);
            b.setBackground(FONDO);
            b.setFont(fuente(Font.PLAIN, 14));
            b.setBorder(new CompoundBorder(new LineBorder(GRIS_BORDE, 1), new EmptyBorder(0, 12, 0, 12)));
            p.add(a);
            p.add(b);
        }
        return p;
    }

    private static class CeldaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, fila, col);
            String s = v == null ? "" : v.toString();
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            setBorder(null);
            if (s.startsWith("<html><b>")) {
                setHorizontalAlignment(LEFT);
                setBorder(new EmptyBorder(0, 28, 0, 0));
            }
            setBackground(sel ? t.getSelectionBackground() : FONDO);
            setForeground(Color.BLACK);
            if (s.equals(">")) {
                setFont(fuente(Font.BOLD, 34));
                setForeground(GRIS_TXT);
                setText("›");
                return this;
            }
            setFont(fuente(Font.PLAIN, 13));
            if (!s.startsWith("<html>")) {
                int ancho = Math.max(30, t.getColumnModel().getColumn(col).getWidth() - 28);
                setText("<html><div style='text-align:center;width:" + ancho + "px'>" + esc(s) + "</div></html>");
            }
            return this;
        }
    }

    public interface ClicTabla { void clic(int fila, int col, int veces); }

    public static void alClic(JTable t, ClicTabla accion) {
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int f = t.rowAtPoint(e.getPoint()), c = t.columnAtPoint(e.getPoint());
                if (f >= 0 && c >= 0) accion.clic(f, c, e.getClickCount());
            }
        });
    }

    public static String hogarHtml(Datos.Hogar h) {
        return "<html><b>" + h.codigo + "</b><br><b>" + esc(h.jefe) + "</b></html>";
    }

    public static Double numero(String s) {
        try {
            return Double.valueOf(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
