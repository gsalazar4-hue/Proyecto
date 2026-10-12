/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enph;

/**
 *
 * @author thain
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) {

        Connection conexion = ConexionOracle.obtenerConexion();

        if (conexion != null) {

            System.out.println("CONEXION CORRECTA CON ORACLE");

            try {
                Statement consulta = conexion.createStatement();

                ResultSet resultado = consulta.executeQuery(
                        "SELECT COUNT(*) FROM USER_TABLES"
                );

                if (resultado.next()) {
                    System.out.println(
                            "TABLAS EN ORACLE: " + resultado.getInt(1)
                    );
                }

                resultado.close();
                consulta.close();

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }

            ConexionOracle.cerrarConexion(conexion);
        }
    }
}