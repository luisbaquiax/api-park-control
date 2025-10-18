package org.parkcontrol.apiparkcontrol.dto.empresa_sucursal;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObtenerSucursalesEmpresaDTO {
    private SucursalDTO sucursalDTO;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SucursalDTO {
        private Long idSucursal;
        private String nombre;
        private String direccionCompleta;
        private String ciudad;
        private String departamento;
        private String horaApertura;
        private String horaCierre;
        private Integer capacidad2Ruedas;
        private Integer capacidad4Ruedas;
        private String estado;
        private UsuarioSucursalDTO usuario;
    }
}
