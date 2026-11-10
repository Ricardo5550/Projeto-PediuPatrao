document.addEventListener("DOMContentLoaded", function () {

    const campoInicio = document.getElementById("dataInicio");
    const campoFim = document.getElementById("dataFim");
    const botaoAplicar = document.getElementById("aplicarFiltros");
    const botaoLimpar = document.getElementById("limparFiltros");
    const botaoAtualizar = document.getElementById("atualizarDashboard");
    const mensagemErro = document.getElementById("erroDashboard");

    let graficoVendas = null;
    let graficoStatus = null;

    const moeda = new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    });

    async function consultarAPI(endpoint, parametros) {
        const consulta = parametros.toString();
        const url = endpoint + (consulta ? "?" + consulta : "");

        const resposta = await fetch(url, {
            cache: "no-store"
        });

        if (!resposta.ok) {
            throw new Error("Erro HTTP: " + resposta.status);
        }

        return resposta.json();
    }

    async function carregarDashboard() {

        const dataInicio = campoInicio.value;
        const dataFim = campoFim.value;

        if (dataInicio && dataFim && dataInicio > dataFim) {
            mensagemErro.textContent = "A data inicial não pode ser posterior à data final.";
            mensagemErro.classList.remove("d-none");
            return;
        }

        mensagemErro.classList.add("d-none");

        const parametros = new URLSearchParams();

        if (dataInicio) {
            parametros.set("dataInicio", dataInicio);
        }

        if (dataFim) {
            parametros.set("dataFim", dataFim);
        }

        try {
            const [resumo, vendas, produtos] = await Promise.all([
                consultarAPI("/api/dashboard/resumo", parametros),
                consultarAPI("/api/dashboard/vendas", parametros),
                consultarAPI("/api/dashboard/produtos", parametros)
            ]);

            document.getElementById("faturamentoTotal").textContent = moeda.format(resumo.faturamentoTotal ?? 0);

            document.getElementById("quantidadePedidos").textContent = resumo.quantidadePedidos ?? 0;

            document.getElementById("quantidadeFinalizados").textContent = resumo.quantidadeFinalizados ?? 0;

            document.getElementById("quantidadeCancelados").textContent = resumo.quantidadeCancelados ?? 0;

            const canvasVendas = document.getElementById("graficoVendas");

            if (graficoVendas) {
                graficoVendas.destroy();
            }

            graficoVendas = new Chart(canvasVendas.getContext("2d"), {
                type: "bar",
                data: {
                    labels: vendas.map(venda =>
                        venda.data.split("-").reverse().join("/")
                    ),
                    datasets: [{
                        label: "Faturamento (R$)",
                        data: vendas.map(venda =>
                            Number(venda.faturamento)
                        ),
                        backgroundColor: "rgba(13, 110, 253, 0.6)",
                        borderColor: "rgba(13, 110, 253, 1)",
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        yAxes: [{
                            ticks: {
                                beginAtZero: true
                            }
                        }]
                    }
                }
            });

            const finalizados = Number(resumo.quantidadeFinalizados ?? 0);
            const cancelados = Number(resumo.quantidadeCancelados ?? 0);
            const total = Number(resumo.quantidadePedidos ?? 0);
            const outros = Math.max(0, total - finalizados - cancelados);

            const canvasStatus = document.getElementById("graficoStatus");

            if (graficoStatus) {
                graficoStatus.destroy();
            }

            graficoStatus = new Chart(canvasStatus.getContext("2d"), {
                type: "doughnut",
                data: {
                    labels: ["Finalizados", "Cancelados", "Outros"],
                    datasets: [{
                        data: [finalizados, cancelados, outros],
                        backgroundColor: ["#28a745", "#dc3545", "#ffc107"]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    legend: {
                        position: "bottom"
                    }
                }
            });

            const tabela = document.getElementById("rankingProdutos");

            tabela.innerHTML = "";

            if (produtos.length === 0) {
                const linha = tabela.insertRow();
                const celula = linha.insertCell();

                celula.colSpan = 2;
                celula.textContent = "Nenhum produto vendido no período.";
            } else {
                produtos.forEach(produto => {
                    const linha = tabela.insertRow();

                    linha.insertCell().textContent = produto.nomeProduto;

                    linha.insertCell().textContent = produto.quantidadeVendida;
                });
            }

        } catch (erro) {
            console.error("Erro ao carregar dashboard:", erro);

            mensagemErro.textContent = "Não foi possível carregar os dados do dashboard.";

            mensagemErro.classList.remove("d-none");
        }
    }

    botaoAplicar.addEventListener("click", carregarDashboard);

    botaoAtualizar.addEventListener("click", carregarDashboard);

    botaoLimpar.addEventListener("click", function () {
        campoInicio.value = "";
        campoFim.value = "";

        carregarDashboard();
    });

    carregarDashboard();
});