package com.redvecinalia.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class VerificadorTablasRunner implements CommandLineRunner {

    private final DataSource dataSource;

    public VerificadorTablasRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        System.out.println("============================================================");
        System.out.println("  VERIFICACION DE TABLAS EN red_vecinal.db");
        System.out.println("============================================================");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {

            int count = 0;
            while (rs.next()) {
                System.out.println("  ✓ " + rs.getString("name"));
                count++;
            }
            System.out.println("------------------------------------------------------------");
            System.out.println("  Total: " + count + " tabla(s)");
            System.out.println("  Esperadas: 4 (conversacion, faq, mensaje, tramite)");
        }
        System.out.println("============================================================");
        System.out.println();
    }
}