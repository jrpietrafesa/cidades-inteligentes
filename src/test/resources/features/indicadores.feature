# language: pt
@indicadores
Funcionalidade: Indicadores ambientais da cidade
  Como gestor público
  Quero consultar consumo, emissões e alertas consolidados
  Para reportar o desempenho ambiental da cidade

  @positivo @ambiental
  Cenário: Indicadores refletem uma medição acima da meta
    Dado que anotei os indicadores atuais
    E que existe um edifício "Biblioteca Municipal" com meta diária de 100.00 kWh
    Quando registro uma medição de 150.00 kWh em "2026-10-06T10:00:00"
    E consulto os indicadores
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "indicadores.json"
    E o indicador "edificiosMonitorados" deve ter variado 1
    E o indicador "medicoesRegistradas" deve ter variado 1
    E o indicador "consumoTotalKwh" deve ter variado 150.00
    E o indicador "alertasAbertos" deve ter variado 1
    E a emissão estimada deve corresponder ao consumo total pelo fator 0.0385 kgCO2 por kWh

  @positivo @governanca
  Cenário: Resolver alerta atualiza os indicadores
    Dado que existe um edifício "Centro Cultural" com meta diária de 100.00 kWh
    E que foi registrada uma medição de 150.00 kWh em "2026-10-06T10:00:00"
    E que anotei os indicadores atuais
    E que o alerta do edifício foi resolvido
    Quando consulto os indicadores
    Então o status da resposta deve ser 200
    E o indicador "alertasAbertos" deve ter variado -1
    E o indicador "alertasResolvidos" deve ter variado 1
