# language: pt
@medicoes
Funcionalidade: Registro de medições de consumo energético
  Como medidor inteligente instalado em um edifício público
  Quero enviar as leituras de consumo
  Para que a cidade saiba quando a meta diária de energia foi ultrapassada

  Contexto:
    Dado que existe um edifício "Hospital Municipal Sul" com meta diária de 500.00 kWh

  @positivo @ambiental
  Cenário: Consumo dentro da meta não gera alerta
    Quando registro uma medição de 300.00 kWh em "2026-10-01T08:00:00"
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "medicao.json"
    E o campo "kwh" deve valer 300.00
    E o campo "alertaGerado" deve ser "false"
    E o edifício não deve ter alerta aberto

  @positivo @ambiental
  Cenário: Consumo exatamente igual à meta não gera alerta
    Quando registro uma medição de 500.00 kWh em "2026-10-01T08:00:00"
    Então o status da resposta deve ser 201
    E o campo "alertaGerado" deve ser "false"

  @positivo @ambiental
  Cenário: Consumo acumulado do dia acima da meta gera alerta
    Dado que foi registrada uma medição de 300.00 kWh em "2026-10-01T08:00:00"
    Quando registro uma medição de 250.00 kWh em "2026-10-01T18:00:00"
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "medicao.json"
    E o campo "alertaGerado" deve ser "true"

  @positivo @ambiental
  Cenário: Medições de dias diferentes não se somam
    Dado que foi registrada uma medição de 300.00 kWh em "2026-10-01T08:00:00"
    Quando registro uma medição de 250.00 kWh em "2026-10-02T08:00:00"
    Então o status da resposta deve ser 201
    E o campo "alertaGerado" deve ser "false"

  @positivo
  Cenário: Medição sem data usa o horário atual
    Quando registro uma medição de 10.00 kWh sem informar a data
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "medicao.json"
    E o campo "dataHora" deve estar preenchido

  @positivo @governanca
  Cenário: Listar o histórico de medições do edifício
    Dado que foi registrada uma medição de 10.00 kWh em "2026-10-01T08:00:00"
    E que foi registrada uma medição de 20.00 kWh em "2026-10-03T08:00:00"
    E que foi registrada uma medição de 30.00 kWh em "2026-10-02T08:00:00"
    Quando listo as medições do edifício
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "medicao-lista.json"
    E a lista deve ter 3 medições, da mais recente para a mais antiga

  @negativo @governanca
  Esquema do Cenário: Rejeitar medição inválida: <motivo>
    Quando tento registrar uma medição com valor "<valor>"
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro.json"
    E a mensagem de erro deve citar "kwh"

    Exemplos:
      | motivo         | valor   |
      | consumo zerado | 0       |
      | consumo negativo | -5    |
      | consumo ausente | ausente |

  @negativo
  Cenário: Registrar medição em edifício que não existe
    Quando registro uma medição em um edifício que não existe
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o contrato "erro.json"

  @negativo
  Cenário: Listar medições de edifício que não existe
    Quando listo as medições de um edifício que não existe
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o contrato "erro.json"
