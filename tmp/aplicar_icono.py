from pathlib import Path
root = Path('app/src/main/res')
(root/'values/icon_colors.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="icono_agencia_fondo">#061447</color>
</resources>
''', encoding='utf-8')
(root/'drawable/ic_launcher_foreground.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<!-- Atenea de medio cuerpo: casco y maletín grandes para reconocerlos en pequeño.
     El margen proporcional reserva espacio para las máscaras circular y cuadrada
     del launcher. La imagen se escala completa, sin recortar la figura. -->
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:inset="16.67%">
    <bitmap android:src="@drawable/atenea_icono" android:gravity="fill"
        android:filter="true" />
</inset>
''', encoding='utf-8')
legacy = '''<?xml version="1.0" encoding="utf-8"?>
<!-- Android anterior a API 26: combina el fondo y la misma figura protegida por margen. -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/ic_launcher_background" />
    <item android:drawable="@drawable/ic_launcher_foreground" />
</layer-list>
'''
adaptive = '''<?xml version="1.0" encoding="utf-8"?>
<!-- Android aplica su máscara al conjunto. El margen del foreground protege la figura. -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
'''
for name in ('ic_launcher.xml', 'ic_launcher_round.xml'):
    (root/'mipmap-anydpi'/name).write_text(legacy, encoding='utf-8')
    (root/'mipmap-anydpi-v26'/name).write_text(adaptive, encoding='utf-8')
