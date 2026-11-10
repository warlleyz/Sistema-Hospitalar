package br.edu.pucminas.hospital.quartos.controller;

import br.edu.pucminas.hospital.quartos.dto.*;
import br.edu.pucminas.hospital.quartos.dto.PedidosQuarto.*;
import br.edu.pucminas.hospital.quartos.service.QuartoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quartos")
@Validated
public class QuartoController {
    private final QuartoService service;
    public QuartoController(QuartoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<QuartoResposta> cadastrar(@Valid @RequestBody NovoQuarto pedido) {
        var criado = service.cadastrar(pedido);
        return ResponseEntity.created(URI.create("/quartos/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<QuartoResposta> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public QuartoResposta consultar(@PathVariable @Positive Long id) { return service.consultar(id); }

    @PutMapping("/{id}")
    public QuartoResposta atualizar(@PathVariable @Positive Long id, @Valid @RequestBody AtualizarQuarto pedido) {
        return service.atualizar(id, pedido);
    }
}
