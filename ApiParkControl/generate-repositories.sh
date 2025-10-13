#!/bin/bash

# Carpeta donde se generarán los repositorios
REPO_DIR="src/main/java/org/parkcontrol/apiparkcontrol/repositories"

# Crear la carpeta si no existe
mkdir -p $REPO_DIR

# Lista de entidades
ENTIDADES=(
AcreditacionHorasComercio
BitacoraConfiguracionDescuento
BitacoraSistema
BitacoraTarifaBase
BitacoraTarifaSucursal
ComercioAfiliado
ConfiguracionDescuentoPlan
ConvenioComercioSucursal
CorteCaja
Empresa
EmpresaFlotilla
EvidenciaCambioPlaca
EvidenciaIncidencia
HistorialConsumoSuscripcion
IncidenciaTicket
LiquidacionComercio
Notificacion
OcupacionSucursal
PermisoTemporal
Persona
PlanCorporativo
Rol
SolicitudCambioPlaca
Sucursal
Suscripcion
SuscripcionFlotilla
TarifaBase
TarifaSucursal
Ticket
TipoPlan
TransaccionTicket
Usuario
Vehiculo
)

# Generar cada repositorio
for ENTIDAD in "${ENTIDADES[@]}"; do
  NOMBRE_REPO="${ENTIDAD}Repository"
  ARCHIVO="$REPO_DIR/$NOMBRE_REPO.java"

  cat > $ARCHIVO <<EOL
package org.parkcontrol.apiparkcontrol.repositories;

import org.parkcontrol.apiparkcontrol.models.$ENTIDAD;
import org.springframework.data.jpa.repository.JpaRepository;

public interface $NOMBRE_REPO extends JpaRepository<$ENTIDAD, Long> {
}
EOL

  echo "Generado: $ARCHIVO"
done

echo "Todos los repositorios fueron generados."
