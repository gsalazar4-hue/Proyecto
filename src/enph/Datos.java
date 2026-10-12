package enph;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/** Modelo, reglas de negocio y persistencia. Maneja varios hogares (uno "activo" a la vez) y un único encuestador. */
public final class Datos {
    private Datos() {}

    public static final String ARCHIVO = "enph_datos_v7.txt";
    public static final String NOMBRE_ENCUESTADOR = "Albertano SantaCruz Martinez";
    public static final int DIAS_ENCUESTA = 14;

    public static final Locale ES = Locale.forLanguageTag("es-CO");
    public static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("d 'de' MMMM 'el' yyyy", ES);
    public static final DateTimeFormatter FECHA_LARGA_DEL = DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy", ES);
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static final String[] UNIDADES = {"Mililitro", "Litro", "Gramos", "Onzas", "Libra", "Kilo", "Arroba", "Unidad"};
    public static final String[] UNIDADES_CONTENIDO = {"Mililitro", "Litro", "Gramos", "Onzas", "Libra", "Kilo", "Arroba"};
    public static final String[] FORMAS = {"Compra", "Recibidos como pago por trabajo", "Regalo o donación",
            "Intercambio o trueque", "Traídos de la finca o producidos por el hogar", "Tomados de un negocio del hogar"};
    public static final String[] FRECUENCIAS = {"Diario", "Varias veces por semana", "Semanal", "Quincenal",
            "Mensual", "Bimestral", "Trimestral", "Esporádico"};
    public static final String[] LUGARES = {"Supermercado", "Tienda de barrio", "Plaza de mercado", "Central mayorista",
            "Vendedor ambulante", "Restaurante", "Plazoleta de comidas", "A través de internet", "Persona particular"};
    public static final String[] TIPOS = {"Gasto de hogar", "Gasto personal"};

    public static final LocalDate HOY = LocalDate.now();

    /** Todos los hogares asignados al encuestador. */
    public static final List<Hogar> hogares = new ArrayList<>();

    /** Código del único hogar cuyo jefe usa el dispositivo (cámbielo aquí para usar otro hogar). */
    public static final String CODIGO_JEFE = "HBGA06";

    /** Hogar activo: el que consultan las ventanas de detalle y el dispositivo del jefe de hogar. */
    public static Hogar hogar;

    static { cargar(); }

    // ---------------- Modelo ----------------
    public static class Hogar {
        String codigo, codigoVivienda, direccion, barrio, jefe, telefono = "", correo = "";
        int personas;
        LocalDate inicio;
        boolean finalizado;
        String estadoVisita = "Pendiente";          // Pendiente, En visita, Realizada
        LocalDate fechaEstadoVisita;
        List<Visita> visitas = new ArrayList<>();
        List<Gasto> gastos = new ArrayList<>();
        Set<Integer> diasRevisados = new TreeSet<>();

        List<Gasto> gastosDelDia(int dia) {
            List<Gasto> r = new ArrayList<>();
            for (Gasto g : gastos) if (g.dia == dia) r.add(g);
            r.sort(Comparator.comparingInt(g -> g.orden));
            return r;
        }

        SortedSet<Integer> diasConGastos() {
            SortedSet<Integer> s = new TreeSet<>();
            for (Gasto g : gastos) s.add(g.dia);
            return s;
        }
    }

    public static class Visita {
        int numero;
        LocalDate fecha;
        LocalTime inicio, fin;

        Visita(int numero, LocalDate fecha, LocalTime inicio, LocalTime fin) {
            this.numero = numero; this.fecha = fecha; this.inicio = inicio; this.fin = fin;
        }
    }

    public static class Gasto {
        int dia, orden;
        LocalDate fecha;
        String alimento, unidad, unidadContenido, forma, frecuencia, lugar, tipo;
        double cantidad, valor;
        Double contenido;                           // solo cuando la unidad es "Unidad"
        boolean revisado;
    }

    // ---------------- Reglas de negocio ----------------
    public static int dia() { return dia(hogar); }

    public static int dia(Hogar h) {
        if (h.finalizado) return DIAS_ENCUESTA;
        long d = ChronoUnit.DAYS.between(h.inicio, HOY) + 1;
        return (int) Math.max(1, Math.min(DIAS_ENCUESTA, d));
    }

    public static LocalDate fechaDelDia(int dia) { return hogar.inicio.plusDays(dia - 1); }

    public static String estadoVisita() { return estadoVisita(hogar); }

    public static String estadoVisita(Hogar h) {
        return (h.fechaEstadoVisita != null && h.fechaEstadoVisita.equals(HOY)) ? h.estadoVisita : "Pendiente";
    }

    /** Estado que se muestra en las tablas: un hogar finalizado aparece como Realizada. */
    public static String estadoMostrado(Hogar h) { return h.finalizado ? "Realizada" : estadoVisita(h); }

    public static void setEstadoVisita(String estado) { setEstadoVisita(hogar, estado); }

    public static void setEstadoVisita(Hogar h, String estado) { h.estadoVisita = estado; h.fechaEstadoVisita = HOY; }

