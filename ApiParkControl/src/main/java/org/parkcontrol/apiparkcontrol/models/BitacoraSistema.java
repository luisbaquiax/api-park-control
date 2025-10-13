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
@Table(name = "bitacora_sistema")
public class BitacoraSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora")
    private Long idBitacora;

    @Column(name = "modulo", length = 50, nullable = false)
    private String modulo;

    @Column(name = "accion", length = 100, nullable = false)
    private String accion;

    @Column(name = "tabla_afectada", length = 50)
    private String tablaAfectada;

    @Column(name = "id_registro_afectado")
    private Long idRegistroAfectado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", foreignKey = @ForeignKey(name = "fk_bitacora_usuario"))
    private Usuario usuario;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "datos_antes", columnDefinition = "JSON")
    private String datosAntes;

    @Column(name = "datos_despues", columnDefinition = "JSON")
    private String datosDespues;

    @Column(name = "fecha_accion")
    private LocalDateTime fechaAccion = LocalDateTime.now();
}
