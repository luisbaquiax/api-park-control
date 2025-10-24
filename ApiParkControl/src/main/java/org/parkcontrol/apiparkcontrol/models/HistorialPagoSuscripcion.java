package org.parkcontrol.apiparkcontrol.models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historial_pago_suscripcion")
public class HistorialPagoSuscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_pago_suscripcion")
    private Long idHistorialPagoSuscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion", nullable = false)
    private Suscripcion suscripcion;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 50)
    private MetodoPago metodoPago;

    @Column(name = "numero_transaccion", length = 100)
    private String numeroTransaccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_pago", nullable = false, length = 50)
    private MotivoPago motivoPago;


    public enum MetodoPago {
        TARJETA_CREDITO,
        TARJETA_DEBITO,
        TRANSFERENCIA_BANCARIA,
        PAYPAL,
        OTRO
    }

    public enum EstadoPago {
        COMPLETADO,
        PENDIENTE,
        FALLIDO
    }

    public enum MotivoPago {
        RENOVACION,
        COMPRA_INICIAL,
        OTRO
    }



}
