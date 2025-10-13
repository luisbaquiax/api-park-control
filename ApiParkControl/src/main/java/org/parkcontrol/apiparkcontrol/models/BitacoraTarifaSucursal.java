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
@Table(name = "bitacora_tarifa_sucursal")
public class BitacoraTarifaSucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_tarifa_sucursal")
    private Long idBitacoraTarifaSucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarifa_sucursal")
    private TarifaSucursal tarifaSucursal;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false)
    private Accion accion;

    @Column(name = "precio_anterior", precision = 10, scale = 2)
    private BigDecimal precioAnterior;

    @Column(name = "precio_nuevo", precision = 10, scale = 2)
    private BigDecimal precioNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    private void prePersist() {
        this.fechaCambio = LocalDateTime.now();
        this.accion = Accion.CREACION;
    }

    @PreUpdate
    private void preUpdate() {
        this.fechaCambio = LocalDateTime.now();
        this.accion = Accion.ACTUALIZACION;
    }

    public enum Accion {
        CREACION,
        ACTUALIZACION,
        ACTIVACION,
        DESACTIVACION
    }
}
