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
@Table(name = "suscripcion")
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_plan", nullable = false)
    private TipoPlan tipoPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarifa_base_referencia", nullable = false)
    private TarifaBase tarifaBaseReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodo_contratado", nullable = false)
    private PeriodoContratado periodoContratado;

    @Column(name = "descuento_aplicado", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoAplicado;

    @Column(name = "precio_plan", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPlan;

    @Column(name = "horas_mensuales_incluidas", nullable = false)
    private Integer horasMensualesIncluidas;

    @Column(name = "horas_consumidas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasConsumidas;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "fecha_compra", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSuscripcion estado;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "numero_transaccion", length = 100)
    private String numeroTransaccion;

    @PrePersist
    private void prePersist() {
        this.fechaCompra = LocalDateTime.now();
        this.estado = EstadoSuscripcion.ACTIVA;
    }
    public enum EstadoSuscripcion{
        ACTIVA,
        VENCIDA,
        CANCELADA,
        SUSPENDIDA
    }
    public enum PeriodoContratado{
        MENSUAL,
        ANUAL
    }
}
