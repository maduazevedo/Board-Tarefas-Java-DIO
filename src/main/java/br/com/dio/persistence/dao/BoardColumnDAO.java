package br.com.dio.persistence.dao;
import br.com.dio.persistence.dto.BoardColumnDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.CardEntity;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.AllArgsConstructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.dio.persistence.entity.BoardColumnKindEnum.findByName;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class BoardColumnDAO {

    private Connection connection;

    public BoardColumnEntity insert(final BoardColumnEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARD_COLUMNS (name, kind, `order`, board_id) values (?, ?, ?, ?);";
        try(var statement = connection.prepareStatement(sql)){
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getKind().name());
            statement.setInt(3, entity.getOrder());
            statement.setLong(4, entity.getBoard().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;
    }

    public List<BoardColumnEntity> findByBoardId(final Long board_id) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = "SELECT id, name,`order`, kind FROM BOARD_COLUMNS WHERE board_id = ? ORDER BY `order`";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, board_id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while(resultSet.next()){
                var entity = new BoardColumnEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setOrder(resultSet.getInt("order"));
                entity.setKind(findByName(resultSet.getString("kind")));
                entities.add(entity);
            }
            return entities;
        }
    }
    public List<BoardColumnDTO> findByBoardIdWithDetails(final Long board_id) throws SQLException {
        List<BoardColumnDTO> dtos = new ArrayList<>();
        var sql =
        """
        SELECT bc.id, 
               bc.name, 
               bc.kind,
               (SELECT COUNT(c.id) FROM CARDS c where c.board_column_id = bc.id) cards_amount
        FROM BOARD_COLUMNS bc
        WHERE board_id = ? 
        ORDER BY `order`;
        """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, board_id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while(resultSet.next()){
                var dto = new BoardColumnDTO(
                        resultSet.getLong("bc.id"),
                        resultSet.getString("bc.name"),
                        findByName(resultSet.getString("bc.kind")),
                        resultSet.getInt("cards_amount")
                );
                dtos.add(dto);
            }
            return dtos;
        }
    }
    public Optional<BoardColumnEntity> findById(final Long board_id) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql =
        """
        SELECT bc.name, 
               bc.kind,
               c.id,
               c.title,
               c.description
        FROM BOARD_COLUMNS bc
        LEFT JOIN CARDS c
            ON c.board_column_id = bc.id
        WHERE bc.id = ?;
        """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, board_id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if(resultSet.next()){
                var entity = new BoardColumnEntity();
                entity.setName(resultSet.getString("bc.name"));
                entity.setKind(findByName(resultSet.getString("bc.kind")));
                do{
                    if(isNull(resultSet.getString("c.title"))){
                        break;
                    }
                    var card = new CardEntity();
                    card.setId(resultSet.getLong("c.id"));
                    card.setTitle(resultSet.getString("c.title"));
                    card.setDescription(resultSet.getString("c.description"));
                    entity.getCards().add(card);
                }while (resultSet.next());
                return Optional.of(entity);
            }
            return Optional.empty();
        }
    }

}
