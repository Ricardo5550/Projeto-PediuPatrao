document.addEventListener("DOMContentLoaded", async function () {
    try {
        const resposta = await fetch("/api/dashboard/resumo");

        if (!resposta.ok) {
            throw new Error("Erro HTTP: " + resposta.status);
        }

        const dados = await resposta.json();

        const moeda = new Intl.NumberFormat("pt-BR", {
            style: "currency",
            currency: "BRL"
        });

        document.getElementById("faturamentoTotal").textContent = moeda.format(dados.faturamentoTotal ?? 0);

        document.getElementById("quantidadePedidos").textContent = dados.quantidadePedidos ?? 0;

        document.getElementById("quantidadeFinalizados").textContent = dados.quantidadeFinalizados ?? 0;

        document.getElementById("quantidadeCancelados").textContent = dados.quantidadeCancelados ?? 0;

    } catch (erro) {
        console.error("Erro ao carregar dashboard:", erro);

        const mensagem = document.getElementById("erroDashboard");
        mensagem.textContent = "Não foi possível carregar os indicadores.";
        mensagem.classList.remove("d-none");
    }
});