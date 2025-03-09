package br.com.dio.persistence.dto;

import br.com.dio.persistence.entity.BoardColumnKindEnum;

public record BoardColumnInfoDTO(Long id,
                                 int order,
                                 BoardColumnKindEnum kind) {
}