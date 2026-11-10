# Dashboard Administrativo — Pediu Patrão

## 1. Introdução

O Dashboard Administrativo foi desenvolvido como uma funcionalidade do sistema PediuPatrão, com o objetivo de facilitar o acompanhamento das vendas e dos pedidos realizados pelo estabelecimento.

A solução apresenta indicadores financeiros, gráficos e informações gerenciais, permitindo uma visualização mais organizada dos dados e auxiliando na tomada de decisões.

## 2. Requisitos Funcionais

Os seguintes requisitos foram considerados no desenvolvimento:

- **RF01 — Indicadores gerais de vendas:** apresentar faturamento total, quantidade de pedidos, pedidos finalizados e cancelados.
- **RF02 — Análise de vendas:** apresentar um gráfico com o faturamento diário.
- **RF03 — Situação dos pedidos:** apresentar um gráfico com a distribuição dos pedidos por situação.
- **RF04 — Produtos mais vendidos:** apresentar um ranking dos produtos vendidos, considerando suas quantidades.
- **RF05 — Filtros de consulta:** permitir consultas por data inicial e data final.
- **RF06 — Integração com API:** utilizar endpoints REST para consultar os dados armazenados.
- **RF07 — Atualização e tratamento de dados:** permitir a atualização das informações e tratar erros durante as consultas.

Os requisitos foram implementados em sua versão básica. O RF03 apresenta os status finalizados, cancelados e outros, sendo necessário detalhar futuramente as demais situações.

## 3. Requisitos Não Funcionais

- **Segurança:** restringir o acesso ao dashboard a usuários com perfil ADMIN.
- **Usabilidade:** apresentar uma interface simples, organizada e integrada ao PediuPatrão.
- **Manutenibilidade:** separar as responsabilidades entre controlador, serviço, repositórios e objetos DTO.
- **Confiabilidade:** utilizar os registros existentes no banco de dados para calcular os indicadores.
- **Compatibilidade:** permitir a utilização do dashboard em navegadores modernos.

## 4. Regras de Negócio

O cálculo do faturamento considera somente pedidos com status FINALIZADO.

O valor utilizado corresponde ao valor total do pedido, incluindo os descontos aplicados.

Os pedidos cancelados não são considerados no faturamento.

O ranking de produtos considera as quantidades dos itens pertencentes aos pedidos finalizados.

Os filtros utilizam o campo `dataHoraEntrada` para selecionar os pedidos dentro do período informado, incluindo as datas inicial e final.

Quando não existem vendas no período, o faturamento deve ser apresentado como R$ 0,00.

## 5. Documentação da API

A comunicação entre o dashboard e o backend é realizada por meio de três endpoints REST.

### 5.1. Indicadores Gerais

**Método:** GET

**Endpoint:** `/api/dashboard/resumo`

Retorna o faturamento total e as quantidades de pedidos registrados, finalizados e cancelados.

Exemplo de resposta:

```json
{
  "faturamentoTotal": 90.00,
  "quantidadePedidos": 4,
  "quantidadeFinalizados": 1,
  "quantidadeCancelados": 1
}
```

### 5.2. Vendas por Dia

**Método:** GET

**Endpoint:** `/api/dashboard/vendas`

Retorna o faturamento e a quantidade de vendas agrupados por dia.

Exemplo de resposta:

```json
[
  {
    "data": "2026-10-14",
    "faturamento": 90.00,
    "quantidadeVendas": 1
  }
]
```

### 5.3. Produtos Mais Vendidos

**Método:** GET

**Endpoint:** `/api/dashboard/produtos`

Retorna os produtos vendidos, organizados pela quantidade total vendida.

Cada registro apresenta o identificador, o nome e a quantidade vendida.

### 5.4. Parâmetros e Tratamento de Erros

Os três endpoints aceitam os seguintes parâmetros opcionais:

- `dataInicio`: data inicial da consulta.
- `dataFim`: data final da consulta.

As datas devem seguir o formato `AAAA-MM-DD`.

Quando a data inicial é posterior à final, a API retorna HTTP 400 com uma mensagem informando o problema.

O acesso aos endpoints exige autenticação e autorização de administrador.

## 6. Arquitetura e Tecnologias

O projeto utiliza Java, Spring Boot, Spring Security e MongoDB no backend.

A interface utiliza HTML, CSS, JavaScript, Thymeleaf, Bootstrap e Chart.js.

A implementação foi organizada principalmente nas seguintes classes:

- **DashboardController:** recebe as requisições HTTP e retorna os resultados da API.
- **DashboardService:** realiza os cálculos, agrupamentos e filtros.
- **PedidoRepository e ProdutoRepository:** permitem consultar os registros do banco de dados.
- **DashboardResumoDTO:** transporta os indicadores gerais.
- **VendaPorDiaDTO:** representa os resultados diários.
- **ProdutoMaisVendidoDTO:** representa os produtos do ranking.

O arquivo `dashboard.html` apresenta os componentes visuais, enquanto `dashboard.js` realiza as consultas e atualiza as informações da página.

## 7. Funcionamento e Evidências

O dashboard foi integrado à interface principal do PediuPatrão e pode ser acessado pelo menu lateral.

Durante o desenvolvimento, foram observados os seguintes resultados:

- Exibição dos indicadores gerais com dados do MongoDB.
- Apresentação do gráfico de faturamento diário.
- Apresentação do gráfico de situação dos pedidos.
- Exibição do ranking de produtos mais vendidos.
- Inclusão dos campos de período e dos botões Aplicar Filtros, Limpar e Atualizar Dados.

Essas funcionalidades foram observadas visualmente durante o desenvolvimento. A validação completa dos filtros, das permissões e dos cenários de erro ainda deve ser registrada como evidência.

## 8. Limitações e Melhorias Futuras

A implementação atual possui algumas limitações:

- O gráfico de situação dos pedidos ainda agrupa diferentes status na categoria Outros.
- A análise temporal utiliza a data de entrada do pedido, e não sua data efetiva de finalização.
- Pedidos antigos sem itens registrados podem não aparecer no ranking.
- A segurança, especialmente a proteção CSRF, e o tratamento de erros podem ser aperfeiçoados.
- O processamento dos indicadores poderá ser otimizado com consultas agregadas no MongoDB.

## 9. Conclusão

O desenvolvimento do Dashboard Administrativo permitiu ampliar as funcionalidades do PediuPatrão, transformando os dados dos pedidos em informações úteis para o acompanhamento das vendas.

A solução integra consultas à API REST, indicadores financeiros, gráficos e rankings em uma interface administrativa.

Além de atender aos principais objetivos da atividade, o projeto demonstra a aplicação de conceitos de Análise e Projeto de Software, como separação de responsabilidades, definição de requisitos, regras de negócio e integração entre sistemas.