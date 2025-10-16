package org.parkcontrol.apiparkcontrol.services.autenticacion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.parkcontrol.apiparkcontrol.repositories.UsuarioRepository;
import org.parkcontrol.apiparkcontrol.models.*;
import org.parkcontrol.apiparkcontrol.dto.autenticacion.*;
import org.parkcontrol.apiparkcontrol.repositories.PersonaRepository;
import org.parkcontrol.apiparkcontrol.repositories.RolRepository;
import org.parkcontrol.apiparkcontrol.repositories.TokenAuthRepository;
import org.parkcontrol.apiparkcontrol.services.email.EmailService;
import org.parkcontrol.apiparkcontrol.utils.Encriptation;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class authenticateService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;


    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private TokenAuthRepository tokenAutenticacionRepository;
    
    @Autowired
    private EmailService emailService;

    private Encriptation passwordEncoder = new Encriptation();

    @Transactional
    public Usuario registerUser(RegisterUserDTO userDTO) {

        // Crear y guardar la persona
        Persona persona = new Persona();
        persona.setNombre(userDTO.getNombre());
        persona.setApellido(userDTO.getApellido());
        persona.setFechaNacimiento(LocalDate.parse(userDTO.getFechaNacimiento()));
        persona.setDpi(userDTO.getDpi());
        persona.setCorreo(userDTO.getCorreo());
        persona.setTelefono(userDTO.getTelefono());
        persona.setDireccionCompleta(userDTO.getDireccionCompleta());
        persona.setCiudad(userDTO.getCiudad());
        persona.setPais(userDTO.getPais());
        persona.setCodigoPostal(userDTO.getCodigoPostal());
        persona.setEstado(Persona.Estado.ACTIVO);
        personaRepository.save(persona);

        // Crear y guardar el usuario
        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        //Buscamos por el nombre del rol, si no existe lanza una excepción
        //Nombre rol = "CLIENTE"
        Rol rol = rolRepository.findByNombreRol("CLIENTE");
        usuario.setRol(rol);
        usuario.setNombreUsuario(userDTO.getNombreUsuario());
        usuario.setContraseniaHash(passwordEncoder.encrypt(userDTO.getContraseniaHash()));
        usuario.setDobleFactorHabilitado(userDTO.isDobleFactorHabilitado());
        usuario.setEstado(Usuario.EstadoUsuario.valueOf(userDTO.getEstado()));
        usuario.setDebeCambiarContrasenia(false);
        usuario.setEsPrimeraVez(false);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequest) throws Exception {
        // Buscar el usuario por nombre de usuario
        Usuario usuario = usuarioRepository.findByNombreUsuario(loginRequest.getNombreUsuario());
        if(usuario == null){
            throw new Exception("Nombre de usuario o contraseña incorrectos");
        }

        // Verificar la contraseña
        if (!passwordEncoder.matches(loginRequest.getContrasenia(), usuario.getContraseniaHash())) {
            // Incrementar intentos fallidos
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            
            // Si los intentos fallidos llegan a 5, suspender el usuario
            if (usuario.getIntentosFallidos() >= 5) {
                usuario.setEstado(Usuario.EstadoUsuario.SUSPENDIDO);
            }
            
            usuarioRepository.save(usuario);
            throw new Exception("Nombre de usuario o contraseña incorrectos");
        }
        
        // Verificar si el usuario está activo
        if (usuario.getEstado() != Usuario.EstadoUsuario.ACTIVO) {
            throw new Exception("El usuario no está activo. Contacte al administrador.");
        }
        
        // Manejar autenticación de dos factores
        if (usuario.isDobleFactorHabilitado()) {
            // Obtener información de la persona
            Persona persona = usuario.getPersona();
            
            // Generar código de verificación (6 dígitos)
            String codigoVerificacion = String.format("%06d", new Random().nextInt(1000000));
            
            // Crear un token aleatorio de 10 caracteres
            String token = generateRandomToken(10);
            
            // Crear token en la base de datos
            //ESTO DEBERÁ GUARDARSE EN REDIS CAMBIAR DESPUÉS
            TokenAuth tokenAuth = new TokenAuth();
            tokenAuth.setUsuario(usuario);
            tokenAuth.setTokenHash(token);
            tokenAuth.setTipoToken(TokenAuth.TipoToken.DobleFactor); // Aseguramos que sea consistente con la búsqueda
            tokenAuth.setFechaExpiracion(LocalDateTime.now().plusMinutes(15)); // 15 minutos
            tokenAuth.setCodigoVerificacion(codigoVerificacion);
            tokenAuth.setEstado(TokenAuth.EstadoToken.ACTIVO); // Aseguramos que se establezca explícitamente
            TokenAuth savedToken = tokenAutenticacionRepository.save(tokenAuth);
            /*Ejemplo implementacion en Redis
            String redisKey = "2FA_TOKEN:" + token;
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("idUsuario", usuario.getIdUsuario().toString());
            tokenData.put("codigoVerificacion", codigoVerificacion);
            tokenData.put("estado", "ACTIVO");
            tokenData.put("fechaExpiracion", tokenAuth.getFechaExpiracion().toString());
            redisTemplate.opsForHash().putAll(redisKey, tokenData);
            redisTemplate.expireAt(redisKey, tokenAuth.getFechaExpiracion());
             */
            
            /*log.info("Token guardado: ID={}, Hash={}, Tipo={}, Estado={}",
                     savedToken.getIdToken(), savedToken.getTokenHash(), 
                     savedToken.getTipoToken(), savedToken.getEstado());
            */
            // Enviar el código de verificación por correo electrónico
            try {
                emailService.sendVerificationCode(persona.getCorreo(), codigoVerificacion);
            } catch (MessagingException e) {
                throw new Exception("Error al enviar el correo electrónico: " + e.getMessage());
            }
            
            return LoginResponseDTO.builder()
                    .autenticacion(new LoginResponseDTO.Autenticacion(true, false ))
                    .credenciales(LoginResponseDTO.Credenciales.builder()
                            .mensaje("Código de verificación enviado al correo")
                            .idUsuario(usuario.getIdUsuario())
                            .token(token)
                            .build())
                    .build();
        } else {
            // No se requiere autenticación de dos factores
            // Resetear intentos fallidos y actualizar última fecha de acceso
            usuario.setIntentosFallidos(0);
            usuario.setUltimaFechaAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            // Obtener información del rol
            Rol rol = usuario.getRol();
            boolean debeCambiarContrasenia = usuario.isDebeCambiarContrasenia();
            
            return LoginResponseDTO.builder()
                    .autenticacion(new LoginResponseDTO.Autenticacion(false, debeCambiarContrasenia))
                    .credenciales(LoginResponseDTO.Credenciales.builder()
                            .mensaje("Inicio de sesión exitoso")
                            .idUsuario(usuario.getIdUsuario())
                            .rol(rol.getIdRol())
                            .nombreRol(rol.getNombreRol())
                            .nombreUsuario(usuario.getNombreUsuario())
                            .build())
                    .build();
        }
    }
    
    private String generateRandomToken(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            token.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return token.toString();
    }

    @Transactional
    public Map<String, Object> verify2FACode(Verify2FADTO verify2FADTO) throws Exception {
        String token = verify2FADTO.getToken();
        String codigoVerificacion = verify2FADTO.getCodigoVerificacion();
        
        if (token == null || codigoVerificacion == null) {
            throw new Exception("Token y código de verificación son requeridos");
        }
        
        //log.info("Token recibido: {}, Código de verificación recibido: {}", token, codigoVerificacion);
        
        // Buscar todos los tokens activos
        /*List<TokenAutenticacion> allTokens = tokenAutenticacionRepository.findAll();
        log.info("Total de tokens en la base de datos: {}", allTokens.size());
        for (TokenAutenticacion t : allTokens) {
            log.info("Token en DB: {} - Tipo: {} - Estado: {}", t.getTokenHash(), t.getTipoToken(), t.getEstado());
        }*/
        
        // Buscar el token específico
        Optional<TokenAuth> tokenOptional = tokenAutenticacionRepository.findByTokenHash(token);

        if (tokenOptional.isEmpty()) {
            throw new Exception("Token no encontrado");
        }

        TokenAuth tokenRecord = tokenOptional.get();
        /*
            Esta implementacion debe de cambiarse por Redis
            String redisKey = "2FA_TOKEN:" + token;
            if (!redisTemplate.hasKey(redisKey)) {
                throw new Exception("Token inválido o expirado");
            }
            Map<Object, Object> tokenData = redisTemplate.opsForHash().entries(redisKey);
            if (tokenData == null || tokenData.isEmpty()) {
                throw new Exception("Token inválido o expirado");
            }
            String estado = (String) tokenData.get("estado");
            if (!"ACTIVO".equals(estado)) {
                throw new Exception("Token no está activo");
            }
            String codigoVerificacionStored = (String) tokenData.get("codigoVerificacion");
            if (!codigoVerificacionStored.equals(codigoVerificacion)) {
                throw new Exception("Código de verificación incorrecto");
            }
            Long idUsuario = Long.parseLong((String) tokenData.get("idUsuario"));
            // Marcar el token como 'USADO' en Redis
            redisTemplate.opsForHash().put(redisKey, "estado", "USADO");
            // Obtener el usuario asociado al token
            Usuario usuario = usuarioRepository.findById(idUsuario);
            if (usuario.isEmpty()) {
                throw new Exception("Usuario no encontrado");
            }

         */
        
        // Verificamos el tipo del token
        if (!tokenRecord.getTipoToken().equals(TokenAuth.TipoToken.DobleFactor)) {
            throw new Exception("Tipo de token incorrecto");
        }
        
        // Verificar el estado del token
        if (!tokenRecord.getEstado().equals(TokenAuth.EstadoToken.ACTIVO)) {
            throw new Exception("Token no está activo");
        }
        
        // Verificar si el token ha expirado
        if (LocalDateTime.now().isAfter(tokenRecord.getFechaExpiracion())) {
            tokenRecord.setEstado(TokenAuth.EstadoToken.EXPIRADO);
            tokenAutenticacionRepository.save(tokenRecord);
            throw new Exception("El token ha expirado");
        }
        
        // Verificar el código de verificación
        if (!tokenRecord.getCodigoVerificacion().equals(codigoVerificacion)) {
            throw new Exception("Código de verificación incorrecto");
        }
        
        // Marcar el token como 'USADO'
        tokenRecord.setEstado(TokenAuth.EstadoToken.USADO);
        tokenAutenticacionRepository.save(tokenRecord);
        
        // Obtener el usuario asociado al token
        
        Usuario usuario = tokenRecord.getUsuario();
        Rol rol = usuario.getRol();
        
        // Actualizar información del usuario
        usuario.setUltimaFechaAcceso(LocalDateTime.now());
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Código de verificación autenticado correctamente");
        response.put("idUsuario", usuario.getIdUsuario());
        response.put("rol", rol.getIdRol());
        response.put("nombreRol", rol.getNombreRol());
        response.put("nombreUsuario", usuario.getNombreUsuario());
        
        return response;
    }

    @Transactional
    public Map<String, Object> recoverPassword(RecoverPasswordDTO recoverPasswordDTO) throws Exception {
        String correo = recoverPasswordDTO.getCorreo();
        
        if (correo == null || correo.isEmpty()) {
            throw new Exception("El correo es requerido");
        }
        
        // Buscar la persona por correo
        Persona persona = personaRepository.findByCorreo(correo);
        if (persona == null) {
            throw new Exception("Correo no registrado");
        }

        // Buscar el usuario asociado a la persona
        Usuario usuario = usuarioRepository.findByPersona(persona);


        // Generar código de verificación (6 dígitos)
        String codigoVerificacion = String.format("%06d", new Random().nextInt(1000000));
        
        // Crear un token aleatorio
        String token = generateRandomToken(20);
        
        // Crear token en la base de datos
        TokenAuth tokenAutenticacion = new TokenAuth();
        tokenAutenticacion.setUsuario(usuario);
        tokenAutenticacion.setTokenHash(token);
        tokenAutenticacion.setTipoToken(TokenAuth.TipoToken.RESET_PASSWORD);
        tokenAutenticacion.setCodigoVerificacion(codigoVerificacion);
        tokenAutenticacion.setFechaExpiracion(LocalDateTime.now().plusMinutes(15)); // 15 minutos
        tokenAutenticacion.setEstado(TokenAuth.EstadoToken.ACTIVO);
        tokenAutenticacionRepository.save(tokenAutenticacion);
        
        // Enviar correo electrónico con el código de verificación
        try {
            emailService.sendRecoveryCode(persona.getCorreo(), codigoVerificacion);
        } catch (MessagingException e) {
            throw new Exception("Error al enviar el correo electrónico: " + e.getMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Código de recuperación enviado al correo");
        response.put("user", usuario.getIdUsuario());
        response.put("token", token);
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> verifyRecoveryCode(Verify2FADTO verifyRecoveryDTO) throws Exception {
        String token = verifyRecoveryDTO.getToken();
        String codigoVerificacion = verifyRecoveryDTO.getCodigoVerificacion();
        
        if (token == null || codigoVerificacion == null) {
            throw new Exception("Token y código de verificación son requeridos");
        }
        
        // Buscar el token en la base de datos
        Optional<TokenAuth> tokenOptional = tokenAutenticacionRepository
                .findByTokenHashAndTipoToken(token, TokenAuth.TipoToken.RESET_PASSWORD);
        
        if (tokenOptional.isEmpty() || !tokenOptional.get().getEstado().equals(TokenAuth.EstadoToken.ACTIVO)) {
            throw new Exception("Token inválido");
        }
        
        TokenAuth tokenRecord = tokenOptional.get();
        
        // Verificar si el token ha expirado
        if (LocalDateTime.now().isAfter(tokenRecord.getFechaExpiracion())) {
            tokenRecord.setEstado(TokenAuth.EstadoToken.EXPIRADO);
            tokenAutenticacionRepository.save(tokenRecord);
            throw new Exception("El token ha expirado");
        }
        
        // Verificar el código de verificación
        if (!tokenRecord.getCodigoVerificacion().equals(codigoVerificacion)) {
            throw new Exception("Código de verificación incorrecto");
        }
        
        // Marcar el token como 'USADO'
        tokenRecord.setEstado(TokenAuth.EstadoToken.USADO);
        tokenAutenticacionRepository.save(tokenRecord);
        
        // Obtener el usuario asociado al token
        Usuario usuario = tokenRecord.getUsuario();

        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Código de verificación autenticado correctamente");
        response.put("token", tokenRecord.getTokenHash());
        response.put("idUsuario", tokenRecord.getUsuario().getIdUsuario());
        response.put("nombreUsuario", usuario.getNombreUsuario());
        
        return response;
    }
    
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) throws Exception {
        Long idUsuario = resetPasswordDTO.getIdUsuario();
        String token = resetPasswordDTO.getToken();
        String nuevaContrasenia = resetPasswordDTO.getNuevaContrasenia();
        
        if (idUsuario == null || token == null || nuevaContrasenia == null) {
            throw new Exception("ID de usuario, token y nueva contraseña son requeridos");
        }
        
        // Buscar el token en la base de datos
        Optional<TokenAuth> tokenOptional = tokenAutenticacionRepository
                .findByTokenHashAndTipoToken(token, TokenAuth.TipoToken.RESET_PASSWORD);
        
        if (tokenOptional.isEmpty() || !tokenOptional.get().getEstado().equals(TokenAuth.EstadoToken.USADO)
                || !tokenOptional.get().getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new Exception("Token inválido");
        }
        
        TokenAuth tokenRecord = tokenOptional.get();
        
        // Verificar si el token ha expirado
        if (LocalDateTime.now().isAfter(tokenRecord.getFechaExpiracion())) {
            throw new Exception("El token ha expirado");
        }
        
        // Obtener el usuario asociado al token
        Usuario usuario = tokenRecord.getUsuario();
        usuario.setContraseniaHash(passwordEncoder.encrypt(nuevaContrasenia));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void toggle2FAStatus(Long idUsuario) throws Exception {
        // Buscar el usuario por id
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);
        if (usuarioOptional.isEmpty()) {
            throw new Exception("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOptional.get();
        
        // Cambiar el estado de autenticación de dos factores
        usuario.setDobleFactorHabilitado(!usuario.isDobleFactorHabilitado());
        
        // Guardar los cambios
        usuarioRepository.save(usuario);

    }

    //Cambiar contraseña al iniciar sesión por primera vez
    @Transactional
    public Map<String, Object>  changePasswordFirstTime(ResetPasswordDTO changePasswordDTO) throws Exception {
        Long idUsuario = changePasswordDTO.getIdUsuario();
        String nuevaContrasenia = changePasswordDTO.getNuevaContrasenia();

        if (idUsuario == null || nuevaContrasenia == null) {
            throw new Exception("ID de usuario y nueva contraseña son requeridos");
        }

        // Buscar el usuario por id
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);
        if (usuarioOptional.isEmpty()) {
            throw new Exception("Usuario no encontrado");
        }

        Usuario usuario = usuarioOptional.get();

        // Verificar si debe cambiar la contraseña
        if (!usuario.isDebeCambiarContrasenia()) {
            throw new Exception("El usuario ya ha cambiado su contraseña anteriormente");
        }

        // Cambiar la contraseña y actualizar el estado
        usuario.setContraseniaHash(passwordEncoder.encrypt(nuevaContrasenia));
        usuario.setEsPrimeraVez(false);
        usuario.setDebeCambiarContrasenia(false);

        // Guardar los cambios
        usuarioRepository.save(usuario);
        Rol rol = usuario.getRol();

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Contraseña cambiada exitosamente");
        response.put("idUsuario", usuario.getIdUsuario());
        response.put("rol", rol.getIdRol());
        response.put("nombreRol", rol.getNombreRol());
        response.put("nombreUsuario", usuario.getNombreUsuario());


        return response;
    }

}
