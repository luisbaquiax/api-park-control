package org.parkcontrol.apiparkcontrol.dto.messagesuccess;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageSuccess {
    private int code;
    private String message;
}
