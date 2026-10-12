package enph;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.function.Supplier;

import enph.Nav.Rol;

import static enph.Estilo.*;

/** Formulario "Agregar Gasto" / "Editar gasto". */
public class VentanaFormularioGasto extends JFrame {

    public VentanaFormularioGasto(Rol rol, Datos.Gasto editar, Supplier<JFrame> volver,
                                  Supplier<JFrame> despuesDeGuardar) {
        initComponents();
        Nav.configurar(this, editar != null ? "ENPH - Editar gasto" : "ENPH - Agregar gasto", 1120, 740);
        Nav.enlazarEncabezado(this, lnkVisita, lnkHogar, lnkSalir);
        Datos.Hogar h = Datos.hogar;
        boolean edicion = editar != null;

        lblTituloCaja.setText(edicion ? "Editar gasto" : "Agregar Gasto");

        if (edicion) {
            tfNombre.setText(editar.alimento);
            tfCantidad.setText(Datos.fmt(editar.cantidad));
            cbUnidad.setSelectedItem(editar.unidad);
            if (editar.contenido != null) {
                tfContenido.setText(Datos.fmt(editar.contenido));
                cbUContenido.setSelectedItem(editar.unidadContenido);
            }
            cbForma.setSelectedItem(editar.forma);
            if (editar.frecuencia != null && !editar.frecuencia.isEmpty()) cbFrecuencia.setSelectedItem(editar.frecuencia);
            cbLugar.setSelectedItem(editar.lugar);
            cbTipo.setSelectedItem(editar.tipo);
            tfValor.setText(Datos.fmt(editar.valor));
        }

        // "Contenido por unidad" solo con Unidad; "Frecuencia de compra" solo con Compra
        Runnable actualizar = () -> {
            boolean porUnidad = "Unidad".equals(cbUnidad.getSelectedItem());
            habilitar(tfContenido, lblContenido, porUnidad);
            habilitar(cbUContenido, lblUContenido, porUnidad);
            if (!porUnidad) {
                tfContenido.setText("");
                cbUContenido.setSelectedIndex(0);
            }
            boolean compra = "Compra".equals(cbForma.getSelectedItem());
            habilitar(cbFrecuencia, lblFrecuencia, compra);
            if (!compra) cbFrecuencia.setSelectedIndex(0);
        };
        actualizar.run();
        cbUnidad.addActionListener(e -> actualizar.run());
        cbForma.addActionListener(e -> actualizar.run());

        btnGuardar.setText(edicion ? "Guardar Cambios" : "Guardar  ›");
        btnGuardar.setPreferredSize(new Dimension(edicion ? 240 : 190, 46));
        btnGuardar.addActionListener(e -> {
            String nombre = tfNombre.getText().trim();
            if (nombre.isEmpty()) { mensaje("Ingrese el nombre del alimento o bebida."); return; }
            Double cantidad = numero(tfCantidad.getText());
            if (cantidad == null || cantidad <= 0) { mensaje("La cantidad adquirida debe ser un número mayor que cero."); return; }
            if (cbUnidad.getSelectedIndex() == 0) { mensaje("Seleccione la unidad de medida."); return; }
            String unidad = (String) cbUnidad.getSelectedItem();
            Double contenido = null;
            String uContenido = null;
            if (unidad.equals("Unidad")) {
                contenido = numero(tfContenido.getText());
                if (contenido == null || contenido <= 0) { mensaje("Ingrese el contenido por unidad (número mayor que cero)."); return; }
                if (cbUContenido.getSelectedIndex() == 0) { mensaje("Seleccione la unidad del contenido."); return; }
                uContenido = (String) cbUContenido.getSelectedItem();
            }
            if (cbForma.getSelectedIndex() == 0) { mensaje("Seleccione la forma de adquisición."); return; }
            String forma = (String) cbForma.getSelectedItem();
            String frecuencia = "";
            if (forma.equals("Compra")) {
                if (cbFrecuencia.getSelectedIndex() == 0) { mensaje("Seleccione la frecuencia de compra."); return; }
                frecuencia = (String) cbFrecuencia.getSelectedItem();
            }
            if (cbLugar.getSelectedIndex() == 0) { mensaje("Seleccione el lugar de compra u obtención."); return; }
            Double precio = numero(tfValor.getText());
            if (precio == null || precio < 0) { mensaje("El valor pagado o estimado debe ser un número (no negativo)."); return; }
            if (cbTipo.getSelectedIndex() == 0) { mensaje("Seleccione el tipo de gasto."); return; }

            Datos.Gasto g = edicion ? editar : new Datos.Gasto();
            if (!edicion) {
                g.dia = Datos.dia();
                g.fecha = Datos.fechaDelDia(g.dia);
                g.orden = h.gastosDelDia(g.dia).size() + 1;
            }
            g.alimento = nombre; g.cantidad = cantidad; g.unidad = unidad; g.contenido = contenido;
            g.unidadContenido = uContenido; g.forma = forma; g.frecuencia = frecuencia;
            g.lugar = (String) cbLugar.getSelectedItem(); g.valor = precio; g.tipo = (String) cbTipo.getSelectedItem();
            g.revisado = false;                           // un gasto nuevo o editado debe revisarse de nuevo
            if (!edicion) h.gastos.add(g);
            h.diasRevisados.remove(g.dia);
            Datos.guardar();
            ir(despuesDeGuardar.get());
        });
        btnCancelar.addActionListener(e -> ir(volver.get()));
        btnVolver.addActionListener(e -> ir(volver.get()));
        Nav.aplicarRol(rol, panelRaiz, pnlEncabezado, pnlEnlaces, lblUsuario, lblRolUsuario, pnlCuerpo);
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
        btnVolver = new enph.Estilo.Boton();
        pnlCaja = new javax.swing.JPanel();
        lblTituloCaja = new javax.swing.JLabel();
        pnlColumnas = new javax.swing.JPanel();
        pnlIzq = new javax.swing.JPanel();
        pnlCampoNombreAlimento = new javax.swing.JPanel();
        lblNombreAlimento = new javax.swing.JLabel();
        pnlIntNombreAlimento = new javax.swing.JPanel();
        tfNombre = new javax.swing.JTextField();
        pnlParejaCantidad = new javax.swing.JPanel();
        pnlCampoCantidad = new javax.swing.JPanel();
        lblCantidad = new javax.swing.JLabel();
        pnlIntCantidad = new javax.swing.JPanel();
        tfCantidad = new javax.swing.JTextField();
        pnlCampoUnidad = new javax.swing.JPanel();
        lblUnidad = new javax.swing.JLabel();
        pnlIntUnidad = new javax.swing.JPanel();
        cbUnidad = new javax.swing.JComboBox<>();
        pnlParejaContenido = new javax.swing.JPanel();
        pnlCampoContenido = new javax.swing.JPanel();
        lblContenido = new javax.swing.JLabel();
        pnlIntContenido = new javax.swing.JPanel();
        tfContenido = new javax.swing.JTextField();
        pnlCampoUContenido = new javax.swing.JPanel();
        lblUContenido = new javax.swing.JLabel();
        pnlIntUContenido = new javax.swing.JPanel();
        cbUContenido = new javax.swing.JComboBox<>();
        pnlCampoValorTotal = new javax.swing.JPanel();
        lblValorTotal = new javax.swing.JLabel();
        pnlIntValorTotal = new javax.swing.JPanel();
        pnlValor = new javax.swing.JPanel();
        lblSigno = new javax.swing.JLabel();
        tfValor = new javax.swing.JTextField();
        pnlDer = new javax.swing.JPanel();
        pnlCampoForma = new javax.swing.JPanel();
        lblForma = new javax.swing.JLabel();
        pnlIntForma = new javax.swing.JPanel();
        cbForma = new javax.swing.JComboBox<>();
        pnlCampoFrecuencia = new javax.swing.JPanel();
        lblFrecuencia = new javax.swing.JLabel();
        pnlIntFrecuencia = new javax.swing.JPanel();
        cbFrecuencia = new javax.swing.JComboBox<>();
        pnlCampoLugar = new javax.swing.JPanel();
        lblLugar = new javax.swing.JLabel();
        pnlIntLugar = new javax.swing.JPanel();
        cbLugar = new javax.swing.JComboBox<>();
        pnlCampoTipo = new javax.swing.JPanel();
        lblTipo = new javax.swing.JLabel();
        pnlIntTipo = new javax.swing.JPanel();
        cbTipo = new javax.swing.JComboBox<>();
        pnlSur = new javax.swing.JPanel();
        btnCancelar = new enph.Estilo.Boton();
        btnGuardar = new enph.Estilo.Boton();

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
        pnlCuerpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 28, 14, 28));
        pnlCuerpo.setLayout(new java.awt.BorderLayout(0, 10));
        pnlNorte.setOpaque(false);
        pnlNorte.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        btnVolver.setText("‹  Volver");
        btnVolver.setColorFondo(new java.awt.Color(203, 203, 203));
        btnVolver.setColorTexto(new java.awt.Color(0, 0, 0));
        btnVolver.setFont(new java.awt.Font("Poppins", 0, 14));
        btnVolver.setPreferredSize(new java.awt.Dimension(110, 34));
        pnlNorte.add(btnVolver);
        pnlCuerpo.add(pnlNorte, java.awt.BorderLayout.NORTH);
        pnlCaja.setBackground(new java.awt.Color(232, 232, 232));
        pnlCaja.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204), 3, true), javax.swing.BorderFactory.createEmptyBorder(10, 30, 14, 30)));
        pnlCaja.setLayout(new java.awt.BorderLayout(0, 8));
        lblTituloCaja.setText("Agregar Gasto");
        lblTituloCaja.setFont(new java.awt.Font("Poppins", 1, 24));
        pnlCaja.add(lblTituloCaja, java.awt.BorderLayout.NORTH);
        pnlColumnas.setOpaque(false);
        pnlColumnas.setLayout(new java.awt.GridLayout(1, 2, 44, 0));
        pnlIzq.setOpaque(false);
        pnlIzq.setLayout(new java.awt.GridLayout(4, 1, 0, 10));
        pnlCampoNombreAlimento.setOpaque(false);
        pnlCampoNombreAlimento.setLayout(new java.awt.BorderLayout(0, 4));
        lblNombreAlimento.setText("Nombre del alimento o bebida");
        lblNombreAlimento.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoNombreAlimento.add(lblNombreAlimento, java.awt.BorderLayout.NORTH);
        pnlIntNombreAlimento.setOpaque(false);
        pnlIntNombreAlimento.setLayout(new java.awt.BorderLayout());
        tfNombre.setFont(new java.awt.Font("Poppins", 0, 14));
        tfNombre.setBackground(new java.awt.Color(242, 242, 242));
        tfNombre.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(136, 136, 136), 2, true), javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tfNombre.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntNombreAlimento.add(tfNombre, java.awt.BorderLayout.NORTH);
        pnlCampoNombreAlimento.add(pnlIntNombreAlimento, java.awt.BorderLayout.CENTER);
        pnlIzq.add(pnlCampoNombreAlimento);
        pnlParejaCantidad.setOpaque(false);
        pnlParejaCantidad.setLayout(new java.awt.GridLayout(1, 2, 12, 0));
        pnlCampoCantidad.setOpaque(false);
        pnlCampoCantidad.setLayout(new java.awt.BorderLayout(0, 4));
        lblCantidad.setText("Cantidad adquirida");
        lblCantidad.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoCantidad.add(lblCantidad, java.awt.BorderLayout.NORTH);
        pnlIntCantidad.setOpaque(false);
        pnlIntCantidad.setLayout(new java.awt.BorderLayout());
        tfCantidad.setFont(new java.awt.Font("Poppins", 0, 14));
        tfCantidad.setBackground(new java.awt.Color(242, 242, 242));
        tfCantidad.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(136, 136, 136), 2, true), javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tfCantidad.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntCantidad.add(tfCantidad, java.awt.BorderLayout.NORTH);
        pnlCampoCantidad.add(pnlIntCantidad, java.awt.BorderLayout.CENTER);
        pnlParejaCantidad.add(pnlCampoCantidad);
        pnlCampoUnidad.setOpaque(false);
        pnlCampoUnidad.setLayout(new java.awt.BorderLayout(0, 4));
        lblUnidad.setText("Unidad de medida");
        lblUnidad.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoUnidad.add(lblUnidad, java.awt.BorderLayout.NORTH);
        pnlIntUnidad.setOpaque(false);
        pnlIntUnidad.setLayout(new java.awt.BorderLayout());
        cbUnidad.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Mililitro", "Litro", "Gramos", "Onzas", "Libra", "Kilo", "Arroba", "Unidad" }));
        cbUnidad.setFont(new java.awt.Font("Poppins", 0, 13));
        cbUnidad.setBackground(new java.awt.Color(242, 242, 242));
        cbUnidad.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntUnidad.add(cbUnidad, java.awt.BorderLayout.NORTH);
        pnlCampoUnidad.add(pnlIntUnidad, java.awt.BorderLayout.CENTER);
        pnlParejaCantidad.add(pnlCampoUnidad);
        pnlIzq.add(pnlParejaCantidad);
        pnlParejaContenido.setOpaque(false);
        pnlParejaContenido.setLayout(new java.awt.GridLayout(1, 2, 12, 0));
        pnlCampoContenido.setOpaque(false);
        pnlCampoContenido.setLayout(new java.awt.BorderLayout(0, 4));
        lblContenido.setText("Contenido por unidad");
        lblContenido.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoContenido.add(lblContenido, java.awt.BorderLayout.NORTH);
        pnlIntContenido.setOpaque(false);
        pnlIntContenido.setLayout(new java.awt.BorderLayout());
        tfContenido.setFont(new java.awt.Font("Poppins", 0, 14));
        tfContenido.setBackground(new java.awt.Color(242, 242, 242));
        tfContenido.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(136, 136, 136), 2, true), javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tfContenido.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntContenido.add(tfContenido, java.awt.BorderLayout.NORTH);
        pnlCampoContenido.add(pnlIntContenido, java.awt.BorderLayout.CENTER);
        pnlParejaContenido.add(pnlCampoContenido);
        pnlCampoUContenido.setOpaque(false);
        pnlCampoUContenido.setLayout(new java.awt.BorderLayout(0, 4));
        lblUContenido.setText("Unidad del contenido");
        lblUContenido.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoUContenido.add(lblUContenido, java.awt.BorderLayout.NORTH);
        pnlIntUContenido.setOpaque(false);
        pnlIntUContenido.setLayout(new java.awt.BorderLayout());
        cbUContenido.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Mililitro", "Litro", "Gramos", "Onzas", "Libra", "Kilo", "Arroba" }));
        cbUContenido.setFont(new java.awt.Font("Poppins", 0, 13));
        cbUContenido.setBackground(new java.awt.Color(242, 242, 242));
        cbUContenido.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntUContenido.add(cbUContenido, java.awt.BorderLayout.NORTH);
        pnlCampoUContenido.add(pnlIntUContenido, java.awt.BorderLayout.CENTER);
        pnlParejaContenido.add(pnlCampoUContenido);
        pnlIzq.add(pnlParejaContenido);
        pnlCampoValorTotal.setOpaque(false);
        pnlCampoValorTotal.setLayout(new java.awt.BorderLayout(0, 4));
        lblValorTotal.setText("Valor total pagado o estimado");
        lblValorTotal.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoValorTotal.add(lblValorTotal, java.awt.BorderLayout.NORTH);
        pnlIntValorTotal.setOpaque(false);
        pnlIntValorTotal.setLayout(new java.awt.BorderLayout());
        pnlValor.setBackground(new java.awt.Color(242, 242, 242));
        pnlValor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(136, 136, 136), 2, true), javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        pnlValor.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlValor.setLayout(new java.awt.BorderLayout(6, 0));
        lblSigno.setText("$");
        lblSigno.setFont(new java.awt.Font("Poppins", 1, 14));
        pnlValor.add(lblSigno, java.awt.BorderLayout.WEST);
        tfValor.setFont(new java.awt.Font("Poppins", 0, 14));
        tfValor.setOpaque(false);
        tfValor.setBorder(null);
        pnlValor.add(tfValor, java.awt.BorderLayout.CENTER);
        pnlIntValorTotal.add(pnlValor, java.awt.BorderLayout.NORTH);
        pnlCampoValorTotal.add(pnlIntValorTotal, java.awt.BorderLayout.CENTER);
        pnlIzq.add(pnlCampoValorTotal);
        pnlColumnas.add(pnlIzq);
        pnlDer.setOpaque(false);
        pnlDer.setLayout(new java.awt.GridLayout(4, 1, 0, 10));
        pnlCampoForma.setOpaque(false);
        pnlCampoForma.setLayout(new java.awt.BorderLayout(0, 4));
        lblForma.setText("Forma de adquisición");
        lblForma.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoForma.add(lblForma, java.awt.BorderLayout.NORTH);
        pnlIntForma.setOpaque(false);
        pnlIntForma.setLayout(new java.awt.BorderLayout());
        cbForma.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Compra", "Recibidos como pago por trabajo", "Regalo o donación", "Intercambio o trueque", "Traídos de la finca o producidos por el hogar", "Tomados de un negocio del hogar" }));
        cbForma.setFont(new java.awt.Font("Poppins", 0, 13));
        cbForma.setBackground(new java.awt.Color(242, 242, 242));
        cbForma.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntForma.add(cbForma, java.awt.BorderLayout.NORTH);
        pnlCampoForma.add(pnlIntForma, java.awt.BorderLayout.CENTER);
        pnlDer.add(pnlCampoForma);
        pnlCampoFrecuencia.setOpaque(false);
        pnlCampoFrecuencia.setLayout(new java.awt.BorderLayout(0, 4));
        lblFrecuencia.setText("Frecuencia de compra");
        lblFrecuencia.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoFrecuencia.add(lblFrecuencia, java.awt.BorderLayout.NORTH);
        pnlIntFrecuencia.setOpaque(false);
        pnlIntFrecuencia.setLayout(new java.awt.BorderLayout());
        cbFrecuencia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Diario", "Varias veces por semana", "Semanal", "Quincenal", "Mensual", "Bimestral", "Trimestral", "Esporádico" }));
        cbFrecuencia.setFont(new java.awt.Font("Poppins", 0, 13));
        cbFrecuencia.setBackground(new java.awt.Color(242, 242, 242));
        cbFrecuencia.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntFrecuencia.add(cbFrecuencia, java.awt.BorderLayout.NORTH);
        pnlCampoFrecuencia.add(pnlIntFrecuencia, java.awt.BorderLayout.CENTER);
        pnlDer.add(pnlCampoFrecuencia);
        pnlCampoLugar.setOpaque(false);
        pnlCampoLugar.setLayout(new java.awt.BorderLayout(0, 4));
        lblLugar.setText("Lugar de compra u obtención");
        lblLugar.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoLugar.add(lblLugar, java.awt.BorderLayout.NORTH);
        pnlIntLugar.setOpaque(false);
        pnlIntLugar.setLayout(new java.awt.BorderLayout());
        cbLugar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Supermercado", "Tienda de barrio", "Plaza de mercado", "Central mayorista", "Vendedor ambulante", "Restaurante", "Plazoleta de comidas", "A través de internet", "Persona particular" }));
        cbLugar.setFont(new java.awt.Font("Poppins", 0, 13));
        cbLugar.setBackground(new java.awt.Color(242, 242, 242));
        cbLugar.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntLugar.add(cbLugar, java.awt.BorderLayout.NORTH);
        pnlCampoLugar.add(pnlIntLugar, java.awt.BorderLayout.CENTER);
        pnlDer.add(pnlCampoLugar);
        pnlCampoTipo.setOpaque(false);
        pnlCampoTipo.setLayout(new java.awt.BorderLayout(0, 4));
        lblTipo.setText("Tipo de gasto");
        lblTipo.setFont(new java.awt.Font("Poppins", 0, 13));
        pnlCampoTipo.add(lblTipo, java.awt.BorderLayout.NORTH);
        pnlIntTipo.setOpaque(false);
        pnlIntTipo.setLayout(new java.awt.BorderLayout());
        cbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Gasto de hogar", "Gasto personal" }));
        cbTipo.setFont(new java.awt.Font("Poppins", 0, 13));
        cbTipo.setBackground(new java.awt.Color(242, 242, 242));
        cbTipo.setPreferredSize(new java.awt.Dimension(100, 38));
        pnlIntTipo.add(cbTipo, java.awt.BorderLayout.NORTH);
        pnlCampoTipo.add(pnlIntTipo, java.awt.BorderLayout.CENTER);
        pnlDer.add(pnlCampoTipo);
        pnlColumnas.add(pnlDer);
        pnlCaja.add(pnlColumnas, java.awt.BorderLayout.CENTER);
        pnlCuerpo.add(pnlCaja, java.awt.BorderLayout.CENTER);
        pnlSur.setOpaque(false);
        pnlSur.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 30));
        pnlSur.setLayout(new java.awt.BorderLayout());
        btnCancelar.setText("Cancelar");
        btnCancelar.setColorFondo(new java.awt.Color(203, 203, 203));
        btnCancelar.setColorTexto(new java.awt.Color(85, 85, 85));
        btnCancelar.setFont(new java.awt.Font("Poppins", 1, 18));
        btnCancelar.setPreferredSize(new java.awt.Dimension(190, 46));
        pnlSur.add(btnCancelar, java.awt.BorderLayout.WEST);
        btnGuardar.setText("Guardar  ›");
        btnGuardar.setColorFondo(new java.awt.Color(235, 128, 24));
        btnGuardar.setColorTexto(new java.awt.Color(255, 255, 255));
        btnGuardar.setFont(new java.awt.Font("Poppins", 1, 18));
        btnGuardar.setPreferredSize(new java.awt.Dimension(190, 46));
        pnlSur.add(btnGuardar, java.awt.BorderLayout.EAST);
        pnlCuerpo.add(pnlSur, java.awt.BorderLayout.SOUTH);
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
    private enph.Estilo.Boton btnVolver;
    private javax.swing.JPanel pnlCaja;
    private javax.swing.JLabel lblTituloCaja;
    private javax.swing.JPanel pnlColumnas;
    private javax.swing.JPanel pnlIzq;
    private javax.swing.JPanel pnlCampoNombreAlimento;
    private javax.swing.JLabel lblNombreAlimento;
    private javax.swing.JPanel pnlIntNombreAlimento;
    private javax.swing.JTextField tfNombre;
    private javax.swing.JPanel pnlParejaCantidad;
    private javax.swing.JPanel pnlCampoCantidad;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JPanel pnlIntCantidad;
    private javax.swing.JTextField tfCantidad;
    private javax.swing.JPanel pnlCampoUnidad;
    private javax.swing.JLabel lblUnidad;
    private javax.swing.JPanel pnlIntUnidad;
    private javax.swing.JComboBox<String> cbUnidad;
    private javax.swing.JPanel pnlParejaContenido;
    private javax.swing.JPanel pnlCampoContenido;
    private javax.swing.JLabel lblContenido;
    private javax.swing.JPanel pnlIntContenido;
    private javax.swing.JTextField tfContenido;
    private javax.swing.JPanel pnlCampoUContenido;
    private javax.swing.JLabel lblUContenido;
    private javax.swing.JPanel pnlIntUContenido;
    private javax.swing.JComboBox<String> cbUContenido;
    private javax.swing.JPanel pnlCampoValorTotal;
    private javax.swing.JLabel lblValorTotal;
    private javax.swing.JPanel pnlIntValorTotal;
    private javax.swing.JPanel pnlValor;
    private javax.swing.JLabel lblSigno;
    private javax.swing.JTextField tfValor;
    private javax.swing.JPanel pnlDer;
    private javax.swing.JPanel pnlCampoForma;
    private javax.swing.JLabel lblForma;
    private javax.swing.JPanel pnlIntForma;
    private javax.swing.JComboBox<String> cbForma;
    private javax.swing.JPanel pnlCampoFrecuencia;
    private javax.swing.JLabel lblFrecuencia;
    private javax.swing.JPanel pnlIntFrecuencia;
    private javax.swing.JComboBox<String> cbFrecuencia;
    private javax.swing.JPanel pnlCampoLugar;
    private javax.swing.JLabel lblLugar;
    private javax.swing.JPanel pnlIntLugar;
    private javax.swing.JComboBox<String> cbLugar;
    private javax.swing.JPanel pnlCampoTipo;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JPanel pnlIntTipo;
    private javax.swing.JComboBox<String> cbTipo;
    private javax.swing.JPanel pnlSur;
    private enph.Estilo.Boton btnCancelar;
    private enph.Estilo.Boton btnGuardar;
    // End of variables declaration//GEN-END:variables
}
