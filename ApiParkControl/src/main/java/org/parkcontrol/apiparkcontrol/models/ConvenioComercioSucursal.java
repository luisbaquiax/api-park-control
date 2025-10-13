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
@Table(name = "convenio_comercio_sucursal")
public class ConvenioComercioSucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_convenio")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comercio", nullable = false)
    private ComercioAfiliado comercioAfiliado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(name = "horas_gratis_maximo", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasGratisMaximo;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodo_corte", nullable = false)
    private PeriodoCorte periodoCorte;

    @Column(name = "tarifa_por_hora", nullable = false, precision = 5, scale = 2)
    private BigDecimal tarifaPorHora;

    @Column(name = "fecha_inicio_convenio", nullable = false)
    private LocalDateTime fechaInicioConvenio;

    @Column(name = "fecha_fin_convenio")
    private LocalDateTime fechaFinConvenio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = Estado.ACTIVO;
    }

    public enum PeriodoCorte {
        DIARIO,
        SEMANAL,
        MENSUAL,
        ANUAL
    }

    public enum Estado {
        ACTIVO,
        INACTIVO,
        VENCIDO
    }
}
