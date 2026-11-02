# language: pt
@edificios
Funcionalidade: Cadastro de edifícios públicos monitorados
  Como gestor de sustentabilidade da cidade
  Quero cadastrar edifícios com sua meta diária de consumo
  Para acompanhar o compromisso ambiental de cada prédio público

  @positivo @ambiental
  Cenário: Cadastrar edifício com meta ESG válida
    Quando cadastro o edifício "Hospital Municipal Norte" do tipo "HOSPITAL" em "Sao Paulo" com meta diária de 500.00 kWh
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o contrato "edificio.json"
    E o campo "id" deve estar preenchido
    E o campo "nome" deve ser "Hospital Municipal Norte"
    E o campo "tipo" deve ser "HOSPITAL"
    E o campo "limiteDiarioKwh" deve valer 500.00

  @positivo
  Cenário: Consultar um edifício cadastrado
    Dado que existe um edifício "EMEF Jardim Verde" com meta diária de 100.00 kWh
    Quando consulto o edifício cadastrado
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "edificio.json"
    E o campo "nome" deve ser "EMEF Jardim Verde"

  @positivo
  Cenário: Listar os edifícios monitorados
    Dado que existe um edifício "Prefeitura Central" com meta diária de 300.00 kWh
    Quando listo os edifícios
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o contrato "edificio-lista.json"
    E a lista deve conter o edifício cadastrado

  @negativo @governanca
  Esquema do Cenário: Rejeitar cadastro inválido: <motivo>
    Quando tento cadastrar um edifício com nome "<nome>", cidade "<cidade>", tipo "<tipo>" e meta "<meta>"
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o contrato "erro.json"
    E a mensagem de erro deve citar "<campo>"

    Exemplos:
      | motivo           | nome    | cidade    | tipo   | meta    | campo           |
      | nome em branco   |         | Sao Paulo | ESCOLA | 100     | nome            |
      | cidade em branco | Escola  |           | ESCOLA | 100     | cidade          |
      | tipo em branco   | Escola  | Sao Paulo |        | 100     | tipo            |
      | meta zerada      | Escola  | Sao Paulo | ESCOLA | 0       | limiteDiarioKwh |
      | meta negativa    | Escola  | Sao Paulo | ESCOLA | -10     | limiteDiarioKwh |
      | meta ausente     | Escola  | Sao Paulo | ESCOLA | ausente | limiteDiarioKwh |

  @negativo
  Cenário: Rejeitar corpo com JSON malformado
    Quando envio um cadastro de edifício com JSON malformado
    Então o status da resposta deve ser 400

  @negativo
  Cenário: Consultar um edifício que não existe
    Quando consulto um edifício que não existe
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o contrato "erro.json"
    E a mensagem de erro deve citar "nao encontrado"

  @negativo
  Cenário: Consultar um edifício com identificador inválido
    Quando consulto um edifício com identificador "abc"
    Então o status da resposta deve ser 400
