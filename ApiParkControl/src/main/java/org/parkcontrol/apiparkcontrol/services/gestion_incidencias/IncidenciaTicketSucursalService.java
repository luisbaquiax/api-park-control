package org.parkcontrol.apiparkcontrol.services.gestion_incidencias;

import jakarta.transaction.Transactional;
import org.parkcontrol.apiparkcontrol.dto.gestion_incidencias.*;
import org.parkcontrol.apiparkcontrol.dto.empresa_sucursal.UsuarioSucursalDTO;
import org.parkcontrol.apiparkcontrol.services.filestorage.FileStorageService;
import org.parkcontrol.apiparkcontrol.services.filestorage.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.*;

@Service
public class IncidenciaTicketSucursalService {

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

    @Value("${s3.bucket.backend}")
    private String S3_BUCKET_BACKEND;

    @Value("${storage.path.local}")
    private String URL_BASE_LOCAL;

    @Value("${aws.region}")
    private String region;

    @Value("${storage.type}")
    private String storageType;

    //Crear una nueva incidencia en un ticket de sucursal
    @Transactional
    public String crearNuevaIncidenciaSucursal(NuevaIncidenciaDTO nuevaIncidencia, MultipartFile archivoEvidencia) throws Exception {
        //Buscar el ticket asociado
        Ticket ticket = ticketRepository.findById(nuevaIncidencia.getIdTicket())
                .orElseThrow(() -> new Exception("Ticket no encontrado con ID: " + nuevaIncidencia.getIdTicket()));

        //Crear la nueva incidencia
        IncidenciaTicket incidenciaTicket = new IncidenciaTicket();
        incidenciaTicket.setTicket(ticket);
        incidenciaTicket.setTipoIncidencia(IncidenciaTicket.TipoIncidencia.valueOf(nuevaIncidencia.getTipoIncidencia()));
        incidenciaTicket.setDescripcion(nuevaIncidencia.getDescripcion());
        incidenciaTicket.setFechaIncidencia(LocalDateTime.now());
        incidenciaTicket.setResuelto(false);

        incidenciaTicketRepository.save(incidenciaTicket);

        //Crear la nueva evidencia asociada a la incidencia
        //verificar que el archivo no este vacio
        if (archivoEvidencia == null || archivoEvidencia.isEmpty()) {
            throw new Exception("El archivo de evidencia no puede estar vacio");
        }
        //Verificamos donde se va a guardar el archivo en local o en la nube
        String urlEvidencia = "";
        if(storageType.equals("s3")) {
            urlEvidencia = s3StorageService.uploadToS3(archivoEvidencia);
        } else {
            urlEvidencia = fileStorageService.getUrl(archivoEvidencia);
        }

        EvidenciaIncidencia evidenciaIncidencia = new EvidenciaIncidencia();
        evidenciaIncidencia.setIncidencia(incidenciaTicket);
        evidenciaIncidencia.setTipoEvidencia(EvidenciaIncidencia.TipoEvidencia.valueOf(nuevaIncidencia.getTipoEvidencia()));
        evidenciaIncidencia.setNombreArchivo(archivoEvidencia.getOriginalFilename());
        evidenciaIncidencia.setUrlEvidencia(urlEvidencia);
        evidenciaIncidencia.setDescripcion(nuevaIncidencia.getDescripcionEvidencia());
        evidenciaIncidencia.setFechaCarga(LocalDateTime.now());

        evidenciaIncidenciaRepository.save(evidenciaIncidencia);
        return  "Incidencia creada con exito con ID: " + incidenciaTicket.getIdIncidencia();
    }

