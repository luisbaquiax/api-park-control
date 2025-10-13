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

    @Column(name = "nombre_plan", nullable = false, unique = true)
    private String nombrePlan;

    @Column(name = "codigo_plan", nullable = false, unique = true)
    private String codigoPlan;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "horas_mensuales", nullable = false)
    private Integer horasMensuales;

    @Column(name = "dias_aplicables")
    private String diasAplicables;

    @Column(name = "cobertura_horaria")
    private String coberturaHoraria;

    @Column(name = "orden_beneficio", nullable = false)
    private Integer ordenBeneficio;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
