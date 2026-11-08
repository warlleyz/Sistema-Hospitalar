package br.edu.pucminas.hospital;

import br.edu.pucminas.hospital.compartilhado.contratos.Cadastros;
import br.edu.pucminas.hospital.compartilhado.erros.DependenciaIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class FundacaoTest {
    @Autowired Cadastros cadastros;
    @Test void iniciaSemFingirQueCadastrosExistem() {
        assertThatThrownBy(() -> cadastros.paciente(1L)).isInstanceOf(DependenciaIndisponivelException.class);
        assertThatThrownBy(() -> cadastros.profissional(1L)).isInstanceOf(DependenciaIndisponivelException.class);
    }
}
