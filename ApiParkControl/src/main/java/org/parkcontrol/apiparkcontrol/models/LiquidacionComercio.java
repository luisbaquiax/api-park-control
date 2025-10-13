package org.parkcontrol.apiparkcontrol.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "liquidacion_comercio")
public class LiquidacionComercio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_liquidacion")
    private Long idLiquidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_corte_caja", nullable = false)
    private CorteCaja corteCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comercio", nullable = false)
    private ComercioAfiliado comercio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_convenio", nullable = false)
    private ConvenioComercioSucursal convenio;

    @Column(name = "total_horas_otorgadas", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalHorasOtorgadas;

    @Column(name = "tarifa_por_hora", precision = 10, scale = 2, nullable = false)
    private BigDecimal tarifaPorHora;

    @Column(name = "monto_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoLiquidacion estado = EstadoLiquidacion.PENDIENTE;

    @Column(name = "fecha_facturacion")
    private LocalDateTime fechaFacturacion;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public enum EstadoLiquidacion {
        PENDIENTE,
        FACTURADO,
        PAGADO
    }
}
