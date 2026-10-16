
# PediuPatrão

Sistema de gerenciamento de pedidos desenvolvido como projeto acadêmico.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Security
- Spring Data MongoDB
- MongoDB Atlas
- Thymeleaf
- Maven

## Funcionalidades

- Autenticação de usuários e controle de perfis
- Cadastro e consulta de clientes
- Cadastro de produtos
- Registro de pedidos com cálculo automático dos valores
- Controle das etapas de preparação, entrega e retirada
- Aplicação de descontos de até 20% pelo gerente
- Cancelamento de pedidos mediante justificativa

## Execução

1. Ter o Java 21 instalado.
2. Configurar a variável de ambiente MONGODB_URI com a conexão do MongoDB.
3. Executar a aplicação com o Maven Wrapper.

No Windows:

    .\mvnw.cmd spring-boot:run

A aplicação estará disponível em:

    http://localhost:8080

## Observações

O projeto utiliza MongoDB Atlas. As credenciais de acesso ao banco de dados não estão incluídas no repositório.
