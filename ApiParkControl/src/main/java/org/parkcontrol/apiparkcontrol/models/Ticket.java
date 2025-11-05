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
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Long id;

    @Column(name = "folio_numerico", nullable = false, unique = true)
    private String folioNumerico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion")
    private Suscripcion suscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permiso_temporal")
    private PermisoTemporal permisoTemporal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false)
    private TipoCliente tipoCliente;

    @Column(name = "fecha_hora_entrada", nullable = false)
    private LocalDateTime fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "codigo_qr", columnDefinition = "TEXT")
    private String codigoQr;

    @Column(name = "enlace_sms_whatsapp")
    private String enlaceSmsWhatsapp;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTicket estado = EstadoTicket.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum EstadoTicket {
        ACTIVO,
        FINALIZADO,
        PERDIDO,
        INCIDENTE
    }
    public enum TipoCliente {
        SUSCRIPTOR,
        SIN_SUSCRIPCION
    }
}

/*
Ejemplo sql para insertar un ticket:
INSERT INTO TICKET (folio_numerico, id_sucursal, id_vehiculo, id_suscripcion, id_permiso_temporal, tipo_cliente, fecha_hora_entrada, fecha_hora_salida, duracion_minutos, codigo_qr, enlace_sms_whatsapp, estado, fecha_creacion)
VALUES ('1234567890', 1, 1, NULL, NULL, 'SIN_SUSCRIPCION', '2024-06-01 08:00:00', NULL, NULL, 'QR_CODE_EXAMPLE', 'http://example.com/sms', 'ACTIVO', NOW());
 */