    //Obtenemos todas las incidencias de una sucursal
    public List<IncidenciasSucursalDTO> obtenerIncidenciasSucursal(Long idUsuarioSucursal) throws Exception {
        //Buscamos la sucursal del usuario
        Usuario usuario = usuarioRepository.findById(idUsuarioSucursal).orElse(null);
        if(usuario == null) {
            throw new Exception("Usuario no encontrado con ID: " + idUsuarioSucursal);
        }

        //Obtenemos la sucursal del usuario
        Sucursal sucursal = sucursalRepository.findByUsuarioSucursal_IdUsuario(idUsuarioSucursal);
        if(sucursal == null) {
            throw new Exception("Sucursal no encontrada para el usuario con ID: " + idUsuarioSucursal);
        }


        List<IncidenciaTicket> incidencias = incidenciaTicketRepository.findByTicket_Sucursal_IdSucursal(sucursal.getIdSucursal());
        List<IncidenciasSucursalDTO> incidenciasDTO = new ArrayList<>();

        for (IncidenciaTicket incidencia : incidencias) {
            IncidenciasSucursalDTO dto = new IncidenciasSucursalDTO();
            //Informacion del ticket asociado
            Ticket ticket = incidencia.getTicket();
            dto.setIdTicket(ticket.getId());
            dto.setFolioNumerico(ticket.getFolioNumerico());
            dto.setTipoCliente(ticket.getTipoCliente().name());
            dto.setEstadoTicket(ticket.getEstado().name());
            dto.setPlacaVehiculo(ticket.getVehiculo().getPlaca());
            dto.setModeloVehiculo(ticket.getVehiculo().getModelo());
            dto.setColorVehiculo(ticket.getVehiculo().getColor());
            dto.setNombrePropietario(ticket.getVehiculo().getPropietario().getNombre());
            dto.setTelefonoPropietario(ticket.getVehiculo().getPropietario().getTelefono());
            //Informacion de la incidencia
            IncidenciasSucursalDTO.IncidenciasTicketDTO incidenciaDTO = new IncidenciasSucursalDTO.IncidenciasTicketDTO();
            incidenciaDTO.setIdIncidencia(incidencia.getIdIncidencia());
            incidenciaDTO.setTipoIncidencia(incidencia.getTipoIncidencia().name());
            incidenciaDTO.setDescripcion(incidencia.getDescripcion());
            incidenciaDTO.setFechaIncidencia(incidencia.getFechaIncidencia().toString());
            incidenciaDTO.setResuelto(incidencia.isResuelto());
            incidenciaDTO.setFechaResolucion(incidencia.getFechaResolucion() != null ? incidencia.getFechaResolucion().toString() : null);
            incidenciaDTO.setResueltoPor(incidencia.getResueltoPor());
            incidenciaDTO.setObservacionesResolucion(incidencia.getObservacionesResolucion());


            //Obtenemos las evidencias asociadas a la incidencia
            List<EvidenciaIncidencia> evidencias = evidenciaIncidenciaRepository.findByIncidencia_IdIncidencia(incidencia.getIdIncidencia());
            List<IncidenciasSucursalDTO.EvidenciasIncidenciaDTO> evidenciasDTO = new ArrayList<>();

            for (EvidenciaIncidencia evidencia : evidencias) {
                IncidenciasSucursalDTO.EvidenciasIncidenciaDTO evidenciaDTO = new IncidenciasSucursalDTO.EvidenciasIncidenciaDTO();
                evidenciaDTO.setIdEvidenciaIncidencia(evidencia.getIdEvidenciaIncidencia());
                evidenciaDTO.setTipoEvidencia(evidencia.getTipoEvidencia().name());
                evidenciaDTO.setNombreArchivo(evidencia.getNombreArchivo());
                //Crear la URL completa de la evidencia
                if(storageType.equals("local")){
                    evidenciaDTO.setUrlEvidencia(URL_BASE_LOCAL + evidencia.getUrlEvidencia());
                }else {
                    String urlS3 = "https://" + S3_BUCKET_BACKEND + ".s3." + region + ".amazonaws.com/" + evidencia.getUrlEvidencia();
                    evidenciaDTO.setUrlEvidencia(urlS3);
                }
                evidenciaDTO.setDescripcion(evidencia.getDescripcion());
                evidenciaDTO.setFechaCarga(String.valueOf(evidencia.getFechaCarga()));

                evidenciasDTO.add(evidenciaDTO);
            }

            incidenciaDTO.setEvidencias(evidenciasDTO);
            dto.setIncidencias(incidenciaDTO);
            incidenciasDTO.add(dto);
        }

        return incidenciasDTO;
    }


}
