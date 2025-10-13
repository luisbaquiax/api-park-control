package org.parkcontrol.apiparkcontrol.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaccion_ticket")
public class TransaccionTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion", nullable = false)
    private Long idTransaccion;

    @ManyToOne
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobro", nullable = false)
    private TipoCobro tipoCobro;

    @Column(name = "horas_cobradas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasCobradas;

    @Column(name = "horas_gratis_comercio", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasGratisComercio;

    @Column(name = "tarifa_aplicada", nullable = false, precision = 8, scale = 2)
    private BigDecimal tarifaAplicada;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago;

    @Column(name = "numero_transaccion", nullable = false)
    private String numeroTransaccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDateTime fechaTransaccion;

    @PrePersist
    protected void onCreate() {
        fechaTransaccion = LocalDateTime.now();
        estado = Estado.PENDIENTE;
    }

    public enum TipoCobro {
        TARIFA_NORMAL,
        EXCEDENTE_SUSCRIPCION
    }

    public enum Estado {
        PENDIENTE,
        PAGADO,
        CANCELADO
    }
}
