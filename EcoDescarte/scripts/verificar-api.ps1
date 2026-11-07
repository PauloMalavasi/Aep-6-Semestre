param([string]$BaseUrl = 'http://localhost:8080')

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$idsCriados = [System.Collections.Generic.List[string]]::new()
$organizacoesCriadas = [System.Collections.Generic.List[string]]::new()
$verificacoes = 0

function Confirmar([bool]$Condicao, [string]$Descricao) {
    if (-not $Condicao) { throw "FALHOU: $Descricao" }
    $script:verificacoes++
    Write-Host "OK: $Descricao"
}

function Requisicao([string]$Metodo, [string]$Caminho, [string]$Json = '') {
    $parametros = @{
        Uri = "$BaseUrl$Caminho"
        Method = $Metodo
        SkipHttpErrorCheck = $true
        TimeoutSec = 20
    }
    if ($Json) {
        $parametros.ContentType = 'application/json; charset=utf-8'
        $parametros.Body = [System.Text.Encoding]::UTF8.GetBytes($Json)
    }
    return Invoke-WebRequest @parametros
}

# Requer PowerShell 7. Cria somente seus próprios registros e os remove no finally.
try {
    Confirmar ((Requisicao 'GET' '/').StatusCode -eq 200) 'Interface acessível'
    Confirmar ((Requisicao 'GET' '/docs').StatusCode -eq 200) 'Swagger acessível em /docs'
    $openapi = (Requisicao 'GET' '/v3/api-docs').Content | ConvertFrom-Json
    Confirmar ($openapi.info.title -eq 'EcoDescarte API') 'OpenAPI identificado'
    $config = (Requisicao 'GET' '/v3/api-docs/swagger-config').Content | ConvertFrom-Json
    Confirmar ($config.operationsSorter -eq 'method') 'Configuração do Swagger aplicada'
    Confirmar ((Requisicao 'GET' '/ponto_coleta').StatusCode -eq 200) 'MongoDB acessível pela API'

    $marcador = 'Teste entrega ' + [guid]::NewGuid().ToString('N')
    $cadastroOrg = Requisicao 'POST' '/organizacoes' (@{ nome = $marcador } | ConvertTo-Json)
    Confirmar ($cadastroOrg.StatusCode -eq 201) 'Organização cadastrada na segunda coleção'
    $org = $cadastroOrg.Content | ConvertFrom-Json
    $organizacoesCriadas.Add($org.id)
    Confirmar ($cadastroOrg.Headers.Location[0].EndsWith('/organizacoes/' + $org.id)) 'Organização retorna Location'
    Confirmar (@((Requisicao 'GET' '/organizacoes').Content | ConvertFrom-Json).id -contains $org.id) 'Organização listada'
    Confirmar ((Requisicao 'POST' '/organizacoes' '{}').StatusCode -eq 400) 'Organização sem nome rejeitada'
    $dados = @{
        nome = $marcador
        organizacaoId = $org.id
        telefone = '(44) 99999-9999'
        endereco = @{
            logradouro = 'Rua de Teste'; numero = '100'; bairro = 'Centro'
            cep = '87000-000'; cidade = 'Maringá'; uf = 'PR'
        }
        residuos = @('PLASTICO', 'PILHA')
    }
    $cadastro = Requisicao 'POST' '/ponto_coleta' ($dados | ConvertTo-Json -Depth 5)
    Confirmar ($cadastro.StatusCode -eq 201) 'Cadastro retorna 201'
    $ponto = $cadastro.Content | ConvertFrom-Json
    $idsCriados.Add($ponto.id)
    Confirmar ($cadastro.Headers.Location[0].EndsWith('/ponto_coleta/' + $ponto.id)) 'Cadastro retorna Location'
    $consulta = Requisicao 'GET' ('/ponto_coleta/' + $ponto.id)
    Confirmar (($consulta.Content | ConvertFrom-Json).endereco.cidade -eq 'Maringá') 'Persistência do endereço e acentos'
    Confirmar (($consulta.Content | ConvertFrom-Json).organizacaoId -eq $org.id) 'Vínculo entre coleções persistido'
    Confirmar ((Requisicao 'DELETE' ('/organizacoes/' + $org.id)).StatusCode -eq 409) 'Organização em uso não pode ser excluída'
    $dados.organizacaoId = 'organizacao-inexistente-' + [guid]::NewGuid().ToString('N')
    Confirmar ((Requisicao 'POST' '/ponto_coleta' ($dados | ConvertTo-Json -Depth 5)).StatusCode -eq 400) 'Cadastro rejeita organização inexistente'
    Confirmar ((Requisicao 'PUT' ('/ponto_coleta/' + $ponto.id) ($dados | ConvertTo-Json -Depth 5)).StatusCode -eq 400) 'Atualização rejeita vínculo inexistente'
    $dados.organizacaoId = $org.id
    $novoNomeOrg = $marcador + ' responsável'
    Confirmar ((Requisicao 'PUT' ('/organizacoes/' + $org.id) (@{ nome = $novoNomeOrg } | ConvertTo-Json)).StatusCode -eq 200) 'Organização atualizada'
    Confirmar (((Requisicao 'GET' ('/organizacoes/' + $org.id)).Content | ConvertFrom-Json).nome -eq $novoNomeOrg) 'Nome da organização persistido'

    $busca = Requisicao 'GET' ('/ponto_coleta/nome-ponto?nome=' + [uri]::EscapeDataString($marcador.ToLower()))
    Confirmar (@($busca.Content | ConvertFrom-Json).id -contains $ponto.id) 'Busca por nome ignora caixa'
    $residuos = Requisicao 'GET' ('/ponto_coleta/nome-residuo?nome=' + [uri]::EscapeDataString('plástico'))
    Confirmar (@($residuos.Content | ConvertFrom-Json).id -contains $ponto.id) 'Busca por resíduo aceita acento'
    $literal = Requisicao 'GET' ('/ponto_coleta/nome-ponto?nome=' + [uri]::EscapeDataString('.*['))
    Confirmar ($literal.StatusCode -eq 200 -and @($literal.Content | ConvertFrom-Json).Count -eq 0) 'Busca trata caracteres de regex como texto'
    Confirmar ((Requisicao 'GET' '/ponto_coleta/nome-ponto').StatusCode -eq 400) 'Busca vazia retorna 400'
    Confirmar ((Requisicao 'POST' '/ponto_coleta' '{}').StatusCode -eq 400) 'Campos ausentes retornam 400'
    Confirmar ((Requisicao 'POST' '/ponto_coleta' '{invalido').StatusCode -eq 400) 'JSON inválido retorna 400'

    $dados.nome = $marcador + ' atualizado'
    $atualizacao = Requisicao 'PUT' ('/ponto_coleta/' + $ponto.id) ($dados | ConvertTo-Json -Depth 5)
    Confirmar ($atualizacao.StatusCode -eq 200) 'Atualização retorna 200'
    $persistido = (Requisicao 'GET' ('/ponto_coleta/' + $ponto.id)).Content | ConvertFrom-Json
    Confirmar ($persistido.nome -eq $dados.nome -and $persistido.id -eq $ponto.id) 'Atualização persistida preserva o ID'

    # Enviar o ID de um registro existente em POST não pode sobrescrevê-lo.
    $dados.id = $ponto.id
    $dados.nome = $marcador + ' segundo'
    $segundo = Requisicao 'POST' '/ponto_coleta' ($dados | ConvertTo-Json -Depth 5)
    Confirmar ($segundo.StatusCode -eq 201) 'POST com ID fornecido cria um registro novo'
    $ponto2 = $segundo.Content | ConvertFrom-Json
    $idsCriados.Add($ponto2.id)
    Confirmar ($ponto2.id -ne $ponto.id) 'Novo cadastro não sobrescreve o anterior'

    Confirmar ((Requisicao 'DELETE' ('/ponto_coleta/' + $ponto.id)).StatusCode -eq 204) 'Exclusão retorna 204'
    Confirmar ((Requisicao 'GET' ('/ponto_coleta/' + $ponto.id)).StatusCode -eq 404) 'Consulta de removido retorna 404'
    Confirmar ((Requisicao 'PUT' ('/ponto_coleta/' + $ponto.id) ($dados | ConvertTo-Json -Depth 5)).StatusCode -eq 404) 'Atualização de removido retorna 404'
    Confirmar ((Requisicao 'DELETE' ('/ponto_coleta/' + $ponto.id)).StatusCode -eq 404) 'Exclusão de removido retorna 404'

    Confirmar ((Requisicao 'DELETE' ('/ponto_coleta/' + $ponto2.id)).StatusCode -eq 204) 'Segundo ponto removido'
    Confirmar ((Requisicao 'DELETE' ('/organizacoes/' + $org.id)).StatusCode -eq 204) 'Organização sem vínculos removida'
    Confirmar ((Requisicao 'GET' ('/organizacoes/' + $org.id)).StatusCode -eq 404) 'Organização removida retorna 404'
    Write-Host "Concluído: $verificacoes verificações aprovadas."
} finally {
    foreach ($id in $idsCriados | Select-Object -Unique) {
        try {
            $limpeza = Requisicao 'DELETE' ('/ponto_coleta/' + [uri]::EscapeDataString($id))
            if ($limpeza.StatusCode -notin @(204, 404)) { Write-Warning "Verifique o registro de teste ${id}: limpeza retornou $($limpeza.StatusCode)." }
        } catch { Write-Warning "Não foi possível remover o registro de teste $id." }
    }
    foreach ($id in $organizacoesCriadas) {
        try {
            $limpeza = Requisicao 'DELETE' ('/organizacoes/' + [uri]::EscapeDataString($id))
            if ($limpeza.StatusCode -notin @(204, 404)) { Write-Warning "Verifique a organização de teste ${id}: limpeza retornou $($limpeza.StatusCode)." }
        } catch { Write-Warning "Não foi possível remover a organização de teste $id." }
    }
}
