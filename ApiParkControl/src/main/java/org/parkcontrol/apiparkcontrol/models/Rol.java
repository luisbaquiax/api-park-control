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
@Table(name = "rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @PrePersist
    private void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }

}

/*
Ejemplo sql para insertar roles SUCURSAL, BACKOFFICE, CLIENTE, SISTEMA
INSERT INTO rol (nombre_rol, descripcion) VALUES
('SUCURSAL', 'Rol para usuarios que gestionan sucursales'),
('BACKOFFICE', 'Rol para usuarios de backoffice'),
('CLIENTE', 'Rol para usuarios clientes'),
('SISTEMA', 'Rol para usuarios del sistema');
 */