CREATE DATABASE DB_ProyectoVentas;
GO

USE DB_ProyectoVentas;
GO

-- 1. Tabla Sucursales
CREATE TABLE tbl_sucursales (
    sucursal_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_sucursal VARCHAR(100) NOT NULL,
    ciudad VARCHAR(50) NOT NULL
);

-- 2. Tabla Clientes
CREATE TABLE tbl_clientes (
    cliente_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_cliente VARCHAR(100) NOT NULL,
    tipo_cliente VARCHAR(50) NOT NULL
);

-- 3. Tabla Categorías
CREATE TABLE tbl_categorias (
    categoria_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL
);

-- 4. Tabla Productos
CREATE TABLE tbl_productos (
    producto_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_producto VARCHAR(150) NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    categoria_id INT FOREIGN KEY REFERENCES tbl_categorias(categoria_id)
);

-- 5. Tabla Vendedores
CREATE TABLE tbl_vendedores (
    vendedor_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_vendedor VARCHAR(100) NOT NULL
);

-- 6. Tabla Principal de Hechos (Ventas)
CREATE TABLE tbl_ventas (
    venta_id VARCHAR(20) PRIMARY KEY,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    cliente_id INT FOREIGN KEY REFERENCES tbl_clientes(cliente_id),
    producto_id INT FOREIGN KEY REFERENCES tbl_productos(producto_id),
    sucursal_id INT FOREIGN KEY REFERENCES tbl_sucursales(sucursal_id),
    vendedor_id INT FOREIGN KEY REFERENCES tbl_vendedores(vendedor_id),
    canal_venta VARCHAR(50),
    cantidad INT NOT NULL,
    descuento_porcentaje DECIMAL(5, 2),
    descuento DECIMAL(10, 2),
    subtotal DECIMAL(10, 2),
    iva DECIMAL(10, 2),
    total DECIMAL(10, 2),
    metodo_pago VARCHAR(50)
);