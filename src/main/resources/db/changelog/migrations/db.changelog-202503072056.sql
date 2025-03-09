--liquibase formatted sql
--changeset eduarda:202503072056
--comment: board_columns table create

CREATE TABLE BOARD_COLUMNS(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    `order` INT NOT NULL,
    kind VARCHAR(7) NOT NULL,
    board_id BIGINT NOT NULL,
    CONSTRAINT fk_boards_board_columns FOREIGN KEY (board_id) REFERENCES BOARDS(id) ON DELETE CASCADE,
    CONSTRAINT id_order_uk UNIQUE KEY unique_board_id_order (id, `order`)
) ENGINE=InnoDB;

--rollback DROP TABLE BOARD_COLUMNS


