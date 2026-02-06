# Metal Shaders (Liquid Glass)

## Deployment Target

**Обязательно:** в настройках Watch App target должен стоять **watchOS 26.2** (или новее).  
При **watchOS 10.0 / 11.0** компилятор не разрешает использование `ShaderLibrary` — будет ошибка.  
В данном проекте таргет уже выставлен на 26.2.

## Подключение .metal

1. Откройте проект в Xcode (`SomniWatchProject/SomniWatch.xcodeproj`).
2. Добавьте **`LiquidGlass.metal`** в целевой таргет **SomniWatch Watch App**:
   - Правый клик по группе с исходниками → **Add Files to "…"** → выберите `LiquidGlass.metal`.
   - В **Target Membership** отмечен **SomniWatch Watch App**.
3. Соберите проект. После этого `ShaderLibrary.liquidRefraction` и `ShaderLibrary.ripple` доступны в коде.

Если файл не добавлен в target, сборка выдаёт «Unknown type ShaderLibrary»; в коде используются fallback без шейдеров.

## Ограничение API (Watch)

В **Apple Developer Release Notes** указано, что прямые Metal-искажения через `ShaderLibrary` на Watch могут требовать:

- **специального разрешения в Info.plist**, или  
- **использования через Canvas**.

Если при запуске на устройстве шейдеры не применяются или появляются ограничения, проверьте актуальные Release Notes для watchOS и при необходимости добавьте нужный ключ в Info.plist таргета **SomniWatch Watch App** (в Xcode: target → Info или свой `Info.plist`).
