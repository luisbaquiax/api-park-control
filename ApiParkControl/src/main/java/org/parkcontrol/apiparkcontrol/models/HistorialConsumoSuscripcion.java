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
@Table(name = "historial_consumo_suscripcion")
public class HistorialConsumoSuscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_consumo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion", nullable = false)
    private Suscripcion suscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ticket")
    private Ticket ticket;

    @Column(name = "horas_consumidas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasConsumidas;

    @Column(name = "horas_comercio", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasComercio = BigDecimal.ZERO;

    @Column(name = "fecha_consumo", nullable = false)
    private LocalDateTime fechaConsumo;

    @PrePersist
    private void prePersist() {
        this.fechaConsumo = LocalDateTime.now();
    }
}
