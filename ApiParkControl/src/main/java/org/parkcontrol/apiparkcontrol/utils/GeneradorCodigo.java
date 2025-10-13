package org.parkcontrol.apiparkcontrol.utils;

import java.time.LocalDate;
import java.util.UUID;

public class GeneradorCodigo {

    public String getCodigo(String prefijo, LocalDate fecha, long contador){
        return prefijo + "-" + fecha + "-" + contador;
    }

    public String getCode(){
        return UUID.randomUUID().toString();
    }
}
