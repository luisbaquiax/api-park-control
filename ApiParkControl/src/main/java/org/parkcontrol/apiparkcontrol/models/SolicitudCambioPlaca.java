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
@Table(name = "solicitud_cambio_placa")
public class SolicitudCambioPlaca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_cambio")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion", nullable = false)
    private Suscripcion suscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo_actual", nullable = false)
    private Vehiculo vehiculoActual;

    @Column(name = "placa_nueva", nullable = false, length = 20)
    private String placaNueva;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false)
    private Motivo motivo;

    @Column(name = "descripcion_motivo")
    private String descripcionMotivo;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSolicitud estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por")
    private Usuario revisadoPor;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "observaciones_revision", columnDefinition = "TEXT")
    private String observacionesRevision;

    @Column(name = "fecha_efectiva")
    private LocalDateTime fechaEfectiva;

    @PrePersist
    private void prePersist() {
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    @PreUpdate
    private void preUpdate() {
        this.fechaRevision = LocalDateTime.now();
    }

    public enum Motivo {
        VENTA,
        ROBO,
        SINIESTRO,
        OTRO
    }

    public enum EstadoSolicitud {
        PENDIENTE,
        APROBADA,
        RECHAZADA,
        CANCELADA
    }
}
