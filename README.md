Puntería Universal
===================

Aplicación Android independiente para asistencia visual de billar.

Primera versión 0.1.0
- Importa capturas de pantalla de distintos juegos.
- Permite marcar bola blanca, objetivo y tronera.
- Calcula un ángulo geométrico y dibuja líneas de referencia.
- No modifica ni controla otros juegos.

Límites conocidos
Esta versión inicial no detecta automáticamente las bolas ni dibuja una mira flotante sobre otros juegos. Es una base de pruebas para validar geometría y compatibilidad visual. Las siguientes fases incorporarán captura de pantalla autorizada por Android, detección automática de bolas y análisis de trayectorias; cada familia de juegos deberá probarse con capturas reales.

Compilación
GitHub Actions ejecuta gradle assembleDebug y publica el APK como artefacto PUNTERIA-UNIVERSAL-APK.
