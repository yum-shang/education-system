package com.example.education.system.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {
    private Object eventData;
    private Integer eventType;
}
