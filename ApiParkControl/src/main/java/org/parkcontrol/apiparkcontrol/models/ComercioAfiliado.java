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
@Table(name = "comercio_afiliado")
public class ComercioAfiliado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comercio")
    private Long id;

    @Column(name = "nombre_comercial", nullable = false, length = 150)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "nit", nullable = false, unique = true, length = 13)
    private String nit;

    @Column(name = "tipo_comercio", length = 50)
    private String tipoComercio;

    @Column(name = "telefono", length = 8)
    private String telefono;

    @Column(name = "correo_contacto")
    private String correoContacto;

    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    private void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        this.estado = Estado.ACTIVO;
    }

    public enum Estado {
        ACTIVO,
        INACTIVO,
        SUSPENDIDO
    }
}
