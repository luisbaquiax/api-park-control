package org.parkcontrol.apiparkcontrol.services.suscripcion_cliente;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parkcontrol.apiparkcontrol.dto.suscripcion_cliente.*;
import org.parkcontrol.apiparkcontrol.dto.planes_suscripcion.DetalleTipoPlanDTO;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionClienteServiceUnitTest {

    @Mock
    private TipoPlanRepository tipoPlanRepository;
    @Mock
    private ConfiguracionDescuentoPlanRepository configuracionDescuentoPlanRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private TarifaBaseRepository tarifaBaseRepository;
    @Mock
    private HistorialPagoSuscripcionRepository historialPagoSuscripcionRepository;

    @InjectMocks
    private SuscripcionClienteService suscripcionClienteService;

    private Usuario mockUsuario;
    private Empresa mockEmpresa;
    private Sucursal mockSucursal;
    private TipoPlan mockTipoPlan;
    private ConfiguracionDescuentoPlan mockConfigDescuento;
    private Vehiculo mockVehiculo;
    private Suscripcion mockSuscripcion;
    private TarifaBase mockTarifaBase;
    private Persona mockPersona;

    @BeforeEach
    void setUp() {
        // Setup Persona
        mockPersona = new Persona();
        mockPersona.setIdPersona(1L);
        mockPersona.setNombre("Juan");
        mockPersona.setApellido("Pérez");
        mockPersona.setCorreo("juan@test.com");

        // Setup Usuario
        mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1L);
        mockUsuario.setNombreUsuario("juantest");
        mockUsuario.setPersona(mockPersona);
        mockUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);

        // Setup Empresa
        mockEmpresa = new Empresa();
        mockEmpresa.setIdEmpresa(1L);
        mockEmpresa.setNombreComercial("Test Company");
        mockEmpresa.setRazonSocial("Test Company S.A.");
        mockEmpresa.setNit("1234567-8");
        mockEmpresa.setTelefonoPrincipal("12345678");
        mockEmpresa.setDireccionFiscal("Test Address");
        mockEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);

        // Setup Sucursal
        mockSucursal = new Sucursal();
        mockSucursal.setIdSucursal(1L);
        mockSucursal.setEmpresa(mockEmpresa);
        mockSucursal.setNombre("Sucursal Centro");
        mockSucursal.setDireccionCompleta("Centro Ciudad");
        mockSucursal.setCiudad("Guatemala");
        mockSucursal.setDepartamento("Guatemala");
        mockSucursal.setHoraApertura(LocalTime.of(8, 0));
        mockSucursal.setHoraCierre(LocalTime.of(18, 0));
        mockSucursal.setCapacidad2Ruedas(50);
        mockSucursal.setCapacidad4Ruedas(100);
        mockSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);

        // Setup TipoPlan
        mockTipoPlan = new TipoPlan();
        mockTipoPlan.setId(1L);
        mockTipoPlan.setEmpresa(mockEmpresa);
        mockTipoPlan.setNombrePlan(TipoPlan.NombrePlan.WORKWEEK);
        mockTipoPlan.setCodigoPlan("WW-001");
        mockTipoPlan.setDescripcion("Plan Workweek");
        mockTipoPlan.setPrecioPlan(300.00);
        mockTipoPlan.setHorasDia(8);
        mockTipoPlan.setHorasMensuales(160);
        mockTipoPlan.setDiasAplicables("L-M-X-J-V");
        mockTipoPlan.setCoberturaHoraria("08:00 - 18:00");
        mockTipoPlan.setOrdenBeneficio(2);
        mockTipoPlan.setActivo(TipoPlan.EstadoConfiguracion.VIGENTE);
        mockTipoPlan.setFechaCreacion(LocalDateTime.now());

        // Setup ConfiguracionDescuentoPlan
        mockConfigDescuento = new ConfiguracionDescuentoPlan();
        mockConfigDescuento.setId(1L);
        mockConfigDescuento.setTipoPlan(mockTipoPlan);
        mockConfigDescuento.setDescuentoMensual(new BigDecimal("15.00"));
        mockConfigDescuento.setDescuentoAnualAdicional(new BigDecimal("5.00"));
        mockConfigDescuento.setFechaVigenciaInicio(LocalDateTime.now().minusDays(30));
        mockConfigDescuento.setFechaVigenciaFin(LocalDateTime.now().plusDays(330));
        mockConfigDescuento.setCreadoPor(mockUsuario);
        mockConfigDescuento.setEstado(ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE);
        mockConfigDescuento.setFechaCreacion(LocalDateTime.now());

        // Setup Vehiculo
        mockVehiculo = new Vehiculo();
        mockVehiculo.setId(1L);
        mockVehiculo.setPropietario(mockPersona);
        mockVehiculo.setPlaca("ABC123");
        mockVehiculo.setMarca("Toyota");
        mockVehiculo.setModelo("Corolla");
        mockVehiculo.setColor("Blanco");
        mockVehiculo.setTipoVehiculo(Vehiculo.TipoVehiculo.CUATRO_RUEDAS);
        mockVehiculo.setEstado(Vehiculo.EstadoVehiculo.ACTIVO);

        // Setup TarifaBase
        mockTarifaBase = new TarifaBase();
        mockTarifaBase.setIdTarifaBase(1L);
        mockTarifaBase.setEmpresa(mockEmpresa);
        mockTarifaBase.setPrecioPorHora(new BigDecimal("15.00"));
        mockTarifaBase.setEstado(TarifaBase.EstadoTarifaBase.VIGENTE);

        // Setup Suscripcion
        mockSuscripcion = new Suscripcion();
        mockSuscripcion.setId(1L);
        mockSuscripcion.setEmpresa(mockEmpresa);
        mockSuscripcion.setUsuario(mockUsuario);
        mockSuscripcion.setVehiculo(mockVehiculo);
        mockSuscripcion.setTipoPlan(mockTipoPlan);
        mockSuscripcion.setTarifaBaseReferencia(mockTarifaBase);
        mockSuscripcion.setPeriodoContratado(Suscripcion.PeriodoContratado.MENSUAL);
        mockSuscripcion.setDescuentoAplicado(new BigDecimal("0.00"));
        mockSuscripcion.setPrecioPlan(new BigDecimal("300.00"));
        mockSuscripcion.setHorasMensualesIncluidas(160);
        mockSuscripcion.setHorasConsumidas(new BigDecimal("50.00"));
        mockSuscripcion.setFechaInicio(LocalDateTime.now());
        mockSuscripcion.setFechaFin(LocalDateTime.now().plusMonths(1));
        mockSuscripcion.setFechaCompra(LocalDateTime.now());
        mockSuscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        mockSuscripcion.setMetodoPago("TARJETA_CREDITO");
        mockSuscripcion.setNumeroTransaccion("TXN123456");
    }

    @Test
    void testObtenerPlanesSuscripcion_Success() {
        // Arrange
        List<Empresa> empresas = Arrays.asList(mockEmpresa);
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);
        List<TipoPlan> tiposPlanes = Arrays.asList(mockTipoPlan);

        when(empresaRepository.findAll()).thenReturn(empresas);
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(sucursales);
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(tiposPlanes);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(mockConfigDescuento);

        // Act
        PlanesSuscripcionDTO result = suscripcionClienteService.obtenerPlanesSuscripcion();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEmpresasSuscripciones());
        assertEquals(1, result.getEmpresasSuscripciones().size());

        PlanesSuscripcionDTO.EmpresaSuscripcionesDTO empresaDTO = result.getEmpresasSuscripciones().get(0);
        assertEquals(1L, empresaDTO.getIdEmpresa());
        assertEquals("Test Company", empresaDTO.getNombreComercial());
        assertEquals("1234567-8", empresaDTO.getNit());
        assertEquals(1, empresaDTO.getSucursales().size());
        assertEquals(1, empresaDTO.getSuscripciones().size());

        verify(empresaRepository).findAll();
        verify(sucursalRepository).findByEmpresaIdEmpresa(1L);
        verify(tipoPlanRepository).findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE);
    }

    @Test
    void testObtenerPlanesSuscripcionPorCliente_Success() {
        // Arrange
        List<Suscripcion> suscripciones = Arrays.asList(mockSuscripcion);
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);
        List<TipoPlan> tiposPlanes = Arrays.asList(mockTipoPlan);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(suscripciones);
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(sucursales);
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(tiposPlanes);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(mockConfigDescuento);

        // Act
        ClientePlanesSuscripcionDTO result = suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdCliente());
        assertEquals("juantest", result.getNombreCliente());
        assertEquals(1, result.getSuscripcionCliente().size());

        ClientePlanesSuscripcionDTO.SuscripcionClienteDTO suscripcionDTO = result.getSuscripcionCliente().get(0);
        assertEquals(1L, suscripcionDTO.getIdSuscripcion());
        assertEquals("MENSUAL", suscripcionDTO.getPeriodoContratado());
        assertEquals(300.00, suscripcionDTO.getPrecioPlan());
        assertNotNull(suscripcionDTO.getVehiculoClienteDTO()); // Corregido: usar el nombre correcto del campo
        assertEquals("ABC123", suscripcionDTO.getVehiculoClienteDTO().getPlaca());

        verify(usuarioRepository).findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO);
        verify(suscripcionRepository).findByUsuario_IdUsuario(1L);
    }

    @Test
    void testObtenerPlanesSuscripcionPorCliente_ClienteNotFound() {
        // Arrange
        when(usuarioRepository.findByIdUsuarioAndEstado(999L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(999L);
        });

        assertEquals("Cliente no encontrado o inactivo", exception.getMessage());
        verify(usuarioRepository).findByIdUsuarioAndEstado(999L, Usuario.EstadoUsuario.ACTIVO);
    }

    @Test
    void testObtenerVehiculosCliente_Success() {
        // Arrange
        List<Vehiculo> vehiculos = Arrays.asList(mockVehiculo);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findByPropietario_IdPersonaAndEstado(1L, Vehiculo.EstadoVehiculo.ACTIVO))
                .thenReturn(vehiculos);

        // Act
        List<VehiculoClienteDTO> result = suscripcionClienteService.obtenerVehiculosCliente(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        VehiculoClienteDTO vehiculoDTO = result.get(0);
        assertEquals(1L, vehiculoDTO.getIdVehiculo());
        assertEquals("ABC123", vehiculoDTO.getPlaca());
        assertEquals("Toyota", vehiculoDTO.getMarca());
        assertEquals("Corolla", vehiculoDTO.getModelo());
        assertEquals("Blanco", vehiculoDTO.getColor());
        assertEquals("CUATRO_RUEDAS", vehiculoDTO.getTipoVehiculo()); // Corregido: usar el enum correcto

        verify(usuarioRepository).findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO);
        verify(vehiculoRepository).findByPropietario_IdPersonaAndEstado(1L, Vehiculo.EstadoVehiculo.ACTIVO);
    }

    @Test
    void testObtenerVehiculosCliente_ClienteNotFound() {
        // Arrange
        when(usuarioRepository.findByIdUsuarioAndEstado(999L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.obtenerVehiculosCliente(999L);
        });

        assertEquals("Cliente no encontrado o inactivo", exception.getMessage());
        verify(usuarioRepository).findByIdUsuarioAndEstado(999L, Usuario.EstadoUsuario.ACTIVO);
    }

    @Test
    void testNuevaSuscripcionCliente_Success_Mensual() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(1L);
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_CREDITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN789");

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList());
        when(tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE))
                .thenReturn(mockTarifaBase);
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(mockSuscripcion);
        when(historialPagoSuscripcionRepository.save(any(HistorialPagoSuscripcion.class)))
                .thenReturn(new HistorialPagoSuscripcion());

        // Act
        String result = suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);

        // Assert
        assertEquals("Nueva suscripción creada con éxito", result);
        verify(suscripcionRepository).save(any(Suscripcion.class));
        verify(historialPagoSuscripcionRepository).save(any(HistorialPagoSuscripcion.class));
    }

    @Test
    void testNuevaSuscripcionCliente_Success_Anual() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(1L);
        nuevaSuscripcionDTO.setPeriodoContratado("ANUAL");
        nuevaSuscripcionDTO.setMetodoPago("TARJETA_CREDITO");
        nuevaSuscripcionDTO.setNumeroTransaccion("TXN790");

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList());
        when(tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE))
                .thenReturn(mockTarifaBase);
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(mockSuscripcion);
        when(historialPagoSuscripcionRepository.save(any(HistorialPagoSuscripcion.class)))
                .thenReturn(new HistorialPagoSuscripcion());

        // Act
        String result = suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);

        // Assert
        assertEquals("Nueva suscripción creada con éxito", result);
        verify(suscripcionRepository).save(any(Suscripcion.class));
        verify(historialPagoSuscripcionRepository).save(any(HistorialPagoSuscripcion.class));
    }

    @Test
    void testNuevaSuscripcionCliente_ClienteNotFound() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(999L);

        when(usuarioRepository.findByIdUsuarioAndEstado(999L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Cliente no encontrado o inactivo", exception.getMessage());
        verify(usuarioRepository).findByIdUsuarioAndEstado(999L, Usuario.EstadoUsuario.ACTIVO);
    }

    @Test
    void testNuevaSuscripcionCliente_VehiculoNotFound() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(999L);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Vehículo no encontrado o no pertenece al cliente", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_VehiculoNoPertenece() {
        // Arrange
        Persona otraPersona = new Persona();
        otraPersona.setIdPersona(2L);
        
        Vehiculo otroVehiculo = new Vehiculo();
        otroVehiculo.setId(1L);
        otroVehiculo.setPropietario(otraPersona);

        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(otroVehiculo));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Vehículo no encontrado o no pertenece al cliente", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_EmpresaNotFound() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(999L);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Empresa no encontrada", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_TipoPlanNotFound() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(999L);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Tipo de plan no encontrado o inactivo", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_TipoPlanInactivo() {
        // Arrange
        TipoPlan planInactivo = new TipoPlan();
        planInactivo.setId(1L);
        planInactivo.setActivo(TipoPlan.EstadoConfiguracion.HISTORICO);

        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(1L);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(planInactivo));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("Tipo de plan no encontrado o inactivo", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_SuscripcionExistente() {
        // Arrange
        Suscripcion suscripcionExistente = new Suscripcion();
        suscripcionExistente.setEmpresa(mockEmpresa);
        suscripcionExistente.setTipoPlan(mockTipoPlan);
        suscripcionExistente.setVehiculo(mockVehiculo);
        suscripcionExistente.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(1L);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(suscripcionRepository.findByUsuario_IdUsuario(1L))
                .thenReturn(Arrays.asList(suscripcionExistente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("El vehículo ya tiene una suscripción activa con el mismo tipo de plan en esta empresa", exception.getMessage());
    }

    @Test
    void testNuevaSuscripcionCliente_TarifaBaseNotFound() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(1L);
        nuevaSuscripcionDTO.setPeriodoContratado("MENSUAL");

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList());
        when(tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE))
                .thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        assertEquals("No hay una tarifa base vigente para la empresa seleccionada", exception.getMessage());
    }
