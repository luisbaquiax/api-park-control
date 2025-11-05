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
@Table(name = "ocupacion_sucursal")
public class OcupacionSucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ocupacion")
    private Long idOcupacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "ocupacion_2r", nullable = false)
    private Integer ocupacion2R;

    @Column(name = "capacidad_2r", nullable = false)
    private Integer capacidad2R;

    @Column(name = "ocupacion_4r", nullable = false)
    private Integer ocupacion4R;

    @Column(name = "capacidad_4r", nullable = false)
    private Integer capacidad4R;

    @Column(name = "porcentaje_ocupacion_2r", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentajeOcupacion2R;

    @Column(name = "porcentaje_ocupacion_4r", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentajeOcupacion4R;
}

/*Ejemplo sql para datos de ocupacion_sucursal
INSERT INTO ocupacion_sucursal (id_sucursal, fecha_hora, ocupacion_2r, capacidad_2r, ocupacion_4r, capacidad_4r, porcentaje_ocupacion_2r, porcentaje_ocupacion_4r) VALUES (1, '2024-06-15 10:00:00', 30, 100, 20, 80, 30.00, 25.00);
 */
