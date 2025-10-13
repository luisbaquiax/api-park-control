package org.parkcontrol.apiparkcontrol.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "acreditacion_horas_comercio")
public class AcreditacionHorasComercio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAcreditacion;

    @ManyToOne
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "id_convenio", nullable = false)
    private ConvenioComercioSucursal convenio;

    @Column(name = "horas_otorgadas", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasOtorgadas;

    @Column(name = "fecha_acreditacion", nullable = false)
    private LocalDateTime fechaAcreditacion;

    @Column(name = "acreditado_por")
    private Long acreditadoPor;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaAcreditacion = LocalDateTime.now();
    }

}

