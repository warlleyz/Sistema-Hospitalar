package br.edu.pucminas.hospital.compartilhado.erros;

public class DependenciaIndisponivelException extends RuntimeException {
    public DependenciaIndisponivelException(String mensagem) { super(mensagem); }
}
