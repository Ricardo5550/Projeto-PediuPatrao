package com.umc.pediupatrao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VendaPorDiaDTO {
    private LocalDate data;
    private BigDecimal faturamento;
    private long quantidadeVendas;

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getFaturamento() {
        return faturamento;
    }

    public void setFaturamento(BigDecimal faturamento) {
        this.faturamento = faturamento;
    }

    public long getQuantidadeVendas() {
        return quantidadeVendas;
    }

    public void setQuantidadeVendas(long quantidadeVendas) {
        this.quantidadeVendas = quantidadeVendas;
    }
}