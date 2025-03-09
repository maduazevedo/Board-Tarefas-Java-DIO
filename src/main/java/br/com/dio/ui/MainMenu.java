package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void execute() throws SQLException {
        System.out.println("Bem vindo(a) ao gerenciador de boards!");
        System.out.println("Escolha a opção desejada: ");

        var option = -1;

        while(true){
            System.out.println("1. Criar novo board");
            System.out.println("2. Selecionar board existente");
            System.out.println("3. Excluir board");
            System.out.println("4. Sair");
            option = scanner.nextInt();

            switch (option){
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> System.out.println("Opção inválida. Informe uma opção do menu");
            }
        }
    }

    private void createBoard() throws SQLException{
        var entity = new BoardEntity();

        System.out.println("Informe o nome do seu board: ");
        entity.setName(scanner.next());

        System.out.println("Seu board terá colunas além das padrões? Se sim, informe quantas. Caso não, digite 0");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da coluna INICIAL do board");
        var initialColumnName = scanner.next();
        var initialColumn = createColumn(initialColumnName, 0, INITIAL);
        columns.add(initialColumn);

        for (int i = 0; i < additionalColumns; i++){
            System.out.println("Informe o nome da coluna de tarefa PENDENTE");
            var pendingColumnName = scanner.next();
            var pendingColumn = createColumn(pendingColumnName, i+1, PENDING);
            columns.add(pendingColumn);
        }

        System.out.println("Informe o nome da coluna FINAL do board");
        var finalColumnName = scanner.next();
        var finalColumn = createColumn(finalColumnName, additionalColumns+1, FINAL);
        columns.add(finalColumn);

        System.out.println("Informe o nome da coluna de CANCELAMENTO do board");
        var cancelColumnName = scanner.next();
        var cancelColumn = createColumn(cancelColumnName, additionalColumns+2, CANCEL);
        columns.add(cancelColumn);

        entity.setBoardColumns(columns);
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            service.insert(entity);
        }
    }

    private void selectBoard() throws SQLException{
        System.out.println("Informe o id do board que deseja selecionar: ");
        var id = scanner.nextLong();

        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                    b-> new BoardMenu(b).execute(), () -> System.out.printf("Não foi encontrado um board com id %s\n", id));
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("Informe o id do board que será excluído: ");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            if(service.delete(id)){
                System.out.printf("O board %s foi excluido\n", id);
            }else{
                System.out.printf("Não foi encontrado um board com id %s\n", id);
            }
        }
    }

    private BoardColumnEntity createColumn (final String name, final int order, BoardColumnKindEnum kind ){
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setOrder(order);
        boardColumn.setKind(kind);
        return boardColumn;
    }

}
