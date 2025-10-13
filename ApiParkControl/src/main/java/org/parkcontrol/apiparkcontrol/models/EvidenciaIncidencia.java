package org.parkcontrol.apiparkcontrol.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "evidencia_incidencia")
public class EvidenciaIncidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_incidencia", nullable = false)
    private Long idEvidenciaIncidencia;

    @ManyToOne
    @JoinColumn(name = "id_incidencia", nullable = false)
    private IncidenciaTicket incidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evidencia", nullable = false)
    private TipoEvidencia tipoEvidencia;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "url_evidencia", nullable = false, columnDefinition = "TEXT")
    private String urlEvidencia;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @PrePersist
    protected void onCreate() {
        fechaCarga = LocalDateTime.now();
    }

    public enum TipoEvidencia {
        FOTO_VEHICULO,
        DOCUMENTO,
        VIDEO,
        OTRO
    }
}
