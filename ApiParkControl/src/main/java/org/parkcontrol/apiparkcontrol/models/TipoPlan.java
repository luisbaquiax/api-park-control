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
@Table(name = "tipo_plan")
public class TipoPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_plan")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre_plan", nullable = false)
    private NombrePlan nombrePlan;

    @Column(name = "codigo_plan", nullable = false)
    private String codigoPlan;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    //Precio del plan
    @Column(name = "precio_plan")
    private Double precioPlan;

    //Horas por dia del plan
    @Column(name = "horas_dia")
    private Integer horasDia;

    @Column(name = "horas_mensuales", nullable = false)
    private Integer horasMensuales;

    @Column(name = "dias_aplicables")
    private String diasAplicables;

    @Column(name = "cobertura_horaria")
    private String coberturaHoraria;

    @Column(name = "orden_beneficio", nullable = false)
    private Integer ordenBeneficio;

    @Enumerated(EnumType.STRING)
    @Column(name = "activo", nullable = false)
    private EstadoConfiguracion  activo = EstadoConfiguracion.VIGENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum NombrePlan {
        FULL_ACCESS,
        WORKWEEK,
        OFFICE_LIGHT,
        DIARIO_FLEXIBLE,
        NOCTURNO
    }

    public enum EstadoConfiguracion {
        VIGENTE,
        HISTORICO
    }

}