    public static boolean visitaRegistradaHoy() { return visitaRegistradaHoy(hogar); }

    public static boolean visitaRegistradaHoy(Hogar h) {
        for (Visita v : h.visitas) if (v.fecha.equals(HOY)) return true;
        return false;
    }

    /** Hogares con visita por hacer: los que aún no tienen la encuesta finalizada (el del día 14 queda de último). */
    public static List<Hogar> pendientesDelDia() {
        List<Hogar> r = new ArrayList<>();
        for (Hogar h : asignadosOrdenados()) if (!h.finalizado) r.add(h);
        r.sort(Comparator.comparingInt(h -> dia(h) == DIAS_ENCUESTA ? 1 : 0));   // estable: conserva el orden por código
        return r;
    }

    /** Hogares asignados: primero los que están en recolección y luego los finalizados, cada grupo por código. */
    public static List<Hogar> asignadosOrdenados() {
        List<Hogar> r = new ArrayList<>(hogares);
        r.sort(Comparator.<Hogar>comparingInt(h -> h.finalizado ? 1 : 0).thenComparing(h -> h.codigo));
        return r;
    }

    /** Hogar del único jefe de hogar de la aplicación. */
    public static Hogar hogarDelJefe() { return porCodigo(CODIGO_JEFE); }

    private static Hogar porCodigo(String codigo) {
        for (Hogar h : hogares) if (h.codigo.equals(codigo)) return h;
        return null;
    }

    public static boolean todosLosDiasRevisados() {
        for (Gasto g : hogar.gastos) if (!hogar.diasRevisados.contains(g.dia)) return false;
        return true;
    }

    private static double factor(String unidad) {
        switch (unidad) {
            case "Litro": return 1000;
            case "Onzas": return 28.35;
            case "Libra": return 500;
            case "Kilo": return 1000;
            case "Arroba": return 12500;
            default: return 1;                      // Mililitro y Gramos
        }
    }

    /** Equivalencia en gramos o centímetros cúbicos. */
    public static String equivalencia(Gasto g) {
        double base = g.cantidad;
        String u = g.unidad;
        if (u.equals("Unidad")) {
            if (g.contenido == null || g.unidadContenido == null) return "No aplica";
            base = g.cantidad * g.contenido;
            u = g.unidadContenido;
        }
        boolean liquido = u.equals("Mililitro") || u.equals("Litro");
        return fmt(base * factor(u)) + (liquido ? " cc" : " g");
    }

    public static String cantidadTxt(Gasto g) { return fmt(g.cantidad) + " " + g.unidad; }

    public static String contenidoTxt(Gasto g) {
        return g.contenido == null ? "No aplica" : fmt(g.contenido) + " " + g.unidadContenido;
    }

    public static String frecuenciaTxt(Gasto g) {
        return (g.frecuencia == null || g.frecuencia.isEmpty()) ? "No aplica" : g.frecuencia;
    }

    public static String fmt(double v) {
        BigDecimal b = BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return (b.scale() < 0 ? b.setScale(0) : b).toPlainString();
    }

    // ---------------- Persistencia ----------------
    private static String l(String s) {
        return s == null ? "" : s.replace("|", "/").replace("\n", " ");
    }

    public static boolean guardar() {
        try (PrintWriter o = new PrintWriter(new FileWriter(new File(ARCHIVO), StandardCharsets.UTF_8))) {
            for (Hogar h : hogares) {
                o.println(String.join("|", "H", l(h.codigo), l(h.codigoVivienda), l(h.direccion), l(h.barrio), l(h.jefe),
                        l(h.telefono), l(h.correo), String.valueOf(h.personas), h.inicio.toString(),
                        String.valueOf(h.finalizado), l(h.estadoVisita),
                        h.fechaEstadoVisita == null ? "" : h.fechaEstadoVisita.toString(),
                        h.diasRevisados.stream().map(String::valueOf).collect(Collectors.joining(","))));
                for (Visita v : h.visitas) {
                    o.println(String.join("|", "V", h.codigo, String.valueOf(v.numero), v.fecha.toString(),
                            v.inicio.toString(), v.fin.toString()));
                }
                for (Gasto g : h.gastos) {
                    o.println(String.join("|", "G", h.codigo, String.valueOf(g.dia), g.fecha.toString(),
                            String.valueOf(g.orden), l(g.alimento), String.valueOf(g.cantidad), l(g.unidad),
                            g.contenido == null ? "" : String.valueOf(g.contenido), l(g.unidadContenido),
                            l(g.forma), l(g.frecuencia), l(g.lugar), String.valueOf(g.valor), l(g.tipo),
                            String.valueOf(g.revisado)));
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void cargar() {
        File f = new File(ARCHIVO);
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = r.readLine()) != null) {
                    String[] p = linea.split("\\|", -1);
                    if (p[0].equals("H") && p.length >= 14) {
                        Hogar h = new Hogar();
                        h.codigo = p[1]; h.codigoVivienda = p[2]; h.direccion = p[3]; h.barrio = p[4]; h.jefe = p[5];
                        h.telefono = p[6]; h.correo = p[7]; h.personas = Integer.parseInt(p[8]);
                        h.inicio = LocalDate.parse(p[9]); h.finalizado = Boolean.parseBoolean(p[10]);
                        h.estadoVisita = p[11];
                        h.fechaEstadoVisita = p[12].isEmpty() ? null : LocalDate.parse(p[12]);
                        if (!p[13].isEmpty()) for (String d : p[13].split(",")) h.diasRevisados.add(Integer.parseInt(d));
                        hogares.add(h);
                    } else if (p[0].equals("V") && p.length >= 6 && porCodigo(p[1]) != null) {
                        porCodigo(p[1]).visitas.add(new Visita(Integer.parseInt(p[2]), LocalDate.parse(p[3]),
                                LocalTime.parse(p[4]), LocalTime.parse(p[5])));
                    } else if (p[0].equals("G") && p.length >= 16 && porCodigo(p[1]) != null) {
                        Gasto g = new Gasto();
                        g.dia = Integer.parseInt(p[2]); g.fecha = LocalDate.parse(p[3]); g.orden = Integer.parseInt(p[4]);
                        g.alimento = p[5]; g.cantidad = Double.parseDouble(p[6]); g.unidad = p[7];
                        g.contenido = p[8].isEmpty() ? null : Double.valueOf(p[8]);
                        g.unidadContenido = p[9].isEmpty() ? null : p[9];
                        g.forma = p[10]; g.frecuencia = p[11]; g.lugar = p[12];
                        g.valor = Double.parseDouble(p[13]); g.tipo = p[14]; g.revisado = Boolean.parseBoolean(p[15]);
                        porCodigo(p[1]).gastos.add(g);
                    }
                }
            } catch (Exception e) {
                hogares.clear();
            }
        }
        if (hogares.isEmpty()) {
            sembrar();
            guardar();
        }
        hogar = hogarDelJefe();
    }

