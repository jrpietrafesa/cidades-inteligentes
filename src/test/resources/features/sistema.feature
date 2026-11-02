# language: pt
@sistema
Funcionalidade: Identificação e saúde da aplicação
  Como operador do ambiente
  Quero verificar a versão e a saúde da API
  Para confirmar que o serviço está disponível

  @positivo
  Cenário: Consultar a identificação da aplicação
    Quando consulto a identificação da aplicação
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "info.json"
    E o campo "aplicacao" deve ser "Cidades ESG Inteligentes"
    E o campo "ambiente" deve ser "test"

  @positivo
  Cenário: Consultar a saúde da aplicação
    Quando consulto a saúde da aplicação
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "health.json"
    E o campo "status" deve ser "UP"
