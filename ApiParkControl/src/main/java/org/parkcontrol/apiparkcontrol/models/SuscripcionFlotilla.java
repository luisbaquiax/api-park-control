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
@Table(name = "suscripcion_flotilla")
public class SuscripcionFlotilla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion_flotilla")
    private Long idSuscripcionFlotilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_corporativo", nullable = false)
    private PlanCorporativo planCorporativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSuscripcion estado = EstadoSuscripcion.ACTIVA;

    public enum EstadoSuscripcion {
        ACTIVA,
        INACTIVA
    }
}
