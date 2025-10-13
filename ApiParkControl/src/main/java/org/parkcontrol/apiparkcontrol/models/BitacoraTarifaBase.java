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
@Table(name = "bitacora_tarifa_base")
public class BitacoraTarifaBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_tarifa")
    private Long idBitacoraTarifa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarifa_base")
    private TarifaBase tarifaBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false)
    private Accion accion;

    @Column(name = "precio_anterior", precision = 10, scale = 2)
    private BigDecimal precioAnterior;

    @Column(name = "precio_nuevo", precision = 10, scale = 2)
    private BigDecimal precioNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable")
    private Usuario usuarioResponsable;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    private void prePersist() {
        this.fechaCambio = LocalDateTime.now();
    }

    public enum Accion {
        CREACION,
        ACTUALIZACION,
        ACTIVACION,
        DESACTIVACION
    }
}
