package org.parkcontrol.apiparkcontrol.services.gestion_sucursal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionSucursalServiceUnitTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TarifaSucursalRepository tarifaSucursalRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private GestionSucursalService gestionSucursalService;

    private Sucursal mockSucursal;
    private Empresa mockEmpresa;
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setRazonSocial("Test Company S.A.");
        mockEmpresa.setNit("1234567-8");
        mockEmpresa.setDireccionFiscal("Test Fiscal Address");
        mockEmpresa.setTelefonoPrincipal("12345678");
        mockEmpresa.setCorreoPrincipal("empresa@test.com");

        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setNombreUsuario("testuser");

        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setUsuarioSucursal(mockUsuario);
        mockSucursal.setNombre("Test Sucursal");
        mockSucursal.setDireccionCompleta("Test Address");
        mockSucursal.setCiudad("Test City");
        mockSucursal.setDepartamento("Test Department");
        mockSucursal.setHoraApertura(LocalTime.of(8, 0));
        mockSucursal.setHoraCierre(LocalTime.of(18, 0));
        mockSucursal.setCapacidad2Ruedas(50);
        mockSucursal.setCapacidad4Ruedas(30);
        mockSucursal.setLatitud(new BigDecimal("14.634915"));
        mockSucursal.setLongitud(new BigDecimal("-90.506882"));
        mockSucursal.setTelefonoContacto("87654321");
        mockSucursal.setCorreoContacto("sucursal@test.com");
        mockSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
    }

    @Test
    void testObtenerSucursalPorUsuario_Success() {
        // Arrange
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(1L)).thenReturn(mockSucursal);

        // Act
        GetSucursalDTO result = gestionSucursalService.obtenerSucursalPorUsuario(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdSucursal());
        assertEquals("Test Sucursal", result.getNombreSucursal());
        assertEquals("Test City", result.getCiudadSucursal());
        assertEquals("08:00", result.getHoraApertura());
        assertEquals("18:00", result.getHoraCierre());
        assertEquals("ACTIVA", result.getEstadoSucursal());

        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(1L);
    }

    @Test
    void testObtenerSucursalPorUsuario_NotFound() {
        // Arrange
        when(sucursalRepository.findByUsuarioSucursal_IdUsuario(999L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionSucursalService.obtenerSucursalPorUsuario(999L);
        });
        
        assertEquals("Sucursal no encontrada para el usuario con ID: 999", exception.getMessage());
        verify(sucursalRepository).findByUsuarioSucursal_IdUsuario(999L);
    }

    @Test
    void testEditarSucursal_Success() {
        // Arrange
        EditarSucursalDTO editarDTO = new EditarSucursalDTO();
        editarDTO.setIdSucursal(1L);
        editarDTO.setNombreSucursal("Updated Sucursal");
        editarDTO.setDireccionCompletaSucursal("Updated Address");
        editarDTO.setCiudadSucursal("Updated City");
        editarDTO.setDepartamentoSucursal("Updated Department");
        editarDTO.setHoraApertura("09:00");
        editarDTO.setHoraCierre("19:00");
        editarDTO.setCapacidad2Ruedas(75);
        editarDTO.setCapacidad4Ruedas(45);
        editarDTO.setLatitud(15.123456);
        editarDTO.setLongitud(-91.654321);
        editarDTO.setTelefonoContactoSucursal("11111111");
        editarDTO.setCorreoContactoSucursal("updated@test.com");
        editarDTO.setEstadoSucursal("INACTIVA");

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(mockSucursal));
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(mockSucursal);

        // Act
        String result = gestionSucursalService.editarSucursal(editarDTO);

        // Assert
        assertEquals("Sucursal editada exitosamente.", result);
        verify(sucursalRepository).findById(1L);
        verify(sucursalRepository).save(mockSucursal);

        // Verify changes were made to the mock object
        assertEquals("Updated Sucursal", mockSucursal.getNombre());
        assertEquals("Updated Address", mockSucursal.getDireccionCompleta());
        assertEquals(Sucursal.EstadoSucursal.INACTIVA, mockSucursal.getEstado());
    }

    @Test
    void testEditarSucursal_NotFound() {
        // Arrange
        EditarSucursalDTO editarDTO = new EditarSucursalDTO();
        editarDTO.setIdSucursal(999L);

        when(sucursalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gestionSucursalService.editarSucursal(editarDTO);
        });
        
        assertEquals("Sucursal no encontrada con ID: 999", exception.getMessage());
        verify(sucursalRepository).findById(999L);
        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }
}
