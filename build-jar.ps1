# Build JAR file for ToDo App
Write-Host "Building JAR file..." -ForegroundColor Green

# 1. Bersihkan dan buat direktori bin
if (Test-Path bin) { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory -Path bin | Out-Null

# 2. Kompilasi
Write-Host "Compiling Java sources..."
javac -d bin -cp "lib/*" -sourcepath src src/com/todoapp/Main.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "âœ“ Compilation successful!" -ForegroundColor Green
    
    # 3. Ekstrak library ke bin agar JAR bisa jalan mandiri (Fat JAR)
    Write-Host "Extracting libraries..."
    Push-Location bin
    foreach ($jar in Get-ChildItem ../lib/*.jar) {
        jar xf $jar.FullName
    }
    Pop-Location

    # 4. Buat JAR
    Write-Host "Creating JAR file..."
    jar cfe ToDoApp.jar com.todoapp.Main -C bin .
    
    if (Test-Path ToDoApp.jar) {
        Write-Host "âœ“ JAR created: ToDoApp.jar" -ForegroundColor Green
        Write-Host ""
        Write-Host "Sekarang kamu bisa jalankan dengan: java -jar ToDoApp.jar" -ForegroundColor Cyan
    } else {
    Write-Host "âœ— Compilation failed!" -ForegroundColor Red
}
