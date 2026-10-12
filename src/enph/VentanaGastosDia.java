package enph;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import enph.Nav.Rol;

import static enph.Estilo.*;

/** Gastos de un día: detalle, edición y revisión. */
public class VentanaGastosDia extends JFrame {
    private final Rol rol;

    /** @param abrirDetalle si no es null, se abre el detalle de ese gasto al mostrar la ventana. */
    public VentanaGastosDia(Rol rol, int dia, Datos.Gasto abrirDetalle) {
        initComponents();
        Nav.configurar(this, "ENPH - Gastos del día " + dia, 1120, 740);
        Nav.enlazarEncabezado(this, lnkVisita, lnkHogar, lnkSalir);
        this.rol = rol;
        Datos.Hogar h = Datos.hogar;
        List<Datos.Gasto> gs = h.gastosDelDia(dia);
        boolean lectura = h.diasRevisados.contains(dia);

        lblTitulo.setText("Gastos del Dia " + dia);
        lblSubtitulo.setText("Fecha: " + Datos.fechaDelDia(dia).format(Datos.FECHA_CORTA));

        DefaultTableModel m = (DefaultTableModel) tabla.getModel();
        for (Datos.Gasto g : gs) {
            m.addRow(new Object[]{g.orden, g.alimento, Datos.cantidadTxt(g), Datos.contenidoTxt(g), Datos.fmt(g.valor),
                    g.forma, Datos.frecuenciaTxt(g), g.lugar, g.tipo, g.revisado, lectura ? "" : "Editar"});
        }
        estilizar(tabla, spTabla, new int[]{40, 90, 90, 90, 60, 110, 100, 90, 90, 50, 70});
        tabla.setShowVerticalLines(true);                    // el prototipo muestra la cuadrícula completa
        tabla.setGridColor(GRIS_BORDE);
        if (lectura) tabla.setDefaultEditor(Boolean.class, null);   // día revisado: solo lectura
        tabla.getColumnModel().getColumn(10).setCellRenderer((tb, v, sel, foco, fila, col) -> {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(FONDO);
            if (v != null && !v.toString().isEmpty()) {
                p.add(new Boton("Editar", NARANJA, Color.WHITE, false, fuente(Font.BOLD, 11), 56, 22));
            }
            return p;
        });
        // La casilla se marca directamente en la fila (como en el prototipo); "Editar" abre el formulario
        alClic(tabla, (fila, col, veces) -> {
            Datos.Gasto g = gs.get(fila);
            if (col == 10 && !lectura) {
                irAEditar(g, dia);
            } else if (col != 9) {
                detalleGasto(g, lectura);
            }
        });
        m.addTableModelListener(ev -> {
            if (ev.getType() != javax.swing.event.TableModelEvent.UPDATE || ev.getColumn() != 9 || lectura) return;
            int fila = ev.getFirstRow();
            if (fila < 0 || fila >= gs.size()) return;
            gs.get(fila).revisado = Boolean.TRUE.equals(m.getValueAt(fila, 9));
            Datos.guardar();
            btnFinalizar.setEnabled(todosRevisados(gs));
        });

        btnFinalizar.addActionListener(e -> {
            h.diasRevisados.add(dia);
            Datos.guardar();
            ir(new VentanaHistorial(rol));
        });
        btnFinalizar.setEnabled(!lectura && todosRevisados(gs));
        btnVolver.addActionListener(e -> ir(new VentanaHistorial(rol)));
        Nav.aplicarRol(rol, panelRaiz, pnlEncabezado, pnlEnlaces, lblUsuario, lblRolUsuario, pnlCuerpo);

        if (abrirDetalle != null) {
            SwingUtilities.invokeLater(() -> detalleGasto(abrirDetalle, false));
        }
    }


    private static boolean todosRevisados(List<Datos.Gasto> gs) {
        for (Datos.Gasto g : gs) if (!g.revisado) return false;
        return true;
    }

    private void irAEditar(Datos.Gasto g, int dia) {
        ir(new VentanaFormularioGasto(rol, g,
                () -> new VentanaGastosDia(rol, dia, null),
                () -> new VentanaGastosDia(rol, dia, g)));
    }

