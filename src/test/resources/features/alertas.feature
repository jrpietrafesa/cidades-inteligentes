# language: pt
@alertas
Funcionalidade: Gestão de alertas de consumo
  Como responsável pela conformidade ambiental
  Quero acompanhar e resolver os alertas de consumo
  Para manter rastreabilidade de cada ultrapassagem da meta

  Contexto:
    Dado que existe um edifício "Secretaria do Meio Ambiente" com meta diária de 100.00 kWh
    E que foi registrada uma medição de 150.00 kWh em "2026-10-05T09:00:00"

  @positivo @governanca
  Cenário: Consultar alerta aberto com dados de rastreabilidade
    Quando consulto os alertas abertos
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "alerta-lista.json"
    E o alerta do edifício deve registrar consumo de 150.00 kWh contra a meta de 100.00 kWh na data "2026-10-05"

  @positivo @governanca
  Cenário: Nova medição no mesmo dia atualiza o alerta existente
    Dado que foi registrada uma medição de 40.00 kWh em "2026-10-05T15:00:00"
    Quando consulto os alertas abertos
    Então o status da resposta deve ser 200
    E o alerta do edifício deve registrar consumo de 190.00 kWh contra a meta de 100.00 kWh na data "2026-10-05"

  @positivo @governanca
  Cenário: Resolver um alerta aberto
    Dado que o alerta do edifício foi aberto
    Quando resolvo o alerta do edifício
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "alerta.json"
    E o campo "status" deve ser "RESOLVIDO"
    E o edifício não deve ter alerta aberto

  @negativo
  Cenário: Resolver um alerta que não existe
    Quando resolvo um alerta que não existe
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o contrato "erro.json"
    E a mensagem de erro deve citar "nao encontrado"
