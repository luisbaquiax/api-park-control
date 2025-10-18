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
@Table(name = "empresa")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_empresa")
    private Usuario usuarioEmpresa;

    @Column(name = "nombre_comercial", nullable = false)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "nit", nullable = false, unique = true, length = 13)
    private String nit;

    @Column(name = "direccion_fiscal")
    private String direccionFiscal;

    @Column(name = "telefono_principal", length = 8)
    private String telefonoPrincipal;

    @Column(name = "correo_principal")
    private String correoPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEmpresa estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    @PrePersist
    private void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();
        this.estado = EstadoEmpresa.ACTIVA;
    }

    @PreUpdate
    private void preUpdate() {
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }

    public enum EstadoEmpresa {
        ACTIVA,
        INACTIVA
    }
}


/*Ejemplo sql para nueva empresa

INSERT INTO empresa (nombre_comercial, razon_social, nit, direccion_fiscal, telefono_principal, correo_principal, estado, fecha_registro, fecha_ultima_actualizacion)
VALUES ('Tech Solutions', 'Tech Solutions S.A.', '1234567-8', 'Avenida Siempre Viva 742, Ciudad de Guatemala', '12345678', 'techosolutions@gmail.com', 'ACTIVA', NOW(), NOW());

 */