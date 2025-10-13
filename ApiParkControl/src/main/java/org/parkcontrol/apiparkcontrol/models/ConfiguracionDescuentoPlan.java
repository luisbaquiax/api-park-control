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
@Table(name = "configuracion_descuento_plan")
public class ConfiguracionDescuentoPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_config_descuento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_plan", nullable = false)
    private TipoPlan tipoPlan;

    @Column(name = "descuento_mensual", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoMensual;

    @Column(name = "descuento_anual_adicional", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoAnualAdicional;

    @Column(name = "fecha_vigencia_inicio", nullable = false)
    private LocalDateTime fechaVigenciaInicio;

    @Column(name = "fecha_vigencia_fin")
    private LocalDateTime fechaVigenciaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoConfiguracion estado = EstadoConfiguracion.PROGRAMADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum EstadoConfiguracion {
        VIGENTE,
        HISTORICO,
        PROGRAMADO
    }
}
