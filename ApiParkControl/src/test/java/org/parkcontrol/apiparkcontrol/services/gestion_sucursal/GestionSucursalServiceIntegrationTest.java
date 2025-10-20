package org.parkcontrol.apiparkcontrol.services.gestion_sucursal;

import org.springframework.beans.factory.annotation.Autowired;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.parkcontrol.apiparkcontrol.dto.gestion_sucursal.*;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class GestionSucursalServiceIntegrationTest {

    @Autowired
    private GestionSucursalService gestionSucursalService;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RolRepository rolRepository;

    private Empresa testEmpresa;
    private Usuario testUsuario;
    private Sucursal testSucursal;
    private Rol sucursalRol;

    @BeforeEach
    void setUp() {
        // Clean up database
        sucursalRepository.deleteAll();
        usuarioRepository.deleteAll();
        personaRepository.deleteAll();
        empresaRepository.deleteAll();
        rolRepository.deleteAll();

        // Create test role
        sucursalRol = new Rol();
        sucursalRol.setNombreRol("SUCURSAL");
        sucursalRol.setDescripcion("Usuario de sucursal");
        sucursalRol = rolRepository.save(sucursalRol);

        // Create test empresa
        testEmpresa = new Empresa();
        testEmpresa.setNombreComercial("Test Company");
        testEmpresa.setRazonSocial("Test Company S.A.");
        testEmpresa.setNit("1234567-8");
        testEmpresa.setDireccionFiscal("Test Fiscal Address");
        testEmpresa.setTelefonoPrincipal("12345678");
        testEmpresa.setCorreoPrincipal("empresa@test.com");
        testEmpresa.setEstado(Empresa.EstadoEmpresa.ACTIVA);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Create test usuario and persona
        Persona persona = new Persona();
        persona.setNombre("Test");
        persona.setApellido("User");
        persona.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));
        persona.setDpi(generateUniqueDpi());
        persona.setCorreo("test@test.com");
        persona.setTelefono("12345678");
        persona.setDireccionCompleta("Test Address");
        persona.setCiudad("Test City");
        persona.setPais("Test Country");
        persona.setCodigoPostal("12345");
        persona.setEstado(Persona.Estado.ACTIVO);
        persona = personaRepository.save(persona);

        testUsuario = new Usuario();
        testUsuario.setPersona(persona);
        testUsuario.setRol(sucursalRol);
        testUsuario.setNombreUsuario("testuser");
        testUsuario.setContraseniaHash("hashedPassword");
        testUsuario.setDobleFactorHabilitado(false);
        testUsuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        testUsuario.setDebeCambiarContrasenia(false);
        testUsuario.setEsPrimeraVez(false);
        testUsuario.setIntentosFallidos(0);
        testUsuario = usuarioRepository.save(testUsuario);

        // Create test sucursal
        testSucursal = new Sucursal();
        testSucursal.setEmpresa(testEmpresa);
        testSucursal.setUsuarioSucursal(testUsuario);
        testSucursal.setNombre("Test Sucursal");
        testSucursal.setDireccionCompleta("Test Address");
        testSucursal.setCiudad("Test City");
        testSucursal.setDepartamento("Test Department");
        testSucursal.setHoraApertura(LocalTime.of(8, 0));
        testSucursal.setHoraCierre(LocalTime.of(18, 0));
        testSucursal.setCapacidad2Ruedas(50);
        testSucursal.setCapacidad4Ruedas(30);
        testSucursal.setLatitud(new BigDecimal("14.634915"));
        testSucursal.setLongitud(new BigDecimal("-90.506882"));
        testSucursal.setTelefonoContacto("87654321");
        testSucursal.setCorreoContacto("sucursal@test.com");
        testSucursal.setEstado(Sucursal.EstadoSucursal.ACTIVA);
        testSucursal = sucursalRepository.save(testSucursal);
    }

    // ...existing test methods...

    private String generateUniqueDpi() {
        return String.valueOf(System.currentTimeMillis()).substring(0, 13);
    }
}