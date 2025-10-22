package org.parkcontrol.apiparkcontrol.dto.messagesuccess;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageSuccess {
    private int code;
    private String message;
}
