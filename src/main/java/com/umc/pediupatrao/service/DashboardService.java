package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardResumoDTO;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import com.umc.pediupatrao.entity.Pedido;
import java.util.List;
import java.math.BigDecimal;

@Service
public class DashboardService {
    private final PedidoRepository pedidoRepository;

    public DashboardService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public DashboardResumoDTO obterResumo() {
        DashboardResumoDTO resumo = new DashboardResumoDTO();

        List<Pedido> pedidos = pedidoRepository.findAll();

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
}