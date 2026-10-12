package enph;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;

import enph.Nav.Rol;

import static enph.Estilo.*;

/** Jefe de hogar: finalizar la encuesta y generar el script SQL del hogar. */
public class VentanaGenerarScript extends JFrame {

    public VentanaGenerarScript() {
        initComponents();
        Nav.configurar(this, "ENPH - Finalizar encuesta", 1120, 740);
        Nav.datosJefe(lblUsuario, lblRolUsuario);
        Datos.Hogar h = Datos.hogar;

        lblHogar.setText("Hogar " + h.codigo);
        lblDia.setText("Dia actual de la encuesta: " + Datos.dia() + "/" + Datos.DIAS_ENCUESTA);
        lblFecha.setText(Datos.HOY.format(Datos.FECHA_LARGA));

        btnGenerar.setEnabled(h.finalizado);
        btnGenerar.addActionListener(e -> generarScript());
        lblAviso.setVisible(!h.finalizado);
        btnVolver.addActionListener(e -> ir(new VentanaJefe()));
    }


    private static String sql(String s) { return s == null ? "" : s.replace("'", "''"); }

    private static String sqlNum(double v) { return String.format(Locale.US, "%.2f", v); }

    private static String sqlTextoONulo(String s) {
        return (s == null || s.isEmpty()) ? "NULL" : "'" + sql(s) + "'";
    }

