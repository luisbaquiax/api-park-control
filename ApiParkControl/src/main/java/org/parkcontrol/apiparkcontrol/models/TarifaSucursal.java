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
@Table(name = "tarifa_sucursal")
public class TarifaSucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa_sucursal")
    private Long idTarifaSucursal;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal")
    private Sucursal sucursal;

    @Column(name = "precio_por_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorHora;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = "GTQ";

    @Column(name = "fecha_vigencia_inicio", nullable = false)
    private LocalDateTime fechaVigenciaInicio;

    @Column(name = "fecha_vigencia_fin")
    private LocalDateTime fechaVigenciaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTarifaSucursal estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public enum EstadoTarifaSucursal {
        VIGENTE,
        HISTORICO,
        PROGRAMADO
    }
}
