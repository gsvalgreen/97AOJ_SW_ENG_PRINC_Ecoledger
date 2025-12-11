# MinIO no Ecoledger

Este guia explica como levantar o MinIO previsto no `docker-compose.yml`, validar a configuração inicial (buckets `movimentacoes` e `anexos`) e ligar cada microsserviço ao gateway S3-compatível.

## 0. Por que MinIO?

- MinIO funciona como um armazenamento de objetos compatível com S3 dentro do docker-compose. Ele replica localmente as mesmas APIs que os serviços encontrarão em um provedor real (AWS S3, Ceph, etc.), permitindo desenvolver e testar uploads/downloads sem depender de cloud.
- Todos os microsserviços que manipulam evidências ou anexos (Movimentação, Auditoria, Certificação e Notificação) apontam para `http://minio:9000` via variáveis `S3_*`. Quando for migrar para produção, basta trocar `S3_ENDPOINT`, bucket e credenciais porque o contrato permanece idêntico.
- O bucket `movimentacoes` mantém os arquivos privados para consumo interno, enquanto `anexos` pode ser tornado público para expor links ao produtor ou parceiros. Assim, o fluxo de documentos fica isolado em um storage único, com versionamento opcional e backup simples via volumes Docker.

## 1. Subindo a stack

```bash
cd /Users/gustavo.valverde/FIAP/Engineering\ Software\ Development/97AOJ_SW_ENG_PRINC_Ecoledger
docker compose up -d minio minio-setup
```

Durante o `docker compose up`:
* `ecoledger-minio` sobe com usuário/senha padrão `minioadmin`.
* `ecoledger-minio-setup` usa `mc` (MinIO Client) para criar os buckets `movimentacoes` e `anexos` e deixa `anexos` público para acesso de leitura via HTTP.

A consola gráfica está em `http://localhost:9001`, e a API S3 em `http://localhost:9000`.

## 2. Validando buckets e credenciais

### Via MinIO Console
1. Acesse `http://localhost:9001` com login `minioadmin / minioadmin`.
2. Verifique se os buckets `movimentacoes` (privado) e `anexos` (público) existem.

### Via MinIO Client (`mc`)
```bash
mc alias set ecoledger http://localhost:9000 minioadmin minioadmin
mc ls ecoledger
```

Saída esperada: diretórios `movimentacoes/` e `anexos/`.

## 3. Variáveis de ambiente dos microsserviços

Use as mesmas credenciais básicas em cada serviço que gravar/ler anexos:

```
S3_ENDPOINT=http://minio:9000
S3_REGION=us-east-1
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
S3_BUCKET_MOVIMENTACOES=movimentacoes
S3_BUCKET_ANEXOS=anexos
S3_USE_PATH_STYLE=true
```

* `S3_ENDPOINT` aponta para o hostname do serviço dentro da rede Docker (`minio`). Para testes fora do Compose, use `http://localhost:9000`.
* `S3_USE_PATH_STYLE=true` é necessário para compatibilidade com MinIO.

### Serviços que precisam do MinIO
| Serviço | Uso | Observações |
| --- | --- | --- |
| Movimentação | upload dos anexos de movimentação | gravar metadados (hash, URL) no `MovDB` e salvar o arquivo no bucket `movimentacoes`. |
| Auditoria | leitura das evidências anexadas | usar credencial read-only se preferir. |
| Certificação | consulta de anexos e geração de relatórios | acesso leitura. |
| Notificação | envio de links públicos das evidências | ler URLs do bucket `anexos`. |

## 4. Fluxo típico para upload

Exemplo em pseudo código Node.js utilizando AWS SDK S3 compatível:

```js
import { S3Client, PutObjectCommand } from "@aws-sdk/client-s3";

const s3 = new S3Client({
  endpoint: process.env.S3_ENDPOINT,
  region: process.env.S3_REGION,
  forcePathStyle: process.env.S3_USE_PATH_STYLE === "true",
  credentials: {
    accessKeyId: process.env.S3_ACCESS_KEY,
    secretAccessKey: process.env.S3_SECRET_KEY,
  },
});

export async function uploadMovimentacao(fileBuffer, mimeType, key) {
  await s3.send(new PutObjectCommand({
    Bucket: process.env.S3_BUCKET_MOVIMENTACOES,
    Key: key,
    Body: fileBuffer,
    ContentType: mimeType,
  }));
  return `${process.env.S3_PUBLIC_BASE ?? "http://localhost:9000"}/${process.env.S3_BUCKET_MOVIMENTACOES}/${key}`;
}
```

* `key` pode incluir `{producerId}/{movimentacaoId}/{nomeArquivo}` para manter organização.
* Armazene o hash SHA256 do arquivo em banco para garantir imutabilidade (conforme plano do serviço de Movimentação).

## 5. Tornando anexos públicos

O `minio-setup` já torna o bucket `anexos` público. Caso precise refazer manualmente:

```bash
mc anonymous set public ecoledger/anexos
```

Para anexos sensíveis (ex.: documentos com PII), mantenha privado e use URLs assinadas:

```js
import { S3RequestPresigner } from "@aws-sdk/s3-request-presigner";

// gerar URL válida por 15 minutos
const url = await getSignedUrl(s3, new GetObjectCommand({
  Bucket: process.env.S3_BUCKET_MOVIMENTACOES,
  Key: key,
}), { expiresIn: 900 });
```

## 6. Estratégias de segurança

* Alterar `MINIO_ROOT_USER` e `MINIO_ROOT_PASSWORD` em produção.
* Criar usuários IAM específicos por serviço (`mc admin user add`) e políticas com permissões mínimas.
* Habilitar TLS no MinIO (`MINIO_SERVER_URL=https://...` + certificados no volume) caso exponha fora do ambiente local.
* Configurar versionamento de bucket se precisar de histórico (`mc version enable ecoledger/movimentacoes`).

## 7. Backup e limpeza

* Os dados persistem no volume Docker `minio-data`. Para backup local, basta copiar esse volume: `docker run --rm -v minio-data:/data -v $(pwd):/backup alpine tar czf /backup/minio-data.tgz /data`.
* Para resetar, remova o volume: `docker volume rm ecoledger_minio-data` (perde todos os anexos!).

## 8. Troubleshooting rápido

| Sintoma | Correção |
| --- | --- |
| `AccessDenied` ao usar AWS SDK | confirme `S3_ACCESS_KEY`/`SECRET`, bucket correto e `S3_USE_PATH_STYLE=true`. |
| Arquivos não aparecem no console | verifique se o uploader usou bucket errado ou se o container não está na mesma rede. |
| `mc` não conecta | valide DNS `minio` dentro da rede Docker; fora dela use `localhost`. |
| Porta 9000 já usada | altere o mapeamento em `docker-compose.yml` (ex.: `9002:9000`). |

Com este setup, qualquer microsserviço pode manipular objetos S3 sem depender de AWS, mantendo o ambiente local alinhado ao desenho de arquitetura do Ecoledger.
