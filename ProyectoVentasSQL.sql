CREATE DATABASE ProyectoVentas;
GO

USE ProyectoVentas;
GO

CREATE TABLE ventas (
    venta_id VARCHAR(20) NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    cliente VARCHAR(100) NOT NULL,
    tipo_cliente VARCHAR(30) NOT NULL,
    producto VARCHAR(150) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    sucursal VARCHAR(50) NOT NULL,
    ciudad VARCHAR(50) NOT NULL,
    vendedor VARCHAR(100) NOT NULL,
    canal_venta VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    descuento_porcentaje DECIMAL(5,2) NOT NULL,
    descuento DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,

    CONSTRAINT PK_ventas PRIMARY KEY (venta_id)
);
GO