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
@Table(name = "empresa_flotilla")
public class EmpresaFlotilla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa_flotilla")
    private Long idEmpresaFlotilla;

    @Column(name = "nombre_empresa", nullable = false)
    private String nombreEmpresa;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "nit", nullable = false, unique = true)
    private String nit;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "correo_contacto")
    private String correoContacto;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEmpresaFlotilla estado = EstadoEmpresaFlotilla.ACTIVA;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public enum EstadoEmpresaFlotilla {
        ACTIVA,
        INACTIVA,
        SUSPENDIDA
    }
}
