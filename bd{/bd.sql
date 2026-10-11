CREATE DATABASE DatawarehouseVentas;
GO
 
USE DatawarehouseVentas;
GO
 
CREATE TABLE dbo.Ventas (
    venta_id               VARCHAR(20)     NOT NULL PRIMARY KEY,
    fecha                  DATE            NOT NULL,
    hora                   TIME            NOT NULL,
    cliente                VARCHAR(150)    NOT NULL,
    tipo_cliente            VARCHAR(50)     NULL,
    producto                 VARCHAR(200)    NOT NULL,
    categoria                 VARCHAR(100)    NULL,
    sucursal                   VARCHAR(100)    NULL,
    ciudad                      VARCHAR(100)    NULL,
    vendedor                     VARCHAR(150)    NULL,
    canal_venta                    VARCHAR(50)     NULL,
    cantidad                         INT             NOT NULL,
    precio_unitario                    DECIMAL(10,2)   NOT NULL,
    descuento_porcentaje                  DECIMAL(5,2)    NULL DEFAULT 0,
    descuento                                DECIMAL(10,2)   NULL DEFAULT 0,
    subtotal                                   DECIMAL(10,2)   NOT NULL,
    iva                                           DECIMAL(10,2)   NOT NULL,
    total                                           DECIMAL(10,2)   NOT NULL,
    metodo_pago                                       VARCHAR(50)     NULL,
 
    CONSTRAINT CK_Ventas_Cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_Ventas_Total CHECK (total >= 0)
);
GO
 
CREATE INDEX IX_Ventas_Fecha ON dbo.Ventas (fecha);
GO
 
