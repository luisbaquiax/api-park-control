package org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.ObtenerSucursalesEmpresaDTO;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.DetalleTipoPlanDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanesSuscripcionDTO {
    List<EmpresaSuscripcionesDTO> empresasSuscripciones;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmpresaSuscripcionesDTO {
        private Long idEmpresa;
        private String nombreComercial;
        private String nit;
        private String razonSocial;
        private String telefonoContacto;
        private String direccionFiscal;
        private List<ObtenerSucursalesEmpresaDTO.SucursalDTO> sucursales;
        private List<DetalleTipoPlanDTO> suscripciones;
    }

}
