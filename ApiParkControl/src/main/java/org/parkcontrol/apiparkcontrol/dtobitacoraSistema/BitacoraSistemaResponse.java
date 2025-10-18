package org.parkcontrol.apiparkcontrol.dtobitacoraSistema;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraSistemaResponse {
    private Long idBitacora;

    private String modulo;

    private String accion;

    private String tablaAfectada;

    private Long idRegistroAfectado;

    private Long idUsuario;

    private String ipOrigen;

    private String descripcion;

    private String datosAntes;

    private String datosDespues;

    private LocalDateTime fechaAccion;
}
