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

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "usos_maximos")
    private Integer usosMaximos;

    @Column(name = "usos_realizados")
    private Integer usosRealizados;

    @Column(name = "sucursales_validas")
    private String sucursalesValidas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPermiso estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "observaciones")
    private String observaciones;

    public enum TipoVehiculo {
        DOS_RUEDAS,
        CUATRO_RUEDAS
    }

    public enum EstadoPermiso {
        PENDIENTE,
        ACTIVO,
        RECHAZADO,
        EXPIRADO,
        REVOCADO,
        AGOTADO
    }
}
