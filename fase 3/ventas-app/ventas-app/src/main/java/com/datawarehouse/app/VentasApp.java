package com.datawarehouse.app;

import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class VentasApp extends JFrame {

    private static final String TODAS = "(Todas)";

    // Datos de conexión (se leen del .env para no dejar la contraseña escrita en el código)
    private final String url;
    private final String usuario;
    private final String clave;

    // Componentes de la interfaz
    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false; // la tabla es solo de consulta
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = new JTextField(18);
    private final JComboBox<String> cmbCiudad = new JComboBox<>();
    private final JComboBox<String> cmbCategoria = new JComboBox<>();
    private final JLabel lblResumen = new JLabel(" ");

    public VentasApp() {
        super("Consulta de Ventas - Datawarehouse");

        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        String host = env.get("DB_HOST", "localhost");
        String puerto = env.get("DB_PORT", "1433");
        String base = env.get("DB_NAME", "DatawarehouseVentas");
        usuario = env.get("DB_USER", "pentaho_user");
        clave = env.get("DB_PASSWORD", "");
        url = "jdbc:sqlserver://" + host + ":" + puerto
                + ";databaseName=" + base
                + ";encrypt=true;trustServerCertificate=true";

        construirInterfaz();

        try {
            cargarFiltros();   // llena los combos con las ciudades y categorías que existen
            consultar();       // primera carga: todas las ventas
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    // ---------------------------------------------------------------- Interfaz

    private void construirInterfaz() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelFiltros.add(new JLabel("Buscar (cliente, producto o ID):"));
        panelFiltros.add(txtBuscar);
        panelFiltros.add(new JLabel("Ciudad:"));
        panelFiltros.add(cmbCiudad);
        panelFiltros.add(new JLabel("Categoría:"));
        panelFiltros.add(cmbCategoria);

        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnLimpiar);

        btnBuscar.addActionListener(e -> ejecutarConsulta());
        txtBuscar.addActionListener(e -> ejecutarConsulta()); // también al presionar Enter
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cmbCiudad.setSelectedIndex(0);
            cmbCategoria.setSelectedIndex(0);
            ejecutarConsulta();
        });

        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setAutoCreateRowSorter(true); // clic en el encabezado para ordenar

        lblResumen.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        add(panelFiltros, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------- Consultas

    /** Llama a consultar() y muestra un mensaje si algo falla. */
    private void ejecutarConsulta() {
        try {
            consultar();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    /**
     * Arma el SELECT según lo que el usuario escribió/seleccionó.
     * Se usa PreparedStatement con "?" para que los valores viajen como parámetros
     * (así se evita la inyección SQL).
     */
    private void consultar() throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT venta_id, fecha, hora, cliente, tipo_cliente, producto, categoria, "
              + "sucursal, ciudad, vendedor, canal_venta, cantidad, precio_unitario, "
              + "descuento_porcentaje, descuento, subtotal, iva, total, metodo_pago "
              + "FROM dbo.Ventas WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        String texto = txtBuscar.getText().trim();
        if (!texto.isEmpty()) {
            sql.append(" AND (cliente LIKE ? OR producto LIKE ? OR venta_id LIKE ?)");
            String patron = "%" + texto + "%";
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }

        String ciudad = (String) cmbCiudad.getSelectedItem();
        if (ciudad != null && !ciudad.equals(TODAS)) {
            sql.append(" AND ciudad = ?");
            parametros.add(ciudad);
        }

        String categoria = (String) cmbCategoria.getSelectedItem();
        if (categoria != null && !categoria.equals(TODAS)) {
            sql.append(" AND categoria = ?");
            parametros.add(categoria);
        }

        sql.append(" ORDER BY fecha, hora");

        // try-with-resources: cierra la conexión automáticamente al terminar
        try (Connection con = DriverManager.getConnection(url, usuario, clave);
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                llenarTabla(rs);
            }
        }
    }

    /** Pasa el resultado de la consulta al JTable y calcula el resumen de abajo. */
    private void llenarTabla(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnas = meta.getColumnCount();
        int posTotal = rs.findColumn("total");

        Vector<String> encabezados = new Vector<>();
        for (int i = 1; i <= columnas; i++) {
            encabezados.add(meta.getColumnLabel(i));
        }

        Vector<Vector<Object>> filas = new Vector<>();
        BigDecimal sumaTotal = BigDecimal.ZERO;

        while (rs.next()) {
            Vector<Object> fila = new Vector<>();
            for (int i = 1; i <= columnas; i++) {
                Object valor = rs.getObject(i);
                fila.add(valor);
                if (i == posTotal && valor instanceof BigDecimal) {
                    sumaTotal = sumaTotal.add((BigDecimal) valor);
                }
            }
            filas.add(fila);
        }

        modelo.setDataVector(filas, encabezados);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(130);
        }
        lblResumen.setText(filas.size() + " ventas encontradas   |   Total vendido: $" + sumaTotal);
    }

    // ---------------------------------------------------------------- Filtros

    private void cargarFiltros() throws SQLException {
        llenarCombo(cmbCiudad, "ciudad");
        llenarCombo(cmbCategoria, "categoria");
    }

    /** Llena un combo con los valores distintos de una columna (el nombre es fijo, no viene del usuario). */
    private void llenarCombo(JComboBox<String> combo, String columna) throws SQLException {
        combo.removeAllItems();
        combo.addItem(TODAS);
        String sql = "SELECT DISTINCT " + columna + " FROM dbo.Ventas WHERE " + columna
                + " IS NOT NULL ORDER BY " + columna;
        try (Connection con = DriverManager.getConnection(url, usuario, clave);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                combo.addItem(rs.getString(1));
            }
        }
    }

    // ---------------------------------------------------------------- Errores / main

    private void mostrarError(SQLException e) {
        JOptionPane.showMessageDialog(this,
                "No se pudo consultar la base de datos.\n\n" + e.getMessage()
                        + "\n\nRevisa que SQL Server esté encendido (TCP/IP habilitado) y que el .env tenga los datos correctos.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // si falla, se usa el aspecto por defecto de Swing
        }
        SwingUtilities.invokeLater(() -> new VentasApp().setVisible(true));
    }
}
