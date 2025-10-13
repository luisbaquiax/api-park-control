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
@Table(name = "token_autenticacion")
public class TokenAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long idToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_token", nullable = false)
    private TipoToken tipoToken;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoToken estado;

    @Column(name = "codigo_verificacion", length = 10)
    private String codigoVerificacion;

    @PrePersist
    private void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.tipoToken = TipoToken.LOGIN;
        this.estado = EstadoToken.ACTIVO;
    }

    @PreUpdate
    private void preUpdate() {
        this.fechaExpiracion = LocalDateTime.now().plusHours(1);
    }

    public enum TipoToken {
        VERIFICACION_EMAIL,
        RESET_PASSWORD,
        DobleFactor,
        LOGIN
    }

    public enum EstadoToken {
        ACTIVO,
        USADO,
        EXPIRADO
    }
}
