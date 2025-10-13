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
@Table(name = "evidencia_cambio_placa")
public class EvidenciaCambioPlaca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud_cambio", nullable = false)
    private SolicitudCambioPlaca solicitudCambioPlac;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "url_documento", nullable = false, columnDefinition = "TEXT")
    private String urlDocumento;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @PrePersist
    private void prePersist() {
        this.fechaCarga = LocalDateTime.now();
    }

    public enum TipoDocumento {
        DENUNCIA,
        TRASPASO,
        TARJETA_CIRCULACION,
        IDENTIFICACION,
        OTRO
    }
}
