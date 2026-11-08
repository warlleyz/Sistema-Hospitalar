package br.edu.pucminas.hospital.compartilhado.erros;

public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) { super(mensagem); }
}