    private static Hogar nuevoHogar(String cod, String viv, String dir, String barrio, String jefe, String tel,
                                    String correo, int personas, int dia, boolean finalizado) {
        Hogar h = new Hogar();
        h.codigo = cod; h.codigoVivienda = viv; h.direccion = dir; h.barrio = barrio; h.jefe = jefe;
        h.telefono = tel; h.correo = correo; h.personas = personas; h.finalizado = finalizado;
        h.inicio = HOY.minusDays(dia - 1);
        hogares.add(h);
        return h;
    }

    private static void nuevoGasto(Hogar h, int dia, String alimento, double cant, String unidad, Double contenido,
                                   String unidadContenido, String forma, String frec, String lugar, double valor,
                                   String tipo, boolean revisado) {
        Gasto g = new Gasto();
        g.dia = dia; g.fecha = h.inicio.plusDays(dia - 1); g.orden = h.gastosDelDia(dia).size() + 1;
        g.alimento = alimento; g.cantidad = cant; g.unidad = unidad; g.contenido = contenido;
        g.unidadContenido = unidadContenido; g.forma = forma; g.frecuencia = frec; g.lugar = lugar;
        g.valor = valor; g.tipo = tipo; g.revisado = revisado;
        h.gastos.add(g);
    }

    /** Datos de ejemplo tomados del prototipo de media fidelidad: un único encuestador y un único jefe de hogar (HBGA06). */
    private static void sembrar() {
        // Hogar del jefe de hogar: encuesta en el día 14, días 1 a 13 revisados y día 14 pendiente
        Hogar h6 = nuevoHogar("HBGA06", "VBGA05", "Calle 9 No. 40 – 26 Casa 3", "Cabecera del Llano",
                "Claudia Marcela Gomez Leon", "317 645 9823", "claudiamgomez@gmail.com", 4, DIAS_ENCUESTA, false);
        String[] nombres = {"Arroz", "Papa", "Leche", "Pan", "Pollo", "Cebolla", "Panela", "Aceite", "Lentejas",
                "Banano", "Yuca", "Fríjol", "Café"};
        String[] unidades = {"Kilo", "Kilo", "Litro", "Libra", "Kilo", "Libra", "Libra", "Litro", "Libra",
                "Kilo", "Kilo", "Libra", "Libra"};
        for (int d = 1; d <= 13; d++) {
            nuevoGasto(h6, d, nombres[d - 1], 1, unidades[d - 1], null, null, "Compra", "Semanal",
                    "Tienda de barrio", 3000 + d * 500, "Gasto de hogar", true);
            h6.diasRevisados.add(d);
        }
        nuevoGasto(h6, 14, "Tomate", 250, "Gramos", null, null, "Compra", "Varias veces por semana",
                "Plaza de mercado", 2000, "Gasto de hogar", false);
        nuevoGasto(h6, 14, "Gaseosa personal", 2, "Unidad", 600.0, "Mililitro", "Compra", "Quincenal",
                "Plaza de mercado", 7400, "Gasto personal", false);
        nuevoGasto(h6, 14, "Limon", 500, "Gramos", null, null, "Compra", "Diario",
                "Tienda de barrio", 3000, "Gasto de hogar", false);
    }
}
