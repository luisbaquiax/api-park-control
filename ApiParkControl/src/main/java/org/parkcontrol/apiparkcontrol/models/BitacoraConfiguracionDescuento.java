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
@Table(name = "bitacora_configuracion_descuento")
public class BitacoraConfiguracionDescuento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_descuento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_config_descuento", nullable = false)
    private ConfiguracionDescuentoPlan configuracionDescuento;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false)
    private Accion accion = Accion.CREACION;

    @Column(name = "descuento_mensual_anterior", precision = 5, scale = 2)
    private BigDecimal descuentoMensualAnterior;

    @Column(name = "descuento_mensual_nuevo", precision = 5, scale = 2)
    private BigDecimal descuentoMensualNuevo;

    @Column(name = "descuento_anual_anterior", precision = 5, scale = 2)
    private BigDecimal descuentoAnualAnterior;

    @Column(name = "descuento_anual_nuevo", precision = 5, scale = 2)
    private BigDecimal descuentoAnualNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable", nullable = false)
    private Usuario usuarioResponsable;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio = LocalDateTime.now();

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public enum Accion {
        CREACION,
        ACTUALIZACION,
        ACTIVACION,
        DESACTIVACION
    }
}