    /** Detalle del gasto con equivalencia calculada; opciones "Marcar como revisado" y "Editar". */
    private void detalleGasto(Datos.Gasto g, boolean lectura) {
        JDialog d = new JDialog(this, "Gasto No. " + g.orden, true);
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(FONDO);
        p.setBorder(new EmptyBorder(18, 22, 18, 22));
        p.add(etiqueta("Gasto No. " + g.orden + " - Día " + g.dia, Font.BOLD, 20), BorderLayout.NORTH);
        p.add(ficha(new String[][]{
                {"Número de orden", String.valueOf(g.orden)},
                {"Alimento o bebida", g.alimento},
                {"Cantidad adquirida", Datos.fmt(g.cantidad)},
                {"Unidad de medida", g.unidad},
                {"Contenido por unidad", Datos.contenidoTxt(g)},
                {"Equivalencia (g / cc)", Datos.equivalencia(g)},
                {"Forma de adquisición", g.forma},
                {"Valor pagado o estimado", "$ " + Datos.fmt(g.valor)},
                {"Frecuencia de compra", Datos.frecuenciaTxt(g)},
                {"Lugar de compra u obtención", g.lugar},
                {"Tipo de gasto", g.tipo}}), BorderLayout.CENTER);

        Boton marcar = new Boton("Marcar como revisado", NARANJA, Color.WHITE, false, fuente(Font.BOLD, 14), 210, 42);
        marcar.setEnabled(!lectura && !g.revisado);
        marcar.addActionListener(e -> {
            g.revisado = true;
            Datos.guardar();
            d.dispose();
            ir(new VentanaGastosDia(rol, g.dia, null));
        });
        Boton editar = new Boton("Editar", GRIS_ENC, GRIS_TXT, false, fuente(Font.BOLD, 14), 110, 42);
        editar.setEnabled(!lectura);
        editar.addActionListener(e -> {
            d.dispose();
            irAEditar(g, g.dia);
        });
        Boton cerrar = new Boton("Cerrar", GRIS_ENC, GRIS_TXT, false, fuente(Font.BOLD, 14), 100, 42);
        cerrar.addActionListener(e -> d.dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        botones.add(marcar);
        botones.add(editar);
        botones.add(cerrar);
        p.add(botones, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // ------------------------------------------------------------------
    // Navegación (fuera del código generado por el diseñador)
    // ------------------------------------------------------------------
    private void ir(JFrame siguiente) { Nav.ir(this, siguiente); }

    private void mensaje(String texto) { Nav.mensaje(this, texto); }

    /** This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRaiz = new javax.swing.JPanel();
        pnlEncabezado = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        pnlEnlaces = new javax.swing.JPanel();
        lnkVisita = new enph.Estilo.Enlace();
        lnkHogar = new enph.Estilo.Enlace();
        lnkSalir = new enph.Estilo.Enlace();
        pnlUsuario = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        lblRolUsuario = new javax.swing.JLabel();
        pnlCuerpo = new javax.swing.JPanel();
        pnlNorte = new javax.swing.JPanel();
        pnlVolver = new javax.swing.JPanel();
        btnVolver = new enph.Estilo.Boton();
        pnlTitulos = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        spTabla = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        pnlFinalizar = new javax.swing.JPanel();
        btnFinalizar = new enph.Estilo.Boton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelRaiz.setPreferredSize(new java.awt.Dimension(1120, 740));
        panelRaiz.setLayout(new java.awt.BorderLayout());
        pnlEncabezado.setBackground(new java.awt.Color(255, 184, 113));
        pnlEncabezado.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 28, 14, 28));
        pnlEncabezado.setLayout(new java.awt.BorderLayout());
        lblLogo.setText("ENPH");
        lblLogo.setFont(new java.awt.Font("Poppins", 1, 36));
        lblLogo.setForeground(new java.awt.Color(201, 110, 18));
        pnlEncabezado.add(lblLogo, java.awt.BorderLayout.WEST);
        pnlEnlaces.setOpaque(false);
        pnlEnlaces.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 26, 14));
        lnkVisita.setText("Hogares pendientes del día");
        lnkVisita.setFont(new java.awt.Font("Poppins", 1, 15));
        lnkVisita.setForeground(new java.awt.Color(201, 110, 18));
        pnlEnlaces.add(lnkVisita);
        lnkHogar.setText("Hogares asignados");
        lnkHogar.setFont(new java.awt.Font("Poppins", 1, 15));
        lnkHogar.setForeground(new java.awt.Color(201, 110, 18));
        pnlEnlaces.add(lnkHogar);
        lnkSalir.setText("Salir");
        lnkSalir.setFont(new java.awt.Font("Poppins", 1, 15));
        lnkSalir.setForeground(new java.awt.Color(201, 110, 18));
        pnlEnlaces.add(lnkSalir);
        pnlEncabezado.add(pnlEnlaces, java.awt.BorderLayout.CENTER);
        pnlUsuario.setOpaque(false);
        pnlUsuario.setLayout(new java.awt.GridLayout(2, 1));
        lblUsuario.setText("Albertano SantaCruz Martinez");
        lblUsuario.setFont(new java.awt.Font("Poppins", 0, 15));
        lblUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlUsuario.add(lblUsuario);
        lblRolUsuario.setText("Encuestador");
        lblRolUsuario.setFont(new java.awt.Font("Poppins", 1, 15));
        lblRolUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlUsuario.add(lblRolUsuario);
        pnlEncabezado.add(pnlUsuario, java.awt.BorderLayout.EAST);
        panelRaiz.add(pnlEncabezado, java.awt.BorderLayout.NORTH);
        pnlCuerpo.setBackground(new java.awt.Color(242, 242, 242));
        pnlCuerpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 40, 18, 40));
        pnlCuerpo.setLayout(new java.awt.BorderLayout(0, 12));
        pnlNorte.setOpaque(false);
        pnlNorte.setLayout(new java.awt.BorderLayout(0, 6));
        pnlVolver.setOpaque(false);
        pnlVolver.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        btnVolver.setText("‹  Volver");
        btnVolver.setColorFondo(new java.awt.Color(203, 203, 203));
        btnVolver.setColorTexto(new java.awt.Color(0, 0, 0));
        btnVolver.setFont(new java.awt.Font("Poppins", 0, 14));
        btnVolver.setPreferredSize(new java.awt.Dimension(110, 34));
        pnlVolver.add(btnVolver);
        pnlNorte.add(pnlVolver, java.awt.BorderLayout.NORTH);
        pnlTitulos.setOpaque(false);
        pnlTitulos.setLayout(new java.awt.GridLayout(2, 1));
        lblTitulo.setText("Gastos del Dia 14");
        lblTitulo.setFont(new java.awt.Font("Poppins", 1, 30));
        pnlTitulos.add(lblTitulo);
        lblSubtitulo.setText("Fecha: 10/10/2026");
        lblSubtitulo.setFont(new java.awt.Font("Poppins", 0, 14));
        pnlTitulos.add(lblSubtitulo);
        pnlNorte.add(pnlTitulos, java.awt.BorderLayout.CENTER);
        pnlCuerpo.add(pnlNorte, java.awt.BorderLayout.NORTH);
        spTabla.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(136, 136, 136), 2, true));
        tabla.setModel(new javax.swing.table.DefaultTableModel(
                    new Object [][] {
        
                    },
                    new String [] {
                        "No.", "<html><center>Alimento o<br>Bebida</center></html>", "<html><center>Cantidad<br>adquirida</center></html>", "<html><center>Contenido<br>por unidad</center></html>", "Valor", "<html><center>Forma de<br>adquisición</center></html>", "<html><center>Frecuencia<br>de compra</center></html>", "Lugar", "<html><center>Tipo de<br>gasto</center></html>", "", ""
                    }
                ) {
                    Class[] types = new Class [] {
                        java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class, java.lang.Object.class
                    };
                    boolean[] canEdit = new boolean [] {
                        false, false, false, false, false, false, false, false, false, true, false
                    };
        
                    public Class getColumnClass(int columnIndex) {
                        return types [columnIndex];
                    }
        
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return canEdit [columnIndex];
                    }
                });
        tabla.setRowHeight(62);
        tabla.setBackground(new java.awt.Color(242, 242, 242));
        tabla.setGridColor(new java.awt.Color(204, 204, 204));
        tabla.setSelectionBackground(new java.awt.Color(255, 227, 196));
        tabla.setSelectionForeground(new java.awt.Color(0, 0, 0));
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        spTabla.setViewportView(tabla);
        pnlCuerpo.add(spTabla, java.awt.BorderLayout.CENTER);
        pnlFinalizar.setOpaque(false);
        pnlFinalizar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 0));
        btnFinalizar.setText("Finalizar revisión");
        btnFinalizar.setColorFondo(new java.awt.Color(235, 128, 24));
        btnFinalizar.setColorTexto(new java.awt.Color(255, 255, 255));
        btnFinalizar.setFont(new java.awt.Font("Poppins", 1, 18));
        btnFinalizar.setPreferredSize(new java.awt.Dimension(230, 48));
        pnlFinalizar.add(btnFinalizar);
        pnlCuerpo.add(pnlFinalizar, java.awt.BorderLayout.SOUTH);
        panelRaiz.add(pnlCuerpo, java.awt.BorderLayout.CENTER);
        getContentPane().add(panelRaiz, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panelRaiz;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JPanel pnlEnlaces;
    private enph.Estilo.Enlace lnkVisita;
    private enph.Estilo.Enlace lnkHogar;
    private enph.Estilo.Enlace lnkSalir;
    private javax.swing.JPanel pnlUsuario;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblRolUsuario;
    private javax.swing.JPanel pnlCuerpo;
    private javax.swing.JPanel pnlNorte;
    private javax.swing.JPanel pnlVolver;
    private enph.Estilo.Boton btnVolver;
    private javax.swing.JPanel pnlTitulos;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JScrollPane spTabla;
    private javax.swing.JTable tabla;
    private javax.swing.JPanel pnlFinalizar;
    private enph.Estilo.Boton btnFinalizar;
    // End of variables declaration//GEN-END:variables
}
