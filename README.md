# História Viva (Android + ARCore)

Este projeto é um app Android (Kotlin + Jetpack Compose) com um visualizador de Realidade Aumentada (RA) via ARCore.

## Requisitos para a RA funcionar

A tela de RA depende do **Google Play Services para RA (ARCore)**.

- Em muitos emuladores o ARCore **não funciona**, mesmo instalando “ARCore emulator” manualmente.
- Em aparelhos físicos, o ARCore só funciona se o modelo/ROM for compatível e o Play Services para RA puder ser instalado.

## Teste em emulador (quando dá certo)

Em geral, funciona melhor com AVD **Google Play** em versões estáveis:

1. Crie um AVD com imagem **Google Play** (não “Google APIs”).
2. Prefira Android **14 (API 34)** ou **15 (API 35)**.
3. Evite Android 16 preview (“Baklava”) para testes de ARCore.
4. No AVD, configure câmera para **Virtual Scene**.
5. Abra a Play Store e atualize tudo.
6. Instale/atualize “Google Play Services para RA (ARCore)”.

Se a Play Store mostrar “dispositivo não é compatível”, esse AVD não suporta ARCore naquele ambiente.

## Teste em aparelho físico (recomendado)

Use um aparelho compatível com ARCore e com Google Mobile Services.

## Build

```bash
cd historia-viva-android
./gradlew :app:assembleDebug
```

APK gerado em:

`app/build/outputs/apk/debug/app-debug.apk`

