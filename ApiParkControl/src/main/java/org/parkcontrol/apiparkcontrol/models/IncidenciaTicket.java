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
@Table(name = "incidencia_ticket")
public class IncidenciaTicket {
    /**
     * CREATE TABLE INCIDENCIA_TICKET (
     *     id_incidencia BIGINT PRIMARY KEY AUTO_INCREMENT,
     *     id_ticket BIGINT NOT NULL,
     *     tipo_incidencia ENUM('comprobante_perdido', 'fraude', 'vehiculo_no_retirado', 'otro') NOT NULL,
     *     descripcion TEXT NOT NULL,
     *     fecha_incidencia TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     *     resuelto BOOLEAN DEFAULT FALSE,
     *     fecha_resolucion TIMESTAMP NULL,
     *     resuelto_por BIGINT,
     *     observaciones_resolucion TEXT,
     *     FOREIGN KEY (id_ticket) REFERENCES TICKET(id_ticket) ON DELETE CASCADE,
     *     FOREIGN KEY (resuelto_por) REFERENCES USUARIO(id_usuario)
     * );
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia", nullable = false)
    private Long idIncidencia;

    @ManyToOne
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_incidencia", nullable = false)
    private TipoIncidencia tipoIncidencia;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "fecha_incidencia", nullable = false)
    private LocalDateTime fechaIncidencia;

    @Column(name = "resuelto", nullable = false)
    private boolean resuelto;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "resuelto_por")
    private Long resueltoPor;

    @Column(name = "observaciones_resolucion")
    private String observacionesResolucion;

    @PrePersist
    protected void onCreate() {
        fechaIncidencia = LocalDateTime.now();
    }

    public enum TipoIncidencia {
        COMPROBANTE_PERDIDO,
        FRAUDE,
        VEHICULO_NO_RETIRADO,
        OTRO
    }
}
