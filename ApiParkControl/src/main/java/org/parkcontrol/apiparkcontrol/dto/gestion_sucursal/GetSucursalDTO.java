package org.parkcontrol.apiparkcontrol.dto.gestion_sucursal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.UsuarioSucursalDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetSucursalDTO {
    private Long idSucursal;
    private String nombreSucursal;
    private String direccionCompletaSucursal;
    private String ciudadSucursal;
    private String departamentoSucursal;
    private String horaApertura; // LocalTime
    private String horaCierre; // LocalTime
    private Integer capacidad2Ruedas;
    private Integer capacidad4Ruedas;
    private Double latitud;
    private Double longitud;
    private String telefonoContactoSucursal;
    private String correoContactoSucursal;
    private String estadoSucursal;
    private EmpresaSucursalDTO empresa;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpresaSucursalDTO {
        private Long idEmpresa;
        private String nombreComercial;
        private String razonSocial;
        private String nit;
        private String direccionFiscal;
        private String telefonoPrincipal;
        private String correoPrincipal;

    }
}
