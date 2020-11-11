import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.HashMap;
import java.util.Map;

// a simple JavaFX calculator.
public class Main extends Application {
    private static final String[][] template = {
            { "7", "8", "9", "/" },
            { "4", "5", "6", "*" },
            { "1", "2", "3", "-" },
            { "0", "c", "=", "+" }
    };

    private final Map<String, Button> accelerators = new HashMap<>();

    private DoubleProperty stackValue = new SimpleDoubleProperty();
    private DoubleProperty value = new SimpleDoubleProperty();

    private enum Op { NOOP, Adicionar, Subtrair, Multiplicar, Dividir }

    private Op curOp   = Op.NOOP;
    private Op stackOp = Op.NOOP;

    public static void main(String[] args) { launch(args); }

    @Override public void start(Stage stage) {
        final TextField input  = inputValor();
        final TilePane botoes = criarBotoes();

        stage.setTitle("Calculadora Java");
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.setScene(new Scene(criarLayout(input, botoes)));
        stage.show();
    }

    private VBox criarLayout(TextField input, TilePane botoes) {
        final VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: LIGHT_GRAY; -fx-padding: 20; -fx-font-size: 20;");
        layout.getChildren().setAll(input, botoes);
        handleAccelerators(layout);
        input.prefWidthProperty().bind(botoes.widthProperty());
        return layout;
    }

    private void handleAccelerators(VBox layout) {
        layout.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                Button activated = accelerators.get(keyEvent.getText());
                if (activated != null) {
                    activated.fire();
                }
            }
        });
    }

    private TextField inputValor() {
        final TextField input = new TextField();
        input.setStyle("-fx-background-color: gray;");
        input.setAlignment(Pos.CENTER_RIGHT);
        input.setEditable(false);
        input.textProperty().bind(Bindings.format("%.0f", value));
        return input;
    }

    private TilePane criarBotoes() {
        TilePane botoes = new TilePane();
        botoes.setVgap(7);
        botoes.setHgap(7);
        botoes.setPrefColumns(template[0].length);
        for (String[] r: template) {
            for (String s: r) {
                botoes.getChildren().add(criarBotao(s));
            }
        }
        return botoes;
    }

    private Button criarBotao(final String s) {
        Button botao = botoesPadrao(s);

        if (s.matches("[0-9]")) {
            botaoNumerico(s, botao);
        } else {
            final ObjectProperty<Op> triggerOp = determinarOperador(s);
            if (triggerOp.get() != Op.NOOP) {
                botoesOperacoes(botao, triggerOp);
            } else if ("c".equals(s)) {
                botaoLimpar(botao);
            } else if ("=".equals(s)) {
                botaoIgual(botao);
            }
        }

        return botao;
    }

    private ObjectProperty<Op> determinarOperador(String s) {
        final ObjectProperty<Op> operador = new SimpleObjectProperty<>(Op.NOOP);
        switch (s) {
            case "+": operador.set(Op.Adicionar);      break;
            case "-": operador.set(Op.Subtrair); break;
            case "*": operador.set(Op.Multiplicar); break;
            case "/": operador.set(Op.Dividir);   break;
        }
        return operador;
    }

    private void botoesOperacoes(Button botao, final ObjectProperty<Op> triggerOp) {
        botao.setStyle("-fx-base: lightgray;");
        botao.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                curOp = triggerOp.get();
            }
        });
    }

    private Button botoesPadrao(String s) {
        Button botao = new Button(s);
        botao.setStyle("-fx-base: beige;");
        accelerators.put(s, botao);
        botao.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return botao;
    }

    private void botaoNumerico(final String s, Button botao) {
        botao.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (curOp == Op.NOOP) {
                    value.set(value.get() * 10 + Integer.parseInt(s));
                } else {
                    stackValue.set(value.get());
                    value.set(Integer.parseInt(s));
                    stackOp = curOp;
                    curOp = Op.NOOP;
                }
            }
        });
    }

    private void botaoLimpar(Button botao) {
        botao.setStyle("-fx-base: red;");
        botao.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                value.set(0);
            }
        });
    }

    private void botaoIgual(Button botao) {
        botao.setStyle("-fx-base: ghostwhite;");
        botao.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (stackOp) {
                    case Adicionar:
                        try{
                            value.set(stackValue.get() + value.get()); break;
                        }catch(Throwable error){
                            System.out.println("Erro: " + error);
                        }
                    case Subtrair:
                        try{
                            value.set(stackValue.get() - value.get()); break;
                        }catch(Throwable error){
                            System.out.println("Erro: " + error);
                        }
                    case Multiplicar:
                        try{
                            value.set(stackValue.get() * value.get()); break;
                        }catch(Throwable error){
                            System.out.println("Erro: " + error);
                        }
                    case Dividir:
                        try{
                            value.set(stackValue.get() / value.get()); break;
                        }catch(Throwable error){
                            System.out.println("Erro: " + error);
                        }
                }
            }
        });
    }
}