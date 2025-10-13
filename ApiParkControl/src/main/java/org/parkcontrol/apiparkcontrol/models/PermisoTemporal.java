package org.parkcontrol.apiparkcontrol.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permiso_temporal")
public class PermisoTemporal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso_temporal")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion", nullable = false)
    private Suscripcion suscripcion;

    @Column(name = "placa_temporal", nullable = false, length = 20)
    private String placaTemporal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo_permitido", nullable = false)
    private TipoVehiculo tipoVehiculoPermitido;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "usos_maximos")
    private Integer usosMaximos;

    @Column(name = "usos_realizados", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer usosRealizados;

    @Column(name = "sucursales_validas")
    private String sucursalesValidas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPermiso estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por", nullable = false)
    private Usuario aprobadoPor;

    @Column(name = "fecha_aprobacion", nullable = false)
    private LocalDateTime fechaAprobacion;

    @Column(name = "observaciones")
    private String observaciones;

    @PrePersist
    private void prePersist() {
        this.fechaAprobacion = LocalDateTime.now();
        this.estado = EstadoPermiso.ACTIVO;
    }

    public enum TipoVehiculo {
        DOS_RUEDAS,
        CUATRO_RUEDAS
    }

    public enum EstadoPermiso {
        ACTIVO,
        EXPIRADO,
        REVOCADO,
        AGOTADO
    }
}
