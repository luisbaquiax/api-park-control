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
@Table(name = "vehiculo")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_propietario", nullable = false)
    private Persona propietario;

    @Column(name = "placa", nullable = false, unique = true)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false)
    private TipoVehiculo tipoVehiculo;

    @Column(name = "marca", length = 50)
    private String marca;

    @Column(name = "modelo", length = 50)
    private String modelo;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "color", length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoVehiculo estado;

    @Column(name = "fecha_registro", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_ultima_actualizacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime fechaUltimaActualizacion = LocalDateTime.now();

    public enum TipoVehiculo {
        DOS_RUEDAS,
        CUATRO_RUEDAS
    }

    public enum EstadoVehiculo {
        ACTIVO,
        INACTIVO,
        VENDIDO,
        ROBADO
    }

    @PrePersist
    private void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        this.estado = EstadoVehiculo.ACTIVO;
    }

    @PreUpdate
    private void preUpdate() {
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }
}
