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
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "contrasenia_hash", nullable = false)
    private String contraseniaHash;

    @Column(name = "debe_cambiar_contrasenia", nullable = false)
    private boolean debeCambiarContrasenia;

    @Column(name = "doble_factor_habilitado", nullable = false)
    private boolean dobleFactorHabilitado;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "es_primera_vez", nullable = false)
    private boolean esPrimeraVez = true;

    @Column(name = "ultima_fecha_acceso")
    private LocalDateTime ultimaFechaAcceso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private LocalDateTime fechaUltimaActualizacion = LocalDateTime.now();

    @PrePersist
    private void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();
        this.estado = EstadoUsuario.ACTIVO;
    }

    @PreUpdate
    private void preUpdate() {
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }

    public enum EstadoUsuario {
        ACTIVO,
        INACTIVO,
        SUSPENDIDO
    }


}
