# Minebox API

É uma API que permiti fazer upload de arquivos no S3 da AWS, desenvolvi esse projeto com intuito de adicionar ao meu portfólio

![GitHub repo size](https://img.shields.io/github/repo-size/tarcisiodelmondes/minebox?style=for-the-badge)
![GitHub language count](https://img.shields.io/github/languages/count/tarcisiodelmondes/minebox?style=for-the-badge)

### Funcionalidades

- [x] Criar usuário
- [x] Autenticar usuário com token JWT e REFRESH TOKEN
- [x] Atualizar dados do usuário
- [x] Apagar usuário com seus outros dados que estão ligados a ele
- [x] Buscar informações do usuário pelo id
- [x] Salvar arquivos no S3 da AWS
- [x] Renomear nome do arquivo
- [x] Listar arquivos de um usuário
- [x] Baixar um arquivo do usuário autenticado
- [x] Deletar arquivo do S3 e da base de dados
- [x] Rota para atualizar token JTW utilizando o REFRESH TOKEN
- [x] Documentação com SWAGGER
- [x] Testar controllers e services com JUnit e Mockito
- [ ] Adicionar monitoramento com Sentry
- [ ] Deploy na AWS usando CI/CD

### Regras de negócio
- [x] Não pode salvar arquivos maiores do que 50MB
- [x] O usuário precisa ser dono do arquivo para podê-lo deletar ou renomeá-lo 
- [x] O usuário precisa estar logado para atualizar suas informações ou apagar sua conta
- [x] Apagar usuário junto com seus outros dados que estão ligados a ele
- [x] Só o usuário pode visualizar seus arquivos, por 2 horas (É o tempo da url pre assinada que é gerada para o usuário ter acesso ao arquivo expirar).


## 💻 Pré-requisitos

Antes de começar, verifique se você atendeu aos seguintes requisitos:

* Você instalou a versão mais recente do `JAVA 17, MAVEN 3.6 ou superior, Git`
* Você tem uma máquina `<Windows / Linux / Mac>`. 

## 🚀 Executanto o projeto

É necessário configurar as variáveis de ambiente da AWS e as permissões de IAM necessárias para interagir com o S3. Pode adicionar isso no seu .bashrc ou no .zshenv, se estiver usando o Windows sem WSL pode seguir [esse tutorial](https://learn.microsoft.com/pt-br/windows-server/administration/windows-commands/setx)
```bash
// Linux / Mac
export AWS_REGION="region que você configurou o S3"
export AWS_ACCESS_KEY_ID=""
export AWS_SECRET_ACCESS_KEY=""
export BUCKET_NAME="nome do seu bucket"
```
Agora com as variáveis de ambiente da AWS configurada corretamente, clone esse projeto.
```bash
git clone https://github.com/tarcisiodelmondes/minebox
cd minebox
mvn spring-boot:run
```
Se ocorreu tudo bem, você tera um servidor rodando em `http://localhost:8080`, acesse http://localhost:8080/swagger-ui/index.html pelo seu navegador para ter acesso à documentação da API.

## 📝 Licença

Esse projeto está sob licença. Veja o arquivo [LICENÇA](LICENSE.md) para mais detalhes.