/*
    @Test
    void testNuevaSuscripcionCliente_PeriodoInvalido() {
        // Arrange
        NuevaSuscripcionDTO nuevaSuscripcionDTO = new NuevaSuscripcionDTO();
        nuevaSuscripcionDTO.setIdCliente(1L);
        nuevaSuscripcionDTO.setIdVehiculo(1L);
        nuevaSuscripcionDTO.setIdEmpresa(1L);
        nuevaSuscripcionDTO.setIdTipoPlanSuscripcion(1L);
        nuevaSuscripcionDTO.setPeriodoContratado("INVALID");

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(mockVehiculo));
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(mockEmpresa));
        when(tipoPlanRepository.findById(1L)).thenReturn(Optional.of(mockTipoPlan));
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(Arrays.asList());
        when(tarifaBaseRepository.findByEmpresa_IdEmpresaAndEstado(1L, TarifaBase.EstadoTarifaBase.VIGENTE))
                .thenReturn(mockTarifaBase);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.nuevaSuscripcionCliente(nuevaSuscripcionDTO);
        });

        // Verificar que el error sea por el enum inválido o el mensaje de periodo no válido
        assertTrue(exception.getMessage().contains("No enum constant") || 
                  exception.getMessage().contains("Periodo contratado no válido"));
    }
*/
    @Test
    void testRenovarSuscripcionCliente_Success_Mensual() {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(1L);
        renovacionDTO.setNuevoPeriodoContratado("MENSUAL");
        renovacionDTO.setMetodoPago("TARJETA_CREDITO");
        renovacionDTO.setNumeroTransaccion("TXN999");

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(mockSuscripcion);
        when(historialPagoSuscripcionRepository.save(any(HistorialPagoSuscripcion.class)))
                .thenReturn(new HistorialPagoSuscripcion());

        // Act
        String result = suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);

        // Assert
        assertEquals("Suscripción renovada con éxito", result);
        verify(suscripcionRepository).save(any(Suscripcion.class));
        verify(historialPagoSuscripcionRepository).save(any(HistorialPagoSuscripcion.class));
    }

    @Test
    void testRenovarSuscripcionCliente_Success_Anual() {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(1L);
        renovacionDTO.setNuevoPeriodoContratado("ANUAL");
        renovacionDTO.setMetodoPago("TARJETA_CREDITO");
        renovacionDTO.setNumeroTransaccion("TXN1000");

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(mockSuscripcion);
        when(historialPagoSuscripcionRepository.save(any(HistorialPagoSuscripcion.class)))
                .thenReturn(new HistorialPagoSuscripcion());

        // Act
        String result = suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);

        // Assert
        assertEquals("Suscripción renovada con éxito", result);
        verify(suscripcionRepository).save(any(Suscripcion.class));
        verify(historialPagoSuscripcionRepository).save(any(HistorialPagoSuscripcion.class));
    }

    @Test
    void testRenovarSuscripcionCliente_SuscripcionNotFound() {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(999L);

        when(suscripcionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);
        });

        assertEquals("Suscripción no encontrada", exception.getMessage());
        verify(suscripcionRepository).findById(999L);
    }

    @Test
    void testRenovarSuscripcionCliente_PeriodoInvalido() {
        // Arrange
        RenovacionSuscripcionDTO renovacionDTO = new RenovacionSuscripcionDTO();
        renovacionDTO.setIdSuscripcion(1L);
        renovacionDTO.setNuevoPeriodoContratado("INVALID");

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(mockSuscripcion));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            suscripcionClienteService.renovarSuscripcionCliente(renovacionDTO);
        });

        assertEquals("Periodo contratado no válido", exception.getMessage());
    }

    @Test
    void testObtenerEmpresaSuscripciones_WithoutConfigDescuento() {
        // Arrange
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);
        List<TipoPlan> tiposPlanes = Arrays.asList(mockTipoPlan);

        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(sucursales);
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(tiposPlanes);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(null);

        // Act
        PlanesSuscripcionDTO.EmpresaSuscripcionesDTO result = suscripcionClienteService.obtenerEmpresaSuscripciones(mockEmpresa);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdEmpresa());
        assertEquals(1, result.getSuscripciones().size());
        
        DetalleTipoPlanDTO planDTO = result.getSuscripciones().get(0);
        assertNull(planDTO.getConfiguracionDescuento());
    }

    @Test
    void testObtenerPlanesSuscripcionPorCliente_WithMultipleSuscripciones() {
        // Arrange
        Suscripcion suscripcion2 = new Suscripcion();
        suscripcion2.setId(2L);
        suscripcion2.setEmpresa(mockEmpresa);
        suscripcion2.setUsuario(mockUsuario);
        suscripcion2.setVehiculo(mockVehiculo);
        suscripcion2.setTipoPlan(mockTipoPlan);
        suscripcion2.setTarifaBaseReferencia(mockTarifaBase);
        suscripcion2.setPeriodoContratado(Suscripcion.PeriodoContratado.ANUAL);
        suscripcion2.setDescuentoAplicado(new BigDecimal("30.00"));
        suscripcion2.setPrecioPlan(new BigDecimal("3570.00"));
        suscripcion2.setHorasMensualesIncluidas(160);
        suscripcion2.setHorasConsumidas(new BigDecimal("80.00"));
        suscripcion2.setFechaInicio(LocalDateTime.now());
        suscripcion2.setFechaFin(LocalDateTime.now().plusYears(1));
        suscripcion2.setFechaCompra(LocalDateTime.now());
        suscripcion2.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        List<Suscripcion> suscripciones = Arrays.asList(mockSuscripcion, suscripcion2);
        List<Sucursal> sucursales = Arrays.asList(mockSucursal);
        List<TipoPlan> tiposPlanes = Arrays.asList(mockTipoPlan);

        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(suscripcionRepository.findByUsuario_IdUsuario(1L)).thenReturn(suscripciones);
        when(sucursalRepository.findByEmpresaIdEmpresa(1L)).thenReturn(sucursales);
        when(tipoPlanRepository.findByEmpresa_IdEmpresaAndActivo(1L, TipoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(tiposPlanes);
        when(configuracionDescuentoPlanRepository.findByTipoPlan_IdAndEstado(1L, ConfiguracionDescuentoPlan.EstadoConfiguracion.VIGENTE))
                .thenReturn(mockConfigDescuento);

        // Act
        ClientePlanesSuscripcionDTO result = suscripcionClienteService.obtenerPlanesSuscripcionPorCliente(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getSuscripcionCliente().size());
        
        // Verificar primera suscripción (mensual)
        ClientePlanesSuscripcionDTO.SuscripcionClienteDTO suscripcion1DTO = result.getSuscripcionCliente().get(0);
        assertEquals("MENSUAL", suscripcion1DTO.getPeriodoContratado());
        assertEquals(0.00, suscripcion1DTO.getDescuentoAplicado());

        // Verificar segunda suscripción (anual)
        ClientePlanesSuscripcionDTO.SuscripcionClienteDTO suscripcion2DTO = result.getSuscripcionCliente().get(1);
        assertEquals("ANUAL", suscripcion2DTO.getPeriodoContratado());
        assertEquals(30.00, suscripcion2DTO.getDescuentoAplicado());
    }

    @Test
    void testObtenerVehiculosCliente_EmptyList() {
        // Arrange
        when(usuarioRepository.findByIdUsuarioAndEstado(1L, Usuario.EstadoUsuario.ACTIVO))
                .thenReturn(mockUsuario);
        when(vehiculoRepository.findByPropietario_IdPersonaAndEstado(1L, Vehiculo.EstadoVehiculo.ACTIVO))
                .thenReturn(Arrays.asList());

        // Act
        List<VehiculoClienteDTO> result = suscripcionClienteService.obtenerVehiculosCliente(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
