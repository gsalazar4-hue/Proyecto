/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author thain
 */
package enph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionOracle {

    // CONEXION DESDE MI COMPUTADOR
    // CUANDO UTILICE SU PROPIO ORACLE

    private static final String HOST = "Thaina";
    private static final String PUERTO = "1521";
    private static final String SID = "XE";
   // BASE DE DATOS DEL PROYECTO 
private static final String USUARIO = "ENPH_COMPANERO";

// Contraseña exclusiva de la base de datos.
// Configurar esta variable en cada computador.
private static final String CLAVE =
        System.getenv("ENPH_COMPANERO_DB_PASSWORD");

    private static final String URL =
            "jdbc:oracle:thin:@" + HOST + ":" + PUERTO + ":" + SID;

    public static Connection obtenerConexion() {

        try {
            Class.forName("oracle.jdbc.OracleDriver");

            Connection conexion = DriverManager.getConnection(
                    URL, USUARIO, CLAVE);

            System.out.println("CONEXION EXITOSA CON ORACLE");
            return conexion;

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return null;
        }
    }

    public static void cerrarConexion(Connection conexion) {

        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}