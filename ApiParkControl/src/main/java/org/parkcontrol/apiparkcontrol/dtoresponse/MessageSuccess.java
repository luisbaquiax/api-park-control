package org.parkcontrol.apiparkcontrol.dtoresponse;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageSuccess {
    private int code;
    private String message;
}
