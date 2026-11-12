# Himalayan Connect — HC 0.1.0

Projeto Android particular para pesquisa e desenvolvimento de uma interface de pilotagem para a Royal Enfield Himalayan 450.

## Estado desta entrega

Esta é a **base inicial do aplicativo**, não uma versão pronta para conectar ao Tripper Dash.

Inclui:
- Interface Android em Jetpack Compose, tema escuro com detalhes laranja.
- Abas Início, Mapa, Viagem e Música.
- Leitura local de localização GPS, mediante autorização do usuário.
- Leitura de rumo pela bússola/sensor de rotação do aparelho.
- Tela cartográfica ilustrativa (não é mapa rodoviário real).

Ainda não inclui:
- Protocolo/autenticação do Tripper Dash.
- Transmissão H.264/RTP.
- Mapa real, pesquisa de destinos ou cálculo de rotas.
- Android Auto certificado.
- Controles reais de mídia ou joystick.

## Requisitos para compilar

- Android Studio recente.
- JDK 17.
- Android SDK Platform 35.
- Gradle Wrapper gerado pelo Android Studio (ou Gradle instalado).

Abra a pasta `HimalayanConnect` no Android Studio e aguarde a sincronização do Gradle. Se necessário, use **File > Sync Project with Gradle Files**. Depois selecione `app` e execute em um aparelho Android.

Para gerar APK de depuração:
`./gradlew assembleDebug`

O APK, se a compilação terminar com sucesso, ficará em:
`app/build/outputs/apk/debug/app-debug.apk`

## Segurança e privacidade

- O manifesto solicita apenas permissões de localização aproximada e precisa.
- A localização só é solicitada quando o usuário toca em “Ativar GPS”.
- Esta versão não implementa comunicação de rede e não transmite coordenadas.
- Não armazena credenciais, rotas ou histórico.
- Antes de adicionar conexão Wi-Fi/Bluetooth, cada permissão e fluxo será revisado.

## Próximas etapas

1. Validar a interface em diferentes tamanhos de tela.
2. Integrar mapa e roteamento com uma fonte de dados escolhida.
3. Estudar e validar o protocolo do Tripper com a motocicleta disponível.
4. Implementar transmissão e reconexão em módulo isolado.
5. Fazer testes de segurança, desempenho e estabilidade antes de qualquer uso em movimento.

## Licenças

Este projeto inicial foi escrito como código novo para este trabalho. Não incorpora código do APK Rota Auto nem de outros projetos de terceiros. A marca Royal Enfield é mencionada apenas para indicar compatibilidade pretendida; o projeto não é oficial nem endossado pela fabricante.
