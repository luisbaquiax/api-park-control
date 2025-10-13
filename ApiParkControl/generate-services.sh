#!/bin/bash

# Carpeta donde se generarán los servicios
SERVICE_DIR="src/main/java/org/parkcontrol/apiparkcontrol/services"

# Crear la carpeta si no existe
mkdir -p $SERVICE_DIR

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

# Generar cada servicio
for ENTIDAD in "${ENTIDADES[@]}"; do
  NOMBRE_SERVICE="${ENTIDAD}Service"
  ARCHIVO="$SERVICE_DIR/$NOMBRE_SERVICE.java"

  cat > $ARCHIVO <<EOL
package org.parkcontrol.apiparkcontrol.services;

import org.springframework.stereotype.Service;

@Service
public class $NOMBRE_SERVICE {
}
EOL

  echo "Generado: $ARCHIVO"
done

echo "Todos los servicios fueron generados."
