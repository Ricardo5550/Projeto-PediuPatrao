package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.enums.StatusPedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getId() != null) {
            throw new IllegalArgumentException("Um pedido novo não pode possuir ID.");
        }
        if (pedido.getClienteId() == null || pedido.getClienteId().isEmpty()) {
            throw new IllegalArgumentException("O cliente é obrigatório.");
        }
        if (!clienteRepository.existsById(pedido.getClienteId())) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : pedido.getItens()) {
            if (item == null) {
                throw new IllegalArgumentException("O pedido contém um item inválido.");
            }
            if (item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("A quantidade do item deve ser maior que zero.");
            }
            if (item.getProdutoId() == null || item.getProdutoId().isBlank()) {
                throw new IllegalArgumentException("O produto do item é obrigatório.");
            }
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
            if (!Boolean.TRUE.equals(produto.getAtivo())) {
                throw new IllegalArgumentException("O produto está inativo ou indisponível.");
            }
            if (produto.getPreco() == null || !Double.isFinite(produto.getPreco()) || produto.getPreco() <= 0) {
                throw new IllegalArgumentException("O produto possui um preço inválido.");
            }
            BigDecimal precoUnitario = BigDecimal.valueOf(produto.getPreco());

            BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(item.getQuantidade()));

            item.setPrecoUnitario(precoUnitario);
            item.setSubtotal(subtotal);

            total = total.add(subtotal);
        }
        pedido.setValorSubtotal(total);
        pedido.setPercentualDesconto(BigDecimal.ZERO);
        pedido.setValorTotal(total);
        pedido.setDataHoraEntrada(LocalDateTime.now());
        pedido.setStatus("RECEBIDO");

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated() || autenticacao instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("É necessário estar autenticado para criar um pedido.");
        }

        pedido.setResponsavelEntrada(autenticacao.getName());

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido atualizarStatus(String id, String status) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        StatusPedido statusAtual = StatusPedido.valueOf(pedido.getStatus());
        StatusPedido novoStatus = StatusPedido.valueOf(status);

        if (statusAtual == StatusPedido.RECEBIDO) {
            if (novoStatus != StatusPedido.EM_PREPARACAO) {
                throw new IllegalArgumentException("Transição de status inválida.");
            }
        }

        if (statusAtual == StatusPedido.EM_PREPARACAO) {
            if (novoStatus != StatusPedido.PRONTO) {
                throw new IllegalArgumentException("Transição de status inválida.");
            }
        }

        if (statusAtual == StatusPedido.PRONTO) {
            if (novoStatus != StatusPedido.SAIU_PARA_ENTREGA && novoStatus != StatusPedido.RETIRADO) {
                throw new IllegalArgumentException("Transição de status inválida.");
            }
        }

        if (statusAtual == StatusPedido.SAIU_PARA_ENTREGA || statusAtual == StatusPedido.RETIRADO) {
            if (novoStatus != StatusPedido.FINALIZADO) {
                throw new IllegalArgumentException("Transição de status inválida.");
            }
        }

        if (statusAtual == StatusPedido.FINALIZADO || statusAtual == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Não é permitido alterar um pedido encerrado.");
        }

        if (novoStatus == StatusPedido.SAIU_PARA_ENTREGA || novoStatus == StatusPedido.RETIRADO) {
            Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
            if (autenticacao == null || !autenticacao.isAuthenticated() || autenticacao instanceof AnonymousAuthenticationToken) {
                throw new IllegalStateException("É necessário estar autenticado para registrar a saída.");
            }
            pedido.setDataHoraSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(autenticacao.getName());
        }

        pedido.setStatus(status);

        return pedidoRepository.save(pedido);
    }

    public Pedido aplicarDesconto(String id, BigDecimal percentual) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated() || autenticacao instanceof AnonymousAuthenticationToken || autenticacao.getAuthorities().stream()
                        .noneMatch(autoridade -> autoridade.getAuthority().equals("ROLE_GERENTE"))) {
            throw new org.springframework.security.access.AccessDeniedException("Somente o gerente pode aplicar descontos.");
        }

        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(new BigDecimal("20")) > 0) {
            throw new IllegalArgumentException("O desconto deve estar entre 0% e 20%.");
        }

        StatusPedido statusAtual = StatusPedido.valueOf(pedido.getStatus());

        if (statusAtual == StatusPedido.SAIU_PARA_ENTREGA || statusAtual == StatusPedido.RETIRADO || statusAtual == StatusPedido.FINALIZADO || statusAtual == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Não é permitido aplicar desconto após a saída ou encerramento do pedido.");
        }

        BigDecimal valorDesconto = pedido.getValorSubtotal()
                .multiply(percentual)
                .divide(new BigDecimal("100"));

        BigDecimal novoTotal = pedido.getValorSubtotal()
                .subtract(valorDesconto);

        pedido.setPercentualDesconto(percentual);
        pedido.setValorTotal(novoTotal);

        return pedidoRepository.save(pedido);
    }

    public Pedido cancelarPedido(String id, String justificativa) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated() || autenticacao instanceof AnonymousAuthenticationToken || autenticacao.getAuthorities().stream()
                        .noneMatch(autoridade -> autoridade.getAuthority().equals("ROLE_GERENTE"))) {
            throw new org.springframework.security.access.AccessDeniedException("Somente o gerente pode cancelar pedidos.");
        }

        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("É obrigatório informar uma justificativa para o cancelamento.");
        }

        StatusPedido statusAtual = StatusPedido.valueOf(pedido.getStatus());

        if (statusAtual == StatusPedido.SAIU_PARA_ENTREGA ||
                statusAtual == StatusPedido.RETIRADO || statusAtual == StatusPedido.FINALIZADO || statusAtual == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Não é permitido cancelar um pedido que já saiu ou foi encerrado.");
        }

        pedido.setStatus(StatusPedido.CANCELADO.name());

        return pedidoRepository.save(pedido);
    }
}