    private void generarScript() {
        Datos.Hogar h = Datos.hogar;
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("script_" + h.codigo + ".sql"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        if (escribirScript(h, selector.getSelectedFile())) {
            mensaje("Script generado correctamente:\n" + selector.getSelectedFile().getAbsolutePath());
        } else {
            mensaje("No se pudo crear el archivo del script.");
        }
    }

    /** Script SQL del hogar finalizado (tablas según el diccionario de datos del documento). */
    private boolean escribirScript(Datos.Hogar h, File destino) {
        String num = String.format("%02d", Integer.parseInt(h.codigo.replaceAll("\\D", "")));
        String idCuadernillo = "CUAD" + h.codigo;
        String[] palabras = h.jefe.split("\\s+");
        int mitad = Math.max(1, palabras.length / 2);
        String nombres = String.join(" ", Arrays.copyOfRange(palabras, 0, mitad));
        String apellidos = String.join(" ", Arrays.copyOfRange(palabras, mitad, palabras.length));

        try (PrintWriter o = new PrintWriter(new FileWriter(destino, StandardCharsets.UTF_8))) {
            o.println("-- Script generado por el sistema ENPH - Cuadernillo 2 Urbano");
            o.println("-- Hogar " + h.codigo + " | generado el " + Datos.HOY.format(Datos.FECHA_CORTA));
            o.println();
            o.println("CREATE TABLE IF NOT EXISTS VIVIENDA (id_vivienda VARCHAR(10) PRIMARY KEY, Direccion VARCHAR(150) NOT NULL, "
                    + "ReferenciaUbicacion VARCHAR(150) NOT NULL, Piso VARCHAR(10) NULL, id_recuentista VARCHAR(10) NULL);");
            o.println("CREATE TABLE IF NOT EXISTS HOGAR (id_hogar VARCHAR(10) PRIMARY KEY, TotalPersonas INT NOT NULL, "
                    + "id_vivienda VARCHAR(10) NOT NULL, FOREIGN KEY (id_vivienda) REFERENCES VIVIENDA(id_vivienda));");
            o.println("CREATE TABLE IF NOT EXISTS ENCUESTADO (id_encuestado VARCHAR(10) PRIMARY KEY, ComNombre VARCHAR(50) NOT NULL, "
                    + "ComApellido VARCHAR(50) NOT NULL, NumeroOrden INT NOT NULL, Telefono VARCHAR(15) NULL, Correo VARCHAR(100) NULL, "
                    + "id_hogar VARCHAR(10) NOT NULL, FOREIGN KEY (id_hogar) REFERENCES HOGAR(id_hogar));");
            o.println("CREATE TABLE IF NOT EXISTS CUADERNILLO_2 (id_cuadernillo VARCHAR(10) PRIMARY KEY, FechaInicio DATE NOT NULL, "
                    + "FechaFinalizacion DATE NOT NULL, Estado VARCHAR(30) NOT NULL, id_hogar VARCHAR(10) NOT NULL, "
                    + "FOREIGN KEY (id_hogar) REFERENCES HOGAR(id_hogar));");
            o.println("CREATE TABLE IF NOT EXISTS CONTROL_VISITA (id_control_visita VARCHAR(10) PRIMARY KEY, NumeroVisita INT NOT NULL, "
                    + "Fecha DATE NOT NULL, HoraInicioVisita TIME NOT NULL, HoraFinalizacionVisita TIME NOT NULL, "
                    + "ResultadoVisita VARCHAR(50) NULL, FechaHoraProximaVisita DATETIME NULL, id_cuadernillo VARCHAR(10) NOT NULL, "
                    + "FOREIGN KEY (id_cuadernillo) REFERENCES CUADERNILLO_2(id_cuadernillo));");
            o.println("CREATE TABLE IF NOT EXISTS GASTO_DIARIO (id_gasto_diario VARCHAR(10) PRIMARY KEY, Fecha DATE NOT NULL, "
                    + "DiaSemana VARCHAR(15) NOT NULL, NumeroOrden INT NOT NULL, ArticuloServicio VARCHAR(100) NOT NULL, "
                    + "CantidadAdquirida DECIMAL(10,2) NOT NULL, UnidadMedida VARCHAR(50) NOT NULL, "
                    + "ContenidoPorUnidad DECIMAL(10,2) NULL, UnidadContenido VARCHAR(50) NULL, EquivalenciaUnidad VARCHAR(50) NULL, "
                    + "FormaAdquisicion VARCHAR(50) NOT NULL, LugarCompraObtencion VARCHAR(100) NOT NULL, "
                    + "ValorPagado DECIMAL(10,2) NOT NULL, FrecuenciaCompra VARCHAR(50) NULL, TipoGasto VARCHAR(50) NOT NULL, "
                    + "id_cuadernillo VARCHAR(10) NOT NULL, FOREIGN KEY (id_cuadernillo) REFERENCES CUADERNILLO_2(id_cuadernillo));");
            o.println();
            o.println("INSERT INTO VIVIENDA VALUES ('" + sql(h.codigoVivienda) + "','" + sql(h.direccion) + "','"
                    + sql(h.barrio) + "',NULL,NULL);");
            o.println("INSERT INTO HOGAR VALUES ('" + sql(h.codigo) + "'," + h.personas + ",'" + sql(h.codigoVivienda) + "');");
            o.println("INSERT INTO ENCUESTADO VALUES ('E" + sql(h.codigo) + "','" + sql(nombres) + "','" + sql(apellidos)
                    + "',1," + sqlTextoONulo(h.telefono) + "," + sqlTextoONulo(h.correo) + ",'" + sql(h.codigo) + "');");
            o.println("INSERT INTO CUADERNILLO_2 VALUES ('" + idCuadernillo + "','" + h.inicio + "','"
                    + h.inicio.plusDays(Datos.DIAS_ENCUESTA - 1) + "','" + (h.finalizado ? "Finalizado" : "En recolección")
                    + "','" + sql(h.codigo) + "');");
            for (Datos.Visita v : h.visitas) {
                o.println("INSERT INTO CONTROL_VISITA VALUES ('CV" + sql(h.codigo) + String.format("%02d", v.numero) + "',"
                        + v.numero + ",'" + v.fecha + "','" + v.inicio.format(Datos.HORA) + ":00','" + v.fin.format(Datos.HORA)
                        + ":00','Realizada',NULL,'" + idCuadernillo + "');");
            }
            int seq = 1;
            for (Datos.Gasto g : h.gastos) {
                String diaSemana = g.fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, Datos.ES);
                diaSemana = diaSemana.substring(0, 1).toUpperCase(Datos.ES) + diaSemana.substring(1);
                o.println("INSERT INTO GASTO_DIARIO VALUES ('GD" + num + String.format("%04d", seq++) + "','" + g.fecha + "','"
                        + diaSemana + "'," + g.orden + ",'" + sql(g.alimento) + "'," + sqlNum(g.cantidad) + ",'" + sql(g.unidad)
                        + "'," + (g.contenido == null ? "NULL" : sqlNum(g.contenido)) + "," + sqlTextoONulo(g.unidadContenido)
                        + "," + sqlTextoONulo(Datos.equivalencia(g)) + ",'" + sql(g.forma) + "','" + sql(g.lugar) + "',"
                        + sqlNum(g.valor) + "," + sqlTextoONulo(g.frecuencia) + ",'" + sql(g.tipo) + "','" + idCuadernillo + "');");
            }
            return true;
        } catch (IOException e) {
            return false;
        }
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

        java.awt.GridBagConstraints gridBagConstraints;

        panelRaiz = new javax.swing.JPanel();
        pnlFondo = new enph.Nav.FondoTablet();
        pnlDispositivo = new javax.swing.JPanel();
        pnlEncabezado = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        pnlUsuario = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        lblRolUsuario = new javax.swing.JLabel();
        pnlCuerpo = new javax.swing.JPanel();
        pnlTop = new javax.swing.JPanel();
        pnlIzq = new javax.swing.JPanel();
        lblHogar = new javax.swing.JLabel();
        lblDia = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        pnlCentro = new javax.swing.JPanel();
        pnlPila = new javax.swing.JPanel();
        btnGenerar = new enph.Estilo.Boton();
        lblAviso = new javax.swing.JLabel();
        pnlSur = new javax.swing.JPanel();
        btnVolver = new enph.Estilo.Boton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelRaiz.setPreferredSize(new java.awt.Dimension(1120, 740));
        panelRaiz.setLayout(new java.awt.BorderLayout());
        pnlFondo.setLayout(new java.awt.GridBagLayout());
        pnlDispositivo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(64, 68, 74), 14, true));
        pnlDispositivo.setLayout(new java.awt.BorderLayout());
        pnlEncabezado.setBackground(new java.awt.Color(255, 184, 113));
        pnlEncabezado.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 28, 14, 28));
        pnlEncabezado.setLayout(new java.awt.BorderLayout());
        lblLogo.setText("ENPH");
        lblLogo.setFont(new java.awt.Font("Poppins", 1, 36));
        lblLogo.setForeground(new java.awt.Color(201, 110, 18));
        pnlEncabezado.add(lblLogo, java.awt.BorderLayout.WEST);
        pnlUsuario.setOpaque(false);
        pnlUsuario.setLayout(new java.awt.GridLayout(2, 1));
        lblUsuario.setText("Claudia Marcela Gomez Leon");
        lblUsuario.setFont(new java.awt.Font("Poppins", 0, 15));
        lblUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlUsuario.add(lblUsuario);
        lblRolUsuario.setText("Jefe de hogar de HBGA06");
        lblRolUsuario.setFont(new java.awt.Font("Poppins", 1, 15));
        lblRolUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlUsuario.add(lblRolUsuario);
        pnlEncabezado.add(pnlUsuario, java.awt.BorderLayout.EAST);
        pnlDispositivo.add(pnlEncabezado, java.awt.BorderLayout.NORTH);
        pnlCuerpo.setBackground(new java.awt.Color(242, 242, 242));
        pnlCuerpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(26, 50, 22, 50));
        pnlCuerpo.setLayout(new java.awt.BorderLayout());
        pnlTop.setOpaque(false);
        pnlTop.setLayout(new java.awt.BorderLayout());
        pnlIzq.setOpaque(false);
        pnlIzq.setLayout(new java.awt.GridLayout(2, 1));
        lblHogar.setText("Hogar HBGA06");
        lblHogar.setFont(new java.awt.Font("Poppins", 1, 40));
        pnlIzq.add(lblHogar);
        lblDia.setText("Dia actual de la encuesta: 14/14");
        lblDia.setFont(new java.awt.Font("Poppins", 0, 18));
        pnlIzq.add(lblDia);
        pnlTop.add(pnlIzq, java.awt.BorderLayout.WEST);
        lblFecha.setText("10 de octubre el 2026");
        lblFecha.setFont(new java.awt.Font("Poppins", 0, 18));
        lblFecha.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        pnlTop.add(lblFecha, java.awt.BorderLayout.EAST);
        pnlCuerpo.add(pnlTop, java.awt.BorderLayout.NORTH);
        pnlCentro.setOpaque(false);
        pnlCentro.setLayout(new java.awt.GridBagLayout());
        pnlPila.setOpaque(false);
        pnlPila.setLayout(new java.awt.BorderLayout(0, 16));
        btnGenerar.setText("Generar Script");
        btnGenerar.setColorFondo(new java.awt.Color(176, 211, 70));
        btnGenerar.setColorTexto(new java.awt.Color(107, 127, 37));
        btnGenerar.setFont(new java.awt.Font("Poppins", 1, 28));
        btnGenerar.setPreferredSize(new java.awt.Dimension(340, 110));
        pnlPila.add(btnGenerar, java.awt.BorderLayout.CENTER);
        lblAviso.setText("<html><center>El encuestador debe cerrar la encuesta<br>antes de generar el script.</center></html>");
        lblAviso.setFont(new java.awt.Font("Poppins", 0, 14));
        lblAviso.setForeground(new java.awt.Color(85, 85, 85));
        lblAviso.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnlPila.add(lblAviso, java.awt.BorderLayout.SOUTH);
        gridBagConstraints = new java.awt.GridBagConstraints();
        pnlCentro.add(pnlPila, gridBagConstraints);
        pnlCuerpo.add(pnlCentro, java.awt.BorderLayout.CENTER);
        pnlSur.setOpaque(false);
        pnlSur.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        btnVolver.setText("Volver");
        btnVolver.setColorFondo(new java.awt.Color(203, 203, 203));
        btnVolver.setColorTexto(new java.awt.Color(85, 85, 85));
        btnVolver.setFont(new java.awt.Font("Poppins", 1, 18));
        btnVolver.setPreferredSize(new java.awt.Dimension(150, 48));
        pnlSur.add(btnVolver);
        pnlCuerpo.add(pnlSur, java.awt.BorderLayout.SOUTH);
        pnlDispositivo.add(pnlCuerpo, java.awt.BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(22, 40, 22, 40);
        pnlFondo.add(pnlDispositivo, gridBagConstraints);
        panelRaiz.add(pnlFondo, java.awt.BorderLayout.CENTER);
        getContentPane().add(panelRaiz, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panelRaiz;
    private enph.Nav.FondoTablet pnlFondo;
    private javax.swing.JPanel pnlDispositivo;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JPanel pnlUsuario;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblRolUsuario;
    private javax.swing.JPanel pnlCuerpo;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JPanel pnlIzq;
    private javax.swing.JLabel lblHogar;
    private javax.swing.JLabel lblDia;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JPanel pnlCentro;
    private javax.swing.JPanel pnlPila;
    private enph.Estilo.Boton btnGenerar;
    private javax.swing.JLabel lblAviso;
    private javax.swing.JPanel pnlSur;
    private enph.Estilo.Boton btnVolver;
    // End of variables declaration//GEN-END:variables
}
