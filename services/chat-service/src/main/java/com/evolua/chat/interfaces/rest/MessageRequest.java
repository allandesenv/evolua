package com.evolua.chat.interfaces.rest; public record MessageRequest(@jakarta.validation.constraints.NotBlank String recipientId, @jakarta.validation.constraints.NotBlank String content) { }
