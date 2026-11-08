package br.edu.pucminas.hospital.compartilhado.erros;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class TratadorErros {
    public record CampoErro(String campo, String mensagem) { }
    public record ErroApi(String codigo, String mensagem, List<CampoErro> campos) { }

    private ResponseEntity<ErroApi> resposta(int status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(new ErroApi(codigo, mensagem, List.of()));
    }
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErroApi> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(404, "NAO_ENCONTRADO", e.getMessage());
    }
    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<ErroApi> conflito(RegraNegocioException e) {
        return resposta(409, "REGRA_NEGOCIO", e.getMessage());
    }
    @ExceptionHandler(DependenciaIndisponivelException.class)
    ResponseEntity<ErroApi> indisponivel(DependenciaIndisponivelException e) {
        return resposta(503, "MODULO_INDISPONIVEL", e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroApi> validacao(MethodArgumentNotValidException e) {
        var campos = e.getBindingResult().getFieldErrors().stream()
            .map(f -> new CampoErro(f.getField(), f.getDefaultMessage())).toList();
        return ResponseEntity.badRequest().body(new ErroApi("ENTRADA_INVALIDA", "Revise os campos informados.", campos));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ErroApi> formato(Exception e) {
        return resposta(400, "ENTRADA_INVALIDA", "JSON, identificador ou valor de enum invalido.");
    }
    @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
    ResponseEntity<ErroApi> integridade(Exception e) {
        return resposta(409, "CONFLITO_DADOS", "A operacao conflita com os dados atuais. Consulte novamente.");
    }
}
