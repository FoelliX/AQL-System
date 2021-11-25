Windows:	Run sign.bat
Linux:		Run sign.bat

Parameters:
1. zipaligner	(e.g.: "D:\Android\sdks\build-tools\29.0.2\zipalign.exe" on Windows)
2. apksigner	(e.g.: "/path/to/android/build-tools/apksigner" on Linux)
3. keystore		(e.g.: "data/signing/keyForSigning.jks")
4. password for keystore (e.g.: "AQL123" - works for example above)
5. temp apk		(e.g.: "temp.apk")
6. apk to sign	(e.g.: "test.apk")
7. output apk	(e.g.: "output.apk")

Full Windows example:
sign.bat "D:\Android\sdks\build-tools\29.0.2\zipalign.exe" "D:\Android\sdks\build-tools\29.0.2\apksigner.bat" "..\data\signing\keyForSigning.jks" AQL123 temp.apk test.apk output.apk

Full Linux example:
./sign.sh "/path/to/android/build-tools/29.0.2/zipalign" "/path/to/android/build-tools/29.0.2/apksigner" "../data/signing/keyForSigning.jks" AQL123 temp.apk test.apk output.apk