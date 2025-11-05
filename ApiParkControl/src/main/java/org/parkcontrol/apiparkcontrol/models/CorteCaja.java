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
@Table(name = "corte_caja")
public class CorteCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_corte_caja", nullable = false)
    private Long idCorteCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodo", nullable = false)
    private Periodo periodo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "total_ingresos_tarifas", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalIngresosTarifas;

    @Column(name = "total_ingresos_excedentes", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalIngresosExcedentes;

    @Column(name = "total_horas_comercio", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalHorasComercio;

    @Column(name = "total_liquidacion_comercios", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalLiquidacionComercios;

    @Column(name = "total_neto", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalNeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generado_por", nullable = false)
    private Usuario generadoPor;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;



    public enum Periodo {
        DIARIO, SEMANAL, MENSUAL, ANUAL;
    }

    public enum Estado {
        PRELIMINAR, CERRADO, AUDITADO;
    }
}
