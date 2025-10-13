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
@Table(name = "plan_corporativo")
public class PlanCorporativo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan_corporativo")
    private Long idPlanCorporativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa_flotilla", nullable = false)
    private EmpresaFlotilla empresaFlotilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_plan", nullable = false)
    private TipoPlan tipoPlan;

    @Column(name = "nombre_plan_corporativo", nullable = false)
    private String nombrePlanCorporativo;

    @Column(name = "numero_placas_contratadas", nullable = false)
    private Integer numeroPlacasContratadas;

    @Column(name = "descuento_corporativo_adicional", precision = 5, scale = 2, nullable = false)
    private BigDecimal descuentoCorporativoAdicional;

    @Column(name = "precio_plan_corporativo", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioPlanCorporativo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPlanCorporativo estado = EstadoPlanCorporativo.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum EstadoPlanCorporativo {
        ACTIVO,
        VENCIDO,
        CANCELADO
    }
}
