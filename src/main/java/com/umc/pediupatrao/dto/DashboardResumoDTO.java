package com.umc.pediupatrao.dto;

import java.math.BigDecimal;

public class DashboardResumoDTO {
    private BigDecimal faturamentoTotal;
    private long quantidadePedidos;
    private long quantidadeFinalizados;
    private long quantidadeCancelados;

    public BigDecimal getFaturamentoTotal() {
        return faturamentoTotal;
    }

    public void setFaturamentoTotal(BigDecimal faturamentoTotal) {
        this.faturamentoTotal = faturamentoTotal;
    }

    public long getQuantidadeCancelados() {
        return quantidadeCancelados;
    }

    public void setQuantidadeCancelados(long quantidadeCancelados) {
        this.quantidadeCancelados = quantidadeCancelados;
    }

    public long getQuantidadeFinalizados() {
        return quantidadeFinalizados;
    }

    public void setQuantidadeFinalizados(long quantidadeFinalizados) {
        this.quantidadeFinalizados = quantidadeFinalizados;
    }

    public long getQuantidadePedidos() {
        return quantidadePedidos;
    }

    public void setQuantidadePedidos(long quantidadePedidos) {
        this.quantidadePedidos = quantidadePedidos;
    }
}