package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardResumoDTO;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import com.umc.pediupatrao.entity.Pedido;
import java.util.List;
import java.math.BigDecimal;
import com.umc.pediupatrao.dto.VendaPorDiaDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import com.umc.pediupatrao.dto.ProdutoMaisVendidoDTO;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import java.util.HashMap;
import java.util.Comparator;

@Service
public class DashboardService {
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public DashboardService(
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    public DashboardResumoDTO obterResumo(LocalDate dataInicio, LocalDate dataFim) {
        DashboardResumoDTO resumo = new DashboardResumoDTO();

        List<Pedido> pedidos = filtrarPedidos(dataInicio, dataFim);

        long quantidadeFinalizados = 0;
        long quantidadeCancelados = 0;

        BigDecimal faturamentoTotal = BigDecimal.ZERO;

        for (Pedido pedido : pedidos) {
            if ("FINALIZADO".equals(pedido.getStatus())) {
                quantidadeFinalizados++;

                if (pedido.getValorTotal() == null) {
                    throw new IllegalStateException("Pedido finalizado sem valor total.");
                }

                faturamentoTotal = faturamentoTotal.add(pedido.getValorTotal());
            }
            else if ("CANCELADO".equals(pedido.getStatus())) {
                quantidadeCancelados++;
            }
        }

        resumo.setQuantidadeFinalizados(quantidadeFinalizados);
        resumo.setQuantidadeCancelados(quantidadeCancelados);
        resumo.setQuantidadePedidos(pedidos.size());
        resumo.setFaturamentoTotal(faturamentoTotal);

        return resumo;
    }

    public List<VendaPorDiaDTO> obterVendasPorDia(LocalDate dataInicio, LocalDate dataFim) {

        List<Pedido> pedidos = filtrarPedidos(dataInicio, dataFim);

        Map<LocalDate, VendaPorDiaDTO> vendasPorDia = new TreeMap<>();

        for (Pedido pedido : pedidos) {

            if (!"FINALIZADO".equals(pedido.getStatus())) {
                continue;
            }

            if (pedido.getDataHoraEntrada() == null) {
                throw new IllegalStateException("Pedido finalizado sem data de entrada.");
            }

            if (pedido.getValorTotal() == null) {
                throw new IllegalStateException("Pedido finalizado sem valor total.");
            }

            LocalDate data = pedido.getDataHoraEntrada().toLocalDate();

            VendaPorDiaDTO venda = vendasPorDia.computeIfAbsent(data, chave -> {
                VendaPorDiaDTO novaVenda = new VendaPorDiaDTO();

                novaVenda.setData(chave);
                novaVenda.setFaturamento(BigDecimal.ZERO);
                novaVenda.setQuantidadeVendas(0);

                return novaVenda;
            });

            venda.setFaturamento(
                    venda.getFaturamento().add(pedido.getValorTotal())
            );

            venda.setQuantidadeVendas(
                    venda.getQuantidadeVendas() + 1
            );
        }

        return new ArrayList<>(vendasPorDia.values());
    }

    public List<ProdutoMaisVendidoDTO> obterProdutosMaisVendidos(LocalDate dataInicio, LocalDate dataFim) {

        List<Pedido> pedidos = filtrarPedidos(dataInicio, dataFim);

        Map<String, Long> quantidadesPorProduto = new HashMap<>();

        for (Pedido pedido : pedidos) {

            if (!"FINALIZADO".equals(pedido.getStatus())) {
                continue;
            }

            if (pedido.getItens() == null) {
                continue;
            }

            for (ItemPedido item : pedido.getItens()) {

                if (item == null || item.getProdutoId() == null
                        || item.getProdutoId().isBlank()
                        || item.getQuantidade() <= 0) {
                    continue;
                }

                quantidadesPorProduto.merge(
                        item.getProdutoId(),
                        (long) item.getQuantidade(),
                        Long::sum
                );
            }
        }

        List<ProdutoMaisVendidoDTO> ranking = new ArrayList<>();

        for (Map.Entry<String, Long> entrada : quantidadesPorProduto.entrySet()) {

            String produtoId = entrada.getKey();

            String nomeProduto = produtoRepository.findById(produtoId)
                    .map(Produto::getNome)
                    .filter(nome -> !nome.isBlank())
                    .orElse("Produto não encontrado (" + produtoId + ")");

            ProdutoMaisVendidoDTO produtoDTO = new ProdutoMaisVendidoDTO();

            produtoDTO.setProdutoId(produtoId);
            produtoDTO.setNomeProduto(nomeProduto);
            produtoDTO.setQuantidadeVendida(entrada.getValue());

            ranking.add(produtoDTO);
        }

        ranking.sort(
                Comparator.comparingLong(
                                ProdutoMaisVendidoDTO::getQuantidadeVendida
                        ).reversed()
                        .thenComparing(ProdutoMaisVendidoDTO::getProdutoId)
        );

        return ranking;
    }

    private List<Pedido> filtrarPedidos(LocalDate dataInicio, LocalDate dataFim) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }

        List<Pedido> pedidos = pedidoRepository.findAll();

        if (dataInicio == null && dataFim == null) {
            return pedidos;
        }

        List<Pedido> pedidosFiltrados = new ArrayList<>();

        for (Pedido pedido : pedidos) {

            if (pedido.getDataHoraEntrada() == null) {
                continue;
            }

            LocalDate dataPedido =
                    pedido.getDataHoraEntrada().toLocalDate();

            if (dataInicio != null && dataPedido.isBefore(dataInicio)) {
                continue;
            }

            if (dataFim != null && dataPedido.isAfter(dataFim)) {
                continue;
            }

            pedidosFiltrados.add(pedido);
        }

        return pedidosFiltrados;
    }
}