INSERT INTO orders (cantidad, estado, fecha_creacion, fecha_modificacion, producto_id) VALUES (2, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);
INSERT INTO orders (cantidad, estado, fecha_creacion, fecha_modificacion, producto_id) VALUES (3, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2);
INSERT INTO orders (cantidad, estado, fecha_creacion, fecha_modificacion, producto_id) VALUES (1, 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3);

INSERT INTO order_status_history (previous_status, nuevo_estado, fecha_modificacion, razon_cambio, trace_id, order_id) VALUES (NULL, 'PENDING', CURRENT_TIMESTAMP, 'El pedido ha sido creado', 'test-trace-order-001', 1);
INSERT INTO order_status_history (previous_status, nuevo_estado, fecha_modificacion, razon_cambio, trace_id, order_id) VALUES (NULL, 'PENDING', CURRENT_TIMESTAMP, 'El pedido ha sido creado', 'test-trace-order-002', 2);
INSERT INTO order_status_history (previous_status, nuevo_estado, fecha_modificacion, razon_cambio, trace_id, order_id) VALUES ('PENDING', 'CONFIRMED', CURRENT_TIMESTAMP, 'Pedido confirmado por disponibilidad de stock', 'test-trace-order-002', 2);
INSERT INTO order_status_history (previous_status, nuevo_estado, fecha_modificacion, razon_cambio, trace_id, order_id) VALUES (NULL, 'PENDING', CURRENT_TIMESTAMP, 'El pedido ha sido creado', 'test-trace-order-003', 3);
INSERT INTO order_status_history (previous_status, nuevo_estado, fecha_modificacion, razon_cambio, trace_id, order_id) VALUES ('PENDING', 'CANCELLED', CURRENT_TIMESTAMP, 'El pedido ha sido cancelado', 'test-trace-order-003', 3);