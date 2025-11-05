package org.parkcontrol.apiparkcontrol.services.gestion_incidencias;

import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.*;
import org.parkcontrol.apiparkcontrol.services.filestorage.FileStorageService;
import org.parkcontrol.apiparkcontrol.services.filestorage.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class ResolucionIncidenciasService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EvidenciaIncidenciaRepository evidenciaIncidenciaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private IncidenciaTicketRepository incidenciaTicketRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private S3StorageService s3StorageService;
    @Autowired
    private  SucursalRepository sucursalRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    @Value("${s3.bucket.backend}")
    private String S3_BUCKET_BACKEND;

    @Value("${storage.path.local}")
    private String URL_BASE_LOCAL;

    @Value("${aws.region}")
    private String region;

    @Value("${storage.type}")
    private String storageType;

    @Autowired
    IncidenciaTicketSucursalService incidenciaTicketSucursalService;

    //Obtener el detalle de las incidencias de todas las sucursales de una empresa
    public List<DetalleSucursalesIncidenciasDTO> obtenerDetalleIncidenciasPorEmpresa(Long idUsuarioEmpresa) throws Exception {
        //Buscar la empresa del usuario
        Usuario usuarioEmpresa = usuarioRepository.findById(idUsuarioEmpresa)
                .orElseThrow(() -> new Exception("Usuario de empresa no encontrado con ID: " + idUsuarioEmpresa));

        //Buscamos la empresa
        Empresa empresa = empresaRepository.findByUsuarioEmpresa_IdUsuario(usuarioEmpresa.getIdUsuario()).getFirst();

        if(empresa == null){
            throw  new Exception("Empresa no encontrada");
        }


        //Buscar las sucursales de la empresa
        List<Sucursal> sucursales = sucursalRepository.findByEmpresaIdEmpresa(empresa.getIdEmpresa());
        if (sucursales.isEmpty()) {
            throw new Exception("No se encontraron sucursales para la empresa con ID: " + empresa.getIdEmpresa());
        }

        List<DetalleSucursalesIncidenciasDTO> detalleSucursalesIncidenciasDTOList = new ArrayList<>();

        for (Sucursal sucursal : sucursales) {
            List<IncidenciasSucursalDTO> incidenciasSucursalDTOList = incidenciaTicketSucursalService.obtenerIncidenciasSucursal(sucursal.getUsuarioSucursal().getIdUsuario());

            //Si es vacío, continuar con la siguiente sucursal
            if (incidenciasSucursalDTOList.isEmpty()) {
                continue;
            }
            DetalleSucursalesIncidenciasDTO detalleSucursalesIncidenciasdto = new DetalleSucursalesIncidenciasDTO();
            detalleSucursalesIncidenciasdto.setIdSucursal(sucursal.getIdSucursal());
            detalleSucursalesIncidenciasdto.setNombreSucursal(sucursal.getNombre());
            detalleSucursalesIncidenciasdto.setDireccionSucursal(sucursal.getDireccionCompleta());
            detalleSucursalesIncidenciasdto.setTelefonoSucursal(sucursal.getTelefonoContacto());
            detalleSucursalesIncidenciasdto.setIncidenciasSucursalDTOList(incidenciasSucursalDTOList);

            detalleSucursalesIncidenciasDTOList.add(detalleSucursalesIncidenciasdto);
        }

        return detalleSucursalesIncidenciasDTOList;
    }


    //Ahora necesito darle resolución a una incidencia específica
    public String resolverIncidencia(ResolucionIncidenciaDTO resolucionIncidenciaDTO) throws Exception {
        //Buscar la incidencia
        IncidenciaTicket incidenciaTicket = incidenciaTicketRepository.findById(resolucionIncidenciaDTO.getIdIncidencia())
                .orElseThrow(() -> new Exception("Incidencia no encontrada con ID: " + resolucionIncidenciaDTO.getIdIncidencia()));

        //Verificar que el usuario que resuelve exista
        Usuario usuarioResuelve = usuarioRepository.findById(resolucionIncidenciaDTO.getIdUsuarioResuelve())
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + resolucionIncidenciaDTO.getIdUsuarioResuelve()));

        //Verificar que la incidencia no esté ya resuelta
        if (incidenciaTicket.isResuelto()) {
            throw new Exception("La incidencia con ID: " + resolucionIncidenciaDTO.getIdIncidencia() + " ya está resuelta.");
        }

        //Actualizar la incidencia
        incidenciaTicket.setResuelto(true);
        incidenciaTicket.setFechaResolucion(LocalDateTime.now());
        incidenciaTicket.setResueltoPor(usuarioResuelve.getIdUsuario());
        incidenciaTicket.setObservacionesResolucion(resolucionIncidenciaDTO.getObservacionesResolucion());

        incidenciaTicketRepository.save(incidenciaTicket);

        return "Incidencia con ID: " + resolucionIncidenciaDTO.getIdIncidencia() + " resuelta exitosamente.";
    }


}
