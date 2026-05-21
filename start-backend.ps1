$env:JAVA_HOME = "C:\Users\46018\tools\jdk-17.0.13+11"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
cd "D:\JavaWork\edu\backend"
java -cp "target/classes;target/lib/*" com.edu.EduApplication 2>&1 | Out-File -FilePath "D:\JavaWork\edu\backend.log" -Encoding UTF8
