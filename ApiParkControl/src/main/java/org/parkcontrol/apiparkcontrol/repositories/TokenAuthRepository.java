package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.TokenAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenAuthRepository extends JpaRepository<TokenAuth, Long> {
    Optional<TokenAuth> findByTokenHashAndTipoToken(String tokenHash, TokenAuth.TipoToken tipoToken);
    Optional<TokenAuth> findByTokenHash(String tokenHash);
    List<TokenAuth> findByTipoToken(TokenAuth.TipoToken tipoToken);
    List<TokenAuth> findByEstado(TokenAuth.EstadoToken estado);

}